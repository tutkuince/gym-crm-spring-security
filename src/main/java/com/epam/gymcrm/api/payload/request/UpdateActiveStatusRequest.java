package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update active status of a user")
public record UpdateActiveStatusRequest(
        @Schema(description = "Username of the user", example = "john_doe")
        @NotBlank(message = "Username must not be blank")
        String username,

        @Schema(description = "New active status", example = "false")
        @NotNull(message = "isActive must not be null")
        Boolean isActive
) {
}
