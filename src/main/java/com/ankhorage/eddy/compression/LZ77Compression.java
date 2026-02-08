package com.ankhorage.eddy.compression;

public class LZ77Compression implements CompressionAlgorithm {

    private static final int WINDOW_SIZE = 4096;
    private static final int LOOKAHEAD_BUFFER_SIZE = 16;

    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }

        java.io.ByteArrayOutputStream compressed = new java.io.ByteArrayOutputStream();
        int cursor = 0;

        while (cursor < data.length) {
            int bestOffset = 0;
            int bestLength = 0;

            int searchBufferStart = Math.max(0, cursor - WINDOW_SIZE);
            int maxMatchLength = Math.min(LOOKAHEAD_BUFFER_SIZE, data.length - cursor - 1);
            if (maxMatchLength < 0) {
                maxMatchLength = 0;
            }

            for (int i = searchBufferStart; i < cursor; i++) {
                int currentLength = 0;
                while (currentLength < maxMatchLength && (cursor + currentLength) < data.length && data[i + currentLength] == data[cursor + currentLength]) {
                    currentLength++;
                }

                if (currentLength > bestLength) {
                    bestLength = currentLength;
                    bestOffset = cursor - i;
                }
            }

            byte nextByte;
            if (bestLength > 0) {
                 nextByte = data[cursor + bestLength];
            } else {
                 nextByte = data[cursor];
            }

            compressed.write((bestOffset >> 8) & 0xFF);
            compressed.write(bestOffset & 0xFF);
            compressed.write(bestLength);
            compressed.write(nextByte);

            cursor += bestLength + 1;
        }

        return compressed.toByteArray();
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }
        if (data.length % 4 != 0) {
            throw new CompressionException("Invalid compressed data format: length must be a multiple of 4");
        }

        java.io.ByteArrayOutputStream decompressed = new java.io.ByteArrayOutputStream();
        int cursor = 0;

        while (cursor < data.length) {
            int offset = ((data[cursor] & 0xFF) << 8) | (data[cursor + 1] & 0xFF);
            int length = data[cursor + 2] & 0xFF;
            byte nextByte = data[cursor + 3];

            if (offset > decompressed.size()) {
                throw new CompressionException("Invalid offset in compressed data: " + offset);
            }

            if (length > 0) {
                int start = decompressed.size() - offset;
                for (int i = 0; i < length; i++) {
                    decompressed.write(decompressed.toByteArray()[start + i]);
                }
            }

            decompressed.write(nextByte);

            cursor += 4;
        }

        return decompressed.toByteArray();
    }

    @Override
    public String getAlgorithmName() {
        return "LZ77";
    }
}
