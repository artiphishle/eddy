package com.ankhorage.eddy.compression;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;

class CompressionAlgorithmsTest {

    @Test
    void everyCompressionAlgorithmSupportsCompressionAndDecompression()
        throws CompressionException {
        byte[] original = "AAAAABBBBCCCCCDDDDD-eddy-round-trip".getBytes(UTF_8);
        List<CompressionAlgorithm> algorithms = List.of(
            new HuffmanCompression(),
            new RLECompression(),
            new LZ77Compression(),
            new LZWCompression()
        );

        for (CompressionAlgorithm algorithm : algorithms) {
            assertArrayEquals(
                original,
                algorithm.decompress(algorithm.compress(original)),
                algorithm.getAlgorithmName() + " must support compress -> decompress"
            );
        }
    }

    @Test
    void huffmanRoundTripsSingleSymbolAndEveryByteValue()
        throws CompressionException {
        HuffmanCompression huffman = new HuffmanCompression();
        byte[] repeated = "A".repeat(512).getBytes(UTF_8);
        byte[] everyByte = new byte[256];
        for (int index = 0; index < everyByte.length; index++) {
            everyByte[index] = (byte) index;
        }

        assertArrayEquals(
            repeated,
            huffman.decompress(huffman.compress(repeated))
        );
        assertArrayEquals(
            everyByte,
            huffman.decompress(huffman.compress(everyByte))
        );
    }

    @Test
    void huffmanRejectsMalformedPayloads() {
        HuffmanCompression huffman = new HuffmanCompression();

        assertThrows(
            CompressionException.class,
            () -> huffman.decompress(new byte[] {1, 2, 3})
        );
    }

    @Test
    void lz77RoundTripsOverlappingMatchesAndEveryByteValue()
        throws CompressionException {
        LZ77Compression lz77 = new LZ77Compression();
        byte[] overlapping = "A".repeat(1024).getBytes(UTF_8);
        byte[] everyByte = new byte[256];
        for (int index = 0; index < everyByte.length; index++) {
            everyByte[index] = (byte) index;
        }

        assertArrayEquals(
            overlapping,
            lz77.decompress(lz77.compress(overlapping))
        );
        assertArrayEquals(
            everyByte,
            lz77.decompress(lz77.compress(everyByte))
        );
    }

    @Test
    void lz77RejectsMalformedBackReferences() {
        LZ77Compression lz77 = new LZ77Compression();

        assertThrows(
            CompressionException.class,
            () -> lz77.decompress(new byte[] {0, 1, 1, 65})
        );
        assertThrows(
            CompressionException.class,
            () -> lz77.decompress(new byte[] {0, 0, 0})
        );
    }
}
