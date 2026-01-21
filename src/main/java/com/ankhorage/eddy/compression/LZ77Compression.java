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
        // TODO: Implement LZ77 decompression logic
        return new byte[0];
    }

    @Override
    public String getAlgorithmName() {
        return "LZ77";
    }
}
