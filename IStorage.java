import java.util.List;

public interface IStorage {
    void sauvegarderPartie(String mot, boolean gagne, int vies, long duree, List<String> coups);
    List<String> chargerHistorique();
}
