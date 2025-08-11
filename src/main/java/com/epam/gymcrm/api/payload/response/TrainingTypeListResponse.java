package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response containing all available training types")
public record TrainingTypeListResponse(
        @Schema(description = "List of training types")
        List<TrainingTypeResponse> trainingTypes
) {
}
