package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.payload.request.AddTrainingRequest;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.TrainingEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.db.repository.TraineeRepository;
import com.epam.gymcrm.db.repository.TrainerRepository;
import com.epam.gymcrm.db.repository.TrainingRepository;
import com.epam.gymcrm.db.repository.TrainingTypeRepository;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.exception.TrainerScheduleConflictException;
import com.epam.gymcrm.infrastructure.monitoring.metrics.TrainingMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainingTypeRepository trainingTypeRepository;
    @Mock
    private TrainingMetrics metrics;

    @InjectMocks
    private TrainingService trainingService;

    TraineeEntity traineeEntity;
    TrainerEntity trainerEntity;

    @BeforeEach
    void setup() {
        UserEntity traineeUser = new UserEntity();
        traineeUser.setUsername("trainee1");
        traineeUser.setFirstName("Test");
        traineeUser.setLastName("Trainee");

        UserEntity trainerUser = new UserEntity();
        trainerUser.setUsername("trainer1");
        trainerUser.setFirstName("Test");
        trainerUser.setLastName("Trainer");

        traineeEntity = new TraineeEntity();
        traineeEntity.setId(1L);
        traineeEntity.setUser(traineeUser);

        trainerEntity = new TrainerEntity();
        trainerEntity.setId(2L);
        trainerEntity.setUser(trainerUser);
    }

    @Test
    void addTraining_shouldSaveTraining_whenRequestValidAndNoConflict() {
        AddTrainingRequest request = new AddTrainingRequest(
                "trainee1", "trainer1", "Push Day", "2025-08-01 10:00:00", 60);

        when(traineeRepository.findByUserUsername("trainee1")).thenReturn(Optional.of(traineeEntity));
        when(trainerRepository.findByUserUsername("trainer1")).thenReturn(Optional.of(trainerEntity));
        when(trainingRepository.findByTrainerIdAndTrainingDate(eq(2L), any())).thenReturn(Optional.empty());
        doNothing().when(metrics).incrementCreated();

        assertDoesNotThrow(() -> trainingService.addTraining(request));

        verify(trainingRepository).save(any(TrainingEntity.class));
    }

    @Test
    void addTraining_shouldThrowNotFoundException_whenTraineeNotFound() {
        AddTrainingRequest request = new AddTrainingRequest(
                "notfound", "trainer1", "Push Day", "2025-08-01 10:00:00", 60);

        when(traineeRepository.findByUserUsername("notfound")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.addTraining(request));
        verify(traineeRepository).findByUserUsername("notfound");
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void addTraining_shouldThrowNotFoundException_whenTrainerNotFound() {
        AddTrainingRequest request = new AddTrainingRequest(
                "trainee1", "notfound", "Push Day", "2025-08-01 10:00:00", 60);

        when(traineeRepository.findByUserUsername("trainee1")).thenReturn(Optional.of(traineeEntity));
        when(trainerRepository.findByUserUsername("notfound")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainingService.addTraining(request));
        verify(traineeRepository).findByUserUsername("trainee1");
        verify(trainerRepository).findByUserUsername("notfound");
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void addTraining_shouldThrowBadRequestException_whenDateFormatInvalid() {
        AddTrainingRequest request = new AddTrainingRequest(
                "trainee1", "trainer1", "Push Day", "08-01-2025 10:00:00", 60);

        when(traineeRepository.findByUserUsername("trainee1")).thenReturn(Optional.of(traineeEntity));
        when(trainerRepository.findByUserUsername("trainer1")).thenReturn(Optional.of(trainerEntity));

        assertThrows(BadRequestException.class, () -> trainingService.addTraining(request));
        verify(trainingRepository, never()).save(any());
    }

    @Test
    void addTraining_shouldThrowTrainerScheduleConflictException_whenTrainerBusy() {
        AddTrainingRequest request = new AddTrainingRequest(
                "trainee1", "trainer1", "Push Day", "2025-08-01 10:00:00", 60);

        when(traineeRepository.findByUserUsername("trainee1")).thenReturn(Optional.of(traineeEntity));
        when(trainerRepository.findByUserUsername("trainer1")).thenReturn(Optional.of(trainerEntity));
        when(trainingRepository.findByTrainerIdAndTrainingDate(eq(2L), any())).thenReturn(Optional.of(new TrainingEntity()));

        assertThrows(TrainerScheduleConflictException.class, () -> trainingService.addTraining(request));
        verify(trainingRepository, never()).save(any());
    }
}