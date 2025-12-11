import java.util.Scanner;
import java.util.List;

// Gestion de l'affichage dans la console (texte noir sur blanc)
public class AffichageConsole implements Affichage {
    private Scanner scanner;

    // Codes couleurs pour la console (ne marche pas sur tous les terminaux Windows par défaut, mais souvent ok)
    public static final String RESET = "\u001B[0m";
    public static final String ROUGE = "\u001B[31m";
    public static final String VERT = "\u001B[32m";
    public static final String JAUNE = "\u001B[33m";
    public static final String CYAN = "\u001B[36m";

    public AffichageConsole() {
        this.scanner = new Scanner(System.in);
    }

    @Override
    public void afficherMenu() {
        System.out.println(CYAN + "\n--- MENU DU JEU ---" + RESET);
        System.out.println("1. Jouer une partie Solo");
        System.out.println("2. Jouer contre l'IA (Duel)");
        System.out.println("3. Quitter");
        System.out.println("4. Revoir une partie (Replay)");
        System.out.println("5. Voir les statistiques");
    }

    @Override
    public void afficherMessage(String message) {
        System.out.println(message);
    }

    @Override
    public void afficherErreur(String message) {
        System.out.println(ROUGE + "ERREUR : " + message + RESET);
    }

    @Override
    public void afficherSucces(String message) {
        System.out.println(VERT + message + RESET);
    }

    @Override
    public void dessinerPendu(int vies) {
        System.out.println(ROUGE);
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
    public void afficherEtatPartie(char[] motCache, int vies, List<Character> lettresEssayees) {
        System.out.println("\nMot à deviner : " + JAUNE + String.valueOf(motCache) + RESET);
        System.out.println("Vies restantes : " + vies);
        System.out.println("Lettres déjà essayées : " + lettresEssayees);
    }

    @Override
    public void afficherEtatDuel(char[] motCache, int vies, List<Character> lettresEssayees, List<Character> lettresJoueur, List<Character> lettresIA) {
        afficherEtatPartie(motCache, vies, lettresEssayees);
        System.out.println("Score Joueur : " + lettresJoueur.size() + " lettres trouvées.");
        System.out.println("Score IA : " + lettresIA.size() + " lettres trouvées.");
    }

    @Override
    public String demanderTexte(String question) {
        System.out.print(question);
        return scanner.nextLine();
    }

    @Override
    public int demanderNombre(String question) {
        System.out.print(question);
        if (scanner.hasNextInt()) {
            int val = scanner.nextInt();
            scanner.nextLine(); // Consommer le retour à la ligne
            return val;
        } else {
            scanner.nextLine(); // Consommer l'entrée invalide
            return -1;
        }
    }

    @Override
    public boolean demanderRejouer() {
        String reponse = demanderTexte("Voulez-vous rejouer ? (O/N) : ");
        return reponse.equalsIgnoreCase("O");
    }
}