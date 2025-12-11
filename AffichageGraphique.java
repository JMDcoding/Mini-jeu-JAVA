import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CountDownLatch;

// Gestion de l'affichage graphique (fenêtre)
public class AffichageGraphique implements Affichage {
    private JFrame fenetre;
    private JPanel panneauPrincipal;
    private CardLayout gestionnaireVues;
    
    // Composants du Menu
    private JPanel panneauMenu;
    private JButton boutonSolo;
    private JButton boutonDuel;
    private JButton boutonQuitter;

    // Composants du Jeu
    private JPanel panneauJeu;
    private JLabel labelMot;
    private JLabel labelVies;
    private JLabel labelLettres;
    private JTextField champSaisie;
    private JButton boutonValider;
    private PanneauDessinPendu panneauDessin;
    private JTextArea zoneMessages;

    // Variables pour synchroniser l'interface et le code du jeu
    private String dernierTexteSaisi = null;
    private int dernierChoixMenu = -1;
    private final Object verrou = new Object(); // Pour attendre la réponse de l'utilisateur

    public AffichageGraphique() {
        initialiserFenetre();
    }

    private void initialiserFenetre() {
        fenetre = new JFrame("Le Jeu du Pendu");
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.setSize(800, 600);
        fenetre.setLocationRelativeTo(null); // Centrer

        gestionnaireVues = new CardLayout();
        panneauPrincipal = new JPanel(gestionnaireVues);

        creerPanneauMenu();
        creerPanneauJeu();

        panneauPrincipal.add(panneauMenu, "MENU");
        panneauPrincipal.add(panneauJeu, "JEU");

        fenetre.add(panneauPrincipal);
        fenetre.setVisible(true);
    }

    private void creerPanneauMenu() {
        panneauMenu = new JPanel();
        panneauMenu.setLayout(new BoxLayout(panneauMenu, BoxLayout.Y_AXIS));
        panneauMenu.setBackground(new Color(240, 248, 255)); // Bleu très clair

        JLabel titre = new JLabel("LE JEU DU PENDU");
        titre.setFont(new Font("Arial", Font.BOLD, 30));
        titre.setAlignmentX(Component.CENTER_ALIGNMENT);

        boutonSolo = new JButton("Jouer en Solo");
        boutonDuel = new JButton("Jouer contre l'IA");
        JButton boutonReplay = new JButton("Revoir une partie");
        JButton boutonStats = new JButton("Statistiques");
        boutonQuitter = new JButton("Quitter");

        styleBouton(boutonSolo);
        styleBouton(boutonDuel);
        styleBouton(boutonReplay);
        styleBouton(boutonStats);
        styleBouton(boutonQuitter);

        boutonSolo.addActionListener(e -> debloquerChoixMenu(1));
        boutonDuel.addActionListener(e -> debloquerChoixMenu(2));
        boutonQuitter.addActionListener(e -> debloquerChoixMenu(3));
        boutonReplay.addActionListener(e -> debloquerChoixMenu(4));
        boutonStats.addActionListener(e -> debloquerChoixMenu(5));

        panneauMenu.add(Box.createVerticalStrut(50));
        panneauMenu.add(titre);
        panneauMenu.add(Box.createVerticalStrut(30));
        panneauMenu.add(boutonSolo);
        panneauMenu.add(Box.createVerticalStrut(10));
        panneauMenu.add(boutonDuel);
        panneauMenu.add(Box.createVerticalStrut(10));
        panneauMenu.add(boutonReplay);
        panneauMenu.add(Box.createVerticalStrut(10));
        panneauMenu.add(boutonStats);
        panneauMenu.add(Box.createVerticalStrut(10));
        panneauMenu.add(boutonQuitter);
    }

    private void styleBouton(JButton bouton) {
        bouton.setAlignmentX(Component.CENTER_ALIGNMENT);
        bouton.setFont(new Font("Arial", Font.PLAIN, 18));
        bouton.setMaximumSize(new Dimension(200, 50));
    }

    private void debloquerChoixMenu(int choix) {
        synchronized (verrou) {
            dernierChoixMenu = choix;
            verrou.notifyAll();
        }
    }

    private void creerPanneauJeu() {
        panneauJeu = new JPanel(new BorderLayout());
        
        // Zone du haut : Infos
        JPanel haut = new JPanel(new GridLayout(3, 1));
        labelMot = new JLabel("Mot : ", SwingConstants.CENTER);
        labelMot.setFont(new Font("Monospaced", Font.BOLD, 24));
        labelVies = new JLabel("Vies : 7", SwingConstants.CENTER);
        labelLettres = new JLabel("Lettres : ", SwingConstants.CENTER);
        haut.add(labelMot);
        haut.add(labelVies);
        haut.add(labelLettres);

        // Zone centrale : Dessin
        panneauDessin = new PanneauDessinPendu();

        // Zone du bas : Saisie et Messages
        JPanel bas = new JPanel(new BorderLayout());
        JPanel zoneSaisie = new JPanel();
        champSaisie = new JTextField(5);
        boutonValider = new JButton("Essayer");
        JButton boutonUndo = new JButton("Annuler (<)");
        
        boutonValider.addActionListener(e -> {
            synchronized (verrou) {
                dernierTexteSaisi = champSaisie.getText();
                champSaisie.setText("");
                verrou.notifyAll();
            }
        });

        boutonUndo.addActionListener(e -> {
            synchronized (verrou) {
                dernierTexteSaisi = "<";
                verrou.notifyAll();
            }
        });

        zoneSaisie.add(new JLabel("Entrez une lettre : "));
        zoneSaisie.add(champSaisie);
        zoneSaisie.add(boutonValider);
        zoneSaisie.add(boutonUndo);

        zoneMessages = new JTextArea(3, 40);
        zoneMessages.setEditable(false);
        zoneMessages.setLineWrap(true);
        JScrollPane scrollMessages = new JScrollPane(zoneMessages);

        bas.add(zoneSaisie, BorderLayout.NORTH);
        bas.add(scrollMessages, BorderLayout.CENTER);

        panneauJeu.add(haut, BorderLayout.NORTH);
        panneauJeu.add(panneauDessin, BorderLayout.CENTER);
        panneauJeu.add(bas, BorderLayout.SOUTH);
    }

    @Override
    public void afficherMenu() {
        gestionnaireVues.show(panneauPrincipal, "MENU");
    }

    @Override
    public void afficherMessage(String message) {
        // Si on est sur le menu, on affiche une popup pour que le message soit vu
        if (panneauMenu.isVisible()) {
            JOptionPane.showMessageDialog(fenetre, message);
        } else {
            zoneMessages.append(message + "\n");
            // Faire défiler vers le bas
            zoneMessages.setCaretPosition(zoneMessages.getDocument().getLength());
        }
    }

    @Override
    public void afficherErreur(String message) {
        afficherMessage("ERREUR: " + message);
    }

    @Override
    public void afficherSucces(String message) {
        afficherMessage("SUCCES: " + message);
    }

    @Override
    public void dessinerPendu(int vies) {
        panneauDessin.setVies(vies);
        panneauDessin.repaint();
    }

    @Override
    public void afficherEtatPartie(char[] motCache, int vies, List<Character> lettresEssayees) {
        gestionnaireVues.show(panneauPrincipal, "JEU");
        labelMot.setText("Mot : " + String.valueOf(motCache));
        labelVies.setText("Vies restantes : " + vies);
        labelLettres.setText("Essais : " + lettresEssayees.toString());
    }

    @Override
    public void afficherEtatDuel(char[] motCache, int vies, List<Character> lettresEssayees, List<Character> lettresJoueur, List<Character> lettresIA) {
        afficherEtatPartie(motCache, vies, lettresEssayees);
        afficherMessage("Score Joueur: " + lettresJoueur.size() + " | Score IA: " + lettresIA.size());
    }

    @Override
    public String demanderTexte(String question) {
        afficherMessage(question);
        synchronized (verrou) {
            try {
                dernierTexteSaisi = null;
                while (dernierTexteSaisi == null) {
                    verrou.wait();
                }
                return dernierTexteSaisi;
            } catch (InterruptedException e) {
                return "";
            }
        }
    }

    @Override
    public int demanderNombre(String question) {
        // Utilisé principalement pour le menu dans cette version simplifiée
        // Si on est sur le menu, on attend le clic bouton
        if (panneauMenu.isVisible()) {
            synchronized (verrou) {
                try {
                    dernierChoixMenu = -1;
                    while (dernierChoixMenu == -1) {
                        verrou.wait();
                    }
                    return dernierChoixMenu;
                } catch (InterruptedException e) {
                    return -1;
                }
            }
        }
        // Sinon fallback sur une boite de dialogue
        String res = JOptionPane.showInputDialog(fenetre, question);
        try {
            return Integer.parseInt(res);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public boolean demanderRejouer() {
        int reponse = JOptionPane.showConfirmDialog(fenetre, "Voulez-vous rejouer ?", "Fin de partie", JOptionPane.YES_NO_OPTION);
        return reponse == JOptionPane.YES_OPTION;
    }

    // Classe interne pour dessiner le pendu
    class PanneauDessinPendu extends JPanel {
        private int vies = 7;

        public void setVies(int vies) {
            this.vies = vies;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setStroke(new BasicStroke(3));
            g2.setColor(Color.BLACK);

            int w = getWidth();
            int h = getHeight();
            int baseX = w / 2 - 50;
            int baseY = h - 50;

            // Base
            if (vies <= 6) g2.drawLine(baseX, baseY, baseX + 100, baseY); 
            // Poteau
            if (vies <= 5) g2.drawLine(baseX + 50, baseY, baseX + 50, baseY - 200);
            // Traverse
            if (vies <= 4) g2.drawLine(baseX + 50, baseY - 200, baseX + 150, baseY - 200);
            // Corde
            if (vies <= 3) g2.drawLine(baseX + 150, baseY - 200, baseX + 150, baseY - 150);
            // Tête
            if (vies <= 2) g2.drawOval(baseX + 130, baseY - 150, 40, 40);
            // Corps
            if (vies <= 1) g2.drawLine(baseX + 150, baseY - 110, baseX + 150, baseY - 50);
            // Bras et Jambes (simplifié pour 0 vies)
            if (vies <= 0) {
                g2.drawLine(baseX + 150, baseY - 100, baseX + 120, baseY - 70); // Bras G
                g2.drawLine(baseX + 150, baseY - 100, baseX + 180, baseY - 70); // Bras D
                g2.drawLine(baseX + 150, baseY - 50, baseX + 120, baseY - 10);  // Jambe G
                g2.drawLine(baseX + 150, baseY - 50, baseX + 180, baseY - 10);  // Jambe D
            }
        }
    }
}