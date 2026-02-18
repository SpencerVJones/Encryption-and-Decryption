import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;

public class MyFrame extends JFrame implements ActionListener {
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

    protected Map<Character, Character> encryptionMap = new HashMap<>();
    protected Map<Character, Character> decryptionMap = new HashMap<>();
    protected ArrayList<Character> list = new ArrayList<>();

    protected JTextArea inputArea;
    protected JTextArea outputArea;

    protected JButton newKey;
    protected JButton encrypt;
    protected JButton decrypt;
    protected JButton quit;

    protected JLabel inputLabel;
    protected JLabel outputLabel;

    protected JPanel buttonPanel;
    protected JPanel textHeaderPanel;
    protected JPanel textPanel;
    protected JPanel textFooterPanel;

    private JComboBox<String> algorithmSelector;
    private JButton copyButton;
    private JButton themeToggle;
    private JLabel statusLabel;
    private JLabel titleLabel;
    private JLabel outputValueLabel;

    private JPanel rootPanel;
    private RoundedPanel cardPanel;
    private JPanel topRightPanel;
    private JPanel algorithmPanel;
    private JPanel statusPanel;
    private JPanel outputCardPanel;
    private JPanel outputCardHeader;
    private JPanel outputBodyPanel;
    private JScrollPane inputScrollPane;
    private JScrollPane outputScrollPane;
    private JSeparator sectionDivider;

    private final SecureRandom secureRandom = new SecureRandom();
    private final boolean exitOnQuit;

    private SecretKey aesKey;
    private SecretKey desKey;
    private KeyPair rsaKeyPair;
    private int caesarShift = 3;

    private boolean darkMode = true;
    private String activeFontFamily;

    public MyFrame() {
        this(false);
    }

    public MyFrame(boolean exitOnQuit) {
        super("Encryption & Decryption");
        this.exitOnQuit = exitOnQuit;
        this.activeFontFamily = detectFontFamily();

        configureFrame();
        buildUi();
        applyTheme();
        generateKey();
        setVisible(true);
    }

    private void configureFrame() {
        setTitle("Encryption & Decryption");
        setDefaultCloseOperation(exitOnQuit ? JFrame.EXIT_ON_CLOSE : JFrame.DISPOSE_ON_CLOSE);
        setSize(1060, 860);
        setMinimumSize(new Dimension(900, 760));
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private void buildUi() {
        rootPanel = new JPanel(new GridBagLayout());
        rootPanel.setBorder(new EmptyBorder(28, 28, 28, 28));
        setContentPane(rootPanel);

        cardPanel = new RoundedPanel(18);
        cardPanel.setLayout(new BorderLayout());
        cardPanel.setPreferredSize(new Dimension(760, 680));

        buildHeader();
        buildBody();

        cardPanel.add(textHeaderPanel, BorderLayout.NORTH);
        cardPanel.add(textPanel, BorderLayout.CENTER);
        cardPanel.add(textFooterPanel, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        rootPanel.add(cardPanel, gbc);
    }

    private void buildHeader() {
        textHeaderPanel = new JPanel(new BorderLayout());
        textHeaderPanel.setBorder(new EmptyBorder(16, 18, 16, 18));

        JPanel trafficLightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        trafficLightPanel.setOpaque(false);
        trafficLightPanel.add(new TrafficDot(new Color(0xE47D5A)));
        trafficLightPanel.add(new TrafficDot(new Color(0xE4B64D)));
        trafficLightPanel.add(new TrafficDot(new Color(0x66B88A)));

        titleLabel = new JLabel("Encryption & Decryption", SwingConstants.CENTER);
        titleLabel.setFont(uiFont(Font.BOLD, 32));

        topRightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topRightPanel.setOpaque(false);

        themeToggle = new JButton("☾");
        configureGhostControl(themeToggle, "Toggle light/dark mode");
        themeToggle.addActionListener(this);

        quit = new JButton("✕");
        configureGhostControl(quit, "Quit application");
        quit.addActionListener(this);

        topRightPanel.add(themeToggle);
        topRightPanel.add(quit);

        textHeaderPanel.add(trafficLightPanel, BorderLayout.WEST);
        textHeaderPanel.add(titleLabel, BorderLayout.CENTER);
        textHeaderPanel.add(topRightPanel, BorderLayout.EAST);
    }

    private void buildBody() {
        textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBorder(new EmptyBorder(26, 28, 26, 28));
        textPanel.setOpaque(false);

        algorithmPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        algorithmPanel.setOpaque(false);
        JLabel algorithmLabel = new JLabel("Algorithm:");
        algorithmLabel.setFont(uiFont(Font.PLAIN, 16));

        algorithmSelector = new JComboBox<>(new String[]{ALGO_AES, ALGO_DES, ALGO_RSA, ALGO_CAESAR});
        algorithmSelector.setFont(uiFont(Font.PLAIN, 15));
        algorithmSelector.setPreferredSize(new Dimension(170, 42));
        algorithmSelector.setFocusable(false);
        algorithmSelector.addActionListener(this);

        algorithmPanel.add(algorithmLabel);
        algorithmPanel.add(algorithmSelector);

        inputLabel = new JLabel("Input");
        inputLabel.setFont(uiFont(Font.BOLD, 16));

        inputArea = new JTextArea(7, 48);
        inputArea.setLineWrap(true);
        inputArea.setWrapStyleWord(true);
        inputArea.setFont(uiFont(Font.PLAIN, 14));
        inputArea.setBorder(new EmptyBorder(12, 12, 12, 12));
        inputArea.setTabSize(4);

        inputScrollPane = new JScrollPane(inputArea);
        inputScrollPane.setPreferredSize(new Dimension(690, 150));
        inputScrollPane.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true));

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);

        newKey = new JButton("Generate Key");
        encrypt = new JButton("Encrypt");
        decrypt = new JButton("Decrypt");

        configureActionButton(newKey);
        configureActionButton(encrypt);
        configureActionButton(decrypt);

        newKey.setPreferredSize(new Dimension(180, 46));
        encrypt.setPreferredSize(new Dimension(160, 46));
        decrypt.setPreferredSize(new Dimension(160, 46));

        newKey.addActionListener(this);
        encrypt.addActionListener(this);
        decrypt.addActionListener(this);

        buttonPanel.add(newKey);
        buttonPanel.add(encrypt);
        buttonPanel.add(decrypt);

        statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusPanel.setOpaque(false);
        statusLabel = new JLabel("Ready");
        statusLabel.setFont(uiFont(Font.PLAIN, 14));
        statusPanel.add(statusLabel);

        sectionDivider = new JSeparator();
        sectionDivider.setPreferredSize(new Dimension(690, 1));

        outputLabel = new JLabel("Output");
        outputLabel.setFont(uiFont(Font.BOLD, 16));

        outputCardPanel = new JPanel(new BorderLayout());
        outputCardPanel.setBorder(BorderFactory.createLineBorder(new Color(0xE5E7EB), 1, true));

        outputCardHeader = new JPanel(new BorderLayout());
        outputCardHeader.setBorder(new EmptyBorder(10, 12, 10, 10));
        outputValueLabel = new JLabel("Encrypted Text:");
        outputValueLabel.setFont(uiFont(Font.PLAIN, 14));

        copyButton = new JButton("Copy");
        copyButton.setFont(uiFont(Font.PLAIN, 13));
        copyButton.setPreferredSize(new Dimension(74, 34));
        copyButton.setFocusable(false);
        copyButton.addActionListener(this);
        installHover(copyButton);

        outputCardHeader.add(outputValueLabel, BorderLayout.WEST);
        outputCardHeader.add(copyButton, BorderLayout.EAST);

        outputArea = new JTextArea(4, 48);
        outputArea.setEditable(false);
        outputArea.setLineWrap(true);
        outputArea.setWrapStyleWord(true);
        outputArea.setFont(monospaceFont(13));
        outputArea.setBorder(new EmptyBorder(12, 12, 12, 12));

        outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(BorderFactory.createEmptyBorder());

        outputBodyPanel = new JPanel(new BorderLayout());
        outputBodyPanel.add(outputScrollPane, BorderLayout.CENTER);

        outputCardPanel.add(outputCardHeader, BorderLayout.NORTH);
        outputCardPanel.add(outputBodyPanel, BorderLayout.CENTER);

        textFooterPanel = new JPanel(new BorderLayout());
        textFooterPanel.setOpaque(false);
        textFooterPanel.setBorder(new EmptyBorder(0, 0, 8, 0));

        textPanel.add(algorithmPanel);
        textPanel.add(Box.createVerticalStrut(34));
        textPanel.add(inputLabel);
        textPanel.add(Box.createVerticalStrut(12));
        textPanel.add(inputScrollPane);
        textPanel.add(Box.createVerticalStrut(20));
        textPanel.add(buttonPanel);
        textPanel.add(Box.createVerticalStrut(14));
        textPanel.add(statusPanel);
        textPanel.add(Box.createVerticalStrut(22));
        textPanel.add(sectionDivider);
        textPanel.add(Box.createVerticalStrut(24));
        textPanel.add(outputLabel);
        textPanel.add(Box.createVerticalStrut(12));
        textPanel.add(outputCardPanel);
    }

    private void configureActionButton(JButton button) {
        button.setFont(uiFont(Font.PLAIN, 15));
        button.setFocusable(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1, true));
        installHover(button);
    }

    private void configureGhostControl(JButton button, String tooltip) {
        button.setPreferredSize(new Dimension(34, 28));
        button.setFocusable(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(0xD1D5DB), 1, true));
        button.setContentAreaFilled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setToolTipText(tooltip);
        button.setFont(uiFont(Font.PLAIN, 14));
        installHover(button);
    }

    private void installHover(JButton button) {
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                Color hover = (Color) button.getClientProperty("hoverColor");
                Color border = (Color) button.getClientProperty("hoverBorderColor");
                if (hover != null) {
                    button.setBackground(hover);
                }
                if (border != null) {
                    button.setBorder(BorderFactory.createLineBorder(border, 1, true));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Color base = (Color) button.getClientProperty("baseColor");
                Color border = (Color) button.getClientProperty("borderColor");
                if (base != null) {
                    button.setBackground(base);
                }
                if (border != null) {
                    button.setBorder(BorderFactory.createLineBorder(border, 1, true));
                }
            }
        });
    }

    private void applyTheme() {
        Theme theme = darkMode ? Theme.dark() : Theme.light();
        rootPanel.setBackground(theme.appBackground);
        cardPanel.setFillColor(theme.cardBackground);
        cardPanel.setStrokeColor(theme.border);

        textHeaderPanel.setBackground(theme.headerBackground);
        textHeaderPanel.setBorder(new EmptyBorder(16, 18, 16, 18));
        titleLabel.setForeground(theme.textPrimary);

        textPanel.setBackground(theme.cardBackground);
        textPanel.setOpaque(true);
        textFooterPanel.setBackground(theme.cardBackground);

        algorithmPanel.setBackground(theme.cardBackground);
        inputLabel.setForeground(theme.textPrimary);
        outputLabel.setForeground(theme.textPrimary);

        styleComboBox(theme);

        inputArea.setBackground(theme.inputBackground);
        inputArea.setForeground(theme.textPrimary);
        inputArea.setCaretColor(theme.textPrimary);
        inputScrollPane.setBackground(theme.inputBackground);
        inputScrollPane.setBorder(BorderFactory.createLineBorder(theme.border, 1, true));
        inputScrollPane.getViewport().setBackground(theme.inputBackground);

        outputCardPanel.setBackground(theme.outputCardBackground);
        outputCardPanel.setBorder(BorderFactory.createLineBorder(theme.border, 1, true));
        outputCardHeader.setBackground(theme.outputCardBackground);
        outputCardHeader.setBorder(new EmptyBorder(10, 12, 10, 10));
        outputValueLabel.setForeground(theme.textSecondary);

        outputBodyPanel.setBackground(theme.outputBodyBackground);
        outputBodyPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, theme.border));
        outputScrollPane.getViewport().setBackground(theme.outputBodyBackground);
        outputArea.setBackground(theme.outputBodyBackground);
        outputArea.setForeground(theme.textPrimary);
        outputArea.setCaretColor(theme.textPrimary);

        statusPanel.setBackground(theme.cardBackground);
        sectionDivider.setForeground(theme.border);
        sectionDivider.setBackground(theme.border);

        stylePrimaryButton(encrypt, theme);
        styleSecondaryButton(newKey, theme);
        styleSecondaryButton(decrypt, theme);
        styleControlButton(copyButton, theme);
        styleControlButton(themeToggle, theme);
        styleControlButton(quit, theme);

        themeToggle.setText(darkMode ? "☀" : "☾");

        if (statusLabel.getClientProperty("statusState") == null) {
            setStatus("Ready", StatusType.INFO);
        } else {
            refreshStatusColor(theme);
        }
    }

    private void styleComboBox(Theme theme) {
        Color fieldBackground = darkMode ? new Color(0xE5E7EB) : theme.inputBackground;
        algorithmSelector.setBackground(fieldBackground);
        algorithmSelector.setForeground(darkMode ? Color.BLACK : theme.textPrimary);
        algorithmSelector.setBorder(BorderFactory.createLineBorder(theme.border, 1, true));

        Theme activeTheme = theme;
        boolean isDark = darkMode;
        algorithmSelector.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus
            ) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBorder(new EmptyBorder(6, 10, 6, 10));

                if (index == -1) {
                    label.setBackground(fieldBackground);
                    label.setForeground(isDark ? Color.BLACK : activeTheme.textPrimary);
                } else if (isSelected) {
                    label.setBackground(activeTheme.accent);
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(isDark ? new Color(0x1F2937) : Color.WHITE);
                    label.setForeground(isDark ? Color.WHITE : activeTheme.textPrimary);
                }
                return label;
            }
        });

        UIManager.put("ComboBox.selectionBackground", theme.accent);
        UIManager.put("ComboBox.selectionForeground", Color.WHITE);
        JLabel label = (JLabel) algorithmPanel.getComponent(0);
        label.setForeground(theme.textPrimary);
    }

    private void stylePrimaryButton(JButton button, Theme theme) {
        setButtonPalette(button, theme.accent, theme.accentHover, theme.accentBorder, theme.accentHoverBorder, Color.WHITE);
    }

    private void styleSecondaryButton(JButton button, Theme theme) {
        setButtonPalette(
                button,
                theme.secondaryButtonBackground,
                theme.secondaryButtonHover,
                theme.secondaryButtonBorder,
                theme.secondaryButtonHoverBorder,
                theme.textPrimary
        );
    }

    private void styleControlButton(JButton button, Theme theme) {
        setButtonPalette(button, theme.controlBackground, theme.controlHover, theme.controlBorder, theme.controlHoverBorder, theme.textPrimary);
    }

    private void setButtonPalette(
            JButton button,
            Color base,
            Color hover,
            Color border,
            Color hoverBorder,
            Color foreground
    ) {
        button.putClientProperty("baseColor", base);
        button.putClientProperty("hoverColor", hover);
        button.putClientProperty("borderColor", border);
        button.putClientProperty("hoverBorderColor", hoverBorder);

        button.setBackground(base);
        button.setForeground(foreground);
        button.setBorder(BorderFactory.createLineBorder(border, 1, true));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();

        if (source == newKey) {
            generateKey();
        } else if (source == encrypt) {
            encryptText();
        } else if (source == decrypt) {
            decryptText();
        } else if (source == quit) {
            quit();
        } else if (source == copyButton) {
            copyOutput();
        } else if (source == themeToggle) {
            darkMode = !darkMode;
            applyTheme();
        } else if (source == algorithmSelector) {
            generateKey();
        }
    }

    public void generateKey() {
        generateSubstitutionMaps();

        String selectedAlgorithm = selectedAlgorithm();
        try {
            if (ALGO_AES.equals(selectedAlgorithm)) {
                aesKey = generateSymmetricKey("AES", 128);
            } else if (ALGO_DES.equals(selectedAlgorithm)) {
                desKey = generateSymmetricKey("DES", 56);
            } else if (ALGO_RSA.equals(selectedAlgorithm)) {
                rsaKeyPair = generateRsaKeyPair();
            } else if (ALGO_CAESAR.equals(selectedAlgorithm)) {
                caesarShift = secureRandom.nextInt(25) + 1;
            }

            outputValueLabel.setText("Encrypted Text:");
            outputArea.setText("");
            setStatus("Key generated for " + selectedAlgorithm, StatusType.SUCCESS);
        } catch (GeneralSecurityException ex) {
            setStatus("Failed to generate key for " + selectedAlgorithm, StatusType.ERROR);
        }
    }

    public void encryptText() {
        String message = inputArea.getText();
        if (message == null || message.trim().isEmpty()) {
            setStatus("Invalid input", StatusType.ERROR);
            outputArea.setText("");
            return;
        }

        String selectedAlgorithm = selectedAlgorithm();
        try {
            String encrypted = encryptByAlgorithm(message, selectedAlgorithm);
            outputValueLabel.setText("Encrypted Text:");
            outputArea.setText("Encrypted:\n" + encrypted);
            setStatus("Encryption successful", StatusType.SUCCESS);
        } catch (Exception ex) {
            setStatus("Encryption failed: " + compactError(ex), StatusType.ERROR);
        }
    }

    public void decryptText() {
        String message = normalizeInput(inputArea.getText());
        if (message == null || message.trim().isEmpty()) {
            setStatus("Invalid input", StatusType.ERROR);
            outputArea.setText("");
            return;
        }

        String selectedAlgorithm = selectedAlgorithm();
        try {
            String decrypted = decryptByAlgorithm(message, selectedAlgorithm);
            outputValueLabel.setText("Decrypted Text:");
            outputArea.setText("Decrypted:\n" + decrypted);
            setStatus("Decryption successful", StatusType.SUCCESS);
        } catch (Exception ex) {
            if (!ALGO_CAESAR.equals(selectedAlgorithm)) {
                outputValueLabel.setText("Decrypted Text:");
                outputArea.setText("Decrypted:\n" + transformWithMap(message, decryptionMap));
                setStatus("Decryption used fallback mapping", StatusType.INFO);
            } else {
                setStatus("Decryption failed: " + compactError(ex), StatusType.ERROR);
            }
        }
    }

    public void quit() {
        dispose();
        if (exitOnQuit) {
            System.exit(0);
        }
    }

    private void copyOutput() {
        String outputText = outputArea.getText();
        if (outputText == null || outputText.isBlank()) {
            setStatus("Nothing to copy", StatusType.ERROR);
            return;
        }

        String copyValue = outputText;
        if (outputText.startsWith("Encrypted:\n") || outputText.startsWith("Decrypted:\n")) {
            int firstBreak = outputText.indexOf('\n');
            copyValue = firstBreak >= 0 ? outputText.substring(firstBreak + 1) : outputText;
        }

        StringSelection content = new StringSelection(copyValue);
        getToolkit().getSystemClipboard().setContents(content, null);
        setStatus("Output copied", StatusType.SUCCESS);
    }

    private String selectedAlgorithm() {
        Object selection = algorithmSelector.getSelectedItem();
        return selection == null ? ALGO_AES : selection.toString();
    }

    private void generateSubstitutionMaps() {
        encryptionMap.clear();
        decryptionMap.clear();
        list.clear();

        for (int i = ASCII_START; i <= ASCII_END; i++) {
            list.add((char) i);
        }
        java.util.Collections.shuffle(list, secureRandom);

        for (int i = ASCII_START; i <= ASCII_END; i++) {
            char originalChar = (char) i;
            char encryptedChar = list.get(i - ASCII_START);
            encryptionMap.put(originalChar, encryptedChar);
            decryptionMap.put(encryptedChar, originalChar);
        }
    }

    private SecretKey generateSymmetricKey(String algorithm, int keySize) throws GeneralSecurityException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(algorithm);
        keyGenerator.init(keySize, secureRandom);
        return keyGenerator.generateKey();
    }

    private KeyPair generateRsaKeyPair() throws GeneralSecurityException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048, secureRandom);
        return generator.generateKeyPair();
    }

    private String encryptByAlgorithm(String message, String algorithm) throws Exception {
        if (ALGO_AES.equals(algorithm)) {
            if (aesKey == null) {
                aesKey = generateSymmetricKey("AES", 128);
            }
            return encryptSymmetric(message, aesKey, TRANSFORMATION_AES, 16);
        }
        if (ALGO_DES.equals(algorithm)) {
            if (desKey == null) {
                desKey = generateSymmetricKey("DES", 56);
            }
            return encryptSymmetric(message, desKey, TRANSFORMATION_DES, 8);
        }
        if (ALGO_RSA.equals(algorithm)) {
            if (rsaKeyPair == null) {
                rsaKeyPair = generateRsaKeyPair();
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA);
            cipher.init(Cipher.ENCRYPT_MODE, rsaKeyPair.getPublic());
            byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        }
        if (ALGO_CAESAR.equals(algorithm)) {
            return caesarTransform(message, caesarShift);
        }
        throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
    }

    private String decryptByAlgorithm(String message, String algorithm) throws Exception {
        if (ALGO_AES.equals(algorithm)) {
            if (aesKey == null) {
                throw new IllegalStateException("Generate an AES key first");
            }
            return decryptSymmetric(message, aesKey, TRANSFORMATION_AES);
        }
        if (ALGO_DES.equals(algorithm)) {
            if (desKey == null) {
                throw new IllegalStateException("Generate a DES key first");
            }
            return decryptSymmetric(message, desKey, TRANSFORMATION_DES);
        }
        if (ALGO_RSA.equals(algorithm)) {
            if (rsaKeyPair == null) {
                throw new IllegalStateException("Generate an RSA key pair first");
            }
            Cipher cipher = Cipher.getInstance(TRANSFORMATION_RSA);
            cipher.init(Cipher.DECRYPT_MODE, rsaKeyPair.getPrivate());
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(message));
            return new String(decrypted, StandardCharsets.UTF_8);
        }
        if (ALGO_CAESAR.equals(algorithm)) {
            return caesarTransform(message, PRINTABLE_RANGE - caesarShift);
        }
        throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
    }

    private String encryptSymmetric(String message, SecretKey key, String transformation, int ivLength) throws Exception {
        byte[] iv = new byte[ivLength];
        secureRandom.nextBytes(iv);

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv));
        byte[] encrypted = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));

        return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(encrypted);
    }

    private String decryptSymmetric(String payload, SecretKey key, String transformation) throws Exception {
        String[] parts = payload.split(":", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Cipher text must be iv:ciphertext");
        }

        byte[] iv = Base64.getDecoder().decode(parts[0].trim());
        byte[] encrypted = Base64.getDecoder().decode(parts[1].trim());

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

    private String transformWithMap(String message, Map<Character, Character> map) {
        StringBuilder transformedMessage = new StringBuilder();
        for (char letter : message.toCharArray()) {
            transformedMessage.append(map.getOrDefault(letter, letter));
        }
        return transformedMessage.toString();
    }

    private String normalizeInput(String message) {
        if (message == null) {
            return "";
        }

        String normalized = message.strip();
        if (normalized.startsWith("Encrypted Text:")) {
            normalized = normalized.substring("Encrypted Text:".length()).stripLeading();
        }
        if (normalized.startsWith("Encrypted:")) {
            normalized = normalized.substring("Encrypted:".length()).stripLeading();
        }
        if (normalized.startsWith("Decrypted:")) {
            normalized = normalized.substring("Decrypted:".length()).stripLeading();
        }
        return normalized;
    }

    private String compactError(Exception exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return "check input and key";
        }
        return message;
    }

    private void setStatus(String text, StatusType statusType) {
        statusLabel.putClientProperty("statusState", statusType.name());
        statusLabel.setText(text);
        refreshStatusColor(darkMode ? Theme.dark() : Theme.light());
    }

    private void refreshStatusColor(Theme theme) {
        Object state = statusLabel.getClientProperty("statusState");
        String value = state == null ? StatusType.INFO.name() : state.toString();
        if (StatusType.SUCCESS.name().equals(value)) {
            statusLabel.setForeground(theme.successText);
        } else if (StatusType.ERROR.name().equals(value)) {
            statusLabel.setForeground(theme.errorText);
        } else {
            statusLabel.setForeground(theme.textSecondary);
        }
    }

    private Font uiFont(int style, int size) {
        return new Font(activeFontFamily, style, size);
    }

    private Font monospaceFont(int size) {
        String[] choices = {"Menlo", "Consolas", "Monospaced"};
        Set<String> installed = installedFonts();
        for (String choice : choices) {
            if (installed.contains(choice)) {
                return new Font(choice, Font.PLAIN, size);
            }
        }
        return new Font("Monospaced", Font.PLAIN, size);
    }

    private String detectFontFamily() {
        String[] preferred = {"SF Pro Text", "SF Pro Display", "Inter", "Helvetica Neue", "Helvetica", "Arial"};
        Set<String> installed = installedFonts();
        for (String name : preferred) {
            if (installed.contains(name)) {
                return name;
            }
        }
        return "SansSerif";
    }

    private Set<String> installedFonts() {
        String[] familyNames = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        Set<String> fonts = new HashSet<>();
        java.util.Collections.addAll(fonts, familyNames);
        return fonts;
    }

    private enum StatusType {
        INFO,
        SUCCESS,
        ERROR
    }

    private static class Theme {
        final Color appBackground;
        final Color cardBackground;
        final Color headerBackground;
        final Color textPrimary;
        final Color textSecondary;
        final Color accent;
        final Color accentHover;
        final Color accentBorder;
        final Color accentHoverBorder;
        final Color border;
        final Color inputBackground;
        final Color outputCardBackground;
        final Color outputBodyBackground;
        final Color secondaryButtonBackground;
        final Color secondaryButtonHover;
        final Color secondaryButtonBorder;
        final Color secondaryButtonHoverBorder;
        final Color controlBackground;
        final Color controlHover;
        final Color controlBorder;
        final Color controlHoverBorder;
        final Color successText;
        final Color errorText;

        Theme(
                Color appBackground,
                Color cardBackground,
                Color headerBackground,
                Color textPrimary,
                Color textSecondary,
                Color accent,
                Color accentHover,
                Color accentBorder,
                Color accentHoverBorder,
                Color border,
                Color inputBackground,
                Color outputCardBackground,
                Color outputBodyBackground,
                Color secondaryButtonBackground,
                Color secondaryButtonHover,
                Color secondaryButtonBorder,
                Color secondaryButtonHoverBorder,
                Color controlBackground,
                Color controlHover,
                Color controlBorder,
                Color controlHoverBorder,
                Color successText,
                Color errorText
        ) {
            this.appBackground = appBackground;
            this.cardBackground = cardBackground;
            this.headerBackground = headerBackground;
            this.textPrimary = textPrimary;
            this.textSecondary = textSecondary;
            this.accent = accent;
            this.accentHover = accentHover;
            this.accentBorder = accentBorder;
            this.accentHoverBorder = accentHoverBorder;
            this.border = border;
            this.inputBackground = inputBackground;
            this.outputCardBackground = outputCardBackground;
            this.outputBodyBackground = outputBodyBackground;
            this.secondaryButtonBackground = secondaryButtonBackground;
            this.secondaryButtonHover = secondaryButtonHover;
            this.secondaryButtonBorder = secondaryButtonBorder;
            this.secondaryButtonHoverBorder = secondaryButtonHoverBorder;
            this.controlBackground = controlBackground;
            this.controlHover = controlHover;
            this.controlBorder = controlBorder;
            this.controlHoverBorder = controlHoverBorder;
            this.successText = successText;
            this.errorText = errorText;
        }

        static Theme light() {
            return new Theme(
                    new Color(0xF7F8FA),
                    new Color(0xFFFFFF),
                    new Color(0xFBFBFD),
                    new Color(0x111111),
                    new Color(0x6B7280),
                    new Color(0x2563EB),
                    new Color(0x1D4ED8),
                    new Color(0x1D4ED8),
                    new Color(0x1E40AF),
                    new Color(0xE5E7EB),
                    new Color(0xFFFFFF),
                    new Color(0xF9FAFB),
                    new Color(0xF3F4F6),
                    new Color(0xF3F4F6),
                    new Color(0xEAECEF),
                    new Color(0xD1D5DB),
                    new Color(0xBEC4CC),
                    new Color(0xF3F4F6),
                    new Color(0xEAECF0),
                    new Color(0xD1D5DB),
                    new Color(0xBDC5D0),
                    new Color(0x2F855A),
                    new Color(0xB91C1C)
            );
        }

        static Theme dark() {
            return new Theme(
                    new Color(0x111827),
                    new Color(0x1F2937),
                    new Color(0x1A2435),
                    new Color(0xF9FAFB),
                    new Color(0x9CA3AF),
                    new Color(0x3B82F6),
                    new Color(0x2563EB),
                    new Color(0x2563EB),
                    new Color(0x1D4ED8),
                    new Color(0x374151),
                    new Color(0x111827),
                    new Color(0x1F2937),
                    new Color(0x111827),
                    new Color(0x263244),
                    new Color(0x314055),
                    new Color(0x3A4A5E),
                    new Color(0x4B5E78),
                    new Color(0x263244),
                    new Color(0x314055),
                    new Color(0x3A4A5E),
                    new Color(0x4B5E78),
                    new Color(0x7DD3A6),
                    new Color(0xFCA5A5)
            );
        }
    }

    private static class RoundedPanel extends JPanel {
        private final int radius;
        private Color fillColor = new Color(0xFFFFFF);
        private Color strokeColor = new Color(0xE5E7EB);

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        void setFillColor(Color fillColor) {
            this.fillColor = fillColor;
            repaint();
        }

        void setStrokeColor(Color strokeColor) {
            this.strokeColor = strokeColor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(fillColor);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.setColor(strokeColor);
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
        }
    }

    private static class TrafficDot extends JPanel {
        private final Color color;

        TrafficDot(Color color) {
            this.color = color;
            setPreferredSize(new Dimension(14, 14));
            setMinimumSize(new Dimension(14, 14));
            setMaximumSize(new Dimension(14, 14));
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(0, 0, 12, 12);
            g2.dispose();
        }
    }
}
