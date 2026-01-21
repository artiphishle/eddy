package com.ankhorage.eddy.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class HuffmanCompression implements CompressionAlgorithm {

    private static final int ALGORITHM_YEAR = 1951;

    @Override
    public byte[] compress(byte[] data) throws CompressionException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        Map<Byte, Integer> frequencyTable = buildFrequencyTable(data);
        Node root = buildHuffmanTree(frequencyTable);
        Map<Byte, String> huffmanCodes = generateHuffmanCodes(root);

        BitStream bitStream = new BitStream();
        for (byte b : data) {
            bitStream.write(huffmanCodes.get(b));
        }

        try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
             ObjectOutputStream objOut = new ObjectOutputStream(byteOut)) {
            objOut.writeObject(frequencyTable);
            objOut.writeInt(bitStream.getBitCount());
            objOut.write(bitStream.toByteArray());
            return byteOut.toByteArray();
        } catch (IOException e) {
            throw new CompressionException("Error during Huffman compression", e);
        }
    }

    @Override
    public byte[] decompress(byte[] data) throws CompressionException {
        if (data == null || data.length == 0) {
            return new byte[0];
        }

        try (ByteArrayInputStream byteIn = new ByteArrayInputStream(data);
             ObjectInputStream objIn = new ObjectInputStream(byteIn)) {

            @SuppressWarnings("unchecked")
            Map<Byte, Integer> frequencyTable = (Map<Byte, Integer>) objIn.readObject();
            int bitCount = objIn.readInt();

            byte[] compressedData = new byte[byteIn.available()];
            objIn.readFully(compressedData);

            Node root = buildHuffmanTree(frequencyTable);
            return decode(root, bitCount, compressedData);
        } catch (IOException | ClassNotFoundException e) {
            throw new CompressionException("Error during Huffman decompression", e);
        }
    }

    @Override
    public String getAlgorithmName() {
        return "Huffman Coding";
    }

    public int getAlgorithmYear() {
        return ALGORITHM_YEAR;
    }

    private Map<Byte, Integer> buildFrequencyTable(byte[] data) {
        Map<Byte, Integer> frequencyTable = new HashMap<>();
        for (byte b : data) {
            frequencyTable.put(b, frequencyTable.getOrDefault(b, 0) + 1);
        }
        return frequencyTable;
    }

    private Node buildHuffmanTree(Map<Byte, Integer> frequencyTable) {
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>(
            Comparator.comparingInt((Node n) -> n.frequency)
                      .thenComparing(n -> n.data)
        );
        for (Map.Entry<Byte, Integer> entry : frequencyTable.entrySet()) {
            priorityQueue.add(new Node(entry.getKey(), entry.getValue()));
        }

        while (priorityQueue.size() > 1) {
            Node left = priorityQueue.poll();
            Node right = priorityQueue.poll();
            Node parent = new Node(null, left.frequency + right.frequency);
            parent.left = left;
            parent.right = right;
            priorityQueue.add(parent);
        }
        return priorityQueue.poll();
    }

    private Map<Byte, String> generateHuffmanCodes(Node root) {
        Map<Byte, String> huffmanCodes = new HashMap<>();
        generateCodesRecursive(root, "", huffmanCodes);
        return huffmanCodes;
    }

    private void generateCodesRecursive(Node node, String code, Map<Byte, String> huffmanCodes) {
        if (node == null) {
            return;
        }
        if (node.isLeaf()) {
            huffmanCodes.put(node.data, code);
        }
        generateCodesRecursive(node.left, code + "0", huffmanCodes);
        generateCodesRecursive(node.right, code + "1", huffmanCodes);
    }

    private byte[] decode(Node root, int bitCount, byte[] compressedData) {
        ByteArrayOutputStream decodedBytes = new ByteArrayOutputStream();
        Node current = root;
        int bitsProcessed = 0;

        for (byte b : compressedData) {
            for (int i = 7; i >= 0 && bitsProcessed < bitCount; i--) {
                int bit = (b >> i) & 1;
                current = (bit == 0) ? current.left : current.right;
                if (current.isLeaf()) {
                    decodedBytes.write(current.data);
                    current = root;
                }
                bitsProcessed++;
            }
        }
        return decodedBytes.toByteArray();
    }

    private static class Node {
        Byte data;
        int frequency;
        Node left;
        Node right;

        Node(Byte data, int frequency) {
            this.data = data;
            this.frequency = frequency;
        }

        boolean isLeaf() {
            return left == null && right == null;
        }
    }

    private static class BitStream {
        private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        private int bitCount = 0;
        private int currentByte = 0;

        void write(String bits) {
            for (char bit : bits.toCharArray()) {
                write(bit == '1' ? 1 : 0);
            }
        }

        void write(int bit) {
            currentByte = (currentByte << 1) | bit;
            bitCount++;
            if (bitCount % 8 == 0) {
                byteStream.write(currentByte);
                currentByte = 0;
            }
        }

        byte[] toByteArray() {
            if (bitCount % 8 != 0) {
                currentByte <<= (8 - (bitCount % 8));
                byteStream.write(currentByte);
            }
            return byteStream.toByteArray();
        }

        int getBitCount() {
            return bitCount;
        }
    }
}