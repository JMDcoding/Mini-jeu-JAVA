import javax.swing.JOptionPane;

public class monjeu {
    public static void main(String[] args) {
        // Demander à l'utilisateur le mode d'affichage
        String[] options = {"Console", "Graphique (Swing)"};
        int choix = JOptionPane.showOptionDialog(null, "Choisissez le mode d'affichage", "Mode de jeu",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

        IView view;
        if (choix == 0) {
            view = new ConsoleView();
        } else {
            view = new SwingView();
        }
        
        // Initialisation du jeu
        HangmanGame game = new HangmanGame(view);
        
        // Lancer le jeu dans un thread séparé pour ne pas bloquer l'EDT si on est en mode graphique
        new Thread(() -> {
            game.start();
        }).start();
    }
}
