package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.UnassignedActiveTrainerListResponse;
import com.epam.gymcrm.api.payload.response.UnassignedActiveTrainerResponse;

import java.util.List;

public class UnassignedActiveTrainerListResponseMapper {
    public static UnassignedActiveTrainerListResponse toResponse(List<UnassignedActiveTrainerResponse> trainers) {
        return new UnassignedActiveTrainerListResponse(trainers);
    }
}
