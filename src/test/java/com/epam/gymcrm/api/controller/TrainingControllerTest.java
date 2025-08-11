package com.epam.gymcrm.api.controller;


import com.epam.gymcrm.api.payload.request.AddTrainingRequest;
import com.epam.gymcrm.domain.service.TrainingService;
import com.epam.gymcrm.domain.exception.GlobalExceptionHandler;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.exception.TrainerScheduleConflictException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrainingService trainingService;

    @InjectMocks
    private TrainingController trainingController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(trainingController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void addTraining_shouldReturn200_whenRequestValid() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "trainee1",
                "trainer1",
                "Push Day",
                "2025-07-31 09:00:00",
                60
        );

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void addTraining_shouldReturn404_whenTraineeNotFound() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "notfound",
                "trainer1",
                "Push Day",
                "2025-07-31 09:00:00",
                60
        );

        doThrow(new NotFoundException("Trainee not found")).when(trainingService).addTraining(any(AddTrainingRequest.class));

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void addTraining_shouldReturn409_whenTrainerBusy() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "trainee1",
                "trainer1",
                "Push Day",
                "2025-07-31 09:00:00",
                60
        );

        doThrow(new TrainerScheduleConflictException("Trainer busy")).when(trainingService).addTraining(any(AddTrainingRequest.class));

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void addTraining_shouldReturn400_whenValidationFails() throws Exception {
        AddTrainingRequest request = new AddTrainingRequest(
                "trainee1",
                "trainer1",
                "",
                "2025-07-31 09:00:00",
                60
        );

        mockMvc.perform(post("/api/v1/trainings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

}