package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Basic info about a trainer assigned to a trainee")
public record TraineeTrainerSummaryResponse(
        @Schema(description = "Username of the trainer", example = "trainer_ahmet")
        String username,

        @Schema(description = "First name of the trainer", example = "Ahmet")
        String firstName,

        @Schema(description = "Last name of the trainer", example = "Yılmaz")
        String lastName,

        @Schema(description = "Trainer's specialization ID", example = "2")
        Long specialization
) {
}
