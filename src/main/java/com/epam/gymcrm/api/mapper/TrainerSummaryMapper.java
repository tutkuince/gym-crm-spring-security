package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TrainerSummaryResponse;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.UserEntity;

import java.util.Collection;
import java.util.List;

public class TrainerSummaryMapper {

    public static TrainerSummaryResponse toSummary(TrainerEntity trainerEntity) {
        UserEntity user = trainerEntity.getUser();
        return new TrainerSummaryResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                trainerEntity.getTrainingType() != null ? trainerEntity.getTrainingType().getId() : null
        );
    }

    public static List<TrainerSummaryResponse> toSummaryList(Collection<TrainerEntity> trainers) {
        return trainers.stream()
                .map(TrainerSummaryMapper::toSummary)
                .toList();
    }
}
