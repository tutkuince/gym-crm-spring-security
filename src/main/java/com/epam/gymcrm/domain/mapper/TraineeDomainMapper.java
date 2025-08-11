package com.epam.gymcrm.domain.mapper;

import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.TrainingEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.Training;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TraineeDomainMapper {

    public static TraineeEntity toTraineeEntity(Trainee trainee) {
        TraineeEntity entity = new TraineeEntity();
        entity.setId(trainee.getId());
        entity.setUser(UserDomainMapper.toUserEntity(trainee.getUser()));
        entity.setDateOfBirth(trainee.getDateOfBirth());
        entity.setAddress(trainee.getAddress());

        if (Objects.nonNull(trainee.getTrainings())) {
            Set<TrainingEntity> trainingEntities = trainee.getTrainings().stream()
                    .map(TrainingDomainMapper::toTrainingEntity)
                    .collect(Collectors.toSet());
            entity.setTrainings(trainingEntities);
        }

        if (Objects.nonNull(trainee.getTrainers())) {
            Set<TrainerEntity> trainerEntities = trainee.getTrainers().stream()
                    .map(TrainerDomainMapper::toTrainerEntity)
                    .collect(Collectors.toSet());

            entity.setTrainers(trainerEntities);
        }
        return entity;
    }

    public static Trainee toTrainee(TraineeEntity traineeEntity) {
        if (traineeEntity == null) return null;

        UserEntity userEntity = traineeEntity.getUser();
        if (userEntity == null) {
            throw new IllegalStateException(
                    String.format(
                            "TraineeDomainMapper: Mapping failed for TraineeEntity (id=%d): Associated User entity is null. Data integrity violation!",
                            traineeEntity.getId()
                    )
            );
        }

        Trainee trainee = new Trainee();
        trainee.setId(traineeEntity.getId());
        trainee.setAddress(traineeEntity.getAddress());
        trainee.setDateOfBirth(traineeEntity.getDateOfBirth());
        trainee.setUser(UserDomainMapper.toUser(userEntity));

        // Trainers mapping
        if (Objects.nonNull(traineeEntity.getTrainers()) && !traineeEntity.getTrainers().isEmpty()) {
            Set<Trainer> trainers = traineeEntity.getTrainers().stream()
                    .map(TrainerDomainMapper::toTrainer)
                    .collect(Collectors.toSet());
            trainee.setTrainers(trainers);
        }

        // Trainings mapping
        if (Objects.nonNull(traineeEntity.getTrainings()) && !traineeEntity.getTrainings().isEmpty()) {
            Set<Training> trainings = traineeEntity.getTrainings().stream()
                    .map(TrainingDomainMapper::toTraining)
                    .collect(Collectors.toSet());
            trainee.setTrainings(trainings);
        }

        return trainee;
    }

    public static Trainee toTraineeShallow(TraineeEntity entity) {
        return new Trainee(
                entity.getId(),
                UserDomainMapper.toUser(entity.getUser()),
                entity.getDateOfBirth(),
                entity.getAddress()
        );
    }
    public static TraineeEntity toTraineeEntityShallow(Trainee trainee) {
        TraineeEntity entity = new TraineeEntity();
        entity.setId(trainee.getId());
        entity.setUser(UserDomainMapper.toUserEntity(trainee.getUser()));
        entity.setDateOfBirth(trainee.getDateOfBirth());
        entity.setAddress(trainee.getAddress());
        return entity;
    }

}
