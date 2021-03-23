package com.tjhelmuth;

public class VideoCaptureException extends RuntimeException {
    public VideoCaptureException(String message) {
        super(message);
    }

    public VideoCaptureException(String message, Throwable cause) {
        super(message, cause);
    }
}
