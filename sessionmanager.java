package com.bnpparibas.util;

public class SessionManager {

    private static String currentUser = null;

    public static boolean login(String username) {
        if (currentUser != null && currentUser.equals(username)) {
            return false;
        }
        currentUser = username;
        return true;
    }

    public static void logout() {
        currentUser = null;
    }
}