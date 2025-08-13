package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.mapper.*;
import com.epam.gymcrm.api.payload.request.*;
import com.epam.gymcrm.api.payload.response.*;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.TrainerEntity;
import com.epam.gymcrm.db.entity.TrainingEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.db.repository.TraineeRepository;
import com.epam.gymcrm.db.repository.TrainerRepository;
import com.epam.gymcrm.db.repository.TrainingRepository;
import com.epam.gymcrm.db.repository.specification.TrainingSpecification;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.mapper.TraineeDomainMapper;
import com.epam.gymcrm.domain.mapper.TrainerDomainMapper;
import com.epam.gymcrm.domain.model.Trainee;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.infrastructure.monitoring.metrics.TraineeMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import static com.epam.gymcrm.util.DateConstants.DEFAULT_DATE_FORMATTER;

@Service
public class TraineeService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TraineeMetrics traineeMetrics;
    private final UserAccountService userAccountService;

    private static final Logger logger = LoggerFactory.getLogger(TraineeService.class);

    public TraineeService(
            TraineeRepository traineeRepository,
            TrainerRepository trainerRepository,
            TrainingRepository trainingRepository,
            TraineeMetrics traineeMetrics, UserAccountService userAccountService) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.traineeMetrics = traineeMetrics;
        this.userAccountService = userAccountService;
    }

    @Transactional
    public TraineeRegistrationResponse createTrainee(TraineeRegistrationRequest traineeRegistrationRequest) {
        logger.info("Creating new trainee: {} {}", traineeRegistrationRequest.firstName(), traineeRegistrationRequest.lastName());

        User user = userAccountService.createUser(traineeRegistrationRequest.firstName(), traineeRegistrationRequest.lastName());

        // Check if user is already registered as a trainer
        if (trainerRepository.existsByUserUsername(user.getUsername())) {
            logger.warn("Registration failed: User cannot be both trainee and trainer. username={}", user.getUsername());
            throw new BadRequestException("User cannot be both trainee and trainer.");
        }

        Trainee trainee = new Trainee();
        String dateOfBirth = traineeRegistrationRequest.dateOfBirth();
        String address = traineeRegistrationRequest.address();
        trainee.setUser(user);

        if (Objects.nonNull(dateOfBirth) && !dateOfBirth.isBlank()) {
            trainee.setDateOfBirth(LocalDate.parse(dateOfBirth, DEFAULT_DATE_FORMATTER));
        }

        if (Objects.nonNull(address) && !address.isBlank()) {
            trainee.setAddress(address);
        }

        TraineeEntity traineeEntity = TraineeDomainMapper.toTraineeEntity(trainee);

        TraineeEntity savedTraineeEntity = traineeRepository.save(traineeEntity);
        traineeMetrics.incrementRegistered();

        logger.info("Trainee created: id={}, username={}", savedTraineeEntity.getId(), savedTraineeEntity.getUser().getUsername());
        return TraineeResponseMapper.toTraineeRegisterResponse(savedTraineeEntity, user.getRawPassword());
    }

    public TraineeProfileResponse findByUsername(String username) {
        logger.info("Request to find trainee by username received. Username: {}", username);
        TraineeEntity traineeEntity = traineeRepository.findByUserUsernameWithTrainers(username)
                .orElseThrow(() -> {
                    logger.warn("Find trainee by username failed: No trainee found with username: {}", username);
                    return new NotFoundException("Find trainee by username failed: No trainee found with username: " + username);
                });
        logger.info("Trainee found successfully. id={}, username={}", traineeEntity.getId(), traineeEntity.getUser().getUsername());
        return TraineeProfileMapper.toTraineeProfileResponse(traineeEntity);
    }

    @Transactional
    public TraineeProfileUpdateResponse update(TraineeUpdateRequest request) {
        String username = request.username();
        logger.info("Trainee update request received. username={}", username);

        TraineeEntity traineeEntity = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Update failed: Trainee not found. username={}", username);
                    return new NotFoundException("Trainee not found with username: " + username);
                });

        UserEntity userEntity = traineeEntity.getUser();
        if (userEntity == null) {
            throw new IllegalStateException(
                    String.format("User entity is null while updating Trainee (username=%s). Data integrity violation!", username)
            );
        }

        // Update user fields
        userEntity.setFirstName(request.firstName());
        userEntity.setLastName(request.lastName());
        userEntity.setActive(request.isActive());

        // Optional fields
        if (Objects.nonNull(request.dateOfBirth()) && !request.dateOfBirth().isBlank()) {
            try {
                traineeEntity.setDateOfBirth(LocalDate.parse(request.dateOfBirth(), DEFAULT_DATE_FORMATTER));
            } catch (Exception e) {
                logger.warn("Invalid dateOfBirth during updateTraineeProfile: '{}'", request.dateOfBirth());
                throw new BadRequestException(
                        String.format("Invalid dateOfBirth format: %s. Expected format: yyyy-MM-dd", request.dateOfBirth())
                );
            }
        }

        if (Objects.nonNull(request.address()) && !request.address().isBlank()) {
            traineeEntity.setAddress(request.address());
        }

        // Save
        TraineeEntity saved = traineeRepository.save(traineeEntity);
        traineeMetrics.incrementUpdated();

        logger.info("Trainee profile updated successfully. id={}, username={}", saved.getId(), saved.getUser().getUsername());

        return TraineeProfileUpdateMapper.toTraineeProfileUpdateResponse(saved);
    }


    @Transactional
    public void deleteTraineeByUsername(String username) {
        logger.info("Delete request received for trainee. username={}", username);
        TraineeEntity traineeEntity = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Trainee not found for deletion. username={}", username);
                    return new NotFoundException("Trainee not found with username: " + username);
                });
        traineeRepository.delete(traineeEntity);
        logger.info("Trainee deleted successfully. username={}", username);
    }

    @Transactional
    public TraineeTrainerUpdateResponse updateTraineeTrainers(TraineeTrainerUpdateRequest request) {
        String traineeUsername = request.traineeUsername();
        logger.info("Updating trainers for trainee. username={}, newTrainers={}", traineeUsername, request.trainers());

        TraineeEntity traineeEntity = traineeRepository.findByUserUsernameWithTrainers(traineeUsername)
                .orElseThrow(() -> {
                    logger.warn("Trainee to update trainers not found: username={}", traineeUsername);
                    return new NotFoundException("Trainee not found with username: " + traineeUsername);
                });

        List<String> trainerUsernames = request.trainers().stream()
                .map(TrainerUsernameRequest::trainerUsername)
                .toList();

        List<TrainerEntity> trainerEntities = trainerUsernames.isEmpty()
                ? List.of()
                : trainerRepository.findAllByUserUsernameIn(trainerUsernames);

        traineeEntity.setTrainers(new HashSet<>(trainerEntities));

        // Save et
        TraineeEntity saved = traineeRepository.save(traineeEntity);
        traineeMetrics.incrementTrainerUpdated();

        logger.info("Updated trainers for trainee: id={}", saved.getId());

        return TraineeTrainerUpdateMapper.toResponse(trainerEntities);
    }

    public TraineeTrainingsListResponse getTraineeTrainings(TraineeTrainingsFilter filter) {
        logger.info("Trainee trainings requested. username={}, periodFrom={}, periodTo={}, trainerName={}, trainingType={}",
                filter.username(), filter.periodFrom(), filter.periodTo(), filter.trainerName(), filter.trainingType());

        traineeRepository.findByUserUsernameWithTrainers(filter.username())
                .orElseThrow(() -> {
                    logger.warn("Trainee not found while fetching trainings: username={}", filter.username());
                    return new NotFoundException("Trainee not found: " + filter.username());
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

        Specification<TrainingEntity> specification = TrainingSpecification.traineeUsername(filter.username())
                .and(TrainingSpecification.fromDate(from))
                .and(TrainingSpecification.toDate(to))
                .and(TrainingSpecification.trainerName(filter.trainerName()))
                .and(TrainingSpecification.trainingType(filter.trainingType()));

        List<TrainingEntity> trainings = trainingRepository.findAll(specification);

        logger.info("Trainee trainings fetch completed. username={}, trainingsCount={}", filter.username(), trainings.size());

        return TraineeTrainingsListResponseMapper.toTraineeTrainingsListResponse(trainings);
    }

    @Transactional
    public void updateActivateStatus(UpdateActiveStatusRequest updateActiveStatusRequest) {
        String username = updateActiveStatusRequest.username();
        logger.info("Received request to activate trainee. username={}", username);

        TraineeEntity traineeEntity = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Cannot activate trainee. Trainee not found for activation. username={}", username);
                    return new NotFoundException("Trainee to activate not found. username=" + username);
                });

        Trainee trainee = TraineeDomainMapper.toTraineeShallow(traineeEntity);

        try {
            trainee.getUser().setActive(updateActiveStatusRequest.isActive());
        } catch (IllegalStateException e) {
            logger.warn("Activation SKIPPED: Trainee already active. username={}", username);
            throw e;
        }

        TraineeEntity updated = TraineeDomainMapper.toTraineeEntityShallow(trainee);
        updated.setId(traineeEntity.getId());
        updated.setTrainers(traineeEntity.getTrainers());
        updated.setTrainings(traineeEntity.getTrainings());

        traineeRepository.save(updated);
        if (updateActiveStatusRequest.isActive()) {
            traineeMetrics.incrementActivated();
        } else {
            traineeMetrics.incrementDeactivated();
        }

        logger.info("Trainee activated successfully. id={}, username={}", updated.getId(), updated.getUser().getUsername());
    }


    public UnassignedActiveTrainerListResponse getUnassignedActiveTrainersForTrainee(String username) {
        logger.info("Fetching unassigned active trainers for trainee. username={}", username);

        TraineeEntity traineeEntity = traineeRepository.findByUserUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Unassigned active trainers fetch failed: Trainee not found. username={}", username);
                    return new NotFoundException("Trainee not found: " + username);
                });

        Long traineeEntityId = traineeEntity.getId();
        List<TrainerEntity> unassignedTrainers = trainerRepository.findUnassignedTrainersForTrainee(traineeEntityId);
        logger.info("Unassigned trainers count for trainee: {}", unassignedTrainers.size());

        List<UnassignedActiveTrainerResponse> responses = unassignedTrainers.stream()
                .map(TrainerDomainMapper::toUnassignedActiveTrainerResponse)
                .toList();

        logger.info("Unassigned active trainers fetched for trainee: id={}, unassignedCount={}", traineeEntityId, responses.size());

        return UnassignedActiveTrainerListResponseMapper.toResponse(responses);
    }
}
