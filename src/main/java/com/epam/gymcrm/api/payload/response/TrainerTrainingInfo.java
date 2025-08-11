package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Detailed info about a training session from trainer perspective")
public record TrainerTrainingInfo(
        @Schema(description = "Name of the training", example = "Full Body Workout")
        String trainingName,

        @Schema(description = "Training date and time", example = "2025-08-20 10:00:00")
        String trainingDate,

        @Schema(description = "Type of the training", example = "Strength")
        String trainingType,

        @Schema(description = "Duration in minutes", example = "60")
        Integer trainingDuration,

        @Schema(description = "Name of the trainee who attended the training", example = "John Doe")
        String traineeName
) {
}
