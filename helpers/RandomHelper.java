package com.nals.rw360.helpers;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public final class RandomHelper {
    private static final int DEF_COUNT = 20;

    private static final SecureRandom SECURE_RANDOM;

    static {
        SECURE_RANDOM = new SecureRandom();
        SECURE_RANDOM.nextBytes(new byte[64]);
    }

    private RandomHelper() {
    }

    /**
     * Generate a random string.
     *
     * @return the generated random string with SecureRandom
     */
    public static String generateRandomAlphanumericString(final Integer count) {
        return RandomStringUtils.random(count, 0, 0, true, true, null, SECURE_RANDOM);
    }

    /**
     * Generate a random string.
     *
     * @return the generated random string with SecureRandom
     */
    public static String generateRandomAlphanumericString() {
        return generateRandomAlphanumericString(DEF_COUNT);
    }

    /**
     * Generate a password.
     *
     * @return the generated password
     */
    public static String generatePassword() {
        return generateRandomAlphanumericString();
    }

    /**
     * Generate an activation key.
     *
     * @return the generated activation key
     */
    public static String generateActivationKey() {
        return generateRandomAlphanumericString();
    }

    /**
     * Generate a reset key.
     *
     * @return the generated reset key
     */
    public static String generateResetKey() {
        return generateRandomAlphanumericString();
    }
}
