package com.epam.gymcrm.domain.service;

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
import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.db.repository.*;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.mapper.TrainerDomainMapper;
import com.epam.gymcrm.domain.model.Trainer;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.infrastructure.monitoring.metrics.TrainerMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainingTypeRepository trainingTypeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TrainerMetrics metrics;

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private UserAccountService userAccountService;


    @InjectMocks
    private TrainerService trainerService;

    private TrainerEntity trainerEntity;

    @BeforeEach
    void setUp() {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("ali.veli");
        userEntity.setPassword("12345");
        userEntity.setFirstName("Ali");
        userEntity.setLastName("Veli");
        userEntity.setActive(true);

        TrainingTypeEntity specialization = new TrainingTypeEntity();
        specialization.setId(1L);
        specialization.setTrainingTypeName("Fitness");

        trainerEntity = new TrainerEntity();
        trainerEntity.setId(10L);
        trainerEntity.setUser(userEntity);
        trainerEntity.setTrainingType(specialization);
        trainerEntity.setTrainees(Collections.emptySet());
    }

    @Test
    void createTrainer_shouldRegisterTrainer_whenValidRequest() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Ali", "Veli", 1L);

        TrainingTypeEntity specialization = new TrainingTypeEntity();
        specialization.setId(1L);
        specialization.setTrainingTypeName("Fitness");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(specialization));

        com.epam.gymcrm.domain.model.User newUser = new com.epam.gymcrm.domain.model.User();
        newUser.setUsername("ali.veli");
        newUser.setRawPassword("12345");
        when(userAccountService.createUser("Ali", "Veli")).thenReturn(newUser);

        when(traineeRepository.existsByUserUsername("ali.veli")).thenReturn(false);

        TrainerEntity savedEntity = new TrainerEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("ali.veli");
        savedEntity.setUser(userEntity);
        when(trainerRepository.save(any(TrainerEntity.class))).thenReturn(savedEntity);

        doNothing().when(metrics).incrementRegistered();

        TrainerRegistrationResponse response = trainerService.createTrainer(request);

        assertNotNull(response);
        assertEquals("ali.veli", response.username());
        assertEquals("12345", response.password());

        ArgumentCaptor<TrainerEntity> captor = ArgumentCaptor.forClass(TrainerEntity.class);
        verify(trainerRepository).save(captor.capture());
        TrainerEntity toSave = captor.getValue();
        assertThat(toSave.getUser()).isNotNull();

        verify(trainingTypeRepository).findById(1L);
        verify(userAccountService).createUser("Ali", "Veli");
        verify(traineeRepository).existsByUserUsername("ali.veli");
        verify(metrics).incrementRegistered();
    }

    @Test
    void createTrainer_shouldThrowNotFoundException_whenSpecializationNotFound() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Ali", "Veli", 99L);

        when(trainingTypeRepository.findById(99L)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> trainerService.createTrainer(request));

        assertTrue(ex.getMessage().contains("Specialization (training type) not found"));
        verify(trainingTypeRepository).findById(99L);
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void createTrainer_shouldThrowBadRequest_whenUserIsTrainee() {
        TrainerRegistrationRequest request = new TrainerRegistrationRequest("Ali", "Veli", 1L);

        TrainingTypeEntity specialization = new TrainingTypeEntity();
        specialization.setId(1L);
        specialization.setTrainingTypeName("Fitness");
        when(trainingTypeRepository.findById(1L)).thenReturn(Optional.of(specialization));

        com.epam.gymcrm.domain.model.User newUser = new com.epam.gymcrm.domain.model.User();
        newUser.setUsername("ali.veli");
        newUser.setRawPassword("does-not-matter");
        when(userAccountService.createUser("Ali", "Veli")).thenReturn(newUser);

        when(traineeRepository.existsByUserUsername("ali.veli")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> trainerService.createTrainer(request));

        assertTrue(ex.getMessage().contains("User cannot be both trainer and trainee"));

        verify(trainingTypeRepository).findById(1L);
        verify(userAccountService).createUser("Ali", "Veli");
        verify(traineeRepository).existsByUserUsername("ali.veli");
        verify(trainerRepository, never()).save(any());
        verify(metrics, never()).incrementRegistered();
    }

    @Test
    void getTrainerProfile_shouldReturnProfile_whenTrainerExists() {
        when(trainerRepository.findByUserUsernameWithTrainees("ali.veli"))
                .thenReturn(Optional.of(trainerEntity));

        TrainerProfileResponse response = trainerService.getTrainerProfile("ali.veli");

        assertNotNull(response);
        assertEquals("Ali", response.firstName());
        assertEquals("Veli", response.lastName());
        assertEquals(1L, response.specialization());
        assertTrue(response.isActive());
        assertTrue(response.trainees().isEmpty());

        verify(trainerRepository).findByUserUsernameWithTrainees("ali.veli");
    }

    @Test
    void getTrainerProfile_shouldThrowNotFoundException_whenTrainerNotFound() {
        when(trainerRepository.findByUserUsernameWithTrainees("nonexistent"))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                trainerService.getTrainerProfile("nonexistent")
        );
        assertTrue(ex.getMessage().contains("Trainer not found with username"));

        verify(trainerRepository).findByUserUsernameWithTrainees("nonexistent");
    }

    @Test
    void updateTrainerProfile_shouldUpdateProfile_whenValidRequest() {
        // Arrange
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("ali.veli");
        userEntity.setFirstName("Ali");
        userEntity.setLastName("Veli");
        userEntity.setActive(false);

        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setId(1L);
        trainerEntity.setUser(userEntity);

        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("ali.veli");
        request.setFirstName("Mehmet");
        request.setLastName("Kaya");
        request.setSpecialization(1L);
        request.setActive(true);

        when(trainerRepository.findByUserUsernameWithTrainees("ali.veli"))
                .thenReturn(Optional.of(trainerEntity));
        when(trainerRepository.save(any())).thenReturn(trainerEntity);

        UpdateTrainerProfileResponse response = trainerService.updateTrainerProfile(request);

        assertNotNull(response);
        verify(trainerRepository).findByUserUsernameWithTrainees("ali.veli");
        verify(trainerRepository).save(any(TrainerEntity.class));
    }

    @Test
    void updateTrainerProfile_shouldThrowNotFound_whenTrainerNotFound() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("notfound");
        request.setFirstName("Mehmet");
        request.setLastName("Kaya");
        request.setSpecialization(1L);
        request.setActive(true);
        when(trainerRepository.findByUserUsernameWithTrainees("notfound"))
                .thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.updateTrainerProfile(request));
        verify(trainerRepository).findByUserUsernameWithTrainees("notfound");
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateTrainerProfile_shouldThrowIllegalStateException_whenUpdateProfileFails() {
        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("ali.veli");
        request.setFirstName("Mehmet");
        request.setLastName("Kaya");
        request.setSpecialization(1L);
        request.setActive(true);

        TrainerEntity trainerEntity = new TrainerEntity();
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername("ali.veli");
        trainerEntity.setUser(userEntity);

        when(trainerRepository.findByUserUsernameWithTrainees("ali.veli"))
                .thenReturn(Optional.of(trainerEntity));

        Trainer mockTrainer = mock(Trainer.class);

        try (MockedStatic<TrainerDomainMapper> mockedMapper = mockStatic(TrainerDomainMapper.class)) {
            mockedMapper.when(() -> TrainerDomainMapper.toTrainer(trainerEntity))
                    .thenReturn(mockTrainer);

            doThrow(new IllegalStateException("Update failed"))
                    .when(mockTrainer).updateProfile("Mehmet", "Kaya", true);

            assertThrows(IllegalStateException.class, () -> trainerService.updateTrainerProfile(request));

            verify(trainerRepository, never()).save(any());
        }

        verify(trainerRepository).findByUserUsernameWithTrainees("ali.veli");
    }


    @Test
    void updateTrainerProfile_shouldThrowIllegalState_whenUserIsNull() {
        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setId(1L);
        trainerEntity.setUser(null);

        UpdateTrainerProfileRequest request = new UpdateTrainerProfileRequest();
        request.setUsername("ali.veli");
        request.setFirstName("Mehmet");
        request.setLastName("Kaya");
        request.setSpecialization(1L);
        request.setActive(true);

        when(trainerRepository.findByUserUsernameWithTrainees("ali.veli"))
                .thenReturn(Optional.of(trainerEntity));

        assertThrows(IllegalStateException.class, () -> trainerService.updateTrainerProfile(request));
        verify(trainerRepository).findByUserUsernameWithTrainees("ali.veli");
        verify(trainerRepository, never()).save(any());
    }

    @SuppressWarnings("unchecked")
    @Test
    void getTrainerTrainings_shouldReturnResponse_whenTrainerExists() {
        // Arrange
        String username = "ali.veli";
        TrainerTrainingsFilter filter = new TrainerTrainingsFilter(username, "2024-01-01", "2024-08-01", "Ahmet");

        UserEntity user = new UserEntity();
        user.setUsername(username);
        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setUser(user);

        TrainingEntity trainingEntity = new TrainingEntity();
        trainingEntity.setId(100L);
        trainingEntity.setTrainingName("Push Day");
        trainingEntity.setTrainingDate(LocalDateTime.now());

        when(trainerRepository.findByUserUsernameWithTrainees(username)).thenReturn(Optional.of(trainerEntity));
        when(trainingRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(trainingEntity));

        // Act
        TrainerTrainingsListResponse response = trainerService.getTrainerTrainings(filter);

        // Assert
        assertNotNull(response);
        assertFalse(response.trainings().isEmpty());
        assertEquals("Push Day", response.trainings().getFirst().trainingName());

        verify(trainerRepository).findByUserUsernameWithTrainees(username);
        verify(trainingRepository).findAll(any(Specification.class));
    }

    @Test
    void getTrainerTrainings_shouldThrowNotFoundException_whenTrainerNotExists() {
        // Arrange
        String username = "not.found";
        TrainerTrainingsFilter filter = new TrainerTrainingsFilter(username, "2024-01-01", "2024-08-01", "Ahmet");

        when(trainerRepository.findByUserUsernameWithTrainees(username)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException ex = assertThrows(NotFoundException.class, () -> trainerService.getTrainerTrainings(filter));
        assertTrue(ex.getMessage().contains("Trainer not found"));

        verify(trainerRepository).findByUserUsernameWithTrainees(username);
    }

    @Test
    void updateActivateStatus_shouldUpdateStatus_whenValidRequest() {
        String username = "ali.veli";
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest(username, true);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setActive(false);

        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setUser(userEntity);

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainerEntity));
        when(trainerRepository.save(any(TrainerEntity.class))).thenReturn(trainerEntity);

        assertDoesNotThrow(() -> trainerService.updateActivateStatus(request));

        verify(trainerRepository).findByUserUsername(username);
        verify(trainerRepository).save(any(TrainerEntity.class));
    }

    @Test
    void updateActivateStatus_shouldThrowNotFoundException_whenTrainerNotFound() {
        String username = "notfound";
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest(username, true);

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> trainerService.updateActivateStatus(request));
        verify(trainerRepository).findByUserUsername(username);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateActivateStatus_shouldThrowIllegalStateException_whenAlreadyActiveOrInactive() {
        String username = "ali.veli";
        UpdateActiveStatusRequest requestActive = new UpdateActiveStatusRequest(username, true);
        UpdateActiveStatusRequest requestInactive = new UpdateActiveStatusRequest(username, false);

        UserEntity userEntityActive = new UserEntity();
        userEntityActive.setUsername(username);
        userEntityActive.setActive(true);

        TrainerEntity trainerEntityActive = new TrainerEntity();
        trainerEntityActive.setUser(userEntityActive);

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainerEntityActive));

        assertThrows(IllegalStateException.class, () -> trainerService.updateActivateStatus(requestActive));

        userEntityActive.setActive(false);

        assertThrows(IllegalStateException.class, () -> trainerService.updateActivateStatus(requestInactive));

        verify(trainerRepository, times(2)).findByUserUsername(username);
        verify(trainerRepository, never()).save(any());
    }

    @Test
    void updateActivateStatus_shouldDeactivate_whenRequestIsInactive() {
        String username = "ali.veli";
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest(username, false);

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setActive(true);

        TrainerEntity trainerEntity = new TrainerEntity();
        trainerEntity.setUser(userEntity);

        when(trainerRepository.findByUserUsername(username)).thenReturn(Optional.of(trainerEntity));
        when(trainerRepository.save(any(TrainerEntity.class))).thenReturn(trainerEntity);

        // Trainer mock
        Trainer mockTrainer = mock(Trainer.class);
        User mockUser = new User();
        mockUser.setUsername(username);
        mockUser.setActive(true);

        when(mockTrainer.getUser()).thenReturn(mockUser);

        try (MockedStatic<TrainerDomainMapper> mockedMapper = mockStatic(TrainerDomainMapper.class)) {
            mockedMapper.when(() -> TrainerDomainMapper.toTrainerShallow(trainerEntity))
                    .thenReturn(mockTrainer);

            mockedMapper.when(() -> TrainerDomainMapper.toTrainerEntity(mockTrainer))
                    .thenReturn(trainerEntity);

            assertDoesNotThrow(() -> trainerService.updateActivateStatus(request));

            verify(metrics).incrementDeactivated();
            verify(metrics, never()).incrementActivated();
            verify(trainerRepository).findByUserUsername(username);
            verify(trainerRepository).save(any(TrainerEntity.class));
        }
    }

}