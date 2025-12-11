import java.util.List;

// IA "Intelligente" qui utilise la fréquence des lettres et tente de deviner le mot
public class SmartAI implements IAIStrategy {
    // Ordre de fréquence des lettres en français (du plus fréquent au moins fréquent)
    private String frequence = "ESAINTRULODCPMVGFBHQJYZKWX";

    @Override
    public String choisirCoup(List<Character> lettresEssayees, String motADeviner) {
        // 1. Analyser l'état actuel du mot
        int lettresTrouvees = 0;
        int lettresUniques = 0;
        
        for (int i = 0; i < motADeviner.length(); i++) {
            char c = motADeviner.charAt(i);
            // On compte chaque lettre unique du mot
            if (motADeviner.indexOf(c) == i) {
                lettresUniques++;
                // Si cette lettre a déjà été trouvée
                if (lettresEssayees.contains(c)) {
                    lettresTrouvees++;
                }
            }
        }

        // 2. Si l'IA a trouvé plus de 60% des lettres, elle tente de deviner le mot complet
        double ratio = (double) lettresTrouvees / lettresUniques;
        if (lettresUniques > 0 && ratio > 0.6) {
            return motADeviner;
        }

        // 3. Sinon, elle propose la lettre la plus fréquente non encore essayée
        for (int i = 0; i < frequence.length(); i++) {
            char c = frequence.charAt(i);
            if (!lettresEssayees.contains(c)) {
                return String.valueOf(c);
            }
        }
        
        return " "; // Ne devrait pas arriver
    }

    @Override
    public String getNom() {
        return "Stratégique";
    }
}
