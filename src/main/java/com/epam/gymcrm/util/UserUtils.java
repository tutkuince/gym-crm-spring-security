package com.epam.gymcrm.util;

import com.epam.gymcrm.domain.model.User;
import com.epam.gymcrm.db.repository.UserRepository;

import java.security.SecureRandom;

public class UserUtils {

    private static final int PASSWORD_CHAR_LENGTH = 10;
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    public static String generateUniqueUsername(String firstName, String lastName, UserRepository userRepository) {
        String baseUsername = firstName.toLowerCase() + "." + lastName.toLowerCase();
        String username = baseUsername;
        int counter = 1;
        while (userRepository.existsByUsername(username)) {
            username = baseUsername + counter;
            counter++;
        }
        return username;
    }

    public static String generateRandomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(PASSWORD_CHAR_LENGTH);
        for (int i = 0; i < PASSWORD_CHAR_LENGTH; i++) {
            int idx = random.nextInt(ALPHANUM.length());
            sb.append(ALPHANUM.charAt(idx));
        }
        return sb.toString();
    }

    public static User createUser(String firstName, String lastName, UserRepository userRepository) {
        User user = new User();
        user.setFirstName(firstName);
        user.setLastName(lastName);

        String username = generateUniqueUsername(firstName, lastName, userRepository);
        user.setUsername(username);

        user.setPassword(generateRandomPassword());
        user.setActive(true);
        return user;
    }
}
