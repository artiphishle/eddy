package com.ankhorage.eddy.security;

public interface EncryptionAlgorithm {
    String encrypt(String text, String key) throws SecurityException;
    String decrypt(String text, String key) throws SecurityException;
    String getAlgorithmName();
    String getHistoricalContext();
} 