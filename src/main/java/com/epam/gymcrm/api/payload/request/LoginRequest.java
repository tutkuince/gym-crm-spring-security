package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request with username and password")
public record LoginRequest(

        @Schema(description = "Username of the user", example = "john_doe")
        @NotBlank(message = "Username must not be blank")
        String username,

        @Schema(description = "Password of the user", example = "securePass123")
        @NotBlank(message = "Password must not be blank")
        String password
) {
}
