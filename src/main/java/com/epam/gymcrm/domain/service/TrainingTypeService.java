package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.mapper.TrainingTypeResponseMapper;
import com.epam.gymcrm.api.payload.response.TrainingTypeListResponse;
import com.epam.gymcrm.db.entity.TrainingTypeEntity;
import com.epam.gymcrm.db.repository.TrainingTypeRepository;
import com.epam.gymcrm.domain.mapper.TrainingTypeDomainMapper;
import com.epam.gymcrm.domain.model.TrainingType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;
    private static final Logger logger = LoggerFactory.getLogger(TrainingTypeService.class);

    public TrainingTypeService(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    public TrainingTypeListResponse findAllTrainingTypes() {
        logger.info("Fetching all training types.");

        List<TrainingTypeEntity> trainingTypeEntityList = trainingTypeRepository.findAll();

        List<TrainingType> domainTypes = trainingTypeEntityList.stream()
                .map(TrainingTypeDomainMapper::toDomain)
                .toList();

        logger.info("Training types fetched. Count={}", trainingTypeEntityList.size());
        return TrainingTypeResponseMapper.toListResponse(domainTypes);
    }
}
