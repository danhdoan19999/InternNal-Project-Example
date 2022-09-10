package com.nals.rw360.helpers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class KeyPairHelper {
    public static final String ALGORITHM = "RSA";

    private KeyPairHelper() {
    }

    public static KeyPair generateKeyPair()
        throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(ALGORITHM);
        keyPairGenerator.initialize(2048);
        return keyPairGenerator.genKeyPair();
    }

    public static KeyPair generateAndStorageKeyPair(final Path privatePath, final Path publicPath)
        throws NoSuchAlgorithmException, IOException {

        KeyPair keyPair = generateKeyPair();

        Files.write(privatePath, keyPair.getPrivate().getEncoded());
        Files.write(publicPath, keyPair.getPublic().getEncoded());

        return keyPair;
    }

    public static PublicKey loadPublicKey(final Path path)
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Files.readAllBytes(path);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance(ALGORITHM).generatePublic(keySpec);
    }

    public static PrivateKey loadPrivateKey(final Path path)
        throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] bytes = Files.readAllBytes(path);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance(ALGORITHM).generatePrivate(keySpec);
    }
}
