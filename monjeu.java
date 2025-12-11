public class monjeu {
    public static void main(String[] args) {
        // Injection de dependances (DIP)
        IView view = new ConsoleView();
        IStorage storage = new FileStorage("historique_complet.txt");
        
        // Initialisation du jeu
        HangmanGame game = new HangmanGame(view, storage);
        game.start();
    }
}
