package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after registering a new trainer")
public record TrainerRegistrationResponse(
        @Schema(description = "Generated username for the trainer", example = "trainer_ahmet")
        String username,

        @Schema(description = "Generated password for the trainer", example = "TempPass456!")
        String password
) {
}
