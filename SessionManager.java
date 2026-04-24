package com.bnpparibas.util;

import java.util.HashSet;
import java.util.Set;

public class SessionManager {

    private static final Set<String> activeUsers = new HashSet<>();

    public static boolean login(String username) {

        if (username == null) return false;

        String user = username.toLowerCase();

        if (activeUsers.contains(user)) {
            return false;
        }

        activeUsers.add(user);
        return true;
    }

    public static void logout(String username) {

        if (username == null) return;

        activeUsers.remove(username.toLowerCase());
    }
}