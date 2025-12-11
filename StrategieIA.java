import java.util.List;

// Interface pour l'Intelligence Artificielle
public interface StrategieIA {
    // L'IA choisit une lettre en fonction de l'état actuel du mot (avec des _ pour les inconnues)
    char choisirCoup(char[] motCache, List<Character> lettresEssayees);
}