package io.darkblock.darkblock.app.tools;

import android.util.Pair;

import com.github.kevinsawicki.http.HttpRequest;

import java.nio.charset.StandardCharsets;
import java.security.DigestException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionTools {

    /**
     * returns [private,public]
     * @return
     */
    public static String[] generateRSAKeys(){
        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.initialize(4096);
        KeyPair pair = keyGen.generateKeyPair();
        PrivateKey privateKey = pair.getPrivate();
        PublicKey publicKey = pair.getPublic();
        return new String[]{ new String(Base64.getEncoder().encode(privateKey.getEncoded())), new String(Base64.getEncoder().encode(publicKey.getEncoded())) };
    }

    public static String generateAESKey(){
        //from https://howtodoinjava.com/java/java-security/aes-256-encryption-decryption/
        try{
            String SECRET_KEY = UUID.randomUUID().toString();
            String SALT = UUID.randomUUID().toString();
//		    byte[] iv = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//		    IvParameterSpec ivspec = new IvParameterSpec(iv);
            System.err.println( "generating AES Key" );
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(SECRET_KEY.toCharArray(), SALT.getBytes(), 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKeySpec secretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
            return new String(Base64.getEncoder().encode( secretKey.getEncoded() ));
        }
        catch( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[][] GenerateKeyAndIV(int keyLength, int ivLength, int iterations, byte[] salt, byte[] password, MessageDigest md) {
        int digestLength = md.getDigestLength();
        int requiredLength = (keyLength + ivLength + digestLength - 1) / digestLength * digestLength;
        byte[] generatedData = new byte[requiredLength];
        int generatedLength = 0;
        try {
            md.reset();
            // Repeat process until sufficient data has been generated
            while (generatedLength < keyLength + ivLength) {
                // Digest data (last digest if available, password data, salt if available)
                if (generatedLength > 0)
                    md.update(generatedData, generatedLength - digestLength, digestLength);
                md.update(password);
                if (salt != null)
                    md.update(salt, 0, 8);
                md.digest(generatedData, generatedLength, digestLength);
                // additional rounds
                for (int i = 1; i < iterations; i++) {
                    md.update(generatedData, generatedLength, digestLength);
                    md.digest(generatedData, generatedLength, digestLength);
                }
                generatedLength += digestLength;
            }
            // Copy key and IV into separate byte arrays
            byte[][] result = new byte[2][];
            result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
            if (ivLength > 0)
                result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);
            return result;
        } catch (DigestException e) {
            throw new RuntimeException(e);
        } finally {
            // Clean out temporary data
            Arrays.fill(generatedData, (byte)0);
        }
    }

    public static String byteArrayToHexString(byte[] b) {
        String result = "";
        for (int i=0; i < b.length; i++) {
            result +=
                    Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    public static byte[] aesDecrypt( String aeskey, String cipherText ) {
        try{

            byte[] cipherData = Base64.getDecoder().decode(cipherText);
            byte[] saltData = Arrays.copyOfRange(cipherData, 8, 16);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            final byte[][] keyAndIV = GenerateKeyAndIV(32, 16, 1, saltData, aeskey.getBytes(StandardCharsets.UTF_8), md5);
            SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
            IvParameterSpec iv = new IvParameterSpec(keyAndIV[1]);
            byte[] encrypted = Arrays.copyOfRange(cipherData, 16, cipherData.length);
            Cipher aesCBC = Cipher.getInstance("AES/CBC/PKCS5Padding");
            aesCBC.init(Cipher.DECRYPT_MODE, key, iv);
            byte[] decryptedData = aesCBC.doFinal(encrypted);
            String decryptedText = new String(decryptedData, StandardCharsets.UTF_8);
            byte[] fileBytes = Base64.getDecoder().decode( decryptedText.split( ",", 2 )[1] );
            //Tools.writeFile( "test.jpg", fileBytes );
            System.err.println( "done decrypting" );
            return fileBytes;
        }
        catch( Exception e ){
            e.printStackTrace();
        }
        return null;
    }

    // Convert a string to it's RSA equivalent
    private static Pair<PublicKey,PrivateKey> stringToRSA(String string) throws Exception{
        // Convert public key string to key object
        // https://stackoverflow.com/questions/28294663/how-to-convert-from-string-to-publickey
        byte[] publicBytes = Base64.getDecoder().decode(string);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        Pair<PublicKey,PrivateKey> keys = new Pair<>(
                keyFactory.generatePublic(keySpec),
                keyFactory.generatePrivate(keySpec)
        );

        return keys;
    }

    // Decrypt and RSA encrypted string
    public static byte[] rsaDecrypt(String encrypted, String key) throws Exception {

        PrivateKey privateKey = stringToRSA(key).second;

        // Ok now we can actually decrypt
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
        cipher.init(Cipher.DECRYPT_MODE,privateKey);

        byte[] decrytpedTextArray = cipher.doFinal(encrypted.getBytes());
        return decrytpedTextArray;
    }

    // Encrypt a string via RSA key string
    public static byte[] rsaEncrypt(String plainText, String publicKey) throws Exception {

        PublicKey pubKey = stringToRSA(publicKey).first;

        // Ok now we can actually encrypt
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWITHSHA-512ANDMGF1PADDING");
        cipher.init(Cipher.ENCRYPT_MODE,pubKey);

        byte[] cipherText = cipher.doFinal(plainText.getBytes());
        return cipherText;
    }

}
