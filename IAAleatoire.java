import java.util.List;
import java.util.Random;

// IA "Facile" : Choisit des lettres au hasard
public class IAAleatoire implements StrategieIA {
    private Random generateurAleatoire = new Random();

    @Override
    public char choisirCoup(char[] motCache, List<Character> lettresEssayees) {
        char lettre;
        int essais = 0;
        
        // On cherche une lettre qui n'a pas encore été jouée
        do {
            lettre = (char) ('A' + generateurAleatoire.nextInt(26)); // Lettre entre A et Z
            essais++;
        } while (lettresEssayees.contains(lettre) && essais < 100);
        
        // Sécurité : si on ne trouve pas au hasard, on prend la première disponible
        if (lettresEssayees.contains(lettre)) {
             for(char c = 'A'; c <= 'Z'; c++) {
                 if(!lettresEssayees.contains(c)) {
                     return c;
                 }
             }
        }
        
        return lettre;
    }
}