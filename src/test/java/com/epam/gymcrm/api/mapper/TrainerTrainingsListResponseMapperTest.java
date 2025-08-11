package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TrainerTrainingInfo;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.TrainingEntity;
import com.epam.gymcrm.db.entity.TrainingTypeEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TrainerTrainingsListResponseMapperTest {

    @Test
    void toTrainerTrainingInfo_shouldMapAllFields_whenTrainingEntityIsValid() {
        UserEntity traineeUser = new UserEntity();
        traineeUser.setFirstName("Ali");
        traineeUser.setLastName("Veli");

        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(traineeUser);

        TrainingTypeEntity type = new TrainingTypeEntity();
        type.setTrainingTypeName("Yoga");

        TrainingEntity training = new TrainingEntity();
        training.setTrainingName("Evening Yoga");
        training.setTrainingDate(LocalDateTime.of(2024, 8, 6, 19, 0));
        training.setTrainingType(type);
        training.setTrainingDuration(60);
        training.setTrainee(trainee);

        TrainerTrainingInfo info = TrainerTrainingsListResponseMapper.toTrainerTrainingInfo(training);

        assertEquals("Evening Yoga", info.trainingName());
        assertEquals("2024-08-06T19:00", info.trainingDate());
        assertEquals("Yoga", info.trainingType());
        assertEquals(60, info.trainingDuration());
        assertEquals("Ali Veli", info.traineeName());
    }

    @Test
    void toTrainerTrainingInfo_shouldHandleNullTraineeOrUser() {
        TrainingEntity training = new TrainingEntity();
        training.setTrainingName("Morning Pilates");
        training.setTrainingDate(LocalDateTime.of(2024, 8, 6, 7, 0));
        training.setTrainingDuration(45);
        training.setTrainingType(null);
        training.setTrainee(null); // Trainee yok

        TrainerTrainingInfo info = TrainerTrainingsListResponseMapper.toTrainerTrainingInfo(training);

        assertEquals("Morning Pilates", info.trainingName());
        assertNull(info.trainingType());
        assertEquals(45, info.trainingDuration());
        assertNull(info.traineeName());

        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(null);
        training.setTrainee(trainee);

        info = TrainerTrainingsListResponseMapper.toTrainerTrainingInfo(training);
        assertNull(info.traineeName());
    }

    @Test
    void toTrainerTrainingInfo_shouldHandleNullDateAndTrainingType() {
        UserEntity traineeUser = new UserEntity();
        traineeUser.setFirstName("Ayşe");
        traineeUser.setLastName("Yılmaz");

        TraineeEntity trainee = new TraineeEntity();
        trainee.setUser(traineeUser);

        TrainingEntity training = new TrainingEntity();
        training.setTrainingName("Cardio");
        training.setTrainingDate(null);
        training.setTrainingType(null);
        training.setTrainingDuration(30);
        training.setTrainee(trainee);

        TrainerTrainingInfo info = TrainerTrainingsListResponseMapper.toTrainerTrainingInfo(training);

        assertNull(info.trainingDate());
        assertNull(info.trainingType());
        assertEquals("Ayşe Yılmaz", info.traineeName());
    }

}