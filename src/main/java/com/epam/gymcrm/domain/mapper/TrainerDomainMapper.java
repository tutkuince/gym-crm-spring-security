package com.epam.gymcrm.domain.mapper;

import com.epam.gymcrm.api.payload.response.UnassignedActiveTrainerResponse;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.domain.model.Trainer;

import java.util.Objects;
import java.util.stream.Collectors;

public class TrainerDomainMapper {

    public static TrainerEntity toTrainerEntity(Trainer trainer) {
        TrainerEntity entity = new TrainerEntity();
        entity.setId(trainer.getId());
        entity.setUser(UserDomainMapper.toUserEntity(trainer.getUser()));
        entity.setTrainingType(
                trainer.getSpecialization() != null
                        ? TrainingTypeDomainMapper.toEntity(trainer.getSpecialization())
                        : null
        );
        return entity;
    }

    public static Trainer toTrainer(TrainerEntity entity) {
        if (Objects.isNull(entity.getUser())) {
            throw new IllegalStateException(
                    String.format(
                            "TrainerDomainMapper: Mapping failed for TrainerEntity (id=%d): Associated User entity is null. Data integrity violation!",
                            entity.getId()
                    )
            );
        }

        Trainer trainer = new Trainer();
        trainer.setId(entity.getId());
        trainer.setUser(UserDomainMapper.toUser(entity.getUser()));
        trainer.setSpecialization(
                entity.getTrainingType() != null
                        ? TrainingTypeDomainMapper.toDomain(entity.getTrainingType())
                        : null
        );

        if (Objects.nonNull(entity.getTrainings())) {
            trainer.setTrainings(
                    entity.getTrainings().stream()
                            .map(TrainingDomainMapper::toTraining)
                            .collect(Collectors.toSet())
            );
        }

        if (entity.getTrainees() != null) {
            trainer.setTrainees(
                    entity.getTrainees().stream()
                            .map(TraineeDomainMapper::toTrainee)
                            .collect(Collectors.toSet())
            );
        }

        return trainer;
    }


    public static UnassignedActiveTrainerResponse toUnassignedActiveTrainerResponse(TrainerEntity trainerEntity) {
        return new UnassignedActiveTrainerResponse(
                trainerEntity.getUser().getUsername(),
                trainerEntity.getUser().getFirstName(),
                trainerEntity.getUser().getLastName(),
                trainerEntity.getTrainingType().getId()
        );
    }

    public static Trainer toTrainerShallow(TrainerEntity entity) {
        if (Objects.isNull(entity.getUser())) {
            throw new IllegalStateException(
                    String.format(
                            "TrainerDomainMapper: Mapping failed for TrainerEntity (id=%d): Associated User entity is null. Data integrity violation!",
                            entity.getId()
                    )
            );
        }

        Trainer trainer = new Trainer();
        trainer.setId(entity.getId());
        trainer.setUser(UserDomainMapper.toUser(entity.getUser()));
        trainer.setSpecialization(
                entity.getTrainingType() != null
                        ? TrainingTypeDomainMapper.toDomain(entity.getTrainingType())
                        : null
        );

        return trainer;
    }
}
