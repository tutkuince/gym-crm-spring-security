package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Detailed profile of a trainer including assigned trainees")
public record TrainerProfileResponse(
        @Schema(description = "First name of the trainer", example = "Ahmet")
        String firstName,

        @Schema(description = "Last name of the trainer", example = "Yılmaz")
        String lastName,

        @Schema(description = "Specialization ID of the trainer", example = "3")
        Long specialization,

        @Schema(description = "Whether the trainer is active", example = "true")
        boolean isActive,

        @Schema(description = "List of trainees assigned to the trainer")
        List<TraineeSummaryResponse> trainees
) {
}
