package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Response after updating trainee profile")
public record TraineeProfileUpdateResponse(
        @Schema(description = "Username of the trainee", example = "john_doe")
        String username,

        @Schema(description = "First name of the trainee", example = "John")
        String firstName,

        @Schema(description = "Last name of the trainee", example = "Doe")
        String lastName,

        @Schema(description = "Date of birth", example = "1990-01-01")
        String dateOfBirth,

        @Schema(description = "Address of the trainee", example = "Berlin")
        String address,

        @Schema(description = "Whether the trainee is active", example = "true")
        boolean isActive,

        @Schema(description = "List of trainers assigned to this trainee")
        Set<TraineeTrainerSummaryResponse> trainers
) {
}
