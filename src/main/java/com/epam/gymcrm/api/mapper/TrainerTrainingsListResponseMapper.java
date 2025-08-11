package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TrainerTrainingInfo;
import com.epam.gymcrm.api.payload.response.TrainerTrainingsListResponse;
import com.epam.gymcrm.db.entity.TrainingEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TrainerTrainingsListResponseMapper {
    public static TrainerTrainingInfo toTrainerTrainingInfo(TrainingEntity entity) {
        String traineeFullName = null;
        if (entity.getTrainee() != null && entity.getTrainee().getUser() != null) {
            traineeFullName = entity.getTrainee().getUser().getFirstName() + " " +
                    entity.getTrainee().getUser().getLastName();
        }
        return new TrainerTrainingInfo(
                entity.getTrainingName(),
                Objects.nonNull(entity.getTrainingDate()) ? entity.getTrainingDate().toString() : null,
                entity.getTrainingType() != null ? entity.getTrainingType().getTrainingTypeName() : null,
                entity.getTrainingDuration(),
                traineeFullName
        );
    }

    public static TrainerTrainingsListResponse toTrainerTrainingsListResponse(List<TrainingEntity> trainings) {
        List<TrainerTrainingInfo> infoList = trainings.stream()
                .filter(Objects::nonNull)
                .map(TrainerTrainingsListResponseMapper::toTrainerTrainingInfo)
                .collect(Collectors.toList());
        return new TrainerTrainingsListResponse(infoList);
    }
}
