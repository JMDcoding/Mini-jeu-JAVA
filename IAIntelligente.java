import java.util.List;

// IA "Difficile" : Utilise la fréquence des lettres
public class IAIntelligente implements StrategieIA {
    // Lettres les plus fréquentes en français
    private String ordreFrequence = "ESAINTRULODCPMVGFBHQJYZKWX";

    @Override
    public char choisirCoup(char[] motCache, List<Character> lettresEssayees) {
        // Elle joue la lettre la plus fréquente non encore essayée
        for (int i = 0; i < ordreFrequence.length(); i++) {
            char c = ordreFrequence.charAt(i);
            if (!lettresEssayees.contains(c)) {
                return c;
            }
        }
        return ' '; // Cas impossible
    }
}