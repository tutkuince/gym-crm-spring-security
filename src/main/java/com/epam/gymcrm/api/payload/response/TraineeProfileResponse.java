package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

@Schema(description = "Trainee profile details including assigned trainers")
public record TraineeProfileResponse(
        @Schema(description = "First name of the trainee", example = "John")
        String firstName,

        @Schema(description = "Last name of the trainee", example = "Doe")
        String lastName,

        @Schema(description = "Date of birth in yyyy-MM-dd format", example = "1990-01-15")
        String dateOfBirth,

        @Schema(description = "Address of the trainee", example = "Berlin, Germany")
        String address,

        @Schema(description = "Whether the trainee is active", example = "true")
        boolean isActive,

        @Schema(description = "List of assigned trainers")
        Set<TrainerInfoResponse> trainerInfoResponses
) {
}
