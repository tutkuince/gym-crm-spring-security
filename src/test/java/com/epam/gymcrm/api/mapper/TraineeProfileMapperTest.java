package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeProfileResponse;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.TrainingTypeEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TraineeProfileMapperTest {

    @Test
    void toTraineeProfileResponse_shouldMapFields_whenEntitiesAreValid() {
        UserEntity user = new UserEntity();
        user.setUsername("trainee1");
        user.setFirstName("Ali");
        user.setLastName("Veli");
        user.setActive(true);

        TraineeEntity traineeEntity = getTraineeEntity(user);

        TraineeProfileResponse response = TraineeProfileMapper.toTraineeProfileResponse(traineeEntity);

        assertEquals("Ali", response.firstName());
        assertEquals("Veli", response.lastName());
        assertEquals("1990-01-01", response.dateOfBirth());
        assertEquals("Ankara", response.address());
        assertTrue(response.isActive());
        assertEquals(1, response.trainerInfoResponses().size());
        assertTrue(response.trainerInfoResponses().stream().anyMatch(t -> t.username().equals("trainer1")));
    }

    private static TraineeEntity getTraineeEntity(UserEntity user) {
        UserEntity trainerUser = new UserEntity();
        trainerUser.setUsername("trainer1");
        trainerUser.setFirstName("Trainer");
        trainerUser.setLastName("Bir");

        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setId(77L);

        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setUser(trainerUser);
        trainerEntity.setTrainingType(trainingType);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(1L);
        traineeEntity.setUser(user);
        traineeEntity.setTrainers(Set.of(trainerEntity));
        traineeEntity.setDateOfBirth(LocalDate.of(1990, 1, 1));
        traineeEntity.setAddress("Ankara");
        return traineeEntity;
    }

    @Test
    void toTraineeProfileResponse_shouldThrow_whenTraineeUserIsNull() {
        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(1L);
        traineeEntity.setUser(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                TraineeProfileMapper.toTraineeProfileResponse(traineeEntity)
        );
        assertTrue(ex.getMessage().contains("Associated User entity is null"));
    }

    @Test
    void toTraineeProfileResponse_shouldThrowNPE_whenTrainerUserIsNull() {
        UserEntity user = new UserEntity();
        user.setUsername("trainee1");
        user.setFirstName("Ali");
        user.setLastName("Veli");
        user.setActive(true);

        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setUser(null);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(1L);
        traineeEntity.setUser(user);
        traineeEntity.setTrainers(Set.of(trainerEntity));

        assertThrows(NullPointerException.class, () ->
                TraineeProfileMapper.toTraineeProfileResponse(traineeEntity)
        );
    }

}