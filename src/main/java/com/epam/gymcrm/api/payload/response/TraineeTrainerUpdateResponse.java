package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Response after updating trainee's trainer list")
public record TraineeTrainerUpdateResponse(
        @Schema(description = "Updated list of assigned trainers")
        List<TrainerSummaryResponse> trainers
) {
}
