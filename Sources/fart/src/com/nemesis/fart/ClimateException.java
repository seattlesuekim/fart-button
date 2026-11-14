package com.nemesis.fart;

public final class ClimateException extends Exception {
    public ClimateException(String message) {
        super(message);
    }

    public ClimateException(String message, Throwable cause) {
        super(message, cause);
    }
}
