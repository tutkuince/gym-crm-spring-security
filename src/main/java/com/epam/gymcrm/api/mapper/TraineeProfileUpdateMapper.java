package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeProfileUpdateResponse;
import com.epam.gymcrm.api.payload.response.TraineeTrainerSummaryResponse;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.UserEntity;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TraineeProfileUpdateMapper {

    public static TraineeProfileUpdateResponse toTraineeProfileUpdateResponse(TraineeEntity entity) {
        UserEntity user = entity.getUser();
        if (Objects.isNull(user)) {
            throw new IllegalStateException(
                    String.format(
                            "TraineeProfileUpdateMapper: Mapping failed for TraineeEntity (id=%d): Associated User entity is null. Data integrity violation!",
                            entity.getId()
                    )
            );
        }

        Set<TraineeTrainerSummaryResponse> trainers = new HashSet<>();

        if (Objects.nonNull(entity.getTrainers()) && !entity.getTrainers().isEmpty()) {
            trainers = entity.getTrainers().stream()
                    .map(trainerEntity -> {
                        UserEntity trainerUser = trainerEntity.getUser();
                        if (Objects.isNull(trainerUser)) {
                            throw new IllegalStateException(
                                    String.format(
                                            "TraineeProfileUpdateMapper: Mapping failed for TrainerEntity (id=%d): Associated User entity is null. Data integrity violation!",
                                            trainerEntity.getId()
                                    )
                            );
                        }
                        return new TraineeTrainerSummaryResponse(
                                trainerUser.getUsername(),
                                trainerUser.getFirstName(),
                                trainerUser.getLastName(),
                                trainerEntity.getTrainingType() != null ? trainerEntity.getTrainingType().getId() : null
                        );
                    })
                    .collect(Collectors.toSet());
        }

        return new TraineeProfileUpdateResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                entity.getDateOfBirth() != null ? entity.getDateOfBirth().toString() : null,
                entity.getAddress(),
                user.getActive(),
                trainers
        );
    }
}
