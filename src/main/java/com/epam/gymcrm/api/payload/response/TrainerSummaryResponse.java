package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary information of a trainer")
public record TrainerSummaryResponse(
        @Schema(description = "Username of the trainer", example = "trainer_ayse")
        String trainerUsername,

        @Schema(description = "First name of the trainer", example = "Ayşe")
        String trainerFirstName,

        @Schema(description = "Last name of the trainer", example = "Kaya")
        String trainerLastName,

        @Schema(description = "Specialization ID", example = "2")
        Long trainerSpecialization
) {
}
