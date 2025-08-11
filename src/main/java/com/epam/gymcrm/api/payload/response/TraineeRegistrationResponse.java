package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Response after successfully registering a trainee")
public record TraineeRegistrationResponse(
        @Schema(description = "Generated username for the trainee", example = "john_doe")
        String username,

        @Schema(description = "Auto-generated password for the trainee", example = "TempPass123!")
        String password
) {
}
