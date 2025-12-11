import java.util.List;

public interface IAIStrategy {
    char choisirLettre(List<Character> lettresEssayees, String motADeviner);
    String getNom();
}
