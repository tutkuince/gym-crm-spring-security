package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response listing all trainings conducted by a trainer")
public record TrainerTrainingsListResponse(
        @Schema(description = "List of trainings")
        List<TrainerTrainingInfo> trainings
) {
}
