package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response after updating a trainer's profile")
public record UpdateTrainerProfileResponse(
        @Schema(description = "Username of the trainer", example = "trainer_kemal")
        String username,

        @Schema(description = "First name of the trainer", example = "Kemal")
        String firstName,

        @Schema(description = "Last name of the trainer", example = "Demir")
        String lastName,

        @Schema(description = "Trainer's specialization ID", example = "2")
        Long specialization,

        @Schema(description = "Trainer's active status", example = "true")
        Boolean isActive,

        @Schema(description = "List of trainees assigned to this trainer")
        List<UpdateTraineeSummaryResponse> trainees
) {
}
