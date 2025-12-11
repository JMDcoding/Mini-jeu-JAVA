public class monjeu {
    public static void main(String[] args) {
        // Injection de dependances (DIP)
        IView view = new ConsoleView();
        
        // Initialisation du jeu
        HangmanGame game = new HangmanGame(view);
        game.start();
    }
}
