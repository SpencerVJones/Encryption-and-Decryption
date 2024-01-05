package GUI;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import javax.swing.*;

public class MyFrame extends JFrame implements ActionListener {

    private JButton newKey;
    private JButton encrypt;
    private JButton decrypt;
    private JButton quit;

    private JTextArea inputTextArea;
    private JTextArea outputTextArea;

    private JScrollPane inputScrollPane;
    private JScrollPane outputScrollPane;

    private JPanel buttonPanel;
    private JPanel inputPanel;
    private JPanel outputPanel;

    private ArrayList<Character> list = new ArrayList<>();
    private ArrayList<Character> shuffledList = new ArrayList<>();
    private char character = ' ';

    public MyFrame() {

        super("Encryption Program");
        setTitle("Encryption and Decryption");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        // Initializing buttons
        newKey = new JButton("New Key");
        encrypt = new JButton("Encrypt text");
        decrypt = new JButton("Decrypt text");
        quit = new JButton("Quit");

        // Initializing TextAreas
        inputTextArea = new JTextArea();
        outputTextArea = new JTextArea();

        // Initializing JScrollPane for inputTextArea
        inputScrollPane = new JScrollPane(inputTextArea);
        inputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Initializing JScrollPane for outputTextArea
        outputScrollPane = new JScrollPane(outputTextArea);
        outputScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Initializing panels
        buttonPanel = new JPanel();
        inputPanel = new JPanel();
        outputPanel = new JPanel();

        // New Key Button
        newKey.setPreferredSize(new Dimension(235, 175));
        newKey.setFont(new Font("SanSerif", Font.PLAIN, 35));
        newKey.setBackground(Color.BLACK);
        newKey.setOpaque(true);

        // Encrypt Key Button
        encrypt.setPreferredSize(new Dimension(235, 175));
        encrypt.setFont(new Font("SanSerif", Font.PLAIN, 35));
        encrypt.setBackground(Color.BLACK);
        encrypt.setOpaque(true);

        // Decrypt Key button
        decrypt.setPreferredSize(new Dimension(235, 175));
        decrypt.setFont(new Font("SanSerif", Font.PLAIN, 35));
        decrypt.setBackground(Color.BLACK);
        decrypt.setOpaque(true);

        // Quit Key Button
        quit.setPreferredSize(new Dimension(235, 175));
        quit.setFont(new Font("SanSerif", Font.PLAIN, 35));
        quit.setBackground(Color.BLACK);
        quit.setOpaque(true);

        // Add Action Listeners
        newKey.addActionListener(this);
        encrypt.addActionListener(this);
        decrypt.addActionListener(this);
        quit.addActionListener(this);
        

        // Input Text Area
        inputScrollPane.setPreferredSize(new Dimension(970, 250));
        inputTextArea.setPreferredSize(new Dimension(970, 250));
        inputTextArea.setBackground(Color.WHITE);
        inputTextArea.setFont(new Font("SanSerif", Font.PLAIN, 15));
        inputTextArea.setCaretColor(Color.BLUE);
        inputTextArea.setEditable(true);

        // Output Text Area
        outputScrollPane.setPreferredSize(new Dimension(970, 250));
        outputTextArea.setPreferredSize(new Dimension(970, 250));
        outputTextArea.setBackground(Color.DARK_GRAY);
        outputTextArea.setForeground(Color.WHITE);
        outputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
        outputTextArea.setCaretColor(Color.WHITE);
        outputTextArea.setEditable(true);

        // Button Panel
        buttonPanel.setBounds(10, 10, 970, 187);
        buttonPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        // Input Panel
        inputPanel.setBounds(10, 202, 970, 250);
        inputPanel.add(inputScrollPane);

        // Output Panel
        outputPanel.setBounds(10, 457, 970, 250);
        outputPanel.add(outputScrollPane);

        // Add to button panel
        buttonPanel.add(newKey);
        buttonPanel.add(encrypt);
        buttonPanel.add(decrypt);
        buttonPanel.add(quit);

        // Add to input panel
        inputPanel.add(inputScrollPane);

        // Add to output panel
        outputPanel.add(outputScrollPane);

        // Add to frame
        add(buttonPanel);
        add(inputPanel);
        add(outputPanel);

        setSize(1000, 745);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == newKey) {
            generateKey();
        } else if (e.getSource() == encrypt) {
            encryptText();
        } else if (e.getSource() == decrypt) {
            decryptText();
        } else if (e.getSource() == quit) {
            quit();
        }
    }

    private void generateKey() {
        list.clear();
        shuffledList.clear();
        character = ' ';

        for (int i = 32; i < 127; i++) {
            list.add(Character.valueOf(character));
            character++;
        }

        shuffledList = new ArrayList<>(list);
        Collections.shuffle(shuffledList);

        outputTextArea.append("\nNew key generated:\n");
        for (Character x : shuffledList) {
            outputTextArea.append(x.toString());
        }
        outputTextArea.append("\n");
    }

    private void encryptText() {
        String message = inputTextArea.getText();
        char[] letters = message.toCharArray();

        for (int i = 0; i < letters.length; i++) {
            for (int j = 0; j < list.size(); j++) {
                if (letters[i] == list.get(j)) {
                    letters[i] = shuffledList.get(j);
                    break;
                }
            }
        }

        outputTextArea.append("Encrypted: \n");
        for (char x : letters) {
            outputTextArea.append(Character.toString(x));
        }
        outputTextArea.append("\n");
    }

    private void decryptText() {
        String message = inputTextArea.getText();
        char[] letters = message.toCharArray();

        for (int i = 0; i < letters.length; i++) {
            for (int j = 0; j < shuffledList.size(); j++) {
                if (letters[i] == shuffledList.get(j)) {
                    letters[i] = list.get(j);
                    break;
                }
            }
        }

        outputTextArea.append("Decrypted: \n");
        for (char x : letters) {
            outputTextArea.append(Character.toString(x));
        }
        outputTextArea.append("\n");
    }

    private void quit() {
        outputTextArea.append("Bye!\n");
        System.exit(0);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            MyFrame frame = new MyFrame();
        });
    }
}
