package com.ankhorage.eddy.compression;

import java.util.ArrayList;
import java.util.List;

public class RLECompression implements CompressionAlgorithm {
    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }

        List<Byte> compressed = new ArrayList<>();
        int count = 1;
        byte current = data[0];

        for (int i = 1; i < data.length; i++) {
            if (data[i] == current && count < 255) {
                count++;
            } else {
                compressed.add((byte) count);
                compressed.add(current);
                current = data[i];
                count = 1;
            }
        }
        compressed.add((byte) count);
        compressed.add(current);

        byte[] result = new byte[compressed.size()];
        for (int i = 0; i < compressed.size(); i++) {
            result[i] = compressed.get(i);
        }
        return result;
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }
        if (data.length % 2 != 0) {
            throw new CompressionException("Invalid compressed data format");
        }

        List<Byte> decompressed = new ArrayList<>();
        for (int i = 0; i < data.length; i += 2) {
            int count = data[i] & 0xFF;
            byte value = data[i + 1];
            for (int j = 0; j < count; j++) {
                decompressed.add(value);
            }
        }

        byte[] result = new byte[decompressed.size()];
        for (int i = 0; i < decompressed.size(); i++) {
            result[i] = decompressed.get(i);
        }
        return result;
    }

    @Override
    public String getAlgorithmName() {
        return "Run-Length Encoding";
    }
} 