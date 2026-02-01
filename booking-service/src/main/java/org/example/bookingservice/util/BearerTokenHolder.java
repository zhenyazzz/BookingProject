package org.example.bookingservice.util;


public final class BearerTokenHolder {

    private static final ThreadLocal<String> TOKEN = new ThreadLocal<>();

    private BearerTokenHolder() {}

    public static void setToken(String token) {
        TOKEN.set(token);
    }

    public static String getToken() {
        return TOKEN.get();
    }

    public static void clear() {
        TOKEN.remove();
    }
}
