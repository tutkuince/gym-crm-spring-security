package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeProfileUpdateResponse;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.TrainingTypeEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TraineeProfileUpdateMapperTest {

    @Test
    void shouldThrowException_whenUserIsNull() {
        // Arrange
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(1L);
        trainee.setUser(null);

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                TraineeProfileUpdateMapper.toTraineeProfileUpdateResponse(trainee)
        );
        assertTrue(ex.getMessage().contains("Associated User entity is null"));
    }

    @Test
    void shouldThrowException_whenTrainerUserIsNull() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setUsername("ali.veli");
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(1L);
        trainee.setUser(user);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(10L);
        trainer.setUser(null);

        trainee.setTrainers(Set.of(trainer));

        // Act & Assert
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                TraineeProfileUpdateMapper.toTraineeProfileUpdateResponse(trainee)
        );
        assertTrue(ex.getMessage().contains("Associated User entity is null"));
    }

    @Test
    void shouldReturnResponse_whenAllFieldsValid() {
        // Arrange
        UserEntity user = new UserEntity();
        user.setUsername("ali.veli");
        user.setFirstName("Ali");
        user.setLastName("Veli");
        user.setActive(Boolean.TRUE);

        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(1L);
        trainee.setUser(user);

        TrainerEntity trainer = new TrainerEntity();
        UserEntity trainerUser = new UserEntity();
        trainerUser.setUsername("trainer1");
        trainerUser.setFirstName("Trainer");
        trainerUser.setLastName("One");
        trainer.setId(10L);
        trainer.setUser(trainerUser);
        trainer.setTrainingType(new TrainingTypeEntity());
        trainer.getTrainingType().setId(22L);

        trainee.setTrainers(Set.of(trainer));

        // Act
        TraineeProfileUpdateResponse response = TraineeProfileUpdateMapper.toTraineeProfileUpdateResponse(trainee);

        // Assert
        assertNotNull(response);
        assertEquals("ali.veli", response.username());
        assertEquals(1, response.trainers().size());
        assertTrue(response.trainers().stream().anyMatch(t -> t.username().equals("trainer1")));
    }
}