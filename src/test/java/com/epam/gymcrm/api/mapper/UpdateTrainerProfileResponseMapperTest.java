package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.UpdateTrainerProfileResponse;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.TrainingTypeEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UpdateTrainerProfileResponseMapperTest {

    @Test
    void toResponse_shouldMapAllFields_whenEntitiesAreValid() {
        UserEntity user = new UserEntity();
        user.setUsername("trainer1");
        user.setFirstName("Trainer");
        user.setLastName("Bir");
        user.setActive(true);

        TrainerEntity trainer = getTrainerEntity(user);

        UpdateTrainerProfileResponse res = UpdateTrainerProfileResponseMapper.toResponse(trainer);

        assertEquals("trainer1", res.username());
        assertEquals("Trainer", res.firstName());
        assertEquals("Bir", res.lastName());
        assertEquals(123L, res.specialization());
        assertTrue(res.isActive());
        assertEquals(1, res.trainees().size());
        assertEquals("trainee1", res.trainees().getFirst().trainerUsername());
    }

    @Test
    void toResponse_shouldThrow_whenTrainerUserIsNull() {
        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(10L);
        trainer.setUser(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                UpdateTrainerProfileResponseMapper.toResponse(trainer)
        );
        assertTrue(ex.getMessage().contains("Trainer entity's user is null"));
    }
    @Test
    void toResponse_shouldThrow_whenTraineeUserIsNull() {
        UserEntity user = new UserEntity();
        user.setUsername("trainer1");
        user.setFirstName("Trainer");
        user.setLastName("Bir");
        user.setActive(true);

        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(201L);
        trainee.setUser(null);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(10L);
        trainer.setUser(user);
        trainer.setTrainingType(null);
        trainer.setTrainees(Set.of(trainee));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                UpdateTrainerProfileResponseMapper.toResponse(trainer)
        );
        assertTrue(ex.getMessage().contains("Trainee entity's user is null"));
    }



    private static TrainerEntity getTrainerEntity(UserEntity user) {
        TrainingTypeEntity trainingType = new TrainingTypeEntity();
        trainingType.setId(123L);

        UserEntity traineeUser = new UserEntity();
        traineeUser.setUsername("trainee1");
        traineeUser.setFirstName("Ali");
        traineeUser.setLastName("Veli");

        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(201L);
        trainee.setUser(traineeUser);

        TrainerEntity trainer = new TrainerEntity();
        trainer.setId(10L);
        trainer.setUser(user);
        trainer.setTrainingType(trainingType);
        trainer.setTrainees(Set.of(trainee));
        return trainer;
    }

}