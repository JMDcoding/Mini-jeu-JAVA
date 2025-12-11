import java.util.List;

public class SmartAI implements IAIStrategy {
    private String frequence = "ESAINTRULODCPMVGFBHQJYZKWX";

    @Override
    public String choisirCoup(List<Character> lettresEssayees, String motADeviner) {
        // Vérifier si l'IA peut deviner le mot complet
        int lettresTrouvees = 0;
        int lettresUniques = 0;
        
        for (int i = 0; i < motADeviner.length(); i++) {
            char c = motADeviner.charAt(i);
            // Compter les lettres uniques pour le ratio
            if (motADeviner.indexOf(c) == i) {
                lettresUniques++;
                if (lettresEssayees.contains(c)) {
                    lettresTrouvees++;
                }
            }
        }

        // Si plus de 60% des lettres sont trouvées, l'IA tente le mot complet
        if (lettresUniques > 0 && (double) lettresTrouvees / lettresUniques > 0.6) {
            return motADeviner;
        }

        // Sinon, stratégie classique par fréquence
        for (int i = 0; i < frequence.length(); i++) {
            char c = frequence.charAt(i);
            if (!lettresEssayees.contains(c)) {
                return String.valueOf(c);
            }
        }
        return " ";
    }

    @Override
    public String getNom() {
        return "Stratégique";
    }
}
