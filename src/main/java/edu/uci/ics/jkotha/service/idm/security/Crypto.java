package edu.uci.ics.jkotha.service.idm.security;

import edu.uci.ics.jkotha.service.idm.models.FunctionsRequired;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

public final class Crypto {
    private static SecureRandom secRand = new SecureRandom();
    public static final int ITERATIONS = 10000;
    public static final int KEY_LENGTH = 512;

    // PBKDF2 -- Password-based-key-derivation-function
    // HMAC -- Key-hashed Message Authentication code, used in conjunction with any cryptographic hash function.
    // SHA512 -- 512 bit member function of the SHA-2 cryptographic hash functions family designed by the NSA
    private static final String hashFunction = "PBKDF2WithHmacSHA512";

    private Crypto() { }

    public static byte[] hashPassword( final char[] password, final byte[] salt ) {
        try {
            // Create a SecretKeyFactory
            //System.err.println(password);
//            for (byte b: salt){
//                System.err.print(b);
//                System.err.println("");
//            }
            SecretKeyFactory factory = SecretKeyFactory.getInstance(hashFunction);
            // Create a PBEKeySpec
            // PBEKeySpec is a user-chosen password that can be used with password-based encryption (PBE).
            // Iterations -- the number of times we want the PBKDF2 to execute it's underlying algorithm. The higher the
            //               the number of iterations, the safer the hashed password is.
            PBEKeySpec spec = new PBEKeySpec(password,salt,ITERATIONS,KEY_LENGTH);
            // Generate the secret key from the PBE spec.
            SecretKey key = factory.generateSecret(spec);
            // Retrieve the encoded password from the key and save into a byte[]
            byte[] hashedPassword = key.getEncoded();
            //System.err.println(FunctionsRequired.toStringforDB(hashedPassword));
            // Return the hashed pass
            return hashedPassword;
        } catch ( NoSuchAlgorithmException | InvalidKeySpecException e ) {
            throw new RuntimeException( e );
        }
        //return null;
    }

    /*
        Salt must be generated using a Cryptographically Secure Pseudo-Random Number Generator (CSPRNG). CSPRNGs are
        very different from ordinary pseudo-random number generators, like C's rand() function. CSPRNGs are designed to
        be cryptographically secure, meaning they provide a high level of randomness and are completely unpredictable.
        We do not want our salts to be predictable, so we must use a CSPRNG. Java has such a CSPRNG: SecureRandom.
     */
    public static byte[] genSalt() {
        byte[] salt = new byte[4];
        secRand.nextBytes(salt);
        salt[0] = (byte)( ~(Byte.toUnsignedInt(salt[0]) >>> 2) );
        return salt;
    }
}
