package com.biosteel.teams.chat.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.biosteel.teams.chat.dto.ChatTokenDTO;
import com.biosteel.teams.chat.dto.ChatUserDTO;
import com.google.gson.Gson;

import io.getstream.client.Client;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@Service
public class StreamChatService {

    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private static final String STREAM_ROLE_TEAM = "team";
    private static final String AUTHORIZATION = "Authorization";
    private static final String STREAM_AUTH_TYPE = "Stream-Auth-Type";
    private static final String JWT = "jwt";
    private static final String TYP = "typ";
    private static final String EXP = "exp";

    @Value("${stream.api-key}")
    private String streamApiKey;

    @Value("${stream.api-secret}")
    private String streamApiSecret;

    @Value("${stream.api-base-url}")
    private String streamApiBaseUrl;

    private final OkHttpClient httpClient = new OkHttpClient();

    @Data
    private static class ChannelResponse {
        private List<MemberData> members;
    }

    @Data
    private static class MemberData {
        private UserData user;
        private String role = "user";
    }

    @Data
    private static class UserData {
        private String id;
        private String name;
    }

    public String getStreamApiKey() {
        return streamApiKey;
    }

    public ChatTokenDTO getAccessToken(UUID userId) {
        try {
            Client client = Client.builder(streamApiKey, streamApiSecret).build();
            // Token expires in 24 hours
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.HOUR, 24); // FIXME: Hardcoded

            String token = client.frontendToken(userId.toString(), calendar.getTime()).toString();
            ChatTokenDTO tokenDTO = new ChatTokenDTO(token, streamApiKey, userId.toString());
            return tokenDTO;
        } catch (Exception e) {
            log.error("StreamIO client error for user {}", userId, e);
            throw new RuntimeException("Failed to generate Stream token");
        }
    }

    public Map<String, Object> createTeamChannel(UUID teamId, UUID creatorId, String teamName, String creatorName) {
        if (teamId == null || creatorId == null || teamName == null || creatorName == null) {
            throw new IllegalArgumentException("teamId, creatorId, teamName, and creatorName cannot be null");
        }

        try {
            // First, ensure the creator exists as a user in Stream
            Map<String, Object> userResponse = upsertUser(creatorId, creatorName);
            log.debug("User upsert response: {}", new Gson().toJson(userResponse));

            // Then create the channel with the user as member
            Map<String, Object> body = new HashMap<>();
            Map<String, Object> data = new HashMap<>();
            List<Map<String, Object>> members = new ArrayList<>();

            // Add creator as first member and moderator
            Map<String, Object> creator = new HashMap<>();
            creator.put("user_id", creatorId.toString());
            creator.put("is_moderator", true);
            members.add(creator);

            data.put("members", members);

            Map<String, Object> createdBy = new HashMap<>();
            createdBy.put("id", creatorId.toString());
            data.put("created_by", createdBy);
            data.put("name", teamName);
            data.put("creation_date", new Date().getTime());
            body.put("data", data);

            // Create the channel
            Map<String, Object> channelResponse = apiPost(
                    "/channels/" + STREAM_ROLE_TEAM + "/" + teamId.toString() + "/query?", body);
            log.debug("Channel creation response: {}", new Gson().toJson(channelResponse));

            // Verify the channel was created successfully
            if (channelResponse.containsKey("code")) {
                Double errorCode = (Double) channelResponse.get("code");
                if (errorCode >= 400) {
                    throw new RuntimeException("Failed to create channel: " + channelResponse.get("message"));
                }
            }

            return channelResponse;
        } catch (Exception e) {
            log.error("Error creating team channel for team {} with creator {}", teamId, creatorId, e);
            throw new RuntimeException("Failed to create team channel", e);
        }
    }

    public Map<String, Object> addMemberToChannel(UUID teamId, UUID userId, String userName) {
        if (teamId == null || userId == null) {
            throw new IllegalArgumentException("teamId and userId cannot be null");
        }

        Map<String, Object> userResponse = upsertUser(userId, userName);
        log.debug("User upsert response: {}", new Gson().toJson(userResponse));

        Map<String, Object> body = new HashMap<>();
        List<Map<String, Object>> members = new ArrayList<>();

        Map<String, Object> member = new HashMap<>();
        member.put("user_id", userId.toString());
        members.add(member);

        body.put("add_members", members);

        return apiPost("/channels/" + STREAM_ROLE_TEAM + "/" + teamId.toString() + "?", body);
    }

    public Map<String, Object> removeMemberFromChannel(UUID teamId, UUID userId) {
        if (teamId == null || userId == null) {
            throw new IllegalArgumentException("teamId and userId cannot be null");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("remove_members", Collections.singletonList(userId.toString()));
        return apiPost("/channels/" + STREAM_ROLE_TEAM + "/" + teamId.toString() + "?", body);
    }

    public Map<String, Object> deleteChannel(UUID teamId) {
        if (teamId == null) {
            throw new IllegalArgumentException("teamId cannot be null");
        }
        return apiDelete("/channels/" + STREAM_ROLE_TEAM + "/" + teamId.toString() + "?");
    }

    private Map<String, Object> getChannel(UUID teamId) {
        return apiPost("/channels/" + STREAM_ROLE_TEAM + "/" + teamId.toString() + "/query?", new Object());
    }

    public List<ChatUserDTO> getChannelMembers(UUID teamId) {
        if (teamId == null) {
            throw new IllegalArgumentException("teamId cannot be null");
        }

        Map<String, Object> response = getChannel(teamId);

        if (response == null) {
            log.warn("No response received for team {}", teamId);
            return Collections.emptyList();
        }

        try {
            ChannelResponse channelResponse = new Gson().fromJson(
                    new Gson().toJson(response),
                    ChannelResponse.class);

            if (channelResponse == null || channelResponse.getMembers() == null) {
                log.warn("No members found in response for team {}", teamId);
                return Collections.emptyList();
            }

            return channelResponse.getMembers().stream()
                    .filter(memberData -> memberData.getUser() != null)
                    .map(memberData -> new ChatUserDTO(
                            memberData.getUser().getId(),
                            memberData.getUser().getName(),
                            memberData.getRole(),
                            Boolean.TRUE.equals(memberData.getRole().equalsIgnoreCase("owner"))))
                    .filter(dto -> dto.getUserId() != null && dto.getName() != null)
                    .toList();

        } catch (Exception e) {
            log.error("Error parsing channel members for team {}: {}", teamId, e.getMessage());
            return Collections.emptyList();
        }
    }

    public Map<String, Object> upsertUser(UUID userId, String name) {
        if (userId == null || name == null) {
            throw new IllegalArgumentException("userId and name cannot be null");
        }

        Map<String, Object> user = new HashMap<>();
        user.put("id", userId.toString());
        user.put("name", name);
        user.put("role", "user");
        user.put("last_active", new Date().getTime());

        Map<String, Object> body = new HashMap<>();
        Map<String, Object> users = new HashMap<>();
        users.put(userId.toString(), user);
        body.put("users", users);

        return apiPost("/users?", body);
    }

    public Map<String, Object> addChannelModerator(UUID teamId, UUID userId) {
        if (teamId == null || userId == null) {
            throw new IllegalArgumentException("teamId and userId cannot be null");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("add_moderators", Collections.singletonList(userId.toString()));
        return apiPost("/channels/" + STREAM_ROLE_TEAM + "/" + teamId.toString() + "?", body);
    }

    public Map<String, Object> removeChannelModerator(UUID teamId, UUID userId) {
        if (teamId == null || userId == null) {
            throw new IllegalArgumentException("teamId and userId cannot be null");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("demote_moderators", Collections.singletonList(userId.toString()));
        return apiPost("/channels/" + STREAM_ROLE_TEAM + "/" + teamId.toString() + "?", body);
    }

    private String getServerToken() {
        SecretKey key = Keys.hmacShaKeyFor(streamApiSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .setHeaderParam(TYP, JWT)
                .claim(EXP, getDateXMinutesInFuture(5))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Date getDateXMinutesInFuture(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, minutes);
        return cal.getTime();
    }

    private String getFullPath(String url) {
        return streamApiBaseUrl + url + "api_key=" + streamApiKey;
    }

    public Map<String, Object> getRecordings(String livestreamId) {
        if (livestreamId == null) {
            throw new IllegalArgumentException("livestreamId cannot be null");
        }

        return apiGet("/video/call/livestream/" + livestreamId + "/recordings?");
    }

    private Map<String, Object> apiGet(String url) {
        return apiToStreamImpl(new Request.Builder()
                .url(getFullPath(url))
                .addHeader(STREAM_AUTH_TYPE, JWT)
                .addHeader(AUTHORIZATION, getServerToken())
                .get()
                .build());
    }

    private Map<String, Object> apiDelete(String url) {
        return apiToStreamImpl(new Request.Builder()
                .url(getFullPath(url))
                .addHeader(STREAM_AUTH_TYPE, JWT)
                .addHeader(AUTHORIZATION, getServerToken())
                .delete()
                .build());
    }

    private Map<String, Object> apiPost(String url, Object body) {
        if (body == null) {
            throw new IllegalArgumentException("body cannot be null");
        }

        return apiToStreamImpl(new Request.Builder()
                .url(getFullPath(url))
                .addHeader(STREAM_AUTH_TYPE, JWT)
                .addHeader(AUTHORIZATION, getServerToken())
                .post(parseBody(body))
                .build());
    }

    public Map<String, Object> apiToStreamImpl(Request request) {
        try {
            Response response = httpClient.newCall(request).execute();
            Map<String, Object> result = new Gson().fromJson(response.body().string(),
                    new com.google.gson.reflect.TypeToken<Map<String, Object>>() {
                    }.getType());

            if (result.get("code") != null) {
                Double errorCode = (Double) result.get("code");
                if (errorCode == 401) {
                    throw new RuntimeException("Issue with Stream access/credentials.");
                }
                if (errorCode == 400) {
                    throw new RuntimeException("Stream Bad Request.");
                }
            }
            return result;
        } catch (IOException e) {
            log.error("Error during Stream IO call", e);
            throw new RuntimeException("Error during Stream IO call");
        }
    }

    private RequestBody parseBody(Object body) {
        String jsonBody;
        if (body instanceof String) {
            jsonBody = (String) body;
        } else {
            jsonBody = new Gson().toJson(body);
        }
        return RequestBody.create(jsonBody, JSON);
    }
}