package com.epam.gymcrm.api.controller;

import com.epam.gymcrm.api.payload.request.*;
import com.epam.gymcrm.api.payload.response.*;
import com.epam.gymcrm.domain.service.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/trainees", produces = "application/json")
@Tag(name = "Trainee", description = "API for managing trainees and their profiles")
public class TraineeController {

    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Operation(
            summary = "Register a new trainee",
            description = "Creates a new trainee with personal information and returns generated credentials."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainee successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<TraineeRegistrationResponse> createTrainee(@RequestBody @Valid TraineeRegistrationRequest traineeRegistrationRequest) {
        return new ResponseEntity<>(traineeService.createTrainee(traineeRegistrationRequest), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get trainee profile",
            description = "Retrieves a trainee's profile information and their assigned trainers."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee profile retrieved"),
            @ApiResponse(responseCode = "401", description = "User not authenticated")
    })
    @GetMapping("/profile")
    public ResponseEntity<TraineeProfileResponse> getTraineeByUsername(@RequestParam(name = "username") String username) {
        return ResponseEntity.ok(traineeService.findByUsername(username));
    }

    @Operation(
            summary = "Update trainee profile",
            description = "Updates an existing trainee's personal information and active status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PutMapping
    public ResponseEntity<TraineeProfileUpdateResponse> updateTrainee(@RequestBody @Valid TraineeUpdateRequest traineeUpdateRequest) {
        return new ResponseEntity<>(traineeService.update(traineeUpdateRequest), HttpStatus.OK);
    }

    @Operation(
            summary = "Delete trainee",
            description = "Deletes a trainee and all associated data by their username."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainee deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> deleteTraineeByUsername(@PathVariable(name = "username") String username) {
        traineeService.deleteTraineeByUsername(username);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Update assigned trainers",
            description = "Assigns or replaces the list of trainers for a given trainee."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainers updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PutMapping("/trainers")
    public ResponseEntity<TraineeTrainerUpdateResponse> updateTraineeTrainers(@RequestBody @Valid TraineeTrainerUpdateRequest request) {
        return ResponseEntity.ok(traineeService.updateTraineeTrainers(request));
    }

    @Operation(
            summary = "Get trainee trainings",
            description = "Fetches all training sessions of the trainee, optionally filtered by date, trainer, or training type."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/trainings")
    public ResponseEntity<TraineeTrainingsListResponse> getTraineeTrainings(
            @RequestParam(name = "username") String username,
            @RequestParam(name = "periodFrom", required = false) String periodFrom,
            @RequestParam(name = "periodTo", required = false) String periodTo,
            @RequestParam(name = "trainerName", required = false) String trainerName,
            @RequestParam(name = "trainingType", required = false) String trainingType
    ) {
        TraineeTrainingsFilter filter = new TraineeTrainingsFilter(
                username, periodFrom, periodTo, trainerName, trainingType
        );

        return ResponseEntity.ok(traineeService.getTraineeTrainings(filter));
    }

    @Operation(
            summary = "Update trainee active status",
            description = "Activates or deactivates the trainee based on provided status."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> activateTrainee(@RequestBody @Valid UpdateActiveStatusRequest updateActiveStatusRequest) {
        traineeService.updateActivateStatus(updateActiveStatusRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Get unassigned trainers",
            description = "Returns a list of active trainers not yet assigned to the given trainee."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unassigned trainers retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/unassigned-trainers")
    public ResponseEntity<UnassignedActiveTrainerListResponse> getUnassignedTrainersForTrainee(@RequestParam(name = "username") String username) {
        return ResponseEntity.ok(traineeService.getUnassignedActiveTrainersForTrainee(username));
    }
}
