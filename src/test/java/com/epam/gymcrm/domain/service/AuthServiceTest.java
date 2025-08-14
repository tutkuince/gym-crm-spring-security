package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.payload.request.ChangePasswordRequest;
import com.epam.gymcrm.api.payload.request.LoginRequest;
import com.epam.gymcrm.api.payload.response.LoginResponse;
import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.db.repository.UserRepository;
import com.epam.gymcrm.domain.exception.AccountLockedException;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.InvalidCredentialsException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import com.epam.gymcrm.infrastructure.security.BruteForceService;
import com.epam.gymcrm.infrastructure.security.TokenBlacklist;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private BruteForceService bruteForce;
    @Mock
    private JwtEncoder jwtEncoder;
    @Mock
    private JwtDecoder jwtDecoder;
    @Mock
    private TokenBlacklist blacklist;

    private AuthService authService;

    private static final String USERNAME = "ali.veli";
    private static final String RAW_PASS = "pass1123";
    private static final String HASH_PASS = "{bcrypt}hash";

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                passwordEncoder,
                bruteForce,
                jwtEncoder,
                jwtDecoder,
                blacklist,
                "test-issuer",
                60L
        );
    }

    private Jwt fakeJwt(String token, Instant iat, Instant exp, Map<String, Object> claims) {
        Map<String, Object> headers = Map.of("alg", MacAlgorithm.HS256.getName());
        return new Jwt(token, iat, exp, headers, claims);
    }

    @Test
    void login_shouldSucceed_whenCredentialsCorrectAndActive() {
        when(bruteForce.isBlocked(USERNAME)).thenReturn(false);

        UserEntity userEntity = new UserEntity("Ali", "Veli", USERNAME, HASH_PASS, true);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(RAW_PASS, HASH_PASS)).thenReturn(true);

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(3600);
        Jwt jwt = fakeJwt("TOKEN", now, exp, Map.of("sub", USERNAME));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(jwt);

        LoginResponse resp = authService.login(new LoginRequest(USERNAME, RAW_PASS));

        assertNotNull(resp);
        assertEquals("TOKEN", resp.token());
        assertNotNull(resp.expiresAt());
        verify(bruteForce).registerSuccess(USERNAME);
    }

    @Test
    void login_shouldFail_whenUserNotFound() {
        when(bruteForce.isBlocked(USERNAME)).thenReturn(false);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> authService.login(new LoginRequest(USERNAME, RAW_PASS)));

        verify(bruteForce, never()).registerSuccess(anyString());
    }

    @Test
    void login_shouldFail_whenPasswordIsWrong() {
        when(bruteForce.isBlocked(USERNAME)).thenReturn(false);

        UserEntity userEntity = new UserEntity("Ali", "Veli", USERNAME, HASH_PASS, true);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches("wrong", HASH_PASS)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new LoginRequest(USERNAME, "wrong")));

        verify(bruteForce).registerFailure(USERNAME);
        verify(bruteForce, never()).registerSuccess(anyString());
    }

    @Test
    void login_shouldFail_whenUserIsNotActive() {
        when(bruteForce.isBlocked(USERNAME)).thenReturn(false);

        UserEntity userEntity = new UserEntity("Ali", "Veli", USERNAME, HASH_PASS, false);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(userEntity));
        when(passwordEncoder.matches(RAW_PASS, HASH_PASS)).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> authService.login(new LoginRequest(USERNAME, RAW_PASS)));

        verify(bruteForce, never()).registerSuccess(anyString());
    }

    @Test
    void login_shouldFail_whenBruteForceBlocked() {
        when(bruteForce.isBlocked(USERNAME)).thenReturn(true);

        assertThrows(AccountLockedException.class,
                () -> authService.login(new LoginRequest(USERNAME, RAW_PASS)));

        verify(userRepository, never()).findByUsername(anyString());
        verify(bruteForce, never()).registerSuccess(anyString());
    }

    @Test
    void logout_shouldInvalidateToken_whenValidBearerProvided() {
        String header = "Bearer ABC";
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(600);

        Jwt decoded = fakeJwt("ABC", now, exp, Map.of("jti", "JTI-1"));
        when(jwtDecoder.decode("ABC")).thenReturn(decoded);

        authService.logout(header);

        verify(blacklist).invalidate("JTI-1", exp);
    }

    @Test
    void logout_shouldIgnore_whenHeaderMissingOrInvalid() {
        authService.logout(null);
        authService.logout("Basic xyz");
        verifyNoInteractions(jwtDecoder, blacklist);
    }

    @Test
    void changePassword_shouldSucceed_whenOldCorrect_andNewDifferent() {
        String oldRaw = "old123";
        String newRaw = "new456";
        String currentHash = "HASH_OLD";
        String newHash = "HASH_NEW";

        UserEntity ue = new UserEntity();
        ue.setUsername(USERNAME);
        ue.setPassword(currentHash);

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(ue));
        when(passwordEncoder.matches(oldRaw, currentHash)).thenReturn(true);   // eski doğru
        when(passwordEncoder.matches(newRaw, currentHash)).thenReturn(false); // yeni eskisiyle aynı değil
        when(passwordEncoder.encode(newRaw)).thenReturn(newHash);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        assertDoesNotThrow(() ->
                authService.changePassword(new ChangePasswordRequest(USERNAME, oldRaw, newRaw)));

        assertEquals(newHash, ue.getPassword());

        ArgumentCaptor<UserEntity> cap = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(cap.capture());
        assertEquals(newHash, cap.getValue().getPassword());
    }

    @Test
    void changePassword_shouldThrowNotFound_whenUserMissing() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> authService.changePassword(new ChangePasswordRequest(USERNAME, "o", "n")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrow_whenOldPasswordInvalid() {
        UserEntity ue = new UserEntity();
        ue.setUsername(USERNAME);
        ue.setPassword("HASH");

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(ue));
        when(passwordEncoder.matches("wrong", "HASH")).thenReturn(false);

        assertThrows(BadRequestException.class,
                () -> authService.changePassword(new ChangePasswordRequest(USERNAME, "wrong", "new")));

        verify(userRepository, never()).save(any());
    }

    @Test
    void changePassword_shouldThrow_whenNewPasswordSameAsOld() {
        UserEntity ue = new UserEntity();
        ue.setUsername(USERNAME);
        ue.setPassword("HASH");

        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(ue));

        when(passwordEncoder.matches("old", "HASH")).thenReturn(true);
        when(passwordEncoder.matches("same", "HASH")).thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> authService.changePassword(new ChangePasswordRequest(USERNAME, "old", "same")));

        verify(userRepository, never()).save(any());
    }
}