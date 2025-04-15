package com.ankhorage.eddy.encryption;

public interface EncryptionAlgorithm {
    String encrypt(String text, String key) throws EncryptionException;
    String decrypt(String text, String key) throws EncryptionException;
    String getAlgorithmName();
    String getHistoricalContext();
} 