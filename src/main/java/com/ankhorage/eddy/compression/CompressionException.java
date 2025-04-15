package com.ankhorage.eddy.compression;

public class CompressionException extends Exception {
    public CompressionException(String message) {
        super(message);
    }

    public CompressionException(String message, Throwable cause) {
        super(message, cause);
    }
} 