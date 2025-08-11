package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeProfileResponse;
import com.epam.gymcrm.api.payload.response.TrainerInfoResponse;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.UserEntity;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TraineeProfileMapper {

    public static TraineeProfileResponse toTraineeProfileResponse(TraineeEntity entity) {
        UserEntity entityUser = entity.getUser();
        if (Objects.isNull(entityUser)) {
            throw new IllegalStateException(
                    String.format(
                            "TraineeProfileMapper: Mapping failed for TraineeEntity (id=%d): Associated User entity is null. Data integrity violation!",
                            entity.getId()
                    )
            );
        }

        Set<TrainerInfoResponse> trainerInfoResponses = entity.getTrainers().stream()
                .map(trainerEntity -> new TrainerInfoResponse(
                        trainerEntity.getUser().getUsername(),
                        trainerEntity.getUser().getFirstName(),
                        trainerEntity.getUser().getLastName(),
                        trainerEntity.getTrainingType().getId()
                )).collect(Collectors.toSet());

        return new TraineeProfileResponse(
                entity.getUser().getFirstName(),
                entity.getUser().getLastName(),
                Objects.nonNull(entity.getDateOfBirth()) ? entity.getDateOfBirth().toString() : null,
                entity.getAddress(),
                entity.getUser().getActive(),
                trainerInfoResponses
        );
    }
}
