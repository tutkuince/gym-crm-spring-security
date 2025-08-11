package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.payload.response.TrainingTypeListResponse;
import com.epam.gymcrm.db.entity.TrainingTypeEntity;
import com.epam.gymcrm.db.repository.TrainingTypeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceTest {

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @InjectMocks
    private TrainingTypeService trainingTypeService;

    @Test
    void getAllTrainingTypes_shouldReturnAllTypes() {
        TrainingTypeEntity entity1 = new TrainingTypeEntity();
        entity1.setId(1L);
        entity1.setTrainingTypeName("Cardio");

        TrainingTypeEntity entity2 = new TrainingTypeEntity();
        entity2.setId(2L);
        entity2.setTrainingTypeName("Strength");

        List<TrainingTypeEntity> entities = List.of(
                entity1, entity2
        );
        when(trainingTypeRepository.findAll()).thenReturn(entities);

        TrainingTypeListResponse response = trainingTypeService.findAllTrainingTypes();
        assertNotNull(response);
        assertEquals(2, response.trainingTypes().size());
        assertEquals("Strength", response.trainingTypes().get(1).name());
    }
}