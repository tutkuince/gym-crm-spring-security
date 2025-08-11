package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update trainee profile")
public record TraineeUpdateRequest(
        @Schema(description = "Username of the trainee", example = "john_doe")
        @NotBlank
        String username,

        @Schema(description = "First name of the trainee", example = "John")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name of the trainee", example = "Doe")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Date of birth in yyyy-MM-dd format", example = "1990-01-15")
        String dateOfBirth,

        @Schema(description = "Address of the trainee", example = "Berlin, Germany")
        String address,

        @Schema(description = "Whether the trainee is active or not", example = "true")
        @NotNull(message = "isActive is required")
        boolean isActive
) {
}
