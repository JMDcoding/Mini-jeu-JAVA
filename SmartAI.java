import java.util.List;

public class SmartAI implements IAIStrategy {
    private String frequence = "ESAINTRULODCPMVGFBHQJYZKWX";

    @Override
    public char choisirLettre(List<Character> lettresEssayees, String motADeviner) {
        for (int i = 0; i < frequence.length(); i++) {
            char c = frequence.charAt(i);
            if (!lettresEssayees.contains(c)) {
                return c;
            }
        }
        return ' '; // nE DEVRAIT PAS ARRIVER
    }

    @Override
    public String getNom() {
        return "Stratégique";
    }
}
