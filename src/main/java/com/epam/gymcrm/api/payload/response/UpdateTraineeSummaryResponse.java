package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Summary of a trainee under a specific trainer after profile update")
public record UpdateTraineeSummaryResponse(
        @Schema(description = "Trainer's username", example = "trainer_mustafa")
        String trainerUsername,

        @Schema(description = "Trainer's first name", example = "Mustafa")
        String trainerFirstName,

        @Schema(description = "Trainer's last name", example = "Koç")
        String trainerLastName,

        @Schema(description = "Trainer's specialization ID", example = "5")
        Long trainerSpecialization
) {
}
