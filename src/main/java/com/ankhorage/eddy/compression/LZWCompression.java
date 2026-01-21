package com.ankhorage.eddy.compression;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LZWCompression implements CompressionAlgorithm {

    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }

        int dictSize = 256;
        Map<String, Integer> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put("" + (char) i, i);
        }

        System.out.println("LZW Compression Dictionary Initialized. Size: " + dictionary.size());

        String w = "";
        List<Integer> result = new ArrayList<>();
        for (byte b : data) {
            String wc = w + (char) b;
            if (dictionary.containsKey(wc)) {
                w = wc;
            } else {
                result.add(dictionary.get(w));
                if (dictSize < 4096) { // Limit dictionary size
                    dictionary.put(wc, dictSize++);
                    if(dictSize % 100 == 0) System.out.println("Dictionary grown to: " + dictSize);
                }
                w = "" + (char) b;
            }
        }

        if (!w.equals("")) {
            result.add(dictionary.get(w));
        }

        return toByteArray(result);
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }

        List<Integer> compressed = fromByteArray(data);

        int dictSize = 256;
        Map<Integer, String> dictionary = new HashMap<>();
        for (int i = 0; i < 256; i++) {
            dictionary.put(i, "" + (char) i);
        }

        String w = "" + (char) (int) compressed.remove(0);
        StringBuilder result = new StringBuilder(w);
        for (int k : compressed) {
            String entry;
            if (dictionary.containsKey(k)) {
                entry = dictionary.get(k);
            } else if (k == dictSize) {
                entry = w + w.charAt(0);
            } else {
                throw new CompressionException("Bad compressed k: " + k);
            }

            result.append(entry);

            if (dictSize < 4096) {
                dictionary.put(dictSize++, w + entry.charAt(0));
            }

            w = entry;
        }
        return result.toString().getBytes();
    }

    @Override
    public String getAlgorithmName() {
        return "LZW (1984)";
    }

    private byte[] toByteArray(List<Integer> data) {
        // This is a simplified conversion, not robust for all cases
        byte[] result = new byte[data.size() * 2];
        for (int i = 0; i < data.size(); i++) {
            int value = data.get(i);
            result[i * 2] = (byte) (value >> 8);
            result[i * 2 + 1] = (byte) value;
        }
        return result;
    }

    private List<Integer> fromByteArray(byte[] data) {
        if (data.length % 2 != 0) {
            // simplistic check, might fail for valid data that has an odd length
        }
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < data.length; i += 2) {
            int value = ((data[i] & 0xFF) << 8) | (data[i + 1] & 0xFF);
            result.add(value);
        }
        return result;
    }
}
