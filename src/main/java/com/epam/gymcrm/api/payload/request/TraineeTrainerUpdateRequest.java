package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "Request to update trainee's assigned trainers")
public record TraineeTrainerUpdateRequest(

        @Schema(description = "Username of the trainee", example = "john_doe")
        @NotBlank
        String traineeUsername,

        @Schema(description = "List of trainers to assign")
        @NotEmpty
        List<TrainerUsernameRequest> trainers
) {
}
