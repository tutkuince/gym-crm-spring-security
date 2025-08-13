package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeRegistrationResponse;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.UserEntity;

import java.util.Objects;

public class TraineeResponseMapper {

    public static TraineeRegistrationResponse toTraineeRegisterResponse(TraineeEntity traineeEntity, String rawPassword) {
        UserEntity user = traineeEntity.getUser();
        if (Objects.isNull(user)) {
            throw new IllegalStateException(
                    String.format(
                            "TraineeResponseMapper: Mapping failed for TraineeEntity (id=%d): Associated User entity is null. Data integrity violation!",
                            traineeEntity.getId()
                    )
            );
        }
        return new TraineeRegistrationResponse(user.getUsername(), rawPassword);
    }
}
