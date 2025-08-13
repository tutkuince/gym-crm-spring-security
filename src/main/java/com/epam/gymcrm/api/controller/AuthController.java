package com.epam.gymcrm.api.controller;

import com.epam.gymcrm.api.payload.request.ChangePasswordRequest;
import com.epam.gymcrm.api.payload.request.LoginRequest;
import com.epam.gymcrm.api.payload.response.LoginResponse;
import com.epam.gymcrm.domain.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API for user login, logout and password management")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) { this.authService = authService; }

    @Operation(summary = "User login", description = "Authenticates user and returns a JWT access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "423", description = "Account locked by brute-force protector")
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @Operation(summary = "Logout", description = "Invalidates current token (blacklist) and ends the session.")
    @ApiResponses({ @ApiResponse(responseCode = "204", description = "Logout successful") })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change password", description = "Changes password for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request,
                                               @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(auth) || Objects.isNull(auth.getName())) {
            return ResponseEntity.status(401).build();
        }
        ChangePasswordRequest fixed = new ChangePasswordRequest(auth.getName(), request.oldPassword(), request.newPassword());
        authService.changePassword(fixed);

        authService.logout(authHeader);

        return ResponseEntity.ok().build();
    }
}