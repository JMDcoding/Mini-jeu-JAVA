import java.util.List;
import java.util.Random;

// IA "Bête" qui choisit des lettres au hasard
public class RandomAI implements IAIStrategy {
    private Random random = new Random();

    @Override
    public String choisirCoup(List<Character> lettresEssayees, String motADeviner) {
        char lettre;
        int tentatives = 0;
        
        // On essaie de trouver une lettre au hasard qui n'a pas encore été jouée
        do {
            lettre = (char) ('A' + random.nextInt(26)); // Génère une lettre entre A et Z
            tentatives++;
        } while (lettresEssayees.contains(lettre) && tentatives < 100);
        
        // Si après 100 essais on tombe toujours sur une lettre déjà jouée (très rare),
        // on parcourt l'alphabet pour trouver la première lettre libre.
        if (lettresEssayees.contains(lettre)) {
             for(char c = 'A'; c <= 'Z'; c++) {
                 if(!lettresEssayees.contains(c)) {
                     return String.valueOf(c);
                 }
             }
        }
        
        return String.valueOf(lettre);
    }

    @Override
    public String getNom() {
        return "Aléatoire";
    }
}
