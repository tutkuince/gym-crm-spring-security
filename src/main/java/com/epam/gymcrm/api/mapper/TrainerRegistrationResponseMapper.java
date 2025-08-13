package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TrainerRegistrationResponse;
import com.epam.gymcrm.db.entity.TrainerEntity;

public class TrainerRegistrationResponseMapper {

    public static TrainerRegistrationResponse toResponse(TrainerEntity entity, String rawPassword) {
        return new TrainerRegistrationResponse(
                entity.getUser().getUsername(),
                rawPassword
        );
    }
}
