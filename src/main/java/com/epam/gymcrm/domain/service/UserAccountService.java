package com.epam.gymcrm.domain.service;

import com.epam.gymcrm.db.repository.UserRepository;
import com.epam.gymcrm.domain.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UserAccountService {
    private static final int PASSWORD_CHAR_LENGTH = 10;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserAccountService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String generateUniqueUsername(String firstName, String lastName) {
        String base = (firstName + "." + lastName).toLowerCase();
        String candidate = base;
        int counter = 1;
        while (userRepository.existsByUsername(candidate)) {
            candidate = base + counter++;
        }
        return candidate;
    }

    public String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_CHAR_LENGTH);
        for (int i = 0; i < PASSWORD_CHAR_LENGTH; i++) {
            int ascii = 33 + random.nextInt(94);
            sb.append((char) ascii);
        }
        return sb.toString();
    }

    public String hash(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }

    public User createUser(String firstName, String lastName) {
        String username = generateUniqueUsername(firstName, lastName);
        String raw = generateRandomPassword();
        String hashed = hash(raw);

        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setUsername(username);
        user.setPassword(hashed);
        user.setRawPassword(raw);
        user.setActive(Boolean.TRUE);

        return user;
    }
}
