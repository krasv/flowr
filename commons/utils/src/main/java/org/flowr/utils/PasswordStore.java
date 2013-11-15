package org.flowr.utils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * Password encryption and decryption utility
 * 
 * @author SvenKrause
 */
@SuppressWarnings("restriction")
public class PasswordStore {

    private sun.misc.BASE64Encoder base64encoder;

    private sun.misc.BASE64Decoder base64decoder;

    private SecretKey key;

    /**
     * generates the necessary key based on the given Phrase.
     * 
     * @param keyPhrase
     *            string with at least 8 characters
     * @throws InvalidKeyException
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeySpecException
     */
    public PasswordStore(String keyPhrase) throws InvalidKeyException, UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {
        DESKeySpec keySpec = new DESKeySpec(keyPhrase.getBytes("UTF8"));
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        key = keyFactory.generateSecret(keySpec);
        base64encoder = new BASE64Encoder();
        base64decoder = new BASE64Decoder();
    }

    /**
     * encrypts the given plain password using the stores key phrase
     * @param plainTextPassword
     * @return the encrypted password string
     * @throws UnsupportedEncodingException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public String encrypt(String plainTextPassword) throws UnsupportedEncodingException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        // ENCODE plainTextPassword String
        byte[] cleartext = plainTextPassword.getBytes("UTF8");

        Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
        cipher.init(Cipher.ENCRYPT_MODE, key);
        String encrypedPwd = base64encoder.encode(cipher.doFinal(cleartext));

        return encrypedPwd;
    }

    /**
     * decrypts the given string using the stores key phrase
     * @param encryptedPwd
     * @return
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     */
    public String decryptPassword(String encryptedPwd) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        // DECODE encryptedPwd String
        byte[] encrypedPwdBytes = base64decoder.decodeBuffer(encryptedPwd);

        Cipher cipher = Cipher.getInstance("DES");// cipher is not thread safe
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));

        return new String(plainTextPwdBytes);
    }

    public static void main(String[] args) throws Exception {
        final String usageString = "usage: \n\t-encrypt <key phrase> <real password 1> <real password 2> ... <real password n> \n\tor \n\t-decrypt <key phrase> <encrypted password>\n\n\tPlease note, that the key phrase needs to be at least 8 chars long.";
        if (args.length < 3) {
            System.err.println(usageString);
            System.exit(-1);
        }
        PasswordStore passwordStore = new PasswordStore(args[1]);
        if ("-encrypt".equals(args[0])) {
            for (int i = 2; i < args.length; i++) {
                String arg = args[i];
                System.out.println(arg + ": {" + passwordStore.encrypt(args[i]) + "}");
            }
        } else if ("-decrypt".equals(args[0])) {
            System.out.println(passwordStore.decryptPassword(args[2]));
        } else {
            System.err.println(usageString);
            System.exit(-1);
        }
    }
}
