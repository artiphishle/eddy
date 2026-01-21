# Eddy

A Java text editor program that will become a coding IDE among others. It can be used to encrypt/decrypt & compress/decompress using all the algorithms existing, so we will travel through time to support all the algorithms invented, and maybe invent new ones later ;)

## Compression

### Huffman Coding (1951)

Huffman coding is a lossless data compression algorithm. The idea is to assign variable-length codes to input characters, lengths of the assigned codes are based on the frequencies of corresponding characters. The most frequent character gets the smallest code and the least frequent character gets the largest code.

### RLE (1967)

Run-length encoding (RLE) is a form of lossless data compression in which runs of data (consecutive occurrences of the same data value) are stored as a single occurrence of that data value and a count of its consecutive occurrences, rather than as the original run. As an imaginary example of the concept, when encoding an image built up from colored dots, the sequence "green green green green green green green green green" is shortened to "green x 9". This is most efficient on data that contains many such runs, for example, simple graphic images such as icons, line drawings, games, and animations. For files that do not have many runs, encoding them with RLE could increase the file size.

## Encryption

### Caesar Cipher (100 BCE)

In cryptography, a Caesar cipher, also known as Caesar's cipher, the shift cipher, Caesar's code, or Caesar shift, is one of the simplest and most widely known encryption techniques. It is a type of substitution cipher in which each letter in the plaintext is replaced by a letter some fixed number of positions down the alphabet. For example, with a left shift of 3, D would be replaced by A, E would become B, and so on.[1] The method is named after Julius Caesar, who used it in his private correspondence.
