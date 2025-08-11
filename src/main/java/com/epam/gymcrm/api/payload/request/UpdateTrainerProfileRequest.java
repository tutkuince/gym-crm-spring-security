package com.epam.gymcrm.api.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to update trainer profile information")
public class UpdateTrainerProfileRequest {
        @Schema(description = "Username of the trainer", example = "trainer_ahmet")
        @NotBlank(message = "Username must not be blank")
        private String username;

        @Schema(description = "First name of the trainer", example = "Ahmet")
        @NotBlank(message = "First name must not be blank")
        private String firstName;

        @Schema(description = "Last name of the trainer", example = "Yılmaz")
        @NotBlank(message = "Last name must not be blank")
        private String lastName;

        @Schema(description = "Specialization ID of the trainer (read-only)", example = "2", accessMode = Schema.AccessMode.READ_ONLY)
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        private Long specialization;

        @Schema(description = "Active status of the trainer", example = "true")
        @NotNull(message = "isActive must not be null")
        private Boolean isActive;

        public @NotBlank(message = "Username must not be blank") String getUsername() {
                return username;
        }

        public void setUsername(@NotBlank(message = "Username must not be blank") String username) {
                this.username = username;
        }

        public @NotBlank(message = "First name must not be blank") String getFirstName() {
                return firstName;
        }

        public void setFirstName(@NotBlank(message = "First name must not be blank") String firstName) {
                this.firstName = firstName;
        }

        public @NotBlank(message = "Last name must not be blank") String getLastName() {
                return lastName;
        }

        public void setLastName(@NotBlank(message = "Last name must not be blank") String lastName) {
                this.lastName = lastName;
        }

        public Long getSpecialization() {
                return specialization;
        }

        public void setSpecialization(Long specialization) {
                this.specialization = specialization;
        }

        public @NotNull(message = "isActive must not be null") Boolean getActive() {
                return isActive;
        }

        public void setActive(@NotNull(message = "isActive must not be null") Boolean active) {
                isActive = active;
        }
}
