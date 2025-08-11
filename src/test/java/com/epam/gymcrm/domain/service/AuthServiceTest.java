package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.payload.request.ChangePasswordRequest;
import com.epam.gymcrm.api.payload.request.LoginRequest;
import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.db.repository.UserRepository;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.InvalidCredentialsException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthService authService;

    private static final String USERNAME = "ali.veli";
    private static final String PASSWORD = "pass1123";

    @Test
    void login_shouldSucceed_whenCredentialsCorrectAndActive() {
        // arrange
        UserEntity userEntity = new UserEntity("Ali", "Veli", USERNAME, PASSWORD, true);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));

        // act & assert
        assertDoesNotThrow(() -> authService.login(new LoginRequest(USERNAME, PASSWORD)));
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void login_shouldFail_whenUserNotFound() {
        // arrange
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        // act & assert
        NotFoundException ex = assertThrows(NotFoundException.class, () ->
                authService.login(new LoginRequest(USERNAME, PASSWORD))
        );
        assertTrue(ex.getMessage().contains("User not found"));
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void login_shouldFail_whenPasswordIsWrong() {
        // arrange
        UserEntity userEntity = new UserEntity("Ali", "Veli", USERNAME, PASSWORD, true);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));

        // act & assert
        InvalidCredentialsException ex = assertThrows(InvalidCredentialsException.class, () ->
                authService.login(new LoginRequest(USERNAME, "wrong-pass"))
        );
        assertTrue(ex.getMessage().contains("Invalid credentials"));
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void login_shouldFail_whenUserIsNotActive() {
        // arrange
        UserEntity userEntity = new UserEntity("Ali", "Veli", USERNAME, PASSWORD, false);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));

        // act & assert
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                authService.login(new LoginRequest(USERNAME, PASSWORD))
        );
        assertTrue(ex.getMessage().contains("not active"));
        verify(userRepository).findByUsername(USERNAME);
    }

    @Test
    void changePassword_shouldSucceed_whenOldPasswordCorrectAndNewIsDifferent() {
        String username = "ali.veli";
        String oldPassword = "old123";
        String newPassword = "new456";

        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(oldPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword);

        assertDoesNotThrow(() -> authService.changePassword(request));
        assertEquals(newPassword, userEntity.getPassword());
        verify(userRepository).save(userEntity);
    }

    @Test
    void changePassword_shouldThrowNotFoundException_whenUserNotFound() {
        String username = "notfound";
        ChangePasswordRequest request = new ChangePasswordRequest(username, "old", "new");

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class, () -> authService.changePassword(request));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowBadRequest_whenOldPasswordInvalid() {
        String username = "ali.veli";
        String oldPassword = "wrong";
        String actualPassword = "correct";
        String newPassword = "newpass";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(actualPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.changePassword(request));
        assertTrue(ex.getMessage().contains("Invalid old password"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrowBadRequest_whenNewPasswordIsSameAsOld() {
        String username = "ali.veli";
        String oldPassword = "password";
        String newPassword = "password";
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(username);
        userEntity.setPassword(oldPassword);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(userEntity));

        ChangePasswordRequest request = new ChangePasswordRequest(username, oldPassword, newPassword);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.changePassword(request));
        assertTrue(ex.getMessage().contains("New password cannot be same as old password"));
        verify(userRepository, never()).save(any());
    }
}