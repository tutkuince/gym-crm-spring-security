package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeSummaryResponse;
import com.epam.gymcrm.api.payload.response.TrainerProfileResponse;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

public class TrainerProfileResponseMapper {
    public static TrainerProfileResponse toResponse(TrainerEntity trainerEntity) {
        UserEntity user = trainerEntity.getUser();
        if (user == null) {
            throw new IllegalStateException(
                    String.format(
                            "TrainerProfileResponseMapper: Mapping failed for TrainerEntity (id=%d): Associated User entity is null. Data integrity violation!",
                            trainerEntity.getId()
                    )
            );
        }

        Long specializationId = null;
        if (trainerEntity.getTrainingType() != null) {
            specializationId = trainerEntity.getTrainingType().getId();
        }

        List<TraineeSummaryResponse> trainees;
        if (trainerEntity.getTrainees() != null && !trainerEntity.getTrainees().isEmpty()) {
            trainees = trainerEntity.getTrainees().stream()
                    .map(trainee -> {
                        UserEntity traineeUser = trainee.getUser();
                        if (traineeUser == null) {
                            throw new IllegalStateException(
                                    String.format(
                                            "TrainerProfileResponseMapper: Mapping failed for TraineeEntity (id=%d): Associated User entity is null. Data integrity violation!",
                                            trainee.getId()
                                    )
                            );
                        }
                        return new TraineeSummaryResponse(
                                traineeUser.getUsername(),
                                traineeUser.getFirstName(),
                                traineeUser.getLastName()
                        );
                    })
                    .collect(Collectors.toList());
        } else {
            trainees = List.of();
        }

        return new TrainerProfileResponse(
                user.getFirstName(),
                user.getLastName(),
                specializationId,
                user.getActive(),
                trainees
        );
    }
}
