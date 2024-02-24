import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class FunctionalityTests {

    private MyFrame myFrame;

    @Before
    public void setUp() {
        myFrame = new MyFrame();
    }

    @Test
    public void testKeyGeneration() {
        // Ensure that calling generateKey() results in a new encryption key being generated
        myFrame.generateKey();

        // Check that encryption and decryption maps are populated correctly
        assertFalse(myFrame.encryptionMap.isEmpty());
        assertFalse(myFrame.decryptionMap.isEmpty());
    }

    @Test
    public void testEncryption() {
        // Test encryption with various characters
        myFrame.inputArea.setText("Hello World!");
        myFrame.encryptText();

        // Verify that output is not empty and contains encrypted text
        assertFalse(myFrame.outputArea.getText().isEmpty());
        assertTrue(myFrame.outputArea.getText().contains("Encrypted:"));

        // Test encryption with empty input
        myFrame.inputArea.setText("");
        myFrame.encryptText();

        // Test encryption with special characters, numbers, and letters
        myFrame.inputArea.setText("!@#$%^123abc");
        myFrame.encryptText();

        // Verify that output contains encrypted text
        assertFalse(myFrame.outputArea.getText().isEmpty());
        assertTrue(myFrame.outputArea.getText().contains("Encrypted"));
    }


    @Test
    public void testDecryption() {
        // Test decryption with encrypted string
        myFrame.inputArea.setText("Encrypted Text");
        myFrame.decryptText();

        // Verify that output is not empty and contains decrypted text
        assertFalse(myFrame.outputArea.getText().isEmpty());
        assertTrue(myFrame.outputArea.getText().contains("Decrypted"));

        // Test decryption with empty input
        myFrame.inputArea.setText("");
        myFrame.decryptText();


        // Test decryption with special characters, numbers, and letters
        myFrame.inputArea.setText("!@#$%^123abc");
        myFrame.decryptText();

        // Verify that output contains decrypted text
        assertFalse(myFrame.outputArea.getText().isEmpty());
        assertTrue(myFrame.outputArea.getText().contains("Decrypted"));
    }

    @After
    public void tearDown() {
        myFrame.dispose(); // Close the frame after each test
    }
}
