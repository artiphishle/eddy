package com.ankhorage.eddy.compression;

public interface CompressionAlgorithm {
    byte[] compress(byte[] data) throws CompressionException;
    byte[] decompress(byte[] data) throws CompressionException;
    String getAlgorithmName();
} 