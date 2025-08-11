package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeTrainingInfo;
import com.epam.gymcrm.api.payload.response.TraineeTrainingsListResponse;
import com.epam.gymcrm.db.entity.TrainingEntity;

import java.util.List;

public class TraineeTrainingsListResponseMapper {

    public static TraineeTrainingsListResponse toTraineeTrainingsListResponse(List<TrainingEntity> trainings) {
        List<TraineeTrainingInfo> infos = trainings.stream()
                .map(TraineeTrainingsListResponseMapper::toTraineeTrainingInfo)
                .toList();
        return new TraineeTrainingsListResponse(infos);

    }

    public static TraineeTrainingInfo toTraineeTrainingInfo(TrainingEntity training) {
        return new TraineeTrainingInfo(
                training.getTrainingName(),
                training.getTrainingDate() != null ? training.getTrainingDate().toString() : null,
                training.getTrainingType() != null ? training.getTrainingType().getTrainingTypeName() : null,
                training.getTrainingDuration(),
                training.getTrainer() != null && training.getTrainer().getUser() != null
                        ? training.getTrainer().getUser().getFirstName() + " " + training.getTrainer().getUser().getLastName()
                        : null
        );
    }
}
