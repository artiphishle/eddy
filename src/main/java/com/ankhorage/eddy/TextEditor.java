package com.ankhorage.eddy;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import com.ankhorage.eddy.encryption.CaesarCipher;
import com.ankhorage.eddy.encryption.EncryptionException;
import com.ankhorage.eddy.compression.CompressionAlgorithm;
import com.ankhorage.eddy.compression.CompressionException;
import com.ankhorage.eddy.compression.HuffmanCompression;
import com.ankhorage.eddy.compression.LZ77Compression;
import com.ankhorage.eddy.compression.LZWCompression;
import com.ankhorage.eddy.compression.RLECompression;

public class TextEditor extends JFrame {

    private JTextArea textArea;
    private CaesarCipher caesarCipher;
    private HuffmanCompression huffmanCompression;
    private RLECompression rleCompression;
    private LZ77Compression lz77Compression;
    private LZWCompression lzwCompression;

    public TextEditor() {
        caesarCipher = new CaesarCipher();
        huffmanCompression = new HuffmanCompression();
        rleCompression = new RLECompression();
        lz77Compression = new LZ77Compression();
        lzwCompression = new LZWCompression();

        setTitle("Java Text Editor");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        menuBar.add(createFileMenu());
        menuBar.add(createSecurityMenu());
        menuBar.add(createCompressionMenu());
        setJMenuBar(menuBar);
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");

        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem quitItem = new JMenuItem("Quit");

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);

        newItem.addActionListener(e -> textArea.setText(""));
        openItem.addActionListener(e -> handleFileOpen());
        saveItem.addActionListener(e -> handleFileSave());
        quitItem.addActionListener(e -> System.exit(0));

        return fileMenu;
    }

    private JMenu createSecurityMenu() {
        JMenu securityMenu = new JMenu("Security");

        JMenu encryptMenu = new JMenu("Encrypt");
        JMenuItem caesarEncryptItem = new JMenuItem("Caesar Cipher");
        caesarEncryptItem.addActionListener(e -> handleCaesarOperation(true));
        encryptMenu.add(caesarEncryptItem);

        JMenu decryptMenu = new JMenu("Decrypt");
        JMenuItem caesarDecryptItem = new JMenuItem("Caesar Cipher");
        caesarDecryptItem.addActionListener(e -> handleCaesarOperation(false));
        decryptMenu.add(caesarDecryptItem);

        securityMenu.add(encryptMenu);
        securityMenu.add(decryptMenu);

        return securityMenu;
    }

    private JMenu createCompressionMenu() {
        JMenu compressionMenu = new JMenu("Compression");

        addCompressionActions(compressionMenu, huffmanCompression, "Huffman");
        compressionMenu.addSeparator();
        addCompressionActions(compressionMenu, rleCompression, "RLE");
        compressionMenu.addSeparator();
        addCompressionActions(compressionMenu, lz77Compression, "LZ77");
        compressionMenu.addSeparator();
        addCompressionActions(compressionMenu, lzwCompression, "LZW");

        return compressionMenu;
    }

    private void addCompressionActions(
        JMenu menu,
        CompressionAlgorithm algorithm,
        String label
    ) {
        JMenuItem compressItem = new JMenuItem("Compress (" + label + ")");
        compressItem.addActionListener(e -> handleCompression(algorithm, label, true));
        menu.add(compressItem);

        JMenuItem decompressItem = new JMenuItem("Decompress (" + label + ")");
        decompressItem.addActionListener(e -> handleCompression(algorithm, label, false));
        menu.add(decompressItem);
    }

    private void handleCaesarOperation(boolean isEncrypt) {
        String operation = isEncrypt ? "encryption" : "decryption";
        String key = JOptionPane.showInputDialog(
            this,
            "Enter shift value (0-25):",
            "Caesar Cipher " + operation,
            JOptionPane.QUESTION_MESSAGE
        );

        if (key != null) {
            try {
                String text = getSelectedOrAllText();
                String result = isEncrypt
                    ? caesarCipher.encrypt(text, key)
                    : caesarCipher.decrypt(text, key);
                updateText(result);
            } catch (EncryptionException ex) {
                showError(operation + " error: " + ex.getMessage());
            }
        }
    }

    private void handleCompression(
        CompressionAlgorithm algorithm,
        String label,
        boolean isCompress
    ) {
        try {
            String text = getSelectedOrAllText();
            if (isCompress) {
                byte[] compressed = algorithm.compress(
                    text.getBytes(StandardCharsets.UTF_8)
                );
                updateText(Base64.getEncoder().encodeToString(compressed));
            } else {
                byte[] compressed = Base64.getDecoder().decode(text);
                byte[] decompressed = algorithm.decompress(compressed);
                updateText(new String(decompressed, StandardCharsets.UTF_8));
            }
        } catch (CompressionException ex) {
            String operation = isCompress ? "Compression" : "Decompression";
            showError(operation + " error (" + label + "): " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            showError("Invalid " + label + " compressed data format");
        }
    }

    private String getSelectedOrAllText() {
        String selectedText = textArea.getSelectedText();
        return selectedText != null ? selectedText : textArea.getText();
    }

    private void updateText(String newText) {
        if (textArea.getSelectedText() != null) {
            textArea.replaceSelection(newText);
        } else {
            textArea.setText(newText);
        }
    }

    private void handleFileOpen() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                BufferedReader reader = new BufferedReader(new FileReader(file));
                textArea.read(reader, null);
                reader.close();
            } catch (IOException ex) {
                showError("Error opening file: " + ex.getMessage());
            }
        }
    }

    private void handleFileSave() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fileChooser.getSelectedFile();
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                textArea.write(writer);
                writer.close();
            } catch (IOException ex) {
                showError("Error saving file: " + ex.getMessage());
            }
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TextEditor().setVisible(true));
    }
}
