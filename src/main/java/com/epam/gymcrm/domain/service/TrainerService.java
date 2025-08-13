package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.mapper.TrainerProfileResponseMapper;
import com.epam.gymcrm.api.mapper.TrainerRegistrationResponseMapper;
import com.epam.gymcrm.api.mapper.TrainerTrainingsListResponseMapper;
import com.epam.gymcrm.api.mapper.UpdateTrainerProfileResponseMapper;
import com.epam.gymcrm.api.payload.request.TrainerRegistrationRequest;
import com.epam.gymcrm.api.payload.request.TrainerTrainingsFilter;
import com.epam.gymcrm.api.payload.request.UpdateActiveStatusRequest;
import com.epam.gymcrm.api.payload.request.UpdateTrainerProfileRequest;
import com.epam.gymcrm.api.payload.response.TrainerProfileResponse;
import com.epam.gymcrm.api.payload.response.TrainerRegistrationResponse;
import com.epam.gymcrm.api.payload.response.TrainerTrainingsListResponse;
import com.epam.gymcrm.api.payload.response.UpdateTrainerProfileResponse;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.TrainingEntity;
import com.epam.gymcrm.db.entity.TrainingTypeEntity;
import com.epam.gymcrm.db.repository.TraineeRepository;
import com.epam.gymcrm.db.repository.TrainerRepository;
import com.epam.gymcrm.db.repository.TrainingRepository;
import com.epam.gymcrm.db.repository.TrainingTypeRepository;
import com.epam.gymcrm.db.repository.specification.TrainerTrainingSpecification;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.mapper.TrainerDomainMapper;
import com.epam.gymcrm.domain.mapper.TrainingTypeDomainMapper;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.infrastructure.monitoring.metrics.TrainerMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static com.epam.gymcrm.util.DateConstants.DEFAULT_DATE_FORMATTER;

@Service
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final TraineeRepository traineeRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;
    private final TrainerMetrics metrics;
    private final UserAccountService userAccountService;

    private static final Logger logger = LoggerFactory.getLogger(TrainerService.class);

    public TrainerService(
            TrainerRepository trainerRepository,
            TraineeRepository traineeRepository,
            TrainingRepository trainingRepository,
            TrainingTypeRepository trainingTypeRepository,
            TrainerMetrics metrics,
            UserAccountService userAccountService) {
        this.trainerRepository = trainerRepository;
        this.traineeRepository = traineeRepository;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.metrics = metrics;
        this.userAccountService = userAccountService;
    }

    @Transactional
    public TrainerRegistrationResponse createTrainer(TrainerRegistrationRequest request) {
        logger.info("Registering new trainer: {} {}", request.firstName(), request.lastName());

        TrainingTypeEntity specialization = trainingTypeRepository.findById(request.specialization())
                .orElseThrow(() -> {
                    logger.warn("Trainer registration failed: specialization not found. id={}", request.specialization());
                    return new NotFoundException("Specialization (training type) not found. id=" + request.specialization());
                });

        User user = userAccountService.createUser(request.firstName(), request.lastName());

        if (traineeRepository.existsByUserUsername(user.getUsername())) {
            logger.warn("Registration failed: User cannot be both trainer and trainee. username={}", user.getUsername());
            throw new BadRequestException("User cannot be both trainer and trainee.");
        }

        Trainer trainer = new Trainer();
        trainer.setUser(user);
        trainer.setSpecialization(TrainingTypeDomainMapper.toDomain(specialization));

        TrainerEntity trainerEntity = TrainerDomainMapper.toTrainerEntity(trainer);

        TrainerEntity saved = trainerRepository.save(trainerEntity);
        metrics.incrementRegistered();

        logger.info("Trainer registered successfully. id={}, username={}", saved.getId(), saved.getUser().getUsername());

        return TrainerRegistrationResponseMapper.toResponse(saved, user.getRawPassword());
    }

    public TrainerProfileResponse getTrainerProfile(String username) {
        logger.info("Trainer profile request received. username={}", username);

        TrainerEntity trainerEntity = trainerRepository.findByUserUsernameWithTrainees(username)
                .orElseThrow(() -> {
                    logger.warn("Trainer not found for get trainee profile. username={}", username);
                    return new NotFoundException("Trainer not found with username: " + username);
                });

        return TrainerProfileResponseMapper.toResponse(trainerEntity);
    }

    @Transactional
    public UpdateTrainerProfileResponse updateTrainerProfile(UpdateTrainerProfileRequest request) {
        logger.info("Trainer profile update request received. username={}", request.getUsername());

        TrainerEntity trainerEntity = trainerRepository.findByUserUsernameWithTrainees(request.getUsername())
                .orElseThrow(() -> {
                    logger.warn("Trainer not found for update. username={}", request.getUsername());
                    return new NotFoundException("Trainer not found with username: " + request.getUsername());
                });

        Trainer trainer = TrainerDomainMapper.toTrainer(trainerEntity);

        try {
            trainer.updateProfile(request.getFirstName(), request.getLastName(), request.getActive());
        } catch (IllegalStateException e) {
            logger.error("Failed to update trainer profile for username={}: {}", request.getUsername(), e.getMessage());
            throw e;
        }

        TrainerEntity updatedEntity = TrainerDomainMapper.toTrainerEntity(trainer);
        trainerRepository.save(updatedEntity);
        metrics.incrementUpdated();

        logger.info("Trainer profile updated successfully. username={}", request.getUsername());

        return UpdateTrainerProfileResponseMapper.toResponse(updatedEntity);
    }

    public TrainerTrainingsListResponse getTrainerTrainings(TrainerTrainingsFilter filter) {
        logger.info("Trainer trainings requested. username={}, periodFrom={}, periodTo={}, traineeName={}",
                filter.username(), filter.periodFrom(), filter.periodTo(), filter.traineeName());

        trainerRepository.findByUserUsernameWithTrainees(filter.username())
                .orElseThrow(() -> {
                    logger.warn("Trainer not found while fetching trainings: username={}", filter.username());
                    return new NotFoundException("Trainer not found: " + filter.username());
                });

        LocalDate from = null, to = null;
        if (Objects.nonNull(filter.periodFrom()) && !filter.periodFrom().isBlank()) {
            logger.debug("Filtering from date: {}", filter.periodFrom());
            from = LocalDate.parse(filter.periodFrom(), DEFAULT_DATE_FORMATTER);
        }
        if (Objects.nonNull(filter.periodTo()) && !filter.periodTo().isBlank()) {
            logger.debug("Filtering to date: {}", filter.periodTo());
            to = LocalDate.parse(filter.periodTo(), DEFAULT_DATE_FORMATTER);
        }

        Specification<TrainingEntity> specification = TrainerTrainingSpecification.trainerUsername(filter.username())
                .and(TrainerTrainingSpecification.fromDate(from))
                .and(TrainerTrainingSpecification.toDate(to))
                .and(TrainerTrainingSpecification.traineeName(filter.traineeName()));

        List<TrainingEntity> trainings = trainingRepository.findAll(specification);

        logger.info("Trainer trainings fetch completed. username={}, trainingsCount={}", filter.username(), trainings.size());

        return TrainerTrainingsListResponseMapper.toTrainerTrainingsListResponse(trainings);
    }

    @Transactional
    public void updateActivateStatus(UpdateActiveStatusRequest updateActiveStatusRequest) {
        String username = updateActiveStatusRequest.username();
        logger.info("Received request to update trainer active status. username={}", username);

        TrainerEntity trainerEntity = trainerRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Cannot update trainer active status. Trainer not found for activation. username={}", username);
                    return new NotFoundException("Trainer to activate/de-activate not found. username=" + username);
                });

        Trainer trainer = TrainerDomainMapper.toTrainerShallow(trainerEntity);

        try {
            trainer.getUser().setActive(updateActiveStatusRequest.isActive());
        } catch (IllegalStateException e) {
            logger.warn("Activation SKIPPED: Trainer already in requested state. username={}", username);
            throw e;
        }

        TrainerEntity updated = TrainerDomainMapper.toTrainerEntity(trainer);
        updated.setId(trainerEntity.getId());
        updated.setTrainees(trainerEntity.getTrainees());
        updated.setTrainings(trainerEntity.getTrainings());
        updated.setTrainingType(trainerEntity.getTrainingType());

        trainerRepository.save(updated);
        if (updateActiveStatusRequest.isActive()) {
            metrics.incrementActivated();
        } else {
            metrics.incrementDeactivated();
        }

        logger.info("Trainer active status updated successfully. username={}", username);
    }


}
