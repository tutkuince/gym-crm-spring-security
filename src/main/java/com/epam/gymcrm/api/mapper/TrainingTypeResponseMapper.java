package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TrainingTypeListResponse;
import com.epam.gymcrm.api.payload.response.TrainingTypeResponse;
import com.epam.gymcrm.domain.model.TrainingType;

import java.util.List;

public class TrainingTypeResponseMapper {

    public static TrainingTypeResponse toResponse(TrainingType entity) {
        return new TrainingTypeResponse(entity.getId(), entity.getTrainingTypeName());
    }

    public static TrainingTypeListResponse toListResponse(List<TrainingType> entities) {
        List<TrainingTypeResponse> types = entities.stream()
                .map(TrainingTypeResponseMapper::toResponse)
                .toList();
        return new TrainingTypeListResponse(types);
    }
}
