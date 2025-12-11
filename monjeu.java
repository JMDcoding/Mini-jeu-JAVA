import javax.swing.JOptionPane;

public class monjeu {
    public static void main(String[] args) {
        // Demander à l'utilisateur quel mode d'affichage il préfère
        String[] options = {"Console", "Graphique (Fenêtre)"};
        int choix = JOptionPane.showOptionDialog(null, 
            "Comment voulez-vous jouer ?", 
            "Choix de l'affichage",
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            options, 
            options[1]); // Par défaut Graphique

        Affichage affichage;

        if (choix == 0) {
            // Mode Console
            affichage = new AffichageConsole();
        } else {
            // Mode Graphique
            affichage = new AffichageGraphique();
        }

        // Création et lancement du jeu
        // On utilise un Thread pour ne pas bloquer l'interface graphique
        new Thread(() -> {
            JeuPendu jeu = new JeuPendu(affichage);
            jeu.demarrer();
        }).start();
    }
}
