package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Filter object for fetching trainee trainings")
public record TraineeTrainingsFilter(
        @Schema(description = "Username of the trainee", example = "john_doe")
        @NotBlank(message = "Username must not be blank")
        String username,

        @Schema(description = "Start date of the filter period", example = "2025-08-01")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "periodFrom must be in the format yyyy-MM-dd")
        String periodFrom,

        @Schema(description = "End date of the filter period", example = "2025-08-31")
        @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "periodTo must be in the format yyyy-MM-dd")
        String periodTo,

        @Schema(description = "Filter by trainer name", example = "Jane Trainer")
        String trainerName,

        @Schema(description = "Filter by training type", example = "Strength")
        String trainingType
) {
}
