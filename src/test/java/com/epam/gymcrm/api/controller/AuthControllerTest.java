package com.epam.gymcrm.api.controller;

import com.epam.gymcrm.api.payload.request.ChangePasswordRequest;
import com.epam.gymcrm.api.payload.request.LoginRequest;
import com.epam.gymcrm.domain.service.AuthService;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.GlobalExceptionHandler;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void login_shouldReturn200_whenLoginSuccessful() throws Exception {
        doNothing().when(authService).login(any(LoginRequest.class));

        mockMvc.perform(get("/api/v1/auth/login")
                        .param("username", "ali.veli")
                        .param("password", "pass1123")
                )
                .andExpect(status().isOk());
    }

    @Test
    void login_shouldReturn404_whenUserNotFound() throws Exception {
        doThrow(new NotFoundException("User not found"))
                .when(authService)
                .login(any(LoginRequest.class));

        mockMvc.perform(get("/api/v1/auth/login")
                        .param("username", "notfound")
                        .param("password", "pass1123")
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void login_shouldReturn400_whenCredentialsInvalid() throws Exception {
        doThrow(new BadRequestException("Invalid credentials"))
                .when(authService)
                .login(any(LoginRequest.class));

        mockMvc.perform(get("/api/v1/auth/login")
                        .param("username", "ali.veli")
                        .param("password", "wrong-pass")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void login_shouldReturn400_whenUserNotActive() throws Exception {
        doThrow(new BadRequestException("User is not active"))
                .when(authService)
                .login(any(LoginRequest.class));

        mockMvc.perform(get("/api/v1/auth/login")
                        .param("username", "ali.veli")
                        .param("password", "pass1123")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    void changePassword_shouldReturn200_whenSuccessful() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("ali.veli", "old-pass", "new-pass");
        doNothing().when(authService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void changePassword_shouldReturn404_whenUserNotFound() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("notfound", "old-pass", "new-pass");
        doThrow(new NotFoundException("User not found")).when(authService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("User not found")));
    }

    @Test
    void changePassword_shouldReturn400_whenOldPasswordInvalid() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("ali.veli", "wrong-old", "new-pass");
        doThrow(new BadRequestException("Invalid old password")).when(authService).changePassword(any(ChangePasswordRequest.class));

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid old password")));
    }

    @Test
    void changePassword_shouldReturn400_whenNewPasswordBlank() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest("ali.veli", "old-pass", " ");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("New password cannot be blank")));
    }
}