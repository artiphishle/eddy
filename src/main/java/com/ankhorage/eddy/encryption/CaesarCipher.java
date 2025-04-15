package com.ankhorage.eddy.encryption;

public class CaesarCipher implements EncryptionAlgorithm {
    @Override
    public String encrypt(String text, String key) throws EncryptionException {
        try {
            int shift = Integer.parseInt(key);
            StringBuilder result = new StringBuilder();
            
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (Character.isLetter(c)) {
                    // Handle basic Latin letters (a-z, A-Z)
                    if (c <= 'z' && c >= 'a' || c <= 'Z' && c >= 'A') {
                        char base = Character.isUpperCase(c) ? 'A' : 'a';
                        result.append((char) (((c - base + shift) % 26) + base));
                    } else {
                        // Handle extended Unicode letters
                        // We'll use Unicode blocks for common extended Latin characters
                        if (isLatinExtendedA(c)) {
                            result.append(shiftLatinExtendedA(c, shift));
                        } else if (isLatinExtendedB(c)) {
                            result.append(shiftLatinExtendedB(c, shift));
                        } else {
                            // For other Unicode letters, use modulo with their block size
                            int blockStart = getUnicodeBlockStart(c);
                            int blockSize = getUnicodeBlockSize(c);
                            if (blockSize > 0) {
                                result.append((char) (blockStart + ((c - blockStart + shift) % blockSize)));
                            } else {
                                // If we can't determine the block, preserve the character
                                result.append(c);
                            }
                        }
                    }
                } else {
                    // Non-letter characters remain unchanged
                    result.append(c);
                }
            }
            return result.toString();
        } catch (NumberFormatException e) {
            throw new EncryptionException("Caesar cipher key must be a number");
        }
    }

    @Override
    public String decrypt(String text, String key) throws EncryptionException {
        try {
            int shift = Integer.parseInt(key);
            // For decryption, we use the inverse shift
            return encrypt(text, String.valueOf(26 - (shift % 26)));
        } catch (NumberFormatException e) {
            throw new EncryptionException("Caesar cipher key must be a number");
        }
    }

    // Helper methods for Unicode blocks
    private boolean isLatinExtendedA(char c) {
        return c >= '\u0100' && c <= '\u017F';  // Latin Extended-A block
    }

    private boolean isLatinExtendedB(char c) {
        return c >= '\u0180' && c <= '\u024F';  // Latin Extended-B block
    }

    private char shiftLatinExtendedA(char c, int shift) {
        int blockSize = 0x80; // Size of Latin Extended-A block
        int blockStart = 0x0100; // Start of Latin Extended-A block
        return (char) (blockStart + ((c - blockStart + shift) % blockSize));
    }

    private char shiftLatinExtendedB(char c, int shift) {
        int blockSize = 0xD0; // Size of Latin Extended-B block
        int blockStart = 0x0180; // Start of Latin Extended-B block
        return (char) (blockStart + ((c - blockStart + shift) % blockSize));
    }

    private int getUnicodeBlockStart(char c) {
        // Define starts of common Unicode blocks
        if (c >= '\u0400' && c <= '\u04FF') return 0x0400;  // Cyrillic
        if (c >= '\u0370' && c <= '\u03FF') return 0x0370;  // Greek
        if (c >= '\u0600' && c <= '\u06FF') return 0x0600;  // Arabic
        // Add more blocks as needed
        return 0;
    }

    private int getUnicodeBlockSize(char c) {
        // Define sizes of common Unicode blocks
        if (c >= '\u0400' && c <= '\u04FF') return 0x100;  // Cyrillic
        if (c >= '\u0370' && c <= '\u03FF') return 0x90;   // Greek
        if (c >= '\u0600' && c <= '\u06FF') return 0x100;  // Arabic
        // Add more blocks as needed
        return 0;
    }

    @Override
    public String getAlgorithmName() {
        return "Caesar Cipher";
    }

    @Override
    public String getHistoricalContext() {
        return "The Caesar cipher is one of the earliest known and simplest ciphers. " +
               "It was named after Julius Caesar, who used it for secret communication " +
               "around 50 BCE. It is a substitution cipher that shifts each letter in " +
               "the plaintext by a fixed number of positions in the alphabet.";
    }
} 