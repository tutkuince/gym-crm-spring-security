package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeTrainerUpdateResponse;
import com.epam.gymcrm.api.payload.response.TrainerSummaryResponse;
import com.epam.gymcrm.db.entity.TrainerEntity;

import java.util.List;

public class TraineeTrainerUpdateMapper {

    public static TraineeTrainerUpdateResponse toResponse(List<TrainerEntity> trainerEntities) {
        List<TrainerSummaryResponse> trainers = trainerEntities.stream()
                .map(TrainerSummaryMapper::toSummary)
                .toList();
        return new TraineeTrainerUpdateResponse(trainers);
    }
}
