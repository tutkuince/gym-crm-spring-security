package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Trainer username wrapper")
public record TrainerUsernameRequest(
        @Schema(description = "Username of the trainer", example = "trainer_ahmet")
        @NotBlank
        String trainerUsername
) {
}
