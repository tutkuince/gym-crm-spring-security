package com.epam.gymcrm.api.payload.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "List of active trainers not assigned to any trainee")
public record UnassignedActiveTrainerListResponse(
        @Schema(description = "List of unassigned trainers")
        List<UnassignedActiveTrainerResponse> trainers
) {
}
