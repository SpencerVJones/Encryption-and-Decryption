import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import java.awt.*;
import java.util.*;

public class MyFrame extends JFrame implements ActionListener {

    protected Map<Character, Character> encryptionMap = new HashMap<>();
    protected Map<Character, Character> decryptionMap = new HashMap<>();

    protected ArrayList<Character> list = new ArrayList<>();


    // Declaring text areas
    protected JTextArea inputArea;
    protected JTextArea outputArea;


    // Declaring buttons
    protected JButton newKey;
    protected JButton encrypt;
    protected JButton decrypt;
    protected JButton quit;


    // Declaring labels
    protected JLabel inputLabel;
    protected JLabel outputLabel;

    protected JPanel buttonPanel;
    protected JPanel textHeaderPanel;
    protected JPanel textPanel;
    protected JPanel textFooterPanel;


    public MyFrame() { // MyFrame constructor
        super("Encryption Program");
        setTitle("Encryption and Decryption");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Input Text Area : [JTextArea] [inputArea]
        inputArea = new JTextArea();
        inputArea.setPreferredSize(new Dimension(750, 250));
        inputArea.setBackground(Color.WHITE);
        inputArea.setFont(new Font("SanSerif", Font.PLAIN, 15));
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setCaretColor(Color.BLUE);
        inputArea.setEditable(true);

        // Output Text Area : [JTextArea] [outputArea]
        outputArea = new JTextArea(); // Initializing
        outputArea.setPreferredSize(new Dimension(750, 250)); // Set size
        outputArea.setBackground(Color.DARK_GRAY); // Set background color
        outputArea.setForeground(Color.WHITE); // Set font color
        outputArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        outputArea.setLineWrap(true); // Enable line wrapping
        outputArea.setWrapStyleWord(true); // Wrap at word boundaries
        outputArea.setCaretColor(Color.WHITE); // Set caret color
        outputArea.setEditable(false); // Disable editing

        // New Key Button : [JButton] [newKey]
        newKey = new JButton("Generate Key"); // Initializing
        newKey.setFont(new Font("SanSerif", Font.PLAIN, 25)); // Set font color
        newKey.setPreferredSize(new Dimension(175, 80)); // Set size
        newKey.setBackground(Color.BLACK); // Set background color
        newKey.setOpaque(true); // Set opaque
        newKey.addActionListener(this); // Add Action Listener to Button
        newKey.setToolTipText("Click to get a new encryption key"); // Add tooltip


        // Encrypt Button : [JButton] [encrypt]
        encrypt = new JButton("Encrypt Text"); // Initializing
        encrypt.setFont(new Font("SanSerif", Font.PLAIN, 25)); // Set font
        encrypt.setPreferredSize(new Dimension(175, 80)); // Set size
        encrypt.setBackground(Color.BLACK); // Set background color
        encrypt.setOpaque(true); // Set opaque
        encrypt.addActionListener(this); // Add action listener
        encrypt.setToolTipText("Click to encrypt the text entered in the first text box"); // Add tooltip


        // Decrypt Button : [JButton] [decrypt]
        decrypt = new JButton("Decrypt Text"); // Initializing
        decrypt.setFont(new Font("SanSerif", Font.PLAIN, 25)); // Set font
        decrypt.setPreferredSize(new Dimension(175, 80)); // Set size
        decrypt.setBackground(Color.BLACK); // Set background color
        decrypt.setOpaque(true); // Set opaque
        decrypt.addActionListener(this); // Add action listener
        decrypt.setToolTipText("Click to decrypt the text entered in the first text box"); // Add tooltip

        // Quit Button : [JButton] [quit]
        quit = new JButton("Quit"); // Initializing
        quit.setFont(new Font("SanSerif", Font.PLAIN, 25)); // Set Font
        quit.setPreferredSize(new Dimension(175, 80)); // Set size
        quit.setBackground(Color.BLACK); // Set background color
        quit.setOpaque(true); // Set opaque
        quit.addActionListener(this); // Add action listener
        quit.setToolTipText("Click to quit the application"); // Add tooltip


        // Text Input Label : [JLabel] [inputLabel]
        inputLabel = new JLabel("Enter Your Text Here:"); // Initializing and setting text
        inputLabel.setPreferredSize(new Dimension(750, 55)); // Set size
        inputLabel.setFont(new Font("SanSerif", Font.PLAIN, 25)); // Set font
        inputLabel.setHorizontalAlignment(SwingConstants.CENTER); // Alignment

        // Text Output Label : [JLabel] [outputLabel]
        outputLabel = new JLabel("Results:"); // Initializing and setting text
        outputLabel.setPreferredSize(new Dimension(750, 55)); // Set size
        outputLabel.setFont(new Font("SanSerif", Font.PLAIN, 25)); // Set font
        outputLabel.setHorizontalAlignment(SwingConstants.CENTER); // Alignment


        // Button Panel : [JPanel] [buttonPanel]
        buttonPanel = new JPanel(); // Initializing
        buttonPanel.setBounds(0, 0, 750, 90); // Set size and location
        buttonPanel.setLayout(new FlowLayout()); // Layout manager
        buttonPanel.setBackground(Color.LIGHT_GRAY); // Set background color
        buttonPanel.add(newKey); // Add button to panel
        buttonPanel.add(encrypt); // Add button to panel
        buttonPanel.add(decrypt); // Add button to panel
        buttonPanel.add(quit); // Add button to panel

        // Text Header Panel [JPanel] : [textHeaderPanel]
        textHeaderPanel = new JPanel(); // Initializing
        textHeaderPanel.setBounds(0, 90, 750, 15); // Set size and location
        textHeaderPanel.setBackground(Color.GRAY); // Set background color


        // Text Panel[JPanel] : [textPanel]
        textPanel = new JPanel(); // Initializing
        textPanel.setBounds(0, 90, 750, 660); // Set size and location
        textPanel.setLayout(new FlowLayout());
        textPanel.setBackground(Color.GRAY);
        textPanel.add(inputLabel);
        textPanel.add(inputArea);
        textPanel.add(outputLabel);
        textPanel.add(outputArea);

        // Text Footer Panel : [JPanel] [textFooterPanel]
        textFooterPanel = new JPanel(); // Initializing
        textFooterPanel.setBounds(0, 735, 750, 15); // Set size and location
        textFooterPanel.setBackground(Color.GRAY); // Set background color

        // Add elements to frame
        add(buttonPanel);
        add(textHeaderPanel);
        add(textPanel);
        add(textFooterPanel);


        setSize(750, 750); // Set size of GUI
        this.setResizable(false); // Disable resizing
        this.setLocationRelativeTo(null); // Spawn GUI in middle of screen
        setVisible(true); // Make GUI visible
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        // Check which button triggered the action event
        if (e.getSource() == newKey) {
            // Generate a new encryption key
            generateKey();
        } else if (e.getSource() == encrypt) {
            // Encrypt the text from the input area
            encryptText();
        } else if (e.getSource() == decrypt) {
            // Decrypt the text from the input area
            decryptText();
        } else if (e.getSource() == quit) {
            // Quit the application
            quit();
        }
    }

    public void generateKey() {
        // Clear the existing encryption and decryption maps
        encryptionMap.clear();
        decryptionMap.clear();
        outputArea.append(" ");

        // Populate a list with ASCII characters from 32 to 126
        list.clear();
        for (int i = 32; i < 127; i++) {
            list.add((char) i);
        }

        // Shuffle the list to create a randomized key
        Collections.shuffle(list);

        // Generate encryption and decryption maps based on the shuffled list
        for (int i = 32; i < 127; i++) {
            char originalChar = (char) i;
            char encryptedChar = list.get(i - 32);
            encryptionMap.put(originalChar, encryptedChar);
            decryptionMap.put(encryptedChar, originalChar);
        }

        // Clear the output area and display the new key
        outputArea.setText("");
        outputArea.append("New key generated: ");
        for (char encryptedChar : list) {
            outputArea.append(encryptedChar + " ");
        }
        outputArea.append("\n");
    }

    public void encryptText() {
        // Get the text from the input area
        String message = inputArea.getText();
        StringBuilder encryptedMessage = new StringBuilder();

        // Encrypt each character in the message
        for (char letter : message.toCharArray()) {
            char encryptedChar = encryptionMap.getOrDefault(letter, letter);
            encryptedMessage.append(encryptedChar);
        }

        // Display the encrypted message in the output area
        outputArea.append("Encrypted: ");
        outputArea.append(encryptedMessage.toString());

    }


    public void decryptText() {
        // Get the text from the input area
        String message = inputArea.getText();
        StringBuilder decryptedMessage = new StringBuilder();

        // Decrypt each character in the message
        for (char letter : message.toCharArray()) {
            char decryptedChar = decryptionMap.getOrDefault(letter, letter);
            decryptedMessage.append(decryptedChar);
        }

        // Display the decrypted message in the output area
        outputArea.append("Decrypted: ");
        outputArea.append(decryptedMessage.toString());
    }


    public void quit() {
        // Exit the application
        System.exit(0);
    }
}