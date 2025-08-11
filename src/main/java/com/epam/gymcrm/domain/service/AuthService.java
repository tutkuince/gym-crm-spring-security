package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.api.auth.AuthSessionManager;
import com.epam.gymcrm.api.payload.request.ChangePasswordRequest;
import com.epam.gymcrm.api.payload.request.LoginRequest;
import com.epam.gymcrm.db.entity.UserEntity;
import com.epam.gymcrm.db.repository.UserRepository;
import com.epam.gymcrm.domain.mapper.UserDomainMapper;
import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.domain.exception.BadRequestException;
import com.epam.gymcrm.domain.exception.InvalidCredentialsException;
import com.epam.gymcrm.domain.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void login(LoginRequest request) {
        String username = request.username();
        logger.info("Login attempt. username={}", username);

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Login failed: user not found. username={}", username);
                    return new NotFoundException(
                            String.format("Login failed: User not found for login. (username=%s)", username)
                    );
                });

        User user = UserDomainMapper.toUser(userEntity);

        if (!user.checkPassword(request.password())) {
            logger.warn("Login failed: invalid password. username={}", username);
            throw new InvalidCredentialsException("Login failed: Invalid credentials.");
        }

        if (!user.isActive()) {
            logger.warn("Login failed: user not active. username={}", request.username());
            throw new BadRequestException("Login failed: User is not active.");
        }

        AuthSessionManager.login(username);
        logger.info("Login success! username={}", request.username());
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {
        String username = request.username();
        logger.info("Change password attempt. username={}", username);

        UserEntity userEntity = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("Password change failed: user not found. username={}", username);
                    return new NotFoundException(
                            String.format("Password change failed: User not found. (username=%s)", username)
                    );
                });

        User user = UserDomainMapper.toUser(userEntity);

        // Validate old password
        if (!user.checkPassword(request.oldPassword())) {
            logger.warn("Password change failed: old password invalid. username={}", username);
            throw new BadRequestException("Password change failed: Invalid old password.");
        }

        // Prevent changing to the same password
        if (user.checkPassword(request.newPassword())) {
            logger.warn("Password change failed: new password cannot be same as old. username={}", username);
            throw new BadRequestException("Password change failed: New password cannot be same as old password.");
        }

        // Update password in domain
        user.changePassword(request.newPassword());

        // Persist password change
        userEntity.setPassword(user.getPassword());
        userRepository.save(userEntity);

        logger.info("Password changed successfully. username={}", username);
    }
}
