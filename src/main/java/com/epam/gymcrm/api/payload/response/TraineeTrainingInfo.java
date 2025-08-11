package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed info about a training session from trainee perspective")
public record TraineeTrainingInfo(
        @Schema(description = "Name of the training", example = "Cardio Blast")
        String trainingName,

        @Schema(description = "Training date and time", example = "2025-08-22 18:00:00")
        String trainingDate,

        @Schema(description = "Type of the training", example = "Cardio")
        String trainingType,

        @Schema(description = "Duration of the training in minutes", example = "45")
        Integer trainingDuration,

        @Schema(description = "Name of the trainer", example = "Jane Smith")
        String trainerName
) {
}
