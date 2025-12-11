import java.util.List;
import java.util.Random;

public class RandomAI implements IAIStrategy {
    private Random random = new Random();

    @Override
    public String choisirCoup(List<Character> lettresEssayees, String motADeviner) {
        char lettre;
        int tentatives = 0;
        do {
            lettre = (char) ('A' + random.nextInt(26));
            tentatives++;
        } while (lettresEssayees.contains(lettre) && tentatives < 100);
        
        // Fallback
        if (lettresEssayees.contains(lettre)) {
             for(char c = 'A'; c <= 'Z'; c++) {
                 if(!lettresEssayees.contains(c)) return String.valueOf(c);
             }
        }
        return String.valueOf(lettre);
    }

    @Override
    public String getNom() {
        return "Aléatoire";
    }
}
