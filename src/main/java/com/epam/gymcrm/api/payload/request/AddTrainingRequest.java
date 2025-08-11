package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request object for adding a new training session")
public record AddTrainingRequest(
        @Schema(description = "Username of the trainee", example = "john_doe")
        @NotBlank(message = "Trainee username must not be blank")
        String traineeUsername,

        @Schema(description = "Username of the trainer", example = "trainer_jane")
        @NotBlank(message = "Trainer username must not be blank")
        String trainerUsername,

        @Schema(description = "Name of the training session", example = "Leg Day Strength Training")
        @NotBlank(message = "Training name must not be blank")
        String trainingName,

        @Schema(description = "Date and time of the training in format yyyy-MM-dd HH:mm:ss", example = "2025-08-10 14:30:00")
        @NotBlank(message = "Training date must not be blank (yyyy-MM-dd HH:mm:ss)")
        String trainingDate,

        @Schema(description = "Duration of the training in minutes", example = "60")
        @NotNull(message = "Training duration must not be null")
        Integer trainingDuration
) {
}
