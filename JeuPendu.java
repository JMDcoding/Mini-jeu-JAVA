import java.util.*;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JeuPendu {
    private Affichage affichage;
    private Properties config;
    private Properties stats;
    
    // État du jeu
    private String motSecret;
    private char[] motCache;
    private int vies;
    private List<Character> lettresEssayees = new ArrayList<>();
    private List<Character> scoreJoueur = new ArrayList<>(), scoreIA = new ArrayList<>();
    private StrategieIA ia;
    
    // Pour le Undo (Memento)
    private Stack<EtatJeu> historiqueCoups = new Stack<>();

    // Classe interne pour sauvegarder l'état (Memento)
    private class EtatJeu {
        char[] motCache;
        int vies;
        List<Character> lettresEssayees;
        List<Character> scoreJoueur;
        List<Character> scoreIA;

        EtatJeu(char[] mc, int v, List<Character> le, List<Character> sj, List<Character> si) {
            this.motCache = mc.clone();
            this.vies = v;
            this.lettresEssayees = new ArrayList<>(le);
            this.scoreJoueur = new ArrayList<>(sj);
            this.scoreIA = new ArrayList<>(si);
        }
    }

    public JeuPendu(Affichage affichage) {
        this.affichage = affichage;
        chargerConfiguration();
        chargerStatistiques();
    }

    private void chargerConfiguration() {
        config = new Properties();
        try (FileInputStream fis = new FileInputStream("config.properties")) {
            config.load(fis);
        } catch (IOException e) {
            // Valeurs par défaut si fichier manquant
            config.setProperty("vies_max", "7");
            config.setProperty("fichier_mots", "mots.txt");
            config.setProperty("fichier_stats", "stats.properties");
            config.setProperty("fichier_historique", "historique.txt");
        }
    }

    private void chargerStatistiques() {
        stats = new Properties();
        try (FileInputStream fis = new FileInputStream(config.getProperty("fichier_stats"))) {
            stats.load(fis);
        } catch (IOException e) {
            stats.setProperty("parties_jouees", "0");
            stats.setProperty("victoires", "0");
            stats.setProperty("defaites", "0");
        }
    }

    private void sauvegarderStatistiques() {
        try (FileOutputStream fos = new FileOutputStream(config.getProperty("fichier_stats"))) {
            stats.store(fos, "Statistiques du Jeu Pendu");
        } catch (IOException e) {
            affichage.afficherErreur("Impossible de sauvegarder les stats.");
        }
    }

    private List<String> chargerMots() {
        try {
            return Files.readAllLines(Paths.get(config.getProperty("fichier_mots")));
        } catch (IOException e) {
            return Arrays.asList("JAVA", "CODE", "TEST"); // Fallback
        }
    }

    public void demarrer() {
        while (true) {
            affichage.afficherMenu();
            affichage.afficherMessage("4. Revoir une partie (Replay)");
            affichage.afficherMessage("5. Voir les statistiques");
            
            int choix = affichage.demanderNombre("Choix : ");
            
            if (choix == 3) break;
            else if (choix == 1 || choix == 2) jouer(choix == 2);
            else if (choix == 4) menuReplay();
            else if (choix == 5) afficherStats();
            else affichage.afficherErreur("Choix invalide.");
            
            if (choix <= 2 && !affichage.demanderRejouer()) break;
        }
        System.exit(0);
    }

    private void afficherStats() {
        affichage.afficherMessage("\n--- STATISTIQUES GLOBALES ---");
        affichage.afficherMessage("Parties jouées : " + stats.getProperty("parties_jouees"));
        affichage.afficherMessage("Victoires : " + stats.getProperty("victoires"));
        affichage.afficherMessage("Défaites : " + stats.getProperty("defaites"));
    }

    private void jouer(boolean avecIA) {
        List<String> mots = chargerMots();
        motSecret = mots.get(new Random().nextInt(mots.size()));
        motCache = new String(new char[motSecret.length()]).replace('\0', '_').toCharArray();
        vies = Integer.parseInt(config.getProperty("vies_max"));
        lettresEssayees.clear();
        scoreJoueur.clear(); scoreIA.clear();
        historiqueCoups.clear();
        ia = avecIA ? new IAIntelligente() : null;
        
        // Pour l'historique de la partie courante (Replay)
        List<String> sequenceCoups = new ArrayList<>();

        affichage.afficherMessage("Nouvelle partie " + (avecIA ? "Duel" : "Solo") + " ! (Tapez '<' pour annuler)");

        while (vies > 0 && new String(motCache).contains("_")) {
            affichage.dessinerPendu(vies);
            if (avecIA) affichage.afficherEtatDuel(motCache, vies, lettresEssayees, scoreJoueur, scoreIA);
            else affichage.afficherEtatPartie(motCache, vies, lettresEssayees);

            String input = affichage.demanderTexte("Lettre (ou < pour retour) : ");
            if (input.isEmpty()) continue;

            // Gestion du UNDO
            if (input.equals("<")) {
                if (!historiqueCoups.isEmpty()) {
                    restaurerEtat(historiqueCoups.pop());
                    affichage.afficherMessage("Retour en arrière effectué.");
                    // On retire aussi le dernier coup de la séquence replay si besoin
                    if (!sequenceCoups.isEmpty()) sequenceCoups.remove(sequenceCoups.size() - 1);
                } else {
                    affichage.afficherErreur("Impossible de revenir plus loin.");
                }
                continue;
            }

            char c = Character.toUpperCase(input.charAt(0));
            
            // Sauvegarde de l'état AVANT le coup
            historiqueCoups.push(new EtatJeu(motCache, vies, lettresEssayees, scoreJoueur, scoreIA));

            if (traiterCoup(c, true)) {
                sequenceCoups.add(String.valueOf(c)); // Ajout à l'historique Replay
                
                if (avecIA && vies > 0 && new String(motCache).contains("_")) {
                    affichage.afficherMessage("L'IA joue...");
                    try { Thread.sleep(500); } catch (Exception e) {}
                    char coupIA = ia.choisirCoup(motCache, lettresEssayees);
                    traiterCoup(coupIA, false);
                    sequenceCoups.add("IA:" + coupIA); // Marquer les coups IA
                }
            } else {
                // Si coup invalide, on retire l'état sauvegardé inutilement
                historiqueCoups.pop();
            }
        }
        fin(avecIA, sequenceCoups);
    }

    private void restaurerEtat(EtatJeu etat) {
        this.motCache = etat.motCache;
        this.vies = etat.vies;
        this.lettresEssayees = etat.lettresEssayees;
        this.scoreJoueur = etat.scoreJoueur;
        this.scoreIA = etat.scoreIA;
    }

    private boolean traiterCoup(char c, boolean estJoueur) {
        if (lettresEssayees.contains(c)) {
            if (estJoueur) affichage.afficherErreur("Déjà essayé !");
            return false;
        }
        lettresEssayees.add(c);
        if (motSecret.indexOf(c) >= 0) {
            for (int i = 0; i < motSecret.length(); i++) if (motSecret.charAt(i) == c) motCache[i] = c;
            (estJoueur ? scoreJoueur : scoreIA).add(c);
            affichage.afficherSucces((estJoueur ? "Bravo !" : "L'IA a trouvé !") + " " + c);
        } else {
            affichage.afficherErreur((estJoueur ? "Raté !" : "L'IA a raté.") + " " + c);
            vies--;
        }
        return true;
    }

    private void fin(boolean avecIA, List<String> sequenceCoups) {
        affichage.dessinerPendu(vies);
        boolean gagne = !new String(motCache).contains("_");
        affichage.afficherMessage(gagne ? "GAGNÉ ! Mot : " + motSecret : "PERDU ! Mot : " + motSecret);
        
        // Mise à jour des stats
        int pj = Integer.parseInt(stats.getProperty("parties_jouees")) + 1;
        stats.setProperty("parties_jouees", String.valueOf(pj));
        if (gagne) {
            int v = Integer.parseInt(stats.getProperty("victoires")) + 1;
            stats.setProperty("victoires", String.valueOf(v));
        } else {
            int d = Integer.parseInt(stats.getProperty("defaites")) + 1;
            stats.setProperty("defaites", String.valueOf(d));
        }
        sauvegarderStatistiques();

        String resultat = gagne ? "GAGNÉ" : "PERDU";
        if (avecIA) {
            affichage.afficherMessage("Score Joueur: " + scoreJoueur.size() + " - IA: " + scoreIA.size());
            if (scoreJoueur.size() > scoreIA.size()) { affichage.afficherSucces("Vous avez battu l'IA !"); resultat = "GAGNÉ (DUEL)"; }
            else if (scoreIA.size() > scoreJoueur.size()) { affichage.afficherMessage("L'IA gagne !"); resultat = "PERDU (DUEL)"; }
            else { affichage.afficherMessage("Égalité !"); resultat = "EGALITÉ (DUEL)"; }
        }
        sauvegarderHistorique(resultat, sequenceCoups);
    }

    private void sauvegarderHistorique(String resultat, List<String> sequenceCoups) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(config.getProperty("fichier_historique"), true))) {
            String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            // Format: DATE | RESULTAT | MOT | VIES | SEQUENCE_COUPS
            pw.println(date + "|" + resultat + "|" + motSecret + "|" + vies + "|" + String.join(",", sequenceCoups));
        } catch (IOException e) {
            affichage.afficherErreur("Erreur sauvegarde historique.");
        }
    }

    // --- REPLAY ---
    private void menuReplay() {
        List<String> lignes = new ArrayList<>();
        try {
            lignes = Files.readAllLines(Paths.get(config.getProperty("fichier_historique")));
        } catch (IOException e) {
            affichage.afficherErreur("Aucun historique trouvé.");
            return;
        }

        if (lignes.isEmpty()) {
            affichage.afficherMessage("Historique vide.");
            return;
        }

        affichage.afficherMessage("--- CHOISISSEZ UNE PARTIE À REVOIR ---");
        for (int i = 0; i < lignes.size(); i++) {
            // On affiche juste la date et le résultat pour que ce soit lisible
            String[] parts = lignes.get(i).split("\\|");
            if (parts.length >= 3) {
                affichage.afficherMessage((i + 1) + ". " + parts[0] + " - " + parts[1] + " (" + parts[2] + ")");
            }
        }

        int choix = affichage.demanderNombre("Numéro de la partie : ");
        if (choix > 0 && choix <= lignes.size()) {
            jouerReplay(lignes.get(choix - 1));
        }
    }

    private void jouerReplay(String ligneHistorique) {
        try {
            String[] parts = ligneHistorique.split("\\|");
            // parts[2] = mot, parts[4] = sequence
            if (parts.length < 5) {
                affichage.afficherErreur("Données de replay corrompues ou ancien format.");
                return;
            }

            String motReplay = parts[2];
            String[] coups = parts[4].split(",");

            // Initialisation état visuel
            char[] cache = new String(new char[motReplay.length()]).replace('\0', '_').toCharArray();
            int viesReplay = Integer.parseInt(config.getProperty("vies_max"));
            List<Character> essais = new ArrayList<>();
            
            affichage.afficherMessage("--- DÉBUT DU REPLAY ---");
            affichage.afficherMessage("Mot à trouver : " + motReplay);
            Thread.sleep(1000);

            for (String coupStr : coups) {
                boolean estIA = coupStr.startsWith("IA:");
                char c = estIA ? coupStr.charAt(3) : coupStr.charAt(0);

                affichage.dessinerPendu(viesReplay);
                affichage.afficherEtatPartie(cache, viesReplay, essais);
                
                affichage.afficherMessage((estIA ? "L'IA joue : " : "Le joueur joue : ") + c);
                Thread.sleep(1000); // Pause pour voir l'action

                essais.add(c);
                if (motReplay.indexOf(c) >= 0) {
                    for (int i = 0; i < motReplay.length(); i++) if (motReplay.charAt(i) == c) cache[i] = c;
                    affichage.afficherSucces("Trouvé !");
                } else {
                    affichage.afficherErreur("Raté !");
                    viesReplay--;
                }
            }
            affichage.dessinerPendu(viesReplay);
            affichage.afficherEtatPartie(cache, viesReplay, essais);
            affichage.afficherMessage("--- FIN DU REPLAY ---");

        } catch (Exception e) {
            affichage.afficherErreur("Erreur pendant le replay : " + e.getMessage());
        }
    }
}