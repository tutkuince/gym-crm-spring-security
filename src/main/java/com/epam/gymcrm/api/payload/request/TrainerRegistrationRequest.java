package com.epam.gymcrm.api.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to register a new trainer")
public record TrainerRegistrationRequest(
        @Schema(description = "First name of the trainer", example = "Ahmet")
        @NotBlank(message = "First name is required.")
        String firstName,

        @Schema(description = "Last name of the trainer", example = "Yılmaz")
        @NotBlank(message = "Last name is required.")
        String lastName,

        @Schema(description = "Specialization ID of the trainer", example = "3")
        @NotNull(message = "Specialization is required.")
        @Min(value = 1, message = "Specialization id must be positive.")
        Long specialization
) {
}
