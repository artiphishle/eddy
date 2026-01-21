package com.ankhorage.eddy;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Base64;
import com.ankhorage.eddy.encryption.CaesarCipher;
import com.ankhorage.eddy.encryption.EncryptionException;
import com.ankhorage.eddy.compression.RLECompression;
import com.ankhorage.eddy.compression.LZWCompression;
import com.ankhorage.eddy.compression.CompressionException;

public class TextEditor extends JFrame {

    private JTextArea textArea;
    private CaesarCipher caesarCipher;
    private RLECompression rleCompression;
    private LZWCompression lzwCompression;

    public TextEditor() {
        // Initialize our algorithms
        caesarCipher = new CaesarCipher();
        rleCompression = new RLECompression();
        lzwCompression = new LZWCompression();

        // Setup window
        setTitle("Java Text Editor");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Text area
        textArea = new JTextArea();
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Menu bar
        JMenuBar menuBar = new JMenuBar();
        
        // File menu
        JMenu fileMenu = createFileMenu();
        menuBar.add(fileMenu);

        // Security menu
        JMenu securityMenu = createSecurityMenu();
        menuBar.add(securityMenu);

        // Compression menu
        JMenu compressionMenu = createCompressionMenu();
        menuBar.add(compressionMenu);

        setJMenuBar(menuBar);
    }

    private JMenu createFileMenu() {
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newItem = new JMenuItem("New");
        JMenuItem openItem = new JMenuItem("Open");
        JMenuItem saveItem = new JMenuItem("Save");
        JMenuItem quitItem = new JMenuItem("Quit");

        // Add items to menu
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.addSeparator();
        fileMenu.add(quitItem);

        // Action listeners
        newItem.addActionListener(e -> textArea.setText(""));
        openItem.addActionListener(e -> handleFileOpen());
        saveItem.addActionListener(e -> handleFileSave());
        quitItem.addActionListener(e -> System.exit(0));

        return fileMenu;
    }

    private JMenu createSecurityMenu() {
        JMenu securityMenu = new JMenu("Security");
        
        // Create Encrypt submenu
        JMenu encryptMenu = new JMenu("Encrypt");
        JMenuItem caesarEncryptItem = new JMenuItem("Caesar Cipher");
        caesarEncryptItem.addActionListener(e -> handleCaesarOperation(true));
        encryptMenu.add(caesarEncryptItem);

        // Create Decrypt submenu
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
        
        JMenuItem rleCompressItem = new JMenuItem("Compress (RLE)");
        rleCompressItem.addActionListener(e -> handleRleCompression(true));
        compressionMenu.add(rleCompressItem);

        JMenuItem rleDecompressItem = new JMenuItem("Decompress (RLE)");
        rleDecompressItem.addActionListener(e -> handleRleCompression(false));
        compressionMenu.add(rleDecompressItem);

        compressionMenu.addSeparator();

        JMenuItem lzwCompressItem = new JMenuItem("Compress (LZW)");
        lzwCompressItem.addActionListener(e -> handleLzwCompression(true));
        compressionMenu.add(lzwCompressItem);

        JMenuItem lzwDecompressItem = new JMenuItem("Decompress (LZW)");
        lzwDecompressItem.addActionListener(e -> handleLzwCompression(false));
        compressionMenu.add(lzwDecompressItem);

        return compressionMenu;
    }

    private void handleCaesarOperation(boolean isEncrypt) {
        String operation = isEncrypt ? "encryption" : "decryption";
        String key = JOptionPane.showInputDialog(this,
            "Enter shift value (0-25):",
            "Caesar Cipher " + operation,
            JOptionPane.QUESTION_MESSAGE);
        
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

    private void handleRleCompression(boolean isCompress) {
        try {
            String text = getSelectedOrAllText();
            if (isCompress) {
                byte[] compressed = rleCompression.compress(text.getBytes());
                updateText(Base64.getEncoder().encodeToString(compressed));
            } else {
                byte[] decompressed = rleCompression.decompress(
                    Base64.getDecoder().decode(text));
                updateText(new String(decompressed));
            }
        } catch (CompressionException ex) {
            String operation = isCompress ? "Compression" : "Decompression";
            showError(operation + " error (RLE): " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // This catches Base64 decoding errors
            showError("Invalid RLE compressed data format");
        }
    }

    private void handleLzwCompression(boolean isCompress) {
        try {
            String text = getSelectedOrAllText();
            if (isCompress) {
                byte[] compressed = lzwCompression.compress(text.getBytes());
                updateText(Base64.getEncoder().encodeToString(compressed));
            } else {
                byte[] decompressed = lzwCompression.decompress(
                    Base64.getDecoder().decode(text));
                updateText(new String(decompressed));
            }
        } catch (CompressionException ex) {
            String operation = isCompress ? "Compression" : "Decompression";
            showError(operation + " error (LZW): " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // This catches Base64 decoding errors
            showError("Invalid LZW compressed data format");
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
