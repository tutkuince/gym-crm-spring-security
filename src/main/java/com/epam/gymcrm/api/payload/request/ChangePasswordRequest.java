package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request object for changing user password")
public record ChangePasswordRequest(
        @Schema(description = "Username of the user", example = "john_doe")
        @NotBlank(message = "Username is required")
        String username,

        @Schema(description = "Current password of the user", example = "oldPass123")
        @NotBlank(message = "Old password is required")
        String oldPassword,

        @Schema(description = "New password to be set", example = "newSecurePass456")
        @NotBlank(message = "New password cannot be blank")
        String newPassword
) {
}
