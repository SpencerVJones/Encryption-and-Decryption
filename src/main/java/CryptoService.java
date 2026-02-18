import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Locale;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class CryptoService {
    private static final int ASCII_START = 32;
    private static final int ASCII_END = 126;
    private static final int PRINTABLE_RANGE = ASCII_END - ASCII_START + 1;

    private static final String ALGO_AES = "AES";
    private static final String ALGO_DES = "DES";
    private static final String ALGO_RSA = "RSA";
    private static final String ALGO_CAESAR = "Caesar";

    private static final String TRANSFORMATION_AES = "AES/CBC/PKCS5Padding";
    private static final String TRANSFORMATION_DES = "DES/CBC/PKCS5Padding";
    private static final String TRANSFORMATION_RSA = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static final int AES_IV_LENGTH = 16;
    private static final int DES_IV_LENGTH = 8;

    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoResponse generateKey(String algorithmValue) throws GeneralSecurityException {
        String algorithm = normalizeAlgorithm(algorithmValue);
        CryptoResponse response = success(algorithm);

        if (ALGO_AES.equals(algorithm)) {
            SecretKey key = generateSymmetricKey(ALGO_AES, 128);
            response.key = encodeBase64(key.getEncoded());
            return response;
        }
        if (ALGO_DES.equals(algorithm)) {
            SecretKey key = generateSymmetricKey(ALGO_DES, 56);
            response.key = encodeBase64(key.getEncoded());
            return response;
        }
        if (ALGO_RSA.equals(algorithm)) {
            KeyPair pair = generateRsaKeyPair();
            response.publicKey = encodeBase64(pair.getPublic().getEncoded());
            response.privateKey = encodeBase64(pair.getPrivate().getEncoded());
            return response;
        }
        if (ALGO_CAESAR.equals(algorithm)) {
            response.shift = randomCaesarShift();
            return response;
        }

        throw new IllegalArgumentException("Unsupported algorithm: " + algorithmValue);
    }

    public CryptoResponse encrypt(CryptoRequest request) throws Exception {
        String algorithm = normalizeAlgorithm(request.algorithm);
        validateText(request.text, "Text is required for encryption");

        CryptoResponse response = success(algorithm);
        if (ALGO_AES.equals(algorithm)) {
            SecretKey key = resolveSymmetricKey(request.key, ALGO_AES, 128);
            response.result = encryptSymmetric(request.text, key, TRANSFORMATION_AES, AES_IV_LENGTH);
            response.key = encodeBase64(key.getEncoded());
            return response;
        }
        if (ALGO_DES.equals(algorithm)) {
            SecretKey key = resolveSymmetricKey(request.key, ALGO_DES, 56);
            response.result = encryptSymmetric(request.text, key, TRANSFORMATION_DES, DES_IV_LENGTH);
            response.key = encodeBase64(key.getEncoded());
            return response;
        }
        if (ALGO_RSA.equals(algorithm)) {
            KeyPair generatedPair = null;
            PublicKey publicKey;

            if (hasText(request.publicKey)) {
                publicKey = decodePublicKey(request.publicKey);
            } else {
                generatedPair = generateRsaKeyPair();
                publicKey = generatedPair.getPublic();
            }

            Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encrypted = cipher.doFinal(request.text.getBytes(StandardCharsets.UTF_8));
            response.result = encodeBase64(encrypted);
            response.publicKey = encodeBase64(publicKey.getEncoded());
            if (generatedPair != null) {
                response.privateKey = encodeBase64(generatedPair.getPrivate().getEncoded());
            }
            return response;
        }
        if (ALGO_CAESAR.equals(algorithm)) {
            int shift = request.shift == null ? randomCaesarShift() : normalizeShift(request.shift);
            response.result = caesarTransform(request.text, shift);
            response.shift = shift;
            return response;
        }

        throw new IllegalArgumentException("Unsupported algorithm: " + request.algorithm);
    }

    public CryptoResponse decrypt(CryptoRequest request) throws Exception {
        String algorithm = normalizeAlgorithm(request.algorithm);
        validateText(request.text, "Text is required for decryption");

        CryptoResponse response = success(algorithm);
        if (ALGO_AES.equals(algorithm)) {
            SecretKey key = requireSymmetricKey(request.key, ALGO_AES);
            response.result = decryptSymmetric(request.text, key, TRANSFORMATION_AES, AES_IV_LENGTH);
            return response;
        }
        if (ALGO_DES.equals(algorithm)) {
            SecretKey key = requireSymmetricKey(request.key, ALGO_DES);
            response.result = decryptSymmetric(request.text, key, TRANSFORMATION_DES, DES_IV_LENGTH);
            return response;
        }
        if (ALGO_RSA.equals(algorithm)) {
            String privateKeyValue = hasText(request.privateKey) ? request.privateKey : request.key;
            if (!hasText(privateKeyValue)) {
                throw new IllegalArgumentException("RSA decryption requires privateKey");
            }
            PrivateKey privateKey = decodePrivateKey(privateKeyValue);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(decodeBase64(request.text));
            response.result = new String(decrypted, StandardCharsets.UTF_8);
            return response;
        }
        if (ALGO_CAESAR.equals(algorithm)) {
            int shift = request.shift == null ? 3 : normalizeShift(request.shift);
            response.result = caesarTransform(request.text, PRINTABLE_RANGE - shift);
            response.shift = shift;
            return response;
        }

        throw new IllegalArgumentException("Unsupported algorithm: " + request.algorithm);
    }

    private SecretKey resolveSymmetricKey(String keyValue, String algorithm, int keySize)
            throws GeneralSecurityException {
        if (hasText(keyValue)) {
            return decodeSymmetricKey(keyValue, algorithm);
        }
        return generateSymmetricKey(algorithm, keySize);
    }

    private SecretKey requireSymmetricKey(String keyValue, String algorithm) {
        if (!hasText(keyValue)) {
            throw new IllegalArgumentException(algorithm + " decryption requires key");
        }
        return decodeSymmetricKey(keyValue, algorithm);
    }

    private SecretKey generateSymmetricKey(String algorithm, int keySize) throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        keyGenerator.init(keySize, secureRandom);
        return keyGenerator.generateKey();
    }

    private KeyPair generateRsaKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance(ALGO_RSA);
        generator.initialize(2048, secureRandom);
        return generator.generateKeyPair();
    }

    private String encryptSymmetric(String message, SecretKey key, String transformation, int ivLength)
            throws Exception {
        byte[] iv = new byte[ivLength];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return encodeBase64(iv) + ":" + encodeBase64(encrypted);
    }

    private String decryptSymmetric(String payload, SecretKey key, String transformation, int expectedIvLength)
            throws Exception {
        String[] parts = payload.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Cipher text must be iv:ciphertext");
        }

        byte[] iv = decodeBase64(parts[0].trim());
        if (iv.length != expectedIvLength) {
            throw new IllegalArgumentException("Invalid IV length");
        }
        byte[] encrypted = decodeBase64(parts[1].trim());

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv));
        return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
    }

    private String caesarTransform(String text, int shift) {
        StringBuilder builder = new StringBuilder(text.length());
        for (char ch : text.toCharArray()) {
            if (ch < ASCII_START || ch > ASCII_END) {
                builder.append(ch);
                continue;
            }
            int normalized = ch - ASCII_START;
            int rotated = (normalized + shift) % PRINTABLE_RANGE;
            builder.append((char) (rotated + ASCII_START));
        }
        return builder.toString();
    }

    private PublicKey decodePublicKey(String encodedKey) throws GeneralSecurityException {
        byte[] bytes = decodeBase64(encodedKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        return KeyFactory.getInstance(ALGO_RSA).generatePublic(keySpec);
    }

    private PrivateKey decodePrivateKey(String encodedKey) throws GeneralSecurityException {
        byte[] bytes = decodeBase64(encodedKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        return KeyFactory.getInstance(ALGO_RSA).generatePrivate(keySpec);
    }

    private SecretKey decodeSymmetricKey(String encodedKey, String algorithm) {
        byte[] keyBytes = decodeBase64(encodedKey);
        if (ALGO_AES.equals(algorithm) && !isValidAesKeyLength(keyBytes.length)) {
            throw new IllegalArgumentException("AES key must be 16, 24, or 32 bytes (base64 encoded)");
        }
        if (ALGO_DES.equals(algorithm) && keyBytes.length != 8) {
            throw new IllegalArgumentException("DES key must be 8 bytes (base64 encoded)");
        }
        return new SecretKeySpec(keyBytes, algorithm);
    }

    private boolean isValidAesKeyLength(int keyLength) {
        return keyLength == 16 || keyLength == 24 || keyLength == 32;
    }

    private byte[] decodeBase64(String value) {
        try {
            return Base64.getDecoder().decode(value.trim());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Value must be valid base64");
        }
    }

    private String encodeBase64(byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }

    private void validateText(String text, String errorMessage) {
        if (!hasText(text)) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private int randomCaesarShift() {
        return secureRandom.nextInt(25) + 1;
    }

    private int normalizeShift(Integer shift) {
        if (shift == null || shift < 1 || shift > PRINTABLE_RANGE - 1) {
            throw new IllegalArgumentException("Shift must be between 1 and " + (PRINTABLE_RANGE - 1));
        }
        return shift;
    }

    private String normalizeAlgorithm(String algorithmValue) {
        if (!hasText(algorithmValue)) {
            throw new IllegalArgumentException("algorithm is required");
        }
        String normalized = algorithmValue.trim().toUpperCase(Locale.ROOT);
        if (ALGO_AES.equals(normalized)) {
            return ALGO_AES;
        }
        if (ALGO_DES.equals(normalized)) {
            return ALGO_DES;
        }
        if (ALGO_RSA.equals(normalized)) {
            return ALGO_RSA;
        }
        if ("CAESAR".equals(normalized)) {
            return ALGO_CAESAR;
        }
        throw new IllegalArgumentException("Unsupported algorithm: " + algorithmValue);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private CryptoResponse success(String algorithm) {
        CryptoResponse response = new CryptoResponse();
        response.success = true;
        response.algorithm = algorithm;
        return response;
    }

    public static class CryptoRequest {
        public String algorithm;
        public String text;
        public String key;
        public String publicKey;
        public String privateKey;
        public Integer shift;
    }

    public static class CryptoResponse {
        public boolean success;
        public String algorithm;
        public String result;
        public String key;
        public String publicKey;
        public String privateKey;
        public Integer shift;
        public String error;
    }
}
