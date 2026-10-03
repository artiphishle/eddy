package com.ankhorage.eddy.compression;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class LZ77Compression implements CompressionAlgorithm {
    private static final int WINDOW_SIZE = 4096;
    private static final int LOOKAHEAD_BUFFER_SIZE = 255;

    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }

        ByteArrayOutputStream compressed = new ByteArrayOutputStream();
        int cursor = 0;

        while (cursor < data.length) {
            Match match = findBestMatch(data, cursor);
            int literalIndex = cursor + match.length;
            byte nextByte = data[literalIndex];

            compressed.write((match.offset >>> 8) & 0xFF);
            compressed.write(match.offset & 0xFF);
            compressed.write(match.length);
            compressed.write(nextByte);

            cursor = literalIndex + 1;
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
            throw new CompressionException("Invalid LZ77 data length");
        }

        List<Byte> output = new ArrayList<>();
        for (int cursor = 0; cursor < data.length; cursor += 4) {
            int offset = ((data[cursor] & 0xFF) << 8) | (data[cursor + 1] & 0xFF);
            int length = data[cursor + 2] & 0xFF;
            byte nextByte = data[cursor + 3];

            if (length == 0) {
                if (offset != 0) {
                    throw new CompressionException("Invalid LZ77 literal offset");
                }
            } else {
                if (offset < 1 || offset > output.size()) {
                    throw new CompressionException(
                        "Invalid LZ77 back-reference offset: " + offset
                    );
                }

                int start = output.size() - offset;
                for (int index = 0; index < length; index++) {
                    int sourceIndex = start + index;
                    if (sourceIndex < 0 || sourceIndex >= output.size()) {
                        throw new CompressionException(
                            "Invalid LZ77 overlapping back-reference"
                        );
                    }
                    output.add(output.get(sourceIndex));
                }
            }

            output.add(nextByte);
        }

        byte[] result = new byte[output.size()];
        for (int index = 0; index < output.size(); index++) {
            result[index] = output.get(index);
        }
        return result;
    }

    @Override
    public String getAlgorithmName() {
        return "LZ77 (1977)";
    }

    private Match findBestMatch(byte[] data, int cursor) {
        int maxLength = Math.min(
            LOOKAHEAD_BUFFER_SIZE,
            data.length - cursor - 1
        );
        if (maxLength <= 0) {
            return Match.NONE;
        }

        int windowStart = Math.max(0, cursor - WINDOW_SIZE);
        int bestOffset = 0;
        int bestLength = 0;

        for (int candidate = windowStart; candidate < cursor; candidate++) {
            int length = 0;
            while (
                length < maxLength
                    && data[candidate + length] == data[cursor + length]
            ) {
                length++;
            }

            int offset = cursor - candidate;
            if (
                length > bestLength
                    || (
                        length == bestLength
                            && length > 0
                            && offset < bestOffset
                    )
            ) {
                bestLength = length;
                bestOffset = offset;
            }
        }

        return bestLength == 0
            ? Match.NONE
            : new Match(bestOffset, bestLength);
    }

    private static final class Match {
        private static final Match NONE = new Match(0, 0);

        private final int offset;
        private final int length;

        private Match(int offset, int length) {
            this.offset = offset;
            this.length = length;
        }
    }
}
