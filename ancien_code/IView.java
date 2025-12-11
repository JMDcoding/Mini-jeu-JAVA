import java.util.List;

// Interface qui définit les méthodes que toute "Vue" (Console ou Graphique) doit posséder.
// Cela permet de changer l'affichage sans modifier le code du jeu.
public interface IView {
    void afficherMenu();
    void afficherMessage(String message);
    void afficherMessageErreur(String message);
    void afficherMessageSucces(String message);
    void afficherPendu(int vies);
    void afficherEtatJeu(char[] motCache, int vies, List<Character> lettresEssayees);
    void afficherEtatJeuDuel(char[] motCache, int vies, List<Character> lettresEssayees, List<Character> lettresTrouveesJoueur, List<Character> lettresTrouveesIA);
    String demanderSaisie(String prompt);
    int demanderEntier(String prompt);
    boolean demanderRejouer();
}
