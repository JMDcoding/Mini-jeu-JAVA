import java.util.List;

// Interface pour gérer l'affichage (Console ou Graphique)
public interface Affichage {
    void afficherMenu();
    void afficherMessage(String message);
    void afficherErreur(String message);
    void afficherSucces(String message);
    void dessinerPendu(int vies);
    void afficherEtatPartie(char[] motCache, int vies, List<Character> lettresEssayees);
    void afficherEtatDuel(char[] motCache, int vies, List<Character> lettresEssayees, List<Character> lettresJoueur, List<Character> lettresIA);
    String demanderTexte(String question);
    int demanderNombre(String question);
    boolean demanderRejouer();
}