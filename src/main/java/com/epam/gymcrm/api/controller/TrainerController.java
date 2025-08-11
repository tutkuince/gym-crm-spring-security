package com.epam.gymcrm.api.controller;

import com.epam.gymcrm.api.payload.request.TrainerRegistrationRequest;
import com.epam.gymcrm.api.payload.request.TrainerTrainingsFilter;
import com.epam.gymcrm.api.payload.request.UpdateActiveStatusRequest;
import com.epam.gymcrm.api.payload.request.UpdateTrainerProfileRequest;
import com.epam.gymcrm.api.payload.response.TrainerProfileResponse;
import com.epam.gymcrm.api.payload.response.TrainerRegistrationResponse;
import com.epam.gymcrm.api.payload.response.TrainerTrainingsListResponse;
import com.epam.gymcrm.api.payload.response.UpdateTrainerProfileResponse;
import com.epam.gymcrm.domain.service.TrainerService;
import com.epam.gymcrm.domain.exception.UnauthorizedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.epam.gymcrm.api.auth.AuthSessionManager.isLoggedInd;
import static com.epam.gymcrm.api.auth.AuthSessionManager.logout;

@RestController
@RequestMapping(value = "/api/v1/trainers", produces = "application/json")
@Tag(name = "Trainer", description = "API for managing trainers and their trainings")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @Operation(
            summary = "Register new trainer",
            description = "Creates a new trainer with first name, last name, and specialization ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer successfully registered"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PostMapping
    public ResponseEntity<TrainerRegistrationResponse> registerTrainer(
            @Valid @RequestBody TrainerRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(trainerService.createTrainer(request));
    }

    @Operation(
            summary = "Get trainer profile",
            description = "Returns profile of the trainer and the list of assigned trainees"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer profile retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/profile")
    public ResponseEntity<TrainerProfileResponse> getTrainerProfile(@RequestParam(name = "username") String username) {
        // Check if the user is authenticated
        if (!isLoggedInd(username)) {
            // If not logged in, throw an error.
            throw new UnauthorizedException("Trainer not authenticated, please login first!");
        }

        TrainerProfileResponse response = trainerService.getTrainerProfile(username);

        // Logout the user immediately after the successful change.
        logout(username);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update trainer profile",
            description = "Updates a trainer's name and active status. Specialization is read-only."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PutMapping("/profile")
    public ResponseEntity<UpdateTrainerProfileResponse> updateTrainerProfile(@Valid @RequestBody UpdateTrainerProfileRequest request) {
        String username = request.getUsername();

        if (!isLoggedInd(username)) {
            // If not logged in, throw an error.
            throw new UnauthorizedException("Trainer not authenticated, please login first!");
        }

        UpdateTrainerProfileResponse updateTrainerProfileResponse = trainerService.updateTrainerProfile(request);

        logout(username);

        return ResponseEntity.ok(updateTrainerProfileResponse);
    }

    @Operation(
            summary = "Get trainer's trainings",
            description = "Fetches the training sessions held by a trainer. Filters available by date and trainee name."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @GetMapping("/trainings")
    public ResponseEntity<TrainerTrainingsListResponse> getTrainerTrainings(
            @RequestParam("username") String username,
            @RequestParam(value = "periodFrom", required = false) String periodFrom,
            @RequestParam(value = "periodTo", required = false) String periodTo,
            @RequestParam(value = "traineeName", required = false) String traineeName
    ) {

        if (!isLoggedInd(username)) {
            // If not logged in, throw an error.
            throw new UnauthorizedException("Trainer not authenticated, please login first!");
        }

        TrainerTrainingsFilter filter = new TrainerTrainingsFilter(
                username, periodFrom, periodTo, traineeName
        );
        TrainerTrainingsListResponse response = trainerService.getTrainerTrainings(filter);

        logout(username);

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Update trainer active status",
            description = "Activates or deactivates the trainer account"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Status updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized access")
    })
    @PatchMapping("/status")
    public ResponseEntity<Void> updateTrainerActiveStatus(@RequestBody @Valid UpdateActiveStatusRequest request) {
        String username = request.username();

        if (!isLoggedInd(username)) {
            // If not logged in, throw an error.
            throw new UnauthorizedException("Trainer not authenticated, please login first!");
        }

        trainerService.updateActivateStatus(request);

        logout(username);

        return ResponseEntity.ok().build();
    }
}
