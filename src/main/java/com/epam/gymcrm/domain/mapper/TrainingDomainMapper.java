package com.epam.gymcrm.domain.mapper;

import com.epam.gymcrm.db.entity.TrainingEntity;
import com.epam.gymcrm.domain.model.Training;

import java.util.Objects;

public class TrainingDomainMapper {

    public static TrainingEntity toTrainingEntity(Training training) {
        TrainingEntity entity = new TrainingEntity();
        entity.setId(training.getId());
        entity.setTrainingName(training.getTrainingName());
        entity.setTrainingDate(training.getTrainingDate());
        entity.setTrainingDuration(training.getTrainingDuration());

        if (Objects.nonNull(training.getTrainee())) {
            entity.setTrainee(TraineeDomainMapper.toTraineeEntity(training.getTrainee()));
        }

        if (Objects.nonNull(training.getTrainer())) {
            entity.setTrainer(TrainerDomainMapper.toTrainerEntity(training.getTrainer()));
        }

        if (Objects.nonNull(training.getTrainingType())) {
            entity.setTrainingType(TrainingTypeDomainMapper.toEntity(training.getTrainingType()));
        }
        return entity;
    }

    public static Training toTraining(TrainingEntity trainingEntity) {
        Training training = new Training();
        training.setId(trainingEntity.getId());
        training.setTrainingName(trainingEntity.getTrainingName());
        training.setTrainingDate(trainingEntity.getTrainingDate());
        training.setTrainingDuration(trainingEntity.getTrainingDuration());

        if (Objects.nonNull(trainingEntity.getTrainee())) {
            training.setTrainee(TraineeDomainMapper.toTrainee(trainingEntity.getTrainee()));
        }

        if (Objects.nonNull(trainingEntity.getTrainer())) {
            training.setTrainer(TrainerDomainMapper.toTrainer(trainingEntity.getTrainer()));
        }

        if (Objects.nonNull(trainingEntity.getTrainingType())) {
            training.setTrainingType(TrainingTypeDomainMapper.toDomain(trainingEntity.getTrainingType()));
        }

        return training;
    }
}
