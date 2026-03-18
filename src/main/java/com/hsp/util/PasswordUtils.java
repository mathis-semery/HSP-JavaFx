package com.hsp.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    public static String hash(String plainPassword) {
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt());
    }

    public static boolean verify(String plainPassword, String hashedPassword) {
        if (hashedPassword == null || hashedPassword.isEmpty()) return false;
        return BCrypt.checkpw(plainPassword, hashedPassword);
    }
}
