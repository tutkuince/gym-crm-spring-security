package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login response payload")
public record LoginResponse(
        @Schema(description = "JWT access token")
        String token,

        @Schema(description = "Token expiration timestamp (ISO 8601)")
        String expiresAt
) {
}
