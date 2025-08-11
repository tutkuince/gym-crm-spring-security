package com.epam.gymcrm.api.controller;

import com.epam.gymcrm.api.payload.response.TrainingTypeListResponse;
import com.epam.gymcrm.api.payload.response.TrainingTypeResponse;
import com.epam.gymcrm.domain.service.TrainingTypeService;
import com.epam.gymcrm.domain.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TrainingTypeControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TrainingTypeService trainingTypeService;

    @InjectMocks
    private TrainingTypeController trainingTypeController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(trainingTypeController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void getAllTrainingTypes_shouldReturnTrainingTypes() throws Exception {
        List<TrainingTypeResponse> types = List.of(
                new TrainingTypeResponse(1L, "Cardio"),
                new TrainingTypeResponse(2L, "Strength")
        );
        TrainingTypeListResponse response = new TrainingTypeListResponse(types);

        when(trainingTypeService.findAllTrainingTypes()).thenReturn(response);

        mockMvc.perform(get("/api/v1/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainingTypes.length()").value(2))
                .andExpect(jsonPath("$.trainingTypes[0].name").value("Cardio"))
                .andExpect(jsonPath("$.trainingTypes[1].id").value(2));

        verify(trainingTypeService).findAllTrainingTypes();
    }
}