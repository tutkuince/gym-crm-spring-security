package com.epam.gymcrm.domain.mapper;

import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.TrainingEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;
import com.epam.gymcrm.domain.model.User;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class TraineeDomainMapperTest {

    @Test
    void toTraineeEntity_shouldMapAllFields_whenValidTrainee() {
        Trainee trainee = new Trainee();
        trainee.setId(10L);
        trainee.setAddress("Ankara");
        trainee.setDateOfBirth(LocalDate.of(1990, 1, 1));
        User user = new User();
        user.setUsername("ali");
        trainee.setUser(user);

        Trainer trainer = new Trainer();
        trainer.setId(20L);

        Training training = new Training();
        training.setId(30L);

        trainee.setTrainers(Set.of(trainer));
        trainee.setTrainings(Set.of(training));

        try (
                MockedStatic<UserDomainMapper> userMapper = mockStatic(UserDomainMapper.class);
                MockedStatic<TrainerDomainMapper> trainerMapper = mockStatic(TrainerDomainMapper.class);
                MockedStatic<TrainingDomainMapper> trainingMapper = mockStatic(TrainingDomainMapper.class)
        ) {
            UserEntity userEntity = new UserEntity();
            TrainerEntity trainerEntity = new TrainerEntity();
            TrainingEntity trainingEntity = new TrainingEntity();

            userMapper.when(() -> UserDomainMapper.toUserEntity(user)).thenReturn(userEntity);
            trainerMapper.when(() -> TrainerDomainMapper.toTrainerEntity(trainer)).thenReturn(trainerEntity);
            trainingMapper.when(() -> TrainingDomainMapper.toTrainingEntity(training)).thenReturn(trainingEntity);

            TraineeEntity entity = TraineeDomainMapper.toTraineeEntity(trainee);

            assertEquals(10L, entity.getId());
            assertEquals(userEntity, entity.getUser());
            assertEquals("Ankara", entity.getAddress());
            assertEquals(LocalDate.of(1990, 1, 1), entity.getDateOfBirth());
            assertTrue(entity.getTrainers().contains(trainerEntity));
            assertTrue(entity.getTrainings().contains(trainingEntity));
        }
    }

    @Test
    void toTraineeEntity_shouldNotFail_whenTrainersOrTrainingsAreNull() {
        Trainee trainee = new Trainee();
        trainee.setId(10L);
        User user = new User();
        trainee.setUser(user);

        try (MockedStatic<UserDomainMapper> userMapper = mockStatic(UserDomainMapper.class)) {
            UserEntity userEntity = new UserEntity();
            userMapper.when(() -> UserDomainMapper.toUserEntity(user)).thenReturn(userEntity);

            TraineeEntity entity = TraineeDomainMapper.toTraineeEntity(trainee);
            assertEquals(10L, entity.getId());
            assertEquals(userEntity, entity.getUser());
        }
    }

    @Test
    void toTrainee_shouldMapAllFields_whenValidTraineeEntity() {
        TraineeEntity entity = new TraineeEntity();
        entity.setId(100L);
        entity.setAddress("Ankara");
        entity.setDateOfBirth(LocalDate.of(1988, 5, 5));
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("ali");
        entity.setUser(userEntity);

        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setId(55L);

        TrainingEntity trainingEntity = new TrainingEntity();
        trainingEntity.setId(77L);

        entity.setTrainers(Set.of(trainerEntity));
        entity.setTrainings(Set.of(trainingEntity));

        try (
                MockedStatic<UserDomainMapper> userMapper = mockStatic(UserDomainMapper.class);
                MockedStatic<TrainerDomainMapper> trainerMapper = mockStatic(TrainerDomainMapper.class);
                MockedStatic<TrainingDomainMapper> trainingMapper = mockStatic(TrainingDomainMapper.class)
        ) {
            User user = new User();
            Trainer trainer = new Trainer();
            Training training = new Training();

            userMapper.when(() -> UserDomainMapper.toUser(userEntity)).thenReturn(user);
            trainerMapper.when(() -> TrainerDomainMapper.toTrainer(trainerEntity)).thenReturn(trainer);
            trainingMapper.when(() -> TrainingDomainMapper.toTraining(trainingEntity)).thenReturn(training);

            Trainee trainee = TraineeDomainMapper.toTrainee(entity);

            assertEquals(100L, trainee.getId());
            assertEquals("Ankara", trainee.getAddress());
            assertEquals(LocalDate.of(1988, 5, 5), trainee.getDateOfBirth());
            assertEquals(user, trainee.getUser());
            assertTrue(trainee.getTrainers().contains(trainer));
            assertTrue(trainee.getTrainings().contains(training));
        }
    }

    @Test
    void toTrainee_shouldReturnNull_whenTraineeEntityIsNull() {
        assertNull(TraineeDomainMapper.toTrainee(null));
    }

    @Test
    void toTrainee_shouldThrowException_whenUserEntityIsNull() {
        TraineeEntity entity = new TraineeEntity();
        entity.setId(123L);
        entity.setUser(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                TraineeDomainMapper.toTrainee(entity));
        assertTrue(ex.getMessage().contains("Associated User entity is null"));
    }

    @Test
    void toTraineeShallow_shouldMapFields() {
        TraineeEntity entity = new TraineeEntity();
        entity.setId(44L);
        entity.setAddress("Izmir");
        entity.setDateOfBirth(LocalDate.of(2000, 3, 3));
        UserEntity userEntity = new UserEntity();
        entity.setUser(userEntity);

        try (MockedStatic<UserDomainMapper> userMapper = mockStatic(UserDomainMapper.class)) {
            User user = new User();
            userMapper.when(() -> UserDomainMapper.toUser(userEntity)).thenReturn(user);

            Trainee t = TraineeDomainMapper.toTraineeShallow(entity);

            assertEquals(44L, t.getId());
            assertEquals("Izmir", t.getAddress());
            assertEquals(LocalDate.of(2000, 3, 3), t.getDateOfBirth());
            assertEquals(user, t.getUser());
        }
    }

    @Test
    void toTraineeEntityShallow_shouldMapFields() {
        Trainee trainee = new Trainee();
        trainee.setId(55L);
        trainee.setAddress("Izmir");
        trainee.setDateOfBirth(LocalDate.of(2005, 4, 4));
        User user = new User();
        trainee.setUser(user);

        try (MockedStatic<UserDomainMapper> userMapper = mockStatic(UserDomainMapper.class)) {
            UserEntity userEntity = new UserEntity();
            userMapper.when(() -> UserDomainMapper.toUserEntity(user)).thenReturn(userEntity);

            TraineeEntity entity = TraineeDomainMapper.toTraineeEntityShallow(trainee);

            assertEquals(55L, entity.getId());
            assertEquals("Izmir", entity.getAddress());
            assertEquals(LocalDate.of(2005, 4, 4), entity.getDateOfBirth());
            assertEquals(userEntity, entity.getUser());
        }
    }
}