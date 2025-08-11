package com.epam.gymcrm.api.controller;

import com.epam.gymcrm.api.payload.response.TrainingTypeListResponse;
import com.epam.gymcrm.domain.service.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/training-types")
@Tag(name = "Training Types", description = "API for listing available training types")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    public TrainingTypeController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Operation(
            summary = "Get all training types",
            description = "Retrieves a list of all available training types such as Cardio, Strength, etc."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Training types retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<TrainingTypeListResponse> getAll() {
        return ResponseEntity.ok(trainingTypeService.findAllTrainingTypes());
    }

}
