package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Basic trainer info")
public record TrainerInfoResponse(
        @Schema(description = "Username of the trainer", example = "trainer_jane")
        String username,

        @Schema(description = "First name of the trainer", example = "Jane")
        String firstName,

        @Schema(description = "Last name of the trainer", example = "Smith")
        String lastName,

        @Schema(description = "Specialization ID of the trainer", example = "1")
        Long specialization
) {
}
