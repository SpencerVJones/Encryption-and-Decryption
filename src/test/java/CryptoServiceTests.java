import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

public class CryptoServiceTests {
    private CryptoService service;

    @Before
    public void setUp() {
        service = new CryptoService();
    }

    @Test
    public void testAesRoundTrip() throws Exception {
        CryptoService.CryptoRequest encryptRequest = new CryptoService.CryptoRequest();
        encryptRequest.algorithm = "AES";
        encryptRequest.text = "Hello AES";

        CryptoService.CryptoResponse encrypted = service.encrypt(encryptRequest);
        assertNotNull(encrypted.result);
        assertNotNull(encrypted.key);

        CryptoService.CryptoRequest decryptRequest = new CryptoService.CryptoRequest();
        decryptRequest.algorithm = "AES";
        decryptRequest.text = encrypted.result;
        decryptRequest.key = encrypted.key;

        CryptoService.CryptoResponse decrypted = service.decrypt(decryptRequest);
        assertEquals("Hello AES", decrypted.result);
    }

    @Test
    public void testDesRoundTrip() throws Exception {
        CryptoService.CryptoRequest encryptRequest = new CryptoService.CryptoRequest();
        encryptRequest.algorithm = "DES";
        encryptRequest.text = "Hello DES";

        CryptoService.CryptoResponse encrypted = service.encrypt(encryptRequest);
        assertNotNull(encrypted.result);
        assertNotNull(encrypted.key);

        CryptoService.CryptoRequest decryptRequest = new CryptoService.CryptoRequest();
        decryptRequest.algorithm = "DES";
        decryptRequest.text = encrypted.result;
        decryptRequest.key = encrypted.key;

        CryptoService.CryptoResponse decrypted = service.decrypt(decryptRequest);
        assertEquals("Hello DES", decrypted.result);
    }

    @Test
    public void testRsaRoundTrip() throws Exception {
        CryptoService.CryptoRequest encryptRequest = new CryptoService.CryptoRequest();
        encryptRequest.algorithm = "RSA";
        encryptRequest.text = "Hello RSA";

        CryptoService.CryptoResponse encrypted = service.encrypt(encryptRequest);
        assertNotNull(encrypted.result);
        assertNotNull(encrypted.publicKey);
        assertNotNull(encrypted.privateKey);

        CryptoService.CryptoRequest decryptRequest = new CryptoService.CryptoRequest();
        decryptRequest.algorithm = "RSA";
        decryptRequest.text = encrypted.result;
        decryptRequest.privateKey = encrypted.privateKey;

        CryptoService.CryptoResponse decrypted = service.decrypt(decryptRequest);
        assertEquals("Hello RSA", decrypted.result);
    }

    @Test
    public void testCaesarRoundTrip() throws Exception {
        CryptoService.CryptoRequest encryptRequest = new CryptoService.CryptoRequest();
        encryptRequest.algorithm = "Caesar";
        encryptRequest.text = "Hello Caesar";
        encryptRequest.shift = 9;

        CryptoService.CryptoResponse encrypted = service.encrypt(encryptRequest);
        assertNotNull(encrypted.result);
        assertEquals(Integer.valueOf(9), encrypted.shift);

        CryptoService.CryptoRequest decryptRequest = new CryptoService.CryptoRequest();
        decryptRequest.algorithm = "Caesar";
        decryptRequest.text = encrypted.result;
        decryptRequest.shift = encrypted.shift;

        CryptoService.CryptoResponse decrypted = service.decrypt(decryptRequest);
        assertEquals("Hello Caesar", decrypted.result);
    }
}
