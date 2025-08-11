package com.epam.gymcrm.api.payload.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request for registering a new trainee")
public record TraineeRegistrationRequest(
        @Schema(description = "First name of the trainee", example = "John")
        @NotBlank(message = "First name is required")
        String firstName,

        @Schema(description = "Last name of the trainee", example = "Doe")
        @NotBlank(message = "Last name is required")
        String lastName,

        @Schema(description = "Date of birth in yyyy-MM-dd format", example = "1995-08-10")
        @JsonFormat(pattern = "yyyy-MM-dd")
        String dateOfBirth,

        @Schema(description = "Address of the trainee", example = "123 Main St, Berlin")
        String address
) {
}
