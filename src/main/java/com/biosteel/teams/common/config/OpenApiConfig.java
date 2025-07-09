package com.biosteel.teams.common.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.headers.Header;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

        @Value("${openapi.server.url}")
        private String serverUrl;

        private static final List<String> PUBLIC_PATHS = Arrays.asList(
                        "/api/users/register/**",
                        "/api/users/login",
                        "/users/register/**",
                        "/users/login",
                        "/api/users/resendForgotPasswordToken",
                        "/users/resendForgotPasswordToken",
                        "/api/users/resetPassword/request",
                        "/users/resetPassword/request",
                        "/api/users/resetPassword/confirm",
                        "/users/resetPassword/confirm",
                        "/api/public/**",
                        "/public/**",
                        "/v1/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/actuator/health");

        @Bean
        public OpenAPI customOpenAPI() {
                SecurityScheme securityScheme = new SecurityScheme()
                                .type(Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Please enter JWT token");

                // Create user profile schema
                Schema<UserProfileResponse> userProfileSchema = new Schema<UserProfileResponse>()
                                .type("object");
                userProfileSchema.addProperty("success", new Schema<Boolean>()
                                .type("boolean")
                                .example(true));
                userProfileSchema.addProperty("message", new Schema<String>()
                                .type("string")
                                .example("Current user profile retrieved successfully"));
                userProfileSchema.addProperty("data", new Schema<Object>()
                                .type("object")
                                .addProperty("id",
                                                new Schema<String>().type("string")
                                                                .example("123e4567-e89b-12d3-a456-426614174000"))
                                .addProperty("email", new Schema<String>().type("string").example("user@example.com"))
                                .addProperty("firstName", new Schema<String>().type("string").example("John"))
                                .addProperty("lastName", new Schema<String>().type("string").example("Doe")));

                // Create user profile operation
                Operation userProfileOperation = new Operation()
                                .summary("Get current user's profile")
                                .description("Retrieves the profile of the currently authenticated user")
                                .tags(Arrays.asList("User Management"))
                                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                                .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse()
                                                                .description("Successfully retrieved user profile")
                                                                .content(new Content()
                                                                                .addMediaType("application/json",
                                                                                                new MediaType()
                                                                                                                .schema(userProfileSchema))))
                                                .addApiResponse("401", new ApiResponse()
                                                                .description("Unauthorized - Invalid or missing token")));

                // Create login request schema
                Schema<LoginRequest> loginRequestSchema = new Schema<LoginRequest>()
                                .type("object");
                loginRequestSchema.addProperty("username", new Schema<String>()
                                .type("string")
                                .example("user@example.com")
                                .description("Username or email"));
                loginRequestSchema.addProperty("password", new Schema<String>()
                                .type("string")
                                .example("password123")
                                .description("User password"));
                loginRequestSchema.setRequired(Arrays.asList("username", "password"));

                // Create success response schema
                Schema<SuccessResponse> successResponseSchema = new Schema<SuccessResponse>()
                                .type("object");
                successResponseSchema.addProperty("success", new Schema<Boolean>()
                                .type("boolean")
                                .example(true));
                successResponseSchema.addProperty("message", new Schema<String>()
                                .type("string")
                                .example("Login successful"));

                // Create error response schema
                Schema<ErrorResponse> errorResponseSchema = new Schema<ErrorResponse>()
                                .type("object");
                errorResponseSchema.addProperty("error", new Schema<String>()
                                .type("string")
                                .example("Authentication failed: Invalid username or password"));

                Operation loginOperation = new Operation()
                                .summary("Login with username/email and password")
                                .description("Authenticates user and returns JWT token in Authorization header")
                                .tags(Arrays.asList("Authentication"))
                                .requestBody(new RequestBody()
                                                .required(true)
                                                .content(new Content()
                                                                .addMediaType("application/json", new MediaType()
                                                                                .schema(loginRequestSchema))))
                                .responses(new ApiResponses()
                                                .addApiResponse("200", new ApiResponse()
                                                                .description("Successfully authenticated")
                                                                .headers(Map.of(
                                                                                "Authorization", new Header()
                                                                                                .description("Bearer token for authentication")
                                                                                                .schema(new Schema<String>()
                                                                                                                .type("string")
                                                                                                                .example("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))))
                                                                .content(new Content()
                                                                                .addMediaType("application/json",
                                                                                                new MediaType()
                                                                                                                .schema(successResponseSchema))))
                                                .addApiResponse("401", new ApiResponse()
                                                                .description("Invalid credentials")
                                                                .content(new Content()
                                                                                .addMediaType("application/json",
                                                                                                new MediaType()
                                                                                                                .schema(errorResponseSchema)))));

                Server server = new Server();
                server.setUrl(serverUrl);
                server.setDescription("AWS Development Server");
                OpenAPI openAPI = new OpenAPI()
                                .servers(List.of(server))
                                .info(new Info()
                                                .title("BioSteel Teams API")
                                                .version("1.0")
                                                .description("API documentation for BioSteel Teams {prject}")
                                                .termsOfService("http://swagger.io/terms/")
                                                .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                                .components(new Components()
                                                .addSecuritySchemes("bearerAuth", securityScheme));

                // Add paths
                openAPI.path("/api/users/login", new PathItem().post(loginOperation));
                openAPI.path("/api/users/me", new PathItem().get(userProfileOperation));

                // First, add security to all paths
                if (openAPI.getPaths() != null) {
                        openAPI.getPaths().forEach((path, pathItem) -> {
                                SecurityRequirement securityRequirement = new SecurityRequirement()
                                                .addList("bearerAuth");

                                if (pathItem.getGet() != null) {
                                        pathItem.getGet().addSecurityItem(securityRequirement);
                                }
                                if (pathItem.getPost() != null) {
                                        pathItem.getPost().addSecurityItem(securityRequirement);
                                }
                                if (pathItem.getPut() != null) {
                                        pathItem.getPut().addSecurityItem(securityRequirement);
                                }
                                if (pathItem.getDelete() != null) {
                                        pathItem.getDelete().addSecurityItem(securityRequirement);
                                }
                                if (pathItem.getPatch() != null) {
                                        pathItem.getPatch().addSecurityItem(securityRequirement);
                                }
                        });
                }

                // Then remove security from public paths
                if (openAPI.getPaths() != null) {
                        openAPI.getPaths().forEach((path, pathItem) -> {
                                boolean isPublicPath = PUBLIC_PATHS.stream().anyMatch(publicPath -> {
                                        if (publicPath.endsWith("/**")) {
                                                String basePattern = publicPath.substring(0, publicPath.length() - 3);
                                                return path.startsWith(basePattern);
                                        }
                                        return path.equals(publicPath);
                                });

                                if (isPublicPath) {
                                        if (pathItem.getGet() != null) {
                                                pathItem.getGet().setSecurity(null);
                                        }
                                        if (pathItem.getPost() != null) {
                                                pathItem.getPost().setSecurity(null);
                                        }
                                        if (pathItem.getPut() != null) {
                                                pathItem.getPut().setSecurity(null);
                                        }
                                        if (pathItem.getDelete() != null) {
                                                pathItem.getDelete().setSecurity(null);
                                        }
                                        if (pathItem.getPatch() != null) {
                                                pathItem.getPatch().setSecurity(null);
                                        }
                                }
                        });
                }

                return openAPI;
        }

        // Helper classes for type safety
        private static class LoginRequest {
                private String username;
                private String password;
        }

        private static class SuccessResponse {
                private boolean success;
                private String message;
        }

        private static class ErrorResponse {
                private String error;
        }

        private static class UserProfileResponse {
                private boolean success;
                private String message;
                private Object data;
        }
}