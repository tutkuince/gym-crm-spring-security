package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Basic information about an active unassigned trainer")
public record UnassignedActiveTrainerResponse(
        @Schema(description = "Username of the trainer", example = "trainer_hasan")
        String username,

        @Schema(description = "First name", example = "Hasan")
        String firstName,

        @Schema(description = "Last name", example = "Çelik")
        String lastName,

        @Schema(description = "Specialization ID", example = "4")
        Long specialization
) {
}
