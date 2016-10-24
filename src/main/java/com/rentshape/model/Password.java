package com.rentshape.model;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by matt on 10/23/16.
 */
public class Password {
    private static final Random RANDOM = new SecureRandom();
    private static final int ITERATIONS = 10000;
    private static final int KEY_LENGTH = 256;



    private byte[] salt;
    private byte[] hash;

    public Password(String password){
        salt = new byte[16];
        RANDOM.nextBytes(salt);
        hash = hash(password, salt);
    }

    public Password(byte[] salt, byte[] hash){
        this.salt = salt;
        this.hash = hash;
    }

    private static byte[] hash(String password, byte[] salt){
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Error while hashing a password: " + e.getMessage(), e);
        }
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getHash() {
        return hash;
    }

    public boolean isPassword(String password){
        return Arrays.equals(hash, hash(password, salt));
    }
}
