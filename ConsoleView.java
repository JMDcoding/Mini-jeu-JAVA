import java.util.Scanner;
import java.util.List;

public class ConsoleView implements IView {
    private Scanner scanner;

    // Couleurs ANSI
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";
    public static final String MAGENTA = "\u001B[35m";

    public ConsoleView() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void afficherMenu() {
        System.out.println(CYAN + "\n--- MENU DU JEU ---" + RESET);
        System.out.println("1. Jouer une partie");
        System.out.println("2. Jouer contre l'IA (Duel)");
        System.out.println("3. Lancer les tests unitaires");
        System.out.println("4. Quitter");
    }

    @Override
    public void afficherMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void afficherMessageErreur(String message) {
        System.out.println(RED + message + RESET);
    }

    @Override
    public void afficherMessageSucces(String message) {
        System.out.println(GREEN + message + RESET);
    }

    @Override
    public void afficherPendu(int vies) {
        System.out.println(RED);
        switch (vies) {
            case 6: System.out.println("\n\n\n\n\n___"); break;
            case 5: System.out.println("\n |\n |\n |\n |\n___"); break;
            case 4: System.out.println(" ______\n |\n |\n |\n |\n___"); break;
            case 3: System.out.println(" ______\n |    |\n |\n |\n |\n___"); break;
            case 2: System.out.println(" ______\n |    |\n |    O\n |\n |\n___"); break;
            case 1: System.out.println(" ______\n |    |\n |    O\n |   /|\\\n |\n___"); break;
            case 0: System.out.println(" ______\n |    |\n |    O\n |   /|\\\n |   / \\\n___"); break;
        }
        System.out.println(RESET);
    }

    @Override
    public void afficherEtatJeu(char[] motCache, int vies, List<Character> lettresEssayees) {
        System.out.println("\nMot à deviner : " + YELLOW + String.valueOf(motCache) + RESET);
        System.out.println("Vies restantes : " + (vies > 2 ? GREEN : RED) + vies + RESET);
        System.out.println("Lettres essayées : " + lettresEssayees);
    }

    @Override
    public void afficherEtatJeuDuel(char[] motCache, int vies, List<Character> lettresEssayees, List<Character> lettresTrouveesJoueur, List<Character> lettresTrouveesIA) {
        StringBuilder sb = new StringBuilder();
        for (char c : motCache) {
            if (c == '_') {
                sb.append("_");
            } else {
                if (lettresTrouveesJoueur.contains(c)) {
                    sb.append(GREEN).append(c).append(RESET);
                } else if (lettresTrouveesIA.contains(c)) {
                    sb.append(MAGENTA).append(c).append(RESET);
                } else {
                    sb.append(c);
                }
            }
        }
        System.out.println("\nMot à deviner : " + sb.toString());
        System.out.println("Lettres essayées : " + lettresEssayees);
        System.out.println("Légende : " + GREEN + "JOUEUR" + RESET + " vs " + MAGENTA + "IA" + RESET);
    }

    @Override
    public String demanderSaisie(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    @Override
    public int demanderEntier(String prompt) {
        System.out.print(prompt);
        if (scanner.hasNextInt()) {
            int val = scanner.nextInt();
            scanner.nextLine();
            return val;
        } else {
            scanner.nextLine();
            return -1;
        }
    }
}
