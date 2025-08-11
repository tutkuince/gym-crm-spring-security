package com.epam.gymcrm.api.auth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AuthSessionManager {
    private static final Map<String, Boolean> sessionMap = new ConcurrentHashMap<>();

    public static void login(String username) {
        sessionMap.put(username, true);
    }

    public static boolean isLoggedInd(String username) {
        return sessionMap.getOrDefault(username, false);
    }

    public static void logout(String username) {
        sessionMap.remove(username);
    }

    public static void clearAll() {
        sessionMap.clear();
    }
}
