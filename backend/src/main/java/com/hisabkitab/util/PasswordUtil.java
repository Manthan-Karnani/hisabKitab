package com.hisabkitab.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HexFormat;

/**
 * Simple salted SHA-256 password hashing (no extra dependencies).
 * Stored format: saltHex$hashHex
 */
public final class PasswordUtil {

    private PasswordUtil() {}

    public static String hash(String rawPassword) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);
            String saltHex = HexFormat.of().formatHex(salt);
            String hashHex = sha256Hex(saltHex + rawPassword);
            return saltHex + "$" + hashHex;
        } catch (Exception e) {
            throw new IllegalStateException("Password hashing failed", e);
        }
    }

    public static boolean matches(String rawPassword, String stored) {
        try {
            if (stored == null || !stored.contains("$")) return false;
            String[] parts = stored.split("\\$", 2);
            String expected = sha256Hex(parts[0] + rawPassword);
            return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                                         parts[1].getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    private static String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }
}
