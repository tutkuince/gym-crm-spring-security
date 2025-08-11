package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Basic summary information about a trainee")
public record TraineeSummaryResponse(
        @Schema(description = "Username of the trainee", example = "john_doe")
        String username,

        @Schema(description = "First name of the trainee", example = "John")
        String firstName,

        @Schema(description = "Last name of the trainee", example = "Doe")
        String lastName
) {
}
