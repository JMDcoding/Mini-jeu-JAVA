import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SwingView implements IView {
    private JFrame frame;
    private JPanel mainPanel;
    private CardLayout cardLayout;
    
    // Menu Components
    private JPanel menuPanel;
    
    // Game Components
    private JPanel gamePanel;
    private JLabel wordLabel;
    private JLabel messageLabel;
    private JLabel statusLabel;
    private JPanel hangmanPanel;
    private JPanel keyboardPanel;
    private JTextArea infoArea;
    private List<JButton> letterButtons;
    
    private String currentInput = null;
    private final Object inputLock = new Object();
    private int currentVies = 6;

    public SwingView() {
        initializeUI();
    }

    private void initializeUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {}

        frame = new JFrame("Jeu du Pendu - Java Edition");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 750);
        frame.setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        
        createMenuPanel();
        createGamePanel();
        
        mainPanel.add(menuPanel, "MENU");
        mainPanel.add(gamePanel, "GAME");
        
        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void createMenuPanel() {
        menuPanel = new JPanel(new GridBagLayout());
        menuPanel.setBackground(new Color(240, 248, 255));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 50, 10, 50);

        JLabel title = new JLabel("LE JEU DU PENDU", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 48));
        title.setForeground(new Color(25, 25, 112));
        menuPanel.add(title, gbc);

        gbc.insets = new Insets(20, 100, 20, 100);
        
        addButton(menuPanel, "1. Partie Solo", "1", gbc);
        addButton(menuPanel, "2. Duel contre IA", "2", gbc);
        addButton(menuPanel, "3. Tests Unitaires", "3", gbc);
        addButton(menuPanel, "4. Quitter", "4", gbc);
    }

    private void addButton(JPanel panel, String text, String value, GridBagConstraints gbc) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        btn.setFocusPainted(false);
        btn.setBackground(Color.WHITE);
        btn.addActionListener(e -> submitInput(value));
        panel.add(btn, gbc);
    }

    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout(10, 10));
        gamePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top: Info
        JPanel topPanel = new JPanel(new GridLayout(2, 1));
        statusLabel = new JLabel("Vies: 6", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        
        wordLabel = new JLabel("_______", SwingConstants.CENTER);
        wordLabel.setFont(new Font("Monospaced", Font.BOLD, 40));
        wordLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        topPanel.add(statusLabel);
        topPanel.add(wordLabel);
        gamePanel.add(topPanel, BorderLayout.NORTH);

        // Center: Drawing
        hangmanPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawHangman(g, currentVies);
            }
        };
        hangmanPanel.setBackground(Color.WHITE);
        hangmanPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        gamePanel.add(hangmanPanel, BorderLayout.CENTER);

        // Right: Logs
        infoArea = new JTextArea(20, 25);
        infoArea.setEditable(false);
        infoArea.setFont(new Font("Consolas", Font.PLAIN, 12));
        gamePanel.add(new JScrollPane(infoArea), BorderLayout.EAST);

        // Bottom: Keyboard & Message
        JPanel bottomPanel = new JPanel(new BorderLayout());
        
        messageLabel = new JLabel("Choisissez une lettre", SwingConstants.CENTER);
        messageLabel.setFont(new Font("Segoe UI", Font.ITALIC, 16));
        messageLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        bottomPanel.add(messageLabel, BorderLayout.NORTH);

        keyboardPanel = new JPanel(new GridLayout(3, 10, 5, 5));
        letterButtons = new java.util.ArrayList<>();
        
        for (char c = 'A'; c <= 'Z'; c++) {
            String letter = String.valueOf(c);
            JButton btn = new JButton(letter);
            btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btn.addActionListener(e -> {
                btn.setEnabled(false);
                submitInput(letter);
            });
            letterButtons.add(btn);
            keyboardPanel.add(btn);
        }
        
        // Add a text field for full word guessing
        JPanel inputPanel = new JPanel(new FlowLayout());
        JTextField wordField = new JTextField(15);
        JButton wordBtn = new JButton("Deviner le mot");
        wordBtn.addActionListener(e -> {
            if(!wordField.getText().isEmpty()) {
                submitInput(wordField.getText());
                wordField.setText("");
            }
        });
        inputPanel.add(new JLabel("Ou mot complet : "));
        inputPanel.add(wordField);
        inputPanel.add(wordBtn);
        
        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.add(keyboardPanel, BorderLayout.CENTER);
        controlsPanel.add(inputPanel, BorderLayout.SOUTH);
        
        bottomPanel.add(controlsPanel, BorderLayout.CENTER);
        gamePanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void submitInput(String input) {
        synchronized (inputLock) {
            currentInput = input;
            inputLock.notifyAll();
        }
    }

    private void drawHangman(Graphics g, int vies) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setStroke(new BasicStroke(4));
        
        int w = hangmanPanel.getWidth();
        int h = hangmanPanel.getHeight();
        
        g2.setColor(Color.WHITE);
        g2.fillRect(0,0,w,h);
        g2.setColor(new Color(50, 50, 50));
        
        // Base coordinates
        int baseX = w / 2 - 100;
        int baseY = h - 50;
        
        if (vies <= 5) g2.drawLine(baseX, baseY, baseX + 200, baseY); // Base
        if (vies <= 4) g2.drawLine(baseX + 50, baseY, baseX + 50, baseY - 300);  // Poteau
        if (vies <= 3) g2.drawLine(baseX + 50, baseY - 300, baseX + 150, baseY - 300);    // Traverse
        if (vies <= 2) g2.drawLine(baseX + 150, baseY - 300, baseX + 150, baseY - 250);   // Corde
        
        g2.setColor(new Color(139, 0, 0)); // Dark Red for the stickman
        if (vies <= 1) {
            g2.setStroke(new BasicStroke(3));
            g2.drawOval(baseX + 130, baseY - 250, 40, 40);    // Tête
        }
        if (vies <= 0) {
            g2.drawLine(baseX + 150, baseY - 210, baseX + 150, baseY - 130); // Corps
            g2.drawLine(baseX + 150, baseY - 200, baseX + 120, baseY - 160); // Bras G
            g2.drawLine(baseX + 150, baseY - 200, baseX + 180, baseY - 160); // Bras D
            g2.drawLine(baseX + 150, baseY - 130, baseX + 120, baseY - 80); // Jambe G
            g2.drawLine(baseX + 150, baseY - 130, baseX + 180, baseY - 80); // Jambe D
        }
    }

    @Override
    public void afficherMenu() {
        SwingUtilities.invokeLater(() -> cardLayout.show(mainPanel, "MENU"));
    }

    @Override
    public void afficherMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            infoArea.append(message + "\n");
            infoArea.setCaretPosition(infoArea.getDocument().getLength());
            messageLabel.setText(message);
        });
    }

    @Override
    public void afficherMessageErreur(String message) {
        SwingUtilities.invokeLater(() -> {
            infoArea.append("ERREUR: " + message + "\n");
            JOptionPane.showMessageDialog(frame, message, "Erreur", JOptionPane.ERROR_MESSAGE);
        });
    }

    @Override
    public void afficherMessageSucces(String message) {
        SwingUtilities.invokeLater(() -> {
            infoArea.append("SUCCES: " + message + "\n");
            messageLabel.setText(message);
            messageLabel.setForeground(new Color(0, 100, 0));
        });
    }

    @Override
    public void afficherPendu(int vies) {
        this.currentVies = vies;
        SwingUtilities.invokeLater(() -> {
            statusLabel.setText("Vies restantes: " + vies);
            if (vies <= 2) statusLabel.setForeground(Color.RED);
            else statusLabel.setForeground(Color.BLACK);
            hangmanPanel.repaint();
        });
    }

    @Override
    public void afficherEtatJeu(char[] motCache, int vies, List<Character> lettresEssayees) {
        SwingUtilities.invokeLater(() -> {
            cardLayout.show(mainPanel, "GAME");
            
            StringBuilder sb = new StringBuilder();
            for (char c : motCache) {
                sb.append(c).append(" ");
            }
            wordLabel.setText(sb.toString());
            
            // Update keyboard state
            for (JButton btn : letterButtons) {
                String txt = btn.getText();
                if (txt.length() == 1) {
                    char c = txt.charAt(0);
                    btn.setEnabled(!lettresEssayees.contains(c));
                }
            }
            
            afficherPendu(vies);
        });
    }

    @Override
    public void afficherEtatJeuDuel(char[] motCache, int vies, List<Character> lettresEssayees, List<Character> lettresTrouveesJoueur, List<Character> lettresTrouveesIA) {
        afficherEtatJeu(motCache, vies, lettresEssayees);
        SwingUtilities.invokeLater(() -> {
            infoArea.append("Joueur: " + lettresTrouveesJoueur + "\n");
            infoArea.append("IA: " + lettresTrouveesIA + "\n");
        });
    }

    @Override
    public String demanderSaisie(String prompt) {
        SwingUtilities.invokeLater(() -> messageLabel.setText(prompt));
        
        synchronized (inputLock) {
            try {
                inputLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return currentInput;
    }

    @Override
    public int demanderEntier(String prompt) {
        String res = demanderSaisie(prompt);
        try {
            return Integer.parseInt(res);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
