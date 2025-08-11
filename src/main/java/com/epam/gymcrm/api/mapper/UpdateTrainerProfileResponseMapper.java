package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.UpdateTraineeSummaryResponse;
import com.epam.gymcrm.api.payload.response.UpdateTrainerProfileResponse;
import com.epam.gymcrm.db.entity.TrainerEntity;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class UpdateTrainerProfileResponseMapper {

    public static UpdateTrainerProfileResponse toResponse(TrainerEntity trainerEntity) {
        var user = trainerEntity.getUser();
        if (Objects.isNull(user)) {
            throw new IllegalStateException("Trainer entity's user is null!");
        }

        List<UpdateTraineeSummaryResponse> trainees = Objects.isNull(trainerEntity.getTrainees()) ? List.of() :
                trainerEntity.getTrainees().stream()
                        .map(traineeEntity -> {
                            var traineeUser = traineeEntity.getUser();
                            if (Objects.isNull(traineeUser)) {
                                throw new IllegalStateException("Trainee entity's user is null!");
                            }
                            return new UpdateTraineeSummaryResponse(
                                    traineeUser.getUsername(),
                                    traineeUser.getFirstName(),
                                    traineeUser.getLastName(),
                                    Objects.nonNull(trainerEntity.getTrainingType()) ? trainerEntity.getTrainingType().getId() : null
                            );
                        })
                        .collect(Collectors.toList());

        return new UpdateTrainerProfileResponse(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                Objects.nonNull(trainerEntity.getTrainingType()) ? trainerEntity.getTrainingType().getId() : null,
                user.getActive(),
                trainees
        );
    }
}
