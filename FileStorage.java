import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileStorage implements IStorage {
    private String filename;

    public FileStorage(String filename) {
        this.filename = filename;
    }

    @Override
    public void sauvegarderPartie(String mot, boolean gagne, int vies, long duree, List<String> coups) {
        try (FileWriter fw = new FileWriter(filename, true);
             PrintWriter pw = new PrintWriter(fw)) {
            
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String dateStr = now.format(formatter);
            String resultat = gagne ? "GAGNÉ" : "PERDU";
            
            // Format: DATE|RESULTAT|MOT|VIES|DUREE|COUPS
            pw.println(dateStr + "|" + resultat + "|" + mot + "|" + vies + "|" + duree + "|" + String.join(",", coups));
            
        } catch (IOException e) {
            System.out.println("Erreur sauvegarde : " + e.getMessage());
        }
    }

    @Override
    public List<String> chargerHistorique() {
        List<String> lignes = new ArrayList<>();
        File file = new File(filename);
        if (!file.exists()) return lignes;

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                lignes.add(sc.nextLine());
            }
        } catch (IOException e) {
            System.out.println("Erreur lecture : " + e.getMessage());
        }
        return lignes;
    }
}
