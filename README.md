# Eddy

Eddy is a Java-based text editor that is evolving into a full coding IDE.
Beyond editing, Eddy is designed as a chronological journey through the history of computer science, allowing users to encrypt/decrypt and compress/decompress data using algorithms from different eras.

The goal is to support all historically relevant algorithms, ordered by their year of invention — and maybe even invent new ones in the future ;)

## Compression

### RLE (1967)

Run-Length Encoding (RLE) is a form of lossless data compression in which consecutive occurrences of the same value are stored as a single value and a count, rather than as the original sequence.

For example, the sequence:

green green green green green green green green green

can be encoded as:

green x 9

RLE is most efficient on data that contains many repeated values, such as simple graphics, icons, line drawings, or animations.
On data without long runs, RLE may actually increase the file size.

### LZW (1984)

LZW (Lempel–Ziv–Welch) is a lossless dictionary-based compression algorithm introduced in 1984 by Terry A. Welch.
It is an improvement over earlier Lempel–Ziv methods and became widely known through its use in GIF image files.

Unlike some earlier compression techniques, the dictionary does not need to be transmitted.
Both the compressor and decompressor build the dictionary dynamically in the same way while processing the data.

How it works (simplified):

1. Initialize a dictionary with all possible single-character symbols.
2. Read the longest sequence already present in the dictionary.
3. Output the dictionary index of that sequence.
4. Add a new sequence (existing sequence + next character) to the dictionary.
5. Repeat until the input is fully processed.

LZW is particularly effective on data with repeated patterns and serves as a foundational algorithm for many modern compression formats.

## Encryption

### Caesar Cipher (100 BCE)

In cryptography, the Caesar cipher (also known as Caesar’s shift cipher) is one of the simplest and earliest known encryption techniques.

It is a substitution cipher in which each letter in the plaintext is replaced by a letter a fixed number of positions down the alphabet.
For example, with a shift of 3:

A -> D
B -> E
C -> F

The cipher is named after Julius Caesar, who reportedly used it in his private correspondence.

While trivially breakable by modern standards, the Caesar cipher is historically important and serves as an excellent introduction to the concept of encryption.

## Vision

Eddy aims to become an educational and practical editor where algorithms are not just used, but understood in historical context — showing how ideas evolved from simple substitutions and run-lengths to modern cryptography and compression.

