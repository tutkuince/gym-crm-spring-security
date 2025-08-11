package com.epam.gymcrm.api.controller;

import com.epam.gymcrm.api.auth.AuthSessionManager;
import com.epam.gymcrm.api.payload.request.TraineeRegistrationRequest;
import com.epam.gymcrm.api.payload.request.TraineeTrainerUpdateRequest;
import com.epam.gymcrm.api.payload.request.TraineeUpdateRequest;
import com.epam.gymcrm.api.payload.request.UpdateActiveStatusRequest;
import com.epam.gymcrm.api.payload.response.*;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.GlobalExceptionHandler;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.service.TraineeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TraineeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TraineeService traineeService;

    @InjectMocks
    private TraineeController traineeController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(traineeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void cleanup() {
        AuthSessionManager.logout("ali.veli");
        AuthSessionManager.logout("notfound.user");
        AuthSessionManager.logout("unknown");
        AuthSessionManager.clearAll();
    }

    @Test
    void createTrainee_shouldReturnCreatedStatusAndRegisterResponse() throws Exception {
        TraineeRegistrationRequest request = new TraineeRegistrationRequest(
                "Ali", "Veli", "1999-01-01", "İstanbul"
        );
        TraineeRegistrationResponse response = new TraineeRegistrationResponse("ali.veli", "12345");

        when(traineeService.createTrainee(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("ali.veli"))
                .andExpect(jsonPath("$.password").value("12345"));
    }

    @Test
    void getTraineeByUsername_shouldReturnProfileResponse() throws Exception {
        String username = "ali.veli";
        AuthSessionManager.login(username);
        TraineeProfileResponse profile = new TraineeProfileResponse(
                "Ali", "Veli", "1999-01-01", "İstanbul", true, Set.of()
        );

        when(traineeService.findByUsername(username)).thenReturn(profile);

        mockMvc.perform(get("/api/v1/trainees/profile")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Ali"))
                .andExpect(jsonPath("$.lastName").value("Veli"))
                .andExpect(jsonPath("$.dateOfBirth").value("1999-01-01"))
                .andExpect(jsonPath("$.address").value("İstanbul"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void updateTrainee_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "ali.veli",
                                    "firstName": "Ali",
                                    "lastName": "Veli",
                                    "dateOfBirth": "1995-01-01",
                                    "address": "Istanbul",
                                    "isActive": true
                                }
                                """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTrainee_shouldReturn200AndUpdatedProfile_whenAuthenticated() throws Exception {
        AuthSessionManager.login("ali.veli");

        TraineeProfileUpdateResponse response = new TraineeProfileUpdateResponse(
                "ali.veli", "Ali", "Veli", "1995-01-01", "Istanbul", true, Set.of()
        );

        when(traineeService.update(any(TraineeUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "ali.veli",
                                    "firstName": "Ali",
                                    "lastName": "Veli",
                                    "dateOfBirth": "1995-01-01",
                                    "address": "Istanbul",
                                    "isActive": true
                                }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("ali.veli"))
                .andExpect(jsonPath("$.firstName").value("Ali"))
                .andExpect(jsonPath("$.lastName").value("Veli"))
                .andExpect(jsonPath("$.dateOfBirth").value("1995-01-01"))
                .andExpect(jsonPath("$.address").value("Istanbul"))
                .andExpect(jsonPath("$.isActive").value(true));
    }

    @Test
    void updateTrainee_shouldReturn404_whenTraineeNotFound_andAuthenticated() throws Exception {
        AuthSessionManager.login("ali.veli");

        when(traineeService.update(any(TraineeUpdateRequest.class)))
                .thenThrow(new NotFoundException("Trainee to update not found with Username: ali.veli"));

        mockMvc.perform(put("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "ali.veli",
                                    "firstName": "Ali",
                                    "lastName": "Veli",
                                    "isActive": true
                                }
                                """)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTrainee_shouldReturn400_whenBadRequest_andAuthenticated() throws Exception {
        AuthSessionManager.login("ali.veli");

        when(traineeService.update(any(TraineeUpdateRequest.class)))
                .thenThrow(new BadRequestException("Invalid dateOfBirth format"));

        mockMvc.perform(put("/api/v1/trainees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "username": "ali.veli",
                                    "firstName": "Ali",
                                    "lastName": "Veli",
                                    "dateOfBirth": "not-a-date",
                                    "isActive": true
                                }
                                """)
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteTrainee_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(delete("/api/v1/trainees/{username}", "ali.veli"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deleteTrainee_shouldReturn200_whenDeleted_andAuthenticated() throws Exception {
        AuthSessionManager.login("ali.veli");
        mockMvc.perform(delete("/api/v1/trainees/{username}", "ali.veli"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTrainee_shouldReturn404_whenTraineeNotFound_andAuthenticated() throws Exception {
        AuthSessionManager.login("unknown");
        doThrow(new NotFoundException("not found"))
                .when(traineeService).deleteTraineeByUsername("unknown");

        mockMvc.perform(delete("/api/v1/trainees/{username}", "unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTraineeTrainers_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(put("/api/v1/trainees/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "traineeUsername": "ali.veli",
                                        "trainers": [
                                            { "trainerUsername": "trainer1" },
                                            { "trainerUsername": "trainer2" }
                                        ]
                                    }
                                """)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateTraineeTrainers_shouldReturn200AndTrainersList_whenAuthenticated() throws Exception {
        AuthSessionManager.login("ali.veli");
        List<TrainerSummaryResponse> trainers = List.of(
                new TrainerSummaryResponse("trainer1", "Ahmet", "Yılmaz", 1L),
                new TrainerSummaryResponse("trainer2", "Ayşe", "Kara", 2L)
        );
        TraineeTrainerUpdateResponse response = new TraineeTrainerUpdateResponse(trainers);

        when(traineeService.updateTraineeTrainers(any(TraineeTrainerUpdateRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/trainees/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "traineeUsername": "ali.veli",
                                        "trainers": [
                                            { "trainerUsername": "trainer1" },
                                            { "trainerUsername": "trainer2" }
                                        ]
                                    }
                                """)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers.length()").value(2))
                .andExpect(jsonPath("$.trainers[0].trainerUsername").value("trainer1"))
                .andExpect(jsonPath("$.trainers[0].trainerFirstName").value("Ahmet"))
                .andExpect(jsonPath("$.trainers[0].trainerSpecialization").value(1))
                .andExpect(jsonPath("$.trainers[1].trainerUsername").value("trainer2"))
                .andExpect(jsonPath("$.trainers[1].trainerSpecialization").value(2));

        verify(traineeService).updateTraineeTrainers(any(TraineeTrainerUpdateRequest.class));
    }

    @Test
    void updateTraineeTrainers_shouldReturn404_whenTraineeNotFound_andAuthenticated() throws Exception {
        AuthSessionManager.login("ali.veli");

        when(traineeService.updateTraineeTrainers(any(TraineeTrainerUpdateRequest.class)))
                .thenThrow(new NotFoundException("Trainee not found with username: ali.veli"));

        mockMvc.perform(put("/api/v1/trainees/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                    {
                                        "traineeUsername": "ali.veli",
                                        "trainers": [
                                            { "trainerUsername": "trainer1" }
                                        ]
                                    }
                                """)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void getTraineeTrainings_shouldReturn200AndTrainingsList_whenAuthenticated() throws Exception {
        AuthSessionManager.login("ali.veli");

        TraineeTrainingInfo trainingInfo = new TraineeTrainingInfo(
                "Push Day", "2024-06-20T00:00:00", "Strength", 0, null
        );
        TraineeTrainingsListResponse response = new TraineeTrainingsListResponse(
                List.of(trainingInfo)
        );

        when(traineeService.getTraineeTrainings(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/trainings")
                        .param("username", "ali.veli")
                        .param("periodFrom", "2024-01-01")
                        .param("periodTo", "2024-07-31")
                        .param("trainerName", "Ahmet")
                        .param("trainingType", "Strength")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainings.length()").value(1))
                .andExpect(jsonPath("$.trainings[0].trainingName").value("Push Day"))
                .andExpect(jsonPath("$.trainings[0].trainingDate").value("2024-06-20T00:00:00"))
                .andExpect(jsonPath("$.trainings[0].trainingType").value("Strength"));

        verify(traineeService).getTraineeTrainings(any());
    }

    @Test
    void getTraineeTrainings_shouldReturn404_whenTraineeNotFound_andAuthenticated() throws Exception {
        AuthSessionManager.login("notfound.user");
        when(traineeService.getTraineeTrainings(any()))
                .thenThrow(new NotFoundException("Trainee not found: notfound.user"));

        mockMvc.perform(get("/api/v1/trainees/trainings")
                        .param("username", "notfound.user")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Trainee not found")));
    }

    @Test
    void updateActiveStatus_shouldReturn200_whenSuccess_andAuthenticated() throws Exception {
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest("ali.veli", true);

        AuthSessionManager.login("ali.veli");

        doNothing().when(traineeService).updateActivateStatus(any(UpdateActiveStatusRequest.class));

        mockMvc.perform(patch("/api/v1/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(traineeService).updateActivateStatus(any(UpdateActiveStatusRequest.class));
    }

    @Test
    void updateActiveStatus_shouldReturn404_whenNotFound_andAuthenticated() throws Exception {
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest("notfound.user", true);

        AuthSessionManager.login("notfound.user");

        doThrow(new NotFoundException("Trainee not found"))
                .when(traineeService).updateActivateStatus(any(UpdateActiveStatusRequest.class));

        mockMvc.perform(patch("/api/v1/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateActiveStatus_shouldReturn409_whenAlreadyActiveOrInactive_andAuthenticated() throws Exception {
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest("ali.veli", false);

        AuthSessionManager.login("ali.veli");

        doThrow(new IllegalStateException("User is already inactive."))
                .when(traineeService).updateActivateStatus(any(UpdateActiveStatusRequest.class));

        mockMvc.perform(patch("/api/v1/trainees/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getUnassignedActiveTrainersForTrainee_shouldReturn401_whenNotAuthenticated() throws Exception {
        mockMvc.perform(get("/api/v1/trainees/unassigned-trainers")
                        .param("username", "ali.veli"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUnassignedActiveTrainersForTrainee_shouldReturnTrainerList_whenAuthenticated() throws Exception {
        AuthSessionManager.login("ali.veli");
        String username = "ali.veli";
        List<UnassignedActiveTrainerResponse> trainers = List.of(
                new UnassignedActiveTrainerResponse("mehmet.kaya", "Mehmet", "Kaya", 1L),
                new UnassignedActiveTrainerResponse("ayse.dogan", "Ayşe", "Doğan", 2L)
        );
        UnassignedActiveTrainerListResponse response = new UnassignedActiveTrainerListResponse(trainers);

        when(traineeService.getUnassignedActiveTrainersForTrainee(username)).thenReturn(response);

        mockMvc.perform(get("/api/v1/trainees/unassigned-trainers")
                        .param("username", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainers.length()").value(2))
                .andExpect(jsonPath("$.trainers[0].username").value("mehmet.kaya"))
                .andExpect(jsonPath("$.trainers[0].firstName").value("Mehmet"))
                .andExpect(jsonPath("$.trainers[0].specialization").value(1))
                .andExpect(jsonPath("$.trainers[1].username").value("ayse.dogan"))
                .andExpect(jsonPath("$.trainers[1].specialization").value(2));

        verify(traineeService).getUnassignedActiveTrainersForTrainee(username);
    }

    @Test
    void getUnassignedActiveTrainersForTrainee_shouldReturn404_whenTraineeNotFound_andAuthenticated() throws Exception {
        AuthSessionManager.login("not.found");
        String username = "not.found";
        when(traineeService.getUnassignedActiveTrainersForTrainee(username))
                .thenThrow(new NotFoundException("Trainee not found: " + username));

        mockMvc.perform(get("/api/v1/trainees/unassigned-trainers")
                        .param("username", username))
                .andExpect(status().isNotFound());
    }
}