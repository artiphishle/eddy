package com.ankhorage.eddy.compression;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class CompressionAlgorithmsTest {

    @Test
    void everyCompressionAlgorithmRoundTrips() throws CompressionException {
        byte[] original = "AAAAABBBBCCCCCDDDDD-eddy-round-trip".getBytes(UTF_8);
        List<CompressionAlgorithm> algorithms = List.of(
            new RLECompression(),
            new LZWCompression()
        );

        for (CompressionAlgorithm algorithm : algorithms) {
            byte[] compressed = algorithm.compress(original);
            byte[] decompressed = algorithm.decompress(compressed);

            assertArrayEquals(
                original,
                decompressed,
                algorithm.getAlgorithmName() + " must support compress -> decompress"
            );
        }
    }
}
