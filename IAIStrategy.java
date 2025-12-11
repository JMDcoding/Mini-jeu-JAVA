import java.util.List;

public interface IAIStrategy {
    String choisirCoup(List<Character> lettresEssayees, String motADeviner);
    String getNom();
}
