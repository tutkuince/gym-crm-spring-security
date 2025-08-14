package com.epam.gymcrm.api.mapper;

import com.epam.gymcrm.api.payload.response.TraineeRegistrationResponse;
import com.epam.gymcrm.db.entity.TraineeEntity;
import com.epam.gymcrm.db.entity.UserEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraineeResponseMapperTest {

    @Test
    void toTraineeRegisterResponse_shouldReturnResponse_whenUserIsPresent() {
        // given
        UserEntity user = new UserEntity();
        user.setUsername("ali.veli");

        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(77L);
        trainee.setUser(user);

        String rawPassword = "12345";

        // when
        TraineeRegistrationResponse res =
                TraineeResponseMapper.toTraineeRegisterResponse(trainee, rawPassword);

        // then
        assertEquals("ali.veli", res.username());
        assertEquals("12345", res.password());
    }

    @Test
    void toTraineeRegisterResponse_shouldThrow_whenUserIsNull() {
        // given
        TraineeEntity trainee = new TraineeEntity();
        trainee.setId(77L);
        trainee.setUser(null);

        // when / then
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                TraineeResponseMapper.toTraineeRegisterResponse(trainee, "irrelevant")
        );
        assertTrue(ex.getMessage().contains("Associated User entity is null"));
    }
}