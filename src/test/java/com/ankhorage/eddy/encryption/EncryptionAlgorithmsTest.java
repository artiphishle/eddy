package com.ankhorage.eddy.encryption;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class EncryptionAlgorithmsTest {

    @Test
    void everyEncryptionAlgorithmRoundTrips() throws EncryptionException {
        String original = "Eddy keeps encryption reversible: Hello, World! 123";
        String key = "7";
        List<EncryptionAlgorithm> algorithms = List.of(new CaesarCipher());

        for (EncryptionAlgorithm algorithm : algorithms) {
            String encrypted = algorithm.encrypt(original, key);
            String decrypted = algorithm.decrypt(encrypted, key);

            assertEquals(
                original,
                decrypted,
                algorithm.getAlgorithmName() + " must support encrypt -> decrypt"
            );
        }
    }
}
