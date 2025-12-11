import java.util.List;

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
}
