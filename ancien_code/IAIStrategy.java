import java.util.List;

// Interface pour la stratégie de l'IA (Pattern Strategy)
// Permet d'avoir plusieurs niveaux d'intelligence (Aléatoire, Smart, etc.)
public interface IAIStrategy {
    // L'IA choisit un coup (une lettre ou un mot complet)
    String choisirCoup(List<Character> lettresEssayees, String motADeviner);
    
    // Nom de la stratégie (ex: "Facile", "Difficile")
    String getNom();
}
