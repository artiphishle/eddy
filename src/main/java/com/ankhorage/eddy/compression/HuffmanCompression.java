package com.ankhorage.eddy.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Comparator;
import java.util.PriorityQueue;

public class HuffmanCompression implements CompressionAlgorithm {
    private static final int MAGIC = 0x48554631;
    private static final int ALGORITHM_YEAR = 1951;

    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }

        int[] frequencies = buildFrequencyTable(data);
        Node root = buildTree(frequencies);
        String[] codes = new String[256];
        buildCodes(root, "", codes);

        BitWriter bitWriter = new BitWriter();
        for (byte value : data) {
            bitWriter.write(codes[value & 0xFF]);
        }

        try (ByteArrayOutputStream bytes = new ByteArrayOutputStream();
             DataOutputStream output = new DataOutputStream(bytes)) {
            output.writeInt(MAGIC);
            output.writeInt(data.length);
            output.writeShort(countSymbols(frequencies));
            for (int symbol = 0; symbol < frequencies.length; symbol++) {
                if (frequencies[symbol] > 0) {
                    output.writeByte(symbol);
                    output.writeInt(frequencies[symbol]);
                }
            }
            output.writeInt(bitWriter.getBitCount());
            output.write(bitWriter.toByteArray());
            output.flush();
            return bytes.toByteArray();
        } catch (IOException exception) {
            throw new CompressionException("Error during Huffman compression", exception);
        }
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressionException {
        if (data == null) {
            throw new CompressionException("Input data cannot be null");
        }
        if (data.length == 0) {
            return new byte[0];
        }

        try (ByteArrayInputStream bytes = new ByteArrayInputStream(data);
             DataInputStream input = new DataInputStream(bytes)) {
            if (input.readInt() != MAGIC) {
                throw new CompressionException("Invalid Huffman data header");
            }

            int originalLength = input.readInt();
            if (originalLength <= 0) {
                throw new CompressionException("Invalid Huffman original length");
            }

            int symbolCount = input.readUnsignedShort();
            if (symbolCount < 1 || symbolCount > 256) {
                throw new CompressionException("Invalid Huffman symbol count");
            }

            int[] frequencies = new int[256];
            long frequencyTotal = 0;
            for (int index = 0; index < symbolCount; index++) {
                int symbol = input.readUnsignedByte();
                int frequency = input.readInt();
                if (frequency <= 0 || frequencies[symbol] != 0) {
                    throw new CompressionException("Invalid Huffman frequency table");
                }
                frequencies[symbol] = frequency;
                frequencyTotal += frequency;
            }
            if (frequencyTotal != originalLength) {
                throw new CompressionException("Huffman frequency table does not match original length");
            }

            int bitCount = input.readInt();
            if (bitCount < 0) {
                throw new CompressionException("Invalid Huffman bit count");
            }
            int payloadLength = (bitCount + 7) / 8;
            if (bytes.available() != payloadLength) {
                throw new CompressionException("Invalid Huffman payload length");
            }

            byte[] payload = new byte[payloadLength];
            input.readFully(payload);
            return decode(buildTree(frequencies), originalLength, bitCount, payload);
        } catch (EOFException exception) {
            throw new CompressionException("Truncated Huffman data", exception);
        } catch (IOException exception) {
            throw new CompressionException("Error during Huffman decompression", exception);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "Huffman Coding (1951)";
    }

    public int getAlgorithmYear() {
        return ALGORITHM_YEAR;
    }

    private int[] buildFrequencyTable(byte[] data) {
        int[] frequencies = new int[256];
        for (byte value : data) {
            frequencies[value & 0xFF]++;
        }
        return frequencies;
    }

    private int countSymbols(int[] frequencies) {
        int count = 0;
        for (int frequency : frequencies) {
            if (frequency > 0) {
                count++;
            }
        }
        return count;
    }

    private Node buildTree(int[] frequencies) throws CompressionException {
        PriorityQueue<Node> nodes = new PriorityQueue<>(
            Comparator.comparingInt((Node node) -> node.frequency)
                .thenComparingInt(node -> node.minimumSymbol)
        );

        for (int symbol = 0; symbol < frequencies.length; symbol++) {
            if (frequencies[symbol] > 0) {
                nodes.add(Node.leaf(symbol, frequencies[symbol]));
            }
        }
        if (nodes.isEmpty()) {
            throw new CompressionException("Huffman frequency table is empty");
        }

        while (nodes.size() > 1) {
            Node left = nodes.remove();
            Node right = nodes.remove();
            nodes.add(Node.parent(left, right));
        }
        return nodes.remove();
    }

    private void buildCodes(Node node, String prefix, String[] codes) {
        if (node.isLeaf()) {
            codes[node.symbol] = prefix.isEmpty() ? "0" : prefix;
            return;
        }
        buildCodes(node.left, prefix + '0', codes);
        buildCodes(node.right, prefix + '1', codes);
    }

    private byte[] decode(Node root, int originalLength, int bitCount, byte[] payload)
        throws CompressionException {
        if (root.isLeaf()) {
            if (bitCount != originalLength) {
                throw new CompressionException("Invalid single-symbol Huffman bit count");
            }
            for (int bitIndex = 0; bitIndex < bitCount; bitIndex++) {
                if (readBit(payload, bitIndex) != 0) {
                    throw new CompressionException("Invalid single-symbol Huffman payload");
                }
            }
            byte[] result = new byte[originalLength];
            for (int index = 0; index < result.length; index++) {
                result[index] = (byte) root.symbol;
            }
            return result;
        }

        ByteArrayOutputStream decoded = new ByteArrayOutputStream(originalLength);
        Node current = root;
        for (int bitIndex = 0; bitIndex < bitCount; bitIndex++) {
            current = readBit(payload, bitIndex) == 0 ? current.left : current.right;
            if (current == null) {
                throw new CompressionException("Invalid Huffman bitstream");
            }
            if (current.isLeaf()) {
                decoded.write(current.symbol);
                if (decoded.size() > originalLength) {
                    throw new CompressionException("Huffman data expands beyond original length");
                }
                current = root;
            }
        }

        if (current != root || decoded.size() != originalLength) {
            throw new CompressionException("Huffman bitstream does not reconstruct the original length");
        }
        return decoded.toByteArray();
    }

    private int readBit(byte[] payload, int bitIndex) {
        int value = payload[bitIndex / 8] & 0xFF;
        return (value >> (7 - (bitIndex % 8))) & 1;
    }

    private static final class Node {
        private final int symbol;
        private final int frequency;
        private final int minimumSymbol;
        private final Node left;
        private final Node right;

        private Node(int symbol, int frequency, int minimumSymbol, Node left, Node right) {
            this.symbol = symbol;
            this.frequency = frequency;
            this.minimumSymbol = minimumSymbol;
            this.left = left;
            this.right = right;
        }

        private static Node leaf(int symbol, int frequency) {
            return new Node(symbol, frequency, symbol, null, null);
        }

        private static Node parent(Node left, Node right) {
            return new Node(
                -1,
                left.frequency + right.frequency,
                Math.min(left.minimumSymbol, right.minimumSymbol),
                left,
                right
            );
        }

        private boolean isLeaf() {
            return symbol >= 0;
        }
    }

    private static final class BitWriter {
        private final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        private int currentByte;
        private int bitsInCurrentByte;
        private int bitCount;

        private void write(String bits) {
            for (int index = 0; index < bits.length(); index++) {
                currentByte = (currentByte << 1) | (bits.charAt(index) == '1' ? 1 : 0);
                bitsInCurrentByte++;
                bitCount++;
                if (bitsInCurrentByte == 8) {
                    bytes.write(currentByte);
                    currentByte = 0;
                    bitsInCurrentByte = 0;
                }
            }
        }

        private byte[] toByteArray() {
            if (bitsInCurrentByte > 0) {
                bytes.write(currentByte << (8 - bitsInCurrentByte));
                bitsInCurrentByte = 0;
                currentByte = 0;
            }
            return bytes.toByteArray();
        }

        private int getBitCount() {
            return bitCount;
        }
    }
}
