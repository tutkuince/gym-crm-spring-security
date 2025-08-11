package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response listing all trainings of a trainee")
public record TraineeTrainingsListResponse(
        @Schema(description = "List of trainings attended by the trainee")
        List<TraineeTrainingInfo> trainings
) {
}
