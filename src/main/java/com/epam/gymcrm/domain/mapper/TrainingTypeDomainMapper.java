package com.epam.gymcrm.domain.mapper;

import com.epam.gymcrm.db.entity.TrainingTypeEntity;
import com.epam.gymcrm.domain.model.TrainingType;

public class TrainingTypeDomainMapper {

    public static TrainingType toDomain(TrainingTypeEntity entity) {
        TrainingType trainingType = new TrainingType();
        trainingType.setId(entity.getId());
        trainingType.setTrainingTypeName(entity.getTrainingTypeName());
        return trainingType;
    }

    public static TrainingTypeEntity toEntity(TrainingType domain) {
        TrainingTypeEntity entity = new TrainingTypeEntity();
        entity.setId(domain.getId());
        entity.setTrainingTypeName(domain.getTrainingTypeName());
        return entity;
    }
}
