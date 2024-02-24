import static org.junit.Assert.*;
import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class GuiRenderingTests {

    private MyFrame myFrame;

    @Before
    public void setUp() {
        myFrame = new MyFrame();
    }

    @Test
    public void testGuiRendering() {
        // Verify GUI components are displayed correctly
        assertTrue(myFrame.isVisible());
        assertNotNull(myFrame.inputArea);
        assertNotNull(myFrame.outputArea);
        assertNotNull(myFrame.newKey);
        assertNotNull(myFrame.encrypt);
        assertNotNull(myFrame.decrypt);
        assertNotNull(myFrame.quit);
        assertNotNull(myFrame.inputLabel);
        assertNotNull(myFrame.outputLabel);
        assertNotNull(myFrame.buttonPanel);
        assertNotNull(myFrame.textHeaderPanel);
        assertNotNull(myFrame.textPanel);
        assertNotNull(myFrame.textFooterPanel);
    }

    @After
    public void tearDown() {
        myFrame.dispose(); // Close the frame after each test
    }
}

