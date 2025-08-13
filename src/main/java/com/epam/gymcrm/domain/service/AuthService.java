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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BruteForceService bruteForce;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklist blacklist;
    private final String issuer;
    private final long accessTokenMinutes;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       BruteForceService bruteForce,
                       JwtEncoder jwtEncoder,
                       JwtDecoder jwtDecoder,
                       TokenBlacklist blacklist,
                       @Value("${security.jwt.issuer}") String issuer,
                       @Value("${security.jwt.access-token-minutes}") long accessTokenMinutes) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bruteForce = bruteForce;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.blacklist = blacklist;
        this.issuer = issuer;
        this.accessTokenMinutes = accessTokenMinutes;
    }

    public LoginResponse login(LoginRequest request) {
        final String username = request.username();
        logger.info("Login attempt. username={}", username);

        if (bruteForce.isBlocked(username)) {
            long retry = bruteForce.retryAfterSeconds(username);
            logger.warn("Login blocked due to brute-force. username={}, retryAfterSeconds={}", username, retry);
            throw new AccountLockedException(retry); // 423 için custom exception
        }

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Login failed: user not found. username={}", username);
                    return new NotFoundException(String.format(
                            "Login failed: User not found for login. (username=%s)", username));
                });

        if (!passwordEncoder.matches(request.password(), userEntity.getPassword())) {
            bruteForce.registerFailure(username);
            logger.warn("Login failed: invalid password. username={}", username);
            throw new InvalidCredentialsException("Login failed: Invalid credentials.");
        }

        if (!Boolean.TRUE.equals(userEntity.getActive())) {
            logger.warn("Login failed: user not active. username={}", username);
            throw new BadRequestException("Login failed: User is not active.");
        }

        // Successful login
        bruteForce.registerSuccess(username);

        Instant now = Instant.now();
        Instant exp = now.plus(Duration.ofMinutes(accessTokenMinutes));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .subject(userEntity.getUsername())
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .id(UUID.randomUUID().toString())   // jti
                .claim("roles", "USER")             // TODO: real rolls
                .build();

        JwsHeader jws = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(jws, claims)).getTokenValue();

        logger.info("Login success! username={}", username);
        return new LoginResponse(token, exp.toString());
    }

    public void logout(String bearerToken) {
        if (Objects.isNull(bearerToken) || !bearerToken.startsWith("Bearer ")) return;
        String token = bearerToken.substring(7);
        try {
            Jwt jwt = jwtDecoder.decode(token);
            String jti = jwt.getId();
            Instant exp = jwt.getExpiresAt();
            if (Objects.nonNull(jti) && Objects.nonNull(exp)) {
                blacklist.invalidate(jti, exp);
                logger.info("Logout success: token invalidated. jti={}, exp={}", jti, exp);
            }
        } catch (JwtException e) {
            logger.warn("Logout ignored: invalid token provided.");
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        final String username = request.username();
        logger.info("Change password attempt. username={}", username);

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Password change failed: user not found. username={}", username);
                    return new NotFoundException(String.format(
                            "Password change failed: User not found. (username=%s)", username));
                });

        if (!passwordEncoder.matches(request.oldPassword(), userEntity.getPassword())) {
            logger.warn("Password change failed: old password invalid. username={}", username);
            throw new BadRequestException("Password change failed: Invalid old password.");
        }

        if (passwordEncoder.matches(request.newPassword(), userEntity.getPassword())) {
            logger.warn("Password change failed: new password cannot be same as old. username={}", username);
            throw new BadRequestException("Password change failed: New password cannot be same as old password.");
        }

        userEntity.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(userEntity);

        logger.info("Password changed successfully. username={}", username);
    }
}