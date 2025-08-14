package com.epam.gymcrm.api.controller;

import com.epam.gymcrm.api.payload.request.ChangePasswordRequest;
import com.epam.gymcrm.api.payload.request.LoginRequest;
import com.epam.gymcrm.api.payload.response.LoginResponse;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.GlobalExceptionHandler;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.domain.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders
                .standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void login_shouldReturn200_andBody_whenSuccessful() throws Exception {
        LoginResponse lr = new LoginResponse("JWT_TOKEN", "2099-01-01T00:00:00Z");
        when(authService.login(any(LoginRequest.class))).thenReturn(lr);

        LoginRequest body = new LoginRequest("ali.veli", "pass1123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is("JWT_TOKEN")))
                .andExpect(jsonPath("$.expiresAt", is("2099-01-01T00:00:00Z")));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void login_shouldReturn404_whenUserNotFound() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new NotFoundException("User not found"));

        LoginRequest body = new LoginRequest("notfound", "x");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }

    @Test
    void login_shouldReturn400_whenCredentialsInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new BadRequestException("Invalid credentials"));

        LoginRequest body = new LoginRequest("ali.veli", "wrong");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid credentials"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }

    @Test
    void logout_shouldReturn204_andCallService_withNullHeader_whenMissing() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isNoContent());

        verify(authService).logout(null);
    }

    @Test
    void logout_shouldReturn204_andPassHeader_whenProvided() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout")
                        .header("Authorization", "Bearer XYZ"))
                .andExpect(status().isNoContent());

        verify(authService).logout("Bearer XYZ");
    }

    @Test
    void changePassword_shouldReturn401_whenNotAuthenticated() throws Exception {
        ChangePasswordRequest body = new ChangePasswordRequest("ali.veli", "old", "new");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).changePassword(any());
        verify(authService, never()).logout(any());
    }

    @Test
    void changePassword_shouldUseAuthenticatedUsername_andCallLogout_thenReturn200() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ali.veli", null, null));

        ChangePasswordRequest reqBody = new ChangePasswordRequest("ignored", "oldPass", "newPass");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer ABC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isOk());

        // capture arg
        ArgumentCaptor<ChangePasswordRequest> cap = ArgumentCaptor.forClass(ChangePasswordRequest.class);
        verify(authService).changePassword(cap.capture());
        ChangePasswordRequest fixed = cap.getValue();

        assertEquals("ali.veli", fixed.username());   // from SecurityContext
        assertEquals("oldPass",  fixed.oldPassword());
        assertEquals("newPass",  fixed.newPassword());

        verify(authService).logout("Bearer ABC");
    }


    @Test
    void changePassword_shouldReturn404_andErrorBody_whenUserNotFound() throws Exception {
        // Auth koy
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ali.veli", null, null));

        doThrow(new NotFoundException("User not found"))
                .when(authService).changePassword(any(ChangePasswordRequest.class));

        ChangePasswordRequest reqBody = new ChangePasswordRequest("ignored", "old", "new");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer ABC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("User not found")));
    }


    @Test
    void changePassword_shouldReturn400_whenValidationFails_newPasswordBlank() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("ali.veli", null, null));

        ChangePasswordRequest reqBody = new ChangePasswordRequest("ignored", "old", " ");

        mockMvc.perform(put("/api/v1/auth/change-password")
                        .header("Authorization", "Bearer ABC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reqBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("blank")));
    }
}