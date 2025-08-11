package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Single training type information")
public record TrainingTypeResponse(
        @Schema(description = "ID of the training type", example = "1")
        Long id,

        @Schema(description = "Name of the training type", example = "Cardio")
        String name
) {
}
