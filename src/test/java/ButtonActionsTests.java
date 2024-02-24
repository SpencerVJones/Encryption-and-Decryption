import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ButtonActionsTests {

    private MyFrame frame;

    @Before
    public void setUp() {
        frame = new MyFrame();
    }

    @After
    public void tearDown() {
        frame.dispose();
    }

    @Test
    public void testGenerateKeyButton() {
        frame.newKey.doClick();
        assertNotNull(frame.encryptionMap);
        assertNotNull(frame.decryptionMap);
        assertFalse(frame.encryptionMap.isEmpty());
        assertFalse(frame.decryptionMap.isEmpty());
    }

    @Test
    public void testEncryptButton() {
        frame.inputArea.setText("Test Message");
        frame.encrypt.doClick();
        String encryptedText = frame.outputArea.getText();
        assertNotNull(encryptedText);
        assertFalse(encryptedText.isEmpty());
        assertTrue(encryptedText.contains("Encrypted:"));
    }

    @Test
    public void testDecryptButton() {
        frame.inputArea.setText("Encrypted: Encrypted Message");
        frame.decrypt.doClick();
        String decryptedText = frame.outputArea.getText();
        assertNotNull(decryptedText);
        assertFalse(decryptedText.isEmpty());
        assertTrue(decryptedText.contains("Decrypted:"));
    }

    @Test
    public void testQuitButton() {
        frame.quit.doClick();
        assertFalse(frame.isVisible());
    }
}