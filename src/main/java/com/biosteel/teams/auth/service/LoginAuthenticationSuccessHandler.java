package com.biosteel.teams.auth.service;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Service;

import com.biosteel.teams.auth.dto.BearerTokenDto;
import com.biosteel.teams.chat.service.StreamChatService;
import com.biosteel.teams.user.model.User;
import com.biosteel.teams.user.security.DSUserDetails;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * The LoginAuthenticationSuccessHandler is called after a user successfully
 * logs in.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class LoginAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
	@Autowired
	private final TokenService tokenService;
	private final StreamChatService streamChatService;

	public static final String TOKEN_PREFIX = "Bearer";
	public static final String HEADER_STRING = "Authorization";

	/**
	 * On authentication success.
	 *
	 * @param request        the request
	 * @param response       the response
	 * @param authentication the authentication
	 * @throws IOException      Signals that an I/O exception has occurred.
	 * @throws ServletException the servlet exception
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException,
			ServletException {
		User user = null;
		if (authentication != null && authentication.getPrincipal() != null) {
			log.debug("LoginAuthenticationSuccessHandler.onAuthenticationSuccess() authentication.getPrincipal(): "
					+ authentication.getPrincipal());
			log.debug("LoginAuthenticationSuccessHandler.onAuthenticatonSuccess() authentication.getClass(): "
					+ authentication.getClass());
			log.debug(
					"LoginAuthenticationSuccessHandler.onAuthenticationSuccess() authentication.getPrincipal().getClass(): "
							+ authentication.getPrincipal().getClass());
			if (authentication.getPrincipal() instanceof DSUserDetails) {
				log.debug("LoginAuthenticationSuccessHandler.onAuthenticationSuccess:" + "DSUserDetails: "
						+ authentication.getPrincipal());
				user = ((DSUserDetails) authentication.getPrincipal()).getUser();
			}
		}

		if (user == null) {
			return;
		}

		setUserBearerToken(response, user);

		response.addHeader("streamApiKey", streamChatService.getStreamApiKey());
		// ChatUserDTO chatUser = chatService.upsertUser(user.getUserId(),
		// user.getUserName());
		// super.onAuthenticationSuccess(request, response, authentication);
	}

	public void setUserBearerToken(HttpServletResponse response, User user) {
		BearerTokenDto bearerTokenDto = tokenService.generateNewToken(user);

		response.addHeader(HEADER_STRING, TOKEN_PREFIX + " " +
				bearerTokenDto.getAccessToken());
	}

}
