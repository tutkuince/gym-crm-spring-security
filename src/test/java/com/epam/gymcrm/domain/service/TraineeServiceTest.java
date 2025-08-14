package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.payload.request.*;
import com.epam.gymcrm.api.payload.response.*;
import com.epam.gymcrm.db.entity.*;
import com.epam.gymcrm.db.repository.TraineeRepository;
import com.epam.gymcrm.db.repository.TrainerRepository;
import com.epam.gymcrm.db.repository.TrainingRepository;
import com.epam.gymcrm.db.repository.UserRepository;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.infrastructure.monitoring.metrics.TraineeMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceTest {

    @Mock
    private TraineeRepository traineeRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private TrainingRepository trainingRepository;
    @Mock
    private TraineeMetrics metrics;
    @Mock
    private UserAccountService userAccountService;

    @InjectMocks
    private TraineeService traineeService;

    private TraineeEntity traineeEntity;
    private TraineeEntity savedTraineeEntity;

    @BeforeEach
    void setUp() {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("ali.veli");
        userEntity.setPassword("12345");
        userEntity.setFirstName("Ali");
        userEntity.setLastName("Veli");
        userEntity.setActive(true);

        traineeEntity = new TraineeEntity();
        traineeEntity.setId(1L);
        traineeEntity.setUser(userEntity);
        traineeEntity.setDateOfBirth(LocalDate.of(1999, 1, 1));
        traineeEntity.setAddress("İstanbul");

        savedTraineeEntity = new TraineeEntity();
        savedTraineeEntity.setId(1L);
        savedTraineeEntity.setUser(userEntity);
        savedTraineeEntity.setDateOfBirth(LocalDate.of(1999, 1, 1));
        savedTraineeEntity.setAddress("İstanbul");
    }


    @Test
    void createTrainee_shouldReturnRegisterResponse_whenRequestIsValid() {
        // given
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest("Ali", "Veli", "1999-01-01", "İstanbul");

        User createdUser = new User();
        createdUser.setUsername("ali.veli");
        createdUser.setRawPassword("12345");
        when(userAccountService.createUser("Ali", "Veli")).thenReturn(createdUser);

        when(trainerRepository.existsByUserUsername("ali.veli")).thenReturn(false);

        when(traineeRepository.save(any(TraineeEntity.class))).thenReturn(savedTraineeEntity);

        doNothing().when(metrics).incrementRegistered();

        // when
        TraineeRegistrationResponse result = traineeService.createTrainee(request);

        // then
        assertNotNull(result);
        assertEquals("ali.veli", result.username());
        assertEquals("12345", result.password());

        ArgumentCaptor<TraineeEntity> captor = ArgumentCaptor.forClass(TraineeEntity.class);
        verify(traineeRepository).save(captor.capture());
        TraineeEntity toSave = captor.getValue();
        assertNotNull(toSave.getUser());

        verify(metrics).incrementRegistered();
    }

    @Test
    void findByUsername_shouldReturnTraineeProfileResponse_whenTraineeExists() {
        String username = "ali.veli";
        when(traineeRepository.findByUserUsernameWithTrainers(username))
                .thenReturn(Optional.of(traineeEntity));

        TraineeProfileResponse response = traineeService.findByUsername(username);

        assertNotNull(response);
        assertEquals("Ali", response.firstName());
        assertEquals("Veli", response.lastName());

        verify(traineeRepository).findByUserUsernameWithTrainers(username);
    }

    @Test
    void findByUsername_shouldThrowNotFoundException_whenTraineeDoesNotExist() {
        String username = "nonexistent";
        when(traineeRepository.findByUserUsernameWithTrainers(username))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> traineeService.findByUsername(username));

        assertTrue(ex.getMessage().contains("No trainee found with username"));
        verify(traineeRepository).findByUserUsernameWithTrainers(username);
    }

    @Test
    void update_shouldUpdateTrainee_whenRequestIsValid() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                "ali.veli", "Ali", "Veli", "1995-01-01", "Istanbul", true
        );

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("ali.veli");
        userEntity.setPassword("oldpass");
        userEntity.setFirstName("Ali");
        userEntity.setLastName("Old");
        userEntity.setActive(false);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(10L);
        traineeEntity.setUser(userEntity);

        doNothing().when(metrics).incrementUpdated();
        when(traineeRepository.findByUserUsername("ali.veli"))
                .thenReturn(Optional.of(traineeEntity));
        when(traineeRepository.save(any(TraineeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TraineeProfileUpdateResponse response = traineeService.update(request);

        assertNotNull(response);
        assertEquals("ali.veli", response.username());
        assertEquals("Ali", response.firstName());
        assertEquals("Veli", response.lastName());
        assertEquals("1995-01-01", response.dateOfBirth());
        assertEquals("Istanbul", response.address());
        assertTrue(response.isActive());

        verify(traineeRepository).findByUserUsername("ali.veli");
        verify(traineeRepository).save(any(TraineeEntity.class));
    }

    @Test
    void update_shouldThrowNotFoundException_whenTraineeDoesNotExist() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                "notfound", "Test", "User", null, null, true
        );

        when(traineeRepository.findByUserUsername("notfound"))
                .thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> traineeService.update(request));

        assertTrue(ex.getMessage().contains("Trainee not found"));
        verify(traineeRepository).findByUserUsername("notfound");
    }

    @Test
    void update_shouldThrowIllegalStateException_whenUserEntityIsNull() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                "ali.veli", "Ali", "Veli", null, null, true
        );
        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(1L);
        traineeEntity.setUser(null);

        when(traineeRepository.findByUserUsername("ali.veli"))
                .thenReturn(Optional.of(traineeEntity));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> traineeService.update(request));

        assertTrue(ex.getMessage().contains("User entity is null"));
    }

    @Test
    void update_shouldThrowBadRequestException_whenDateOfBirthIsInvalid() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                "ali.veli", "Ali", "Veli", "not-a-date", null, true
        );
        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("ali.veli");
        userEntity.setPassword("oldpass");

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(10L);
        traineeEntity.setUser(userEntity);

        when(traineeRepository.findByUserUsername("ali.veli"))
                .thenReturn(Optional.of(traineeEntity));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> traineeService.update(request));

        assertTrue(ex.getMessage().contains("Invalid dateOfBirth format"));
    }

    @Test
    void update_shouldHandleNullOptionalFields() {
        TraineeUpdateRequest request = new TraineeUpdateRequest(
                "ali.veli", "Ali", "Veli", null, null, true
        );

        UserEntity userEntity = new UserEntity();
        userEntity.setId(1L);
        userEntity.setUsername("ali.veli");
        userEntity.setPassword("oldpass");
        userEntity.setFirstName("Ali");
        userEntity.setLastName("Old");
        userEntity.setActive(false);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(10L);
        traineeEntity.setUser(userEntity);

        when(traineeRepository.findByUserUsername("ali.veli"))
                .thenReturn(Optional.of(traineeEntity));
        doNothing().when(metrics).incrementUpdated();


        when(traineeRepository.save(any(TraineeEntity.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        TraineeProfileUpdateResponse response = traineeService.update(request);

        assertNotNull(response);
        assertEquals("ali.veli", response.username());
        assertEquals("Ali", response.firstName());
        assertEquals("Veli", response.lastName());
        assertNull(response.dateOfBirth());
        assertNull(response.address());
        assertTrue(response.isActive());

        verify(traineeRepository).findByUserUsername("ali.veli");
        verify(traineeRepository).save(any(TraineeEntity.class));
    }

    @Test
    void delete_shouldDeleteTrainee_whenExists() {
        String username = "ali.veli";
        TraineeEntity trainee = new TraineeEntity();
        UserEntity user = new UserEntity();
        user.setUsername(username);
        trainee.setUser(user);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(trainee));

        traineeService.deleteTraineeByUsername(username);

        verify(traineeRepository).delete(trainee);
    }

    @Test
    void delete_shouldThrowNotFoundException_whenTraineeDoesNotExist() {
        String username = "unknown";
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> traineeService.deleteTraineeByUsername(username));
        assertTrue(ex.getMessage().contains("not found"));
    }

    @Test
    void updateTraineeTrainers_shouldUpdateTrainersAndReturnResponse_whenValidRequest() {
        String traineeUsername = "ali.veli";
        TraineeTrainerUpdateRequest request = new TraineeTrainerUpdateRequest(
                traineeUsername,
                List.of(
                        new TrainerUsernameRequest("trainer1"),
                        new TrainerUsernameRequest("trainer2")
                )
        );

        UserEntity traineeUser = new UserEntity();
        traineeUser.setUsername(traineeUsername);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(10L);
        traineeEntity.setUser(traineeUser);
        traineeEntity.setTrainers(new HashSet<>());

        // trainer1
        List<TrainerEntity> trainers = getTrainerEntities();

        when(traineeRepository.findByUserUsernameWithTrainers(traineeUsername)).thenReturn(Optional.of(traineeEntity));
        when(trainerRepository.findAllByUserUsernameIn(any())).thenReturn(trainers);
        when(traineeRepository.save(any(TraineeEntity.class))).thenReturn(traineeEntity);

        TraineeTrainerUpdateResponse response = traineeService.updateTraineeTrainers(request);

        assertNotNull(response);
        assertEquals(2, response.trainers().size());
        assertEquals("trainer1", response.trainers().get(0).trainerUsername());
        assertEquals("Ahmet", response.trainers().get(0).trainerFirstName());
        assertEquals("trainer2", response.trainers().get(1).trainerUsername());
        assertEquals("Ayşe", response.trainers().get(1).trainerFirstName());

        verify(traineeRepository).findByUserUsernameWithTrainers(traineeUsername);
        verify(trainerRepository).findAllByUserUsernameIn(List.of("trainer1", "trainer2"));
        verify(traineeRepository).save(any(TraineeEntity.class));
    }

    private static List<TrainerEntity> getTrainerEntities() {
        TrainerEntity trainer1 = new TrainerEntity();
        UserEntity trainerUser1 = new UserEntity();
        trainerUser1.setUsername("trainer1");
        trainerUser1.setFirstName("Ahmet");
        trainerUser1.setLastName("Yılmaz");
        trainer1.setUser(trainerUser1);
        TrainingTypeEntity trainingType1 = new TrainingTypeEntity();
        trainingType1.setId(1L);
        trainingType1.setTrainingTypeName("Cardio");
        trainer1.setTrainingType(trainingType1);

        // trainer2
        TrainerEntity trainer2 = new TrainerEntity();
        UserEntity trainerUser2 = new UserEntity();
        trainerUser2.setUsername("trainer2");
        trainerUser2.setFirstName("Ayşe");
        trainerUser2.setLastName("Kara");
        trainer2.setUser(trainerUser2);
        TrainingTypeEntity trainingType2 = new TrainingTypeEntity();
        trainingType2.setId(2L);
        trainingType2.setTrainingTypeName("Fitness");
        trainer2.setTrainingType(trainingType2);

        return List.of(trainer1, trainer2);
    }

    @Test
    void updateTraineeTrainers_shouldThrowNotFoundException_whenTraineeNotFound() {
        String traineeUsername = "not.found";
        TraineeTrainerUpdateRequest request = new TraineeTrainerUpdateRequest(
                traineeUsername,
                List.of(new TrainerUsernameRequest("trainer1"))
        );

        when(traineeRepository.findByUserUsernameWithTrainers(traineeUsername)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                traineeService.updateTraineeTrainers(request));
        assertTrue(ex.getMessage().contains("Trainee not found with username"));

        verify(traineeRepository).findByUserUsernameWithTrainers(traineeUsername);
        verifyNoMoreInteractions(trainerRepository);
    }

    @SuppressWarnings("unchecked")
	@Test
    void getTraineeTrainings_shouldReturnList_whenTraineeExists() {
        String username = "ali.veli";
        TraineeEntity traineeEntity = new TraineeEntity();
        when(traineeRepository.findByUserUsernameWithTrainers(username))
                .thenReturn(Optional.of(traineeEntity));

        // Filter
        TraineeTrainingsFilter filter = new TraineeTrainingsFilter(
                username, "2024-01-01", "2024-07-31", "Ahmet", "Strength"
        );

        TrainingEntity training = new TrainingEntity();
        training.setTrainingName("Push Day");
        training.setTrainingDate(LocalDateTime.of(2024, 6, 20, 0, 0, 0));

        List<TrainingEntity> trainingEntities = List.of(training);
        when(trainingRepository.findAll(any(Specification.class)))
                .thenReturn(trainingEntities);

        TraineeTrainingsListResponse response = traineeService.getTraineeTrainings(filter);

        assertNotNull(response);
        assertEquals(1, response.trainings().size());
        assertEquals("Push Day", response.trainings().get(0).trainingName());

        verify(traineeRepository).findByUserUsernameWithTrainers(username);
        verify(trainingRepository).findAll(any(Specification.class));
    }

    @Test
    void getTraineeTrainings_shouldThrowNotFoundException_whenTraineeNotExists() {
        String username = "notfound.user";
        when(traineeRepository.findByUserUsernameWithTrainers(username))
                .thenReturn(Optional.empty());

        TraineeTrainingsFilter filter = new TraineeTrainingsFilter(
                username, null, null, null, null
        );

        assertThrows(NotFoundException.class, () -> {
            traineeService.getTraineeTrainings(filter);
        });

        verify(traineeRepository).findByUserUsernameWithTrainers(username);
        verifyNoInteractions(trainingRepository);
    }

    @Test
    void updateActiveStatus_shouldActivate_whenInactive() {
        String username = "ali.veli";
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest(username, true);

        UserEntity userEntity = new UserEntity();
        userEntity.setActive(false);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setUser(userEntity);

        doNothing().when(metrics).incrementActivated();
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(traineeEntity));
        when(traineeRepository.save(any(TraineeEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // act & assert
        assertDoesNotThrow(() -> traineeService.updateActivateStatus(request));
        verify(traineeRepository).findByUserUsername(username);
        verify(traineeRepository).save(any(TraineeEntity.class));
    }

    @Test
    void updateActiveStatus_shouldDeactivate_whenActive() {
        String username = "ali.veli";
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest(username, false);

        UserEntity userEntity = new UserEntity();
        userEntity.setActive(true);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setUser(userEntity);

        doNothing().when(metrics).incrementDeactivated();
        when(traineeRepository.findByUserUsername(username))
                .thenReturn(Optional.of(traineeEntity));
        when(traineeRepository.save(any(TraineeEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() -> traineeService.updateActivateStatus(request));

        verify(traineeRepository).findByUserUsername(username);
        verify(traineeRepository).save(any(TraineeEntity.class));
    }

    @Test
    void updateActiveStatus_shouldThrowNotFound_whenTraineeNotFound() {
        String username = "nonexistent";
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest(username, true);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                traineeService.updateActivateStatus(request)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
        verify(traineeRepository).findByUserUsername(username);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateActiveStatus_shouldThrowIllegalState_whenAlreadyActive() {
        String username = "ali.veli";
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest(username, true);

        UserEntity userEntity = new UserEntity();
        userEntity.setActive(true);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setUser(userEntity);

        when(traineeRepository.findByUserUsername(username))
                .thenReturn(Optional.of(traineeEntity));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                traineeService.updateActivateStatus(request)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("already active"));
        verify(traineeRepository).findByUserUsername(username);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void updateActiveStatus_shouldThrowIllegalState_whenAlreadyInactive() {
        String username = "ali.veli";
        UpdateActiveStatusRequest request = new UpdateActiveStatusRequest(username, false);

        UserEntity userEntity = new UserEntity();
        userEntity.setActive(false);

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setUser(userEntity);

        when(traineeRepository.findByUserUsername(username))
                .thenReturn(Optional.of(traineeEntity));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                traineeService.updateActivateStatus(request)
        );
        assertTrue(ex.getMessage().toLowerCase().contains("already inactive"));
        verify(traineeRepository).findByUserUsername(username);
        verify(traineeRepository, never()).save(any());
    }

    @Test
    void getUnassignedActiveTrainersForTrainee_shouldReturnList_whenTraineeExists() {
        String username = "ali.veli";
        Long traineeId = 1L;

        TraineeEntity traineeEntity = new TraineeEntity();
        traineeEntity.setId(traineeId);

        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.of(traineeEntity));

        TrainerEntity trainer1 = new TrainerEntity();
        UserEntity user1 = new UserEntity();
        user1.setUsername("mehmet.kaya");
        user1.setFirstName("Mehmet");
        user1.setLastName("Kaya");
        user1.setActive(true);
        trainer1.setUser(user1);

        TrainingTypeEntity type = new TrainingTypeEntity();
        type.setId(1L);
        type.setTrainingTypeName("Cardio");
        trainer1.setTrainingType(type);

        List<TrainerEntity> trainers = List.of(trainer1);
        when(trainerRepository.findUnassignedTrainersForTrainee(traineeId)).thenReturn(trainers);

        UnassignedActiveTrainerListResponse response =
                traineeService.getUnassignedActiveTrainersForTrainee(username);

        assertNotNull(response);
        assertEquals(1, response.trainers().size());
        assertEquals("mehmet.kaya", response.trainers().get(0).username());

        verify(traineeRepository).findByUserUsername(username);
        verify(trainerRepository).findUnassignedTrainersForTrainee(traineeId);
    }

    @Test
    void getUnassignedActiveTrainersForTrainee_shouldThrowNotFound_whenTraineeNotExists() {
        String username = "notfound.user";
        when(traineeRepository.findByUserUsername(username)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> {
            traineeService.getUnassignedActiveTrainersForTrainee(username);
        });

        verify(traineeRepository).findByUserUsername(username);
        verifyNoInteractions(trainerRepository);
    }

    @Test
    void createTrainee_shouldThrowException_whenUserIsAlreadyTrainer() {
        TraineeRegistrationRequest request =
                new TraineeRegistrationRequest("Ali", "Veli", "2000-01-01", "İzmir");

        User createdUser = new User();
        createdUser.setUsername("ali.veli");
        createdUser.setRawPassword("does-not-matter");
        when(userAccountService.createUser("Ali", "Veli")).thenReturn(createdUser);

        when(trainerRepository.existsByUserUsername("ali.veli")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> traineeService.createTrainee(request));

        assertThat(ex.getMessage()).contains("User cannot be both trainee and trainer");
        verify(trainerRepository).existsByUserUsername("ali.veli");
        verify(traineeRepository, never()).save(any(TraineeEntity.class));
        verify(metrics, never()).incrementRegistered();
    }

}