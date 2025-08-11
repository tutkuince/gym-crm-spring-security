package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeRegistrationResponse;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraineeResponseMapperTest {

    @Test
    void toTraineeRegisterResponse_shouldReturnResponse_whenUserIsPresent() {
        UserEntity user = new UserEntity();
        user.setUsername("ali.veli");
        user.setPassword("12345");

        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(77L);
        trainee.setUser(user);

        TraineeRegistrationResponse res = TraineeResponseMapper.toTraineeRegisterResponse(trainee);

        assertEquals("ali.veli", res.username());
        assertEquals("12345", res.password());
    }

    @Test
    void toTraineeRegisterResponse_shouldThrow_whenUserIsNull() {
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(77L);
        trainee.setUser(null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                TraineeResponseMapper.toTraineeRegisterResponse(trainee)
        );
        assertTrue(ex.getMessage().contains("Associated User entity is null"));
    }

}