package com.biosteel.teams.common.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.type}")
    private String type;

    @Value("${firebase.project.id}")
    private String projectId;

    @Value("${firebase.private.key.id}")
    private String privateKeyId;

    @Value("${firebase.private.key}")
    private String privateKey;

    @Value("${firebase.client.email}")
    private String clientEmail;

    @Value("${firebase.client.id}")
    private String clientId;

    @Value("${firebase.auth.uri}")
    private String authUri;

    @Value("${firebase.token.uri}")
    private String tokenUri;

    @Value("${firebase.auth.provider.x509.cert.url}")
    private String authProviderCertUrl;

    @Value("${firebase.client.x509.cert.url}")
    private String clientCertUrl;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(createFirebaseCredentialStream()))
                    .build();

            FirebaseApp.initializeApp(options);
        }

        return FirebaseMessaging.getInstance();
    }

    private InputStream createFirebaseCredentialStream() {
        String formattedPrivateKey = privateKey.replace("\\n", "\n");

        String jsonCredential = String.format(
                "{\n" +
                        "  \"type\": \"%s\",\n" +
                        "  \"project_id\": \"%s\",\n" +
                        "  \"private_key_id\": \"%s\",\n" +
                        "  \"private_key\": \"%s\",\n" +
                        "  \"client_email\": \"%s\",\n" +
                        "  \"client_id\": \"%s\",\n" +
                        "  \"auth_uri\": \"%s\",\n" +
                        "  \"token_uri\": \"%s\",\n" +
                        "  \"auth_provider_x509_cert_url\": \"%s\",\n" +
                        "  \"client_x509_cert_url\": \"%s\"\n" +
                        "}",
                type, projectId, privateKeyId, formattedPrivateKey, clientEmail,
                clientId, authUri, tokenUri, authProviderCertUrl, clientCertUrl);

        return new ByteArrayInputStream(jsonCredential.getBytes(StandardCharsets.UTF_8));
    }
}