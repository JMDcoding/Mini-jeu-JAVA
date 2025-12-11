import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HangmanGame {
    private IView view;
    private IStorage storage;
    private String[] mots = {"JAVA", "PROGRAMMATION", "ORDINATEUR", "INTERFACE", "CONSOLE", "DEVELOPPEUR", "ALGORITHME", "SOLID", "REFACTORING"};
    
    public HangmanGame(IView view, IStorage storage) {
        this.view = view;
        this.storage = storage;
    }

    public void start() {
        boolean continuer = true;
        while (continuer) {
            view.afficherMenu();
            int choix = view.demanderEntier("Votre choix : ");
            switch (choix) {
                case 1: jouerPartieSolo(); break;
                case 2: jouerDuelIA(); break;
                case 3: rejouerHistorique(); break;
                case 4: afficherStatistiques(); break;
                case 5: lancerTests(); break;
                case 6: 
                    view.afficherMessage("Au revoir !");
                    continuer = false; 
                    break;
                default: view.afficherMessageErreur("Choix invalide.");
            }
        }
    }

    private void jouerPartieSolo() {
        String motADeviner = choisirMotAleatoire();
        char[] motCache = masquerMot(motADeviner);
        int vies = 6;
        List<Character> lettresEssayees = new ArrayList<>();
        List<String> historiqueCoups = new ArrayList<>();
        boolean gagne = false;
        long startTime = System.currentTimeMillis();

        view.afficherMessageSucces("\n--- DÉBUT DU JEU DU PENDU ---");
        view.afficherMessage("Tapez 'RET' pour annuler le dernier coup.");

        while (vies > 0 && !gagne) {
            view.afficherPendu(vies);
            view.afficherEtatJeu(motCache, vies, lettresEssayees);
            String input = view.demanderSaisie("Proposez une lettre : ").toUpperCase();

            if (input.equals("RET")) {
                // Logique Undo
                if (!lettresEssayees.isEmpty()) {
                    char derniere = lettresEssayees.remove(lettresEssayees.size() - 1);
                    historiqueCoups.remove(historiqueCoups.size() - 1);
                    if (motADeviner.indexOf(derniere) == -1) vies++;
                    else {
                        for (int i = 0; i < motADeviner.length(); i++) {
                            if (motADeviner.charAt(i) == derniere) motCache[i] = '_';
                        }
                    }
                    view.afficherMessage("Annulé.");
                }
                continue;
            }

            if (input.length() > 1) {
                if (input.equals(motADeviner)) {
                    gagne = true;
                    motCache = motADeviner.toCharArray();
                    historiqueCoups.add("MOT:" + input);
                } else {
                    view.afficherMessageErreur("Ce n'est pas le bon mot !");
                    vies--;
                    historiqueCoups.add("MOT_RATE:" + input);
                }
            } else if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
                char lettre = input.charAt(0);
                if (lettresEssayees.contains(lettre)) {
                    view.afficherMessage("Déjà essayé.");
                    continue;
                }
                lettresEssayees.add(lettre);
                historiqueCoups.add(String.valueOf(lettre));

                if (motADeviner.indexOf(lettre) >= 0) {
                    view.afficherMessageSucces("Bonne lettre !");
                    revelerLettre(motADeviner, motCache, lettre);
                } else {
                    view.afficherMessageErreur("Mauvaise lettre !");
                    vies--;
                }
            }

            if (String.valueOf(motCache).equals(motADeviner)) gagne = true;
        }

        long duree = (System.currentTimeMillis() - startTime) / 1000;
        finDePartie(motADeviner, gagne, vies, duree, historiqueCoups);
    }

    private void jouerDuelIA() {
        view.afficherMessageSucces("\n--- DUEL CONTRE L'IA ---");
        String motADeviner = choisirMotAleatoire();
        
        view.afficherMessage("1. Simple\n2. Sophistiquée");
        int choix = view.demanderEntier("Niveau IA : ");
        IAIStrategy ia = (choix == 2) ? new SmartAI() : new RandomAI();

        char[] motCache = masquerMot(motADeviner);
        List<Character> lettresEssayees = new ArrayList<>();
        List<Character> lettresTrouveesJoueur = new ArrayList<>();
        List<Character> lettresTrouveesIA = new ArrayList<>();
        
        boolean gagne = false;
        String gagnant = "";

        while (!gagne) {
            // Tour Joueur
            view.afficherEtatJeuDuel(motCache, 6, lettresEssayees, lettresTrouveesJoueur, lettresTrouveesIA);
            String input = view.demanderSaisie("Votre tour (lettre ou mot) : ").toUpperCase();
            
            if (input.length() > 1) {
                if (input.equals(motADeviner)) {
                    gagne = true;
                    gagnant = "JOUEUR";
                    // Tout révéler pour le joueur
                    for(char c : motADeviner.toCharArray()) {
                        if(!lettresTrouveesIA.contains(c)) lettresTrouveesJoueur.add(c);
                    }
                    motCache = motADeviner.toCharArray();
                    break;
                } else {
                    view.afficherMessageErreur("Ce n'est pas le bon mot !");
                }
            } else if (input.length() == 1) {
                char l = input.charAt(0);
                if (!lettresEssayees.contains(l)) {
                    lettresEssayees.add(l);
                    if (motADeviner.indexOf(l) >= 0) {
                        view.afficherMessageSucces("Bravo !");
                        lettresTrouveesJoueur.add(l);
                        revelerLettre(motADeviner, motCache, l);
                    } else {
                        view.afficherMessageErreur("Raté !");
                    }
                } else {
                    view.afficherMessage("Déjà essayé.");
                }
            }
            
            if (String.valueOf(motCache).equals(motADeviner)) {
                gagne = true;
                gagnant = "JOUEUR";
                break;
            }

            // Tour IA
            view.afficherMessage("\nL'IA (" + ia.getNom() + ") réfléchit...");
            try { Thread.sleep(1000); } catch (Exception e) {}
            char lettreIA = ia.choisirLettre(lettresEssayees, motADeviner);
            view.afficherMessage("L'IA joue : " + lettreIA);
            lettresEssayees.add(lettreIA);
            
            if (motADeviner.indexOf(lettreIA) >= 0) {
                view.afficherMessageSucces("L'IA a trouvé !");
                lettresTrouveesIA.add(lettreIA);
                revelerLettre(motADeviner, motCache, lettreIA);
            } else {
                view.afficherMessageErreur("L'IA a raté.");
            }

            if (String.valueOf(motCache).equals(motADeviner)) {
                gagne = true;
                gagnant = "IA";
            }
        }
        view.afficherEtatJeuDuel(motCache, 6, lettresEssayees, lettresTrouveesJoueur, lettresTrouveesIA);
        view.afficherMessageSucces("Gagnant : " + gagnant + " (Mot: " + motADeviner + ")");
    }

    private void rejouerHistorique() {
        List<String> hist = storage.chargerHistorique();
        if (hist.isEmpty()) {
            view.afficherMessage("Historique vide.");
            return;
        }
        for (int i = 0; i < hist.size(); i++) {
            view.afficherMessage((i + 1) + ". " + hist.get(i));
        }
        int choix = view.demanderEntier("Choisir partie : ");
        if (choix > 0 && choix <= hist.size()) {
            simulerPartie(hist.get(choix - 1));
        }
    }

    private void simulerPartie(String ligne) {
        String[] parts = ligne.split("\\|");
        if (parts.length < 6) return;
        String mot = parts[2];
        String[] coups = parts[5].split(",");
        char[] cache = masquerMot(mot);
        int vies = 6;

        view.afficherMessage("\n--- REPLAY ---");
        for (String coup : coups) {
            try { Thread.sleep(800); } catch (Exception e) {}
            view.afficherPendu(vies);
            view.afficherMessage("Mot : " + String.valueOf(cache));
            
            if (coup.startsWith("MOT:")) {
                view.afficherMessageSucces("Mot trouvé !");
                return;
            } else if (coup.startsWith("MOT_RATE:")) {
                view.afficherMessageErreur("Mot raté : " + coup.substring(9));
                vies--;
            } else {
                char c = coup.charAt(0);
                view.afficherMessage("Coup : " + c);
                if (mot.indexOf(c) >= 0) revelerLettre(mot, cache, c);
                else vies--;
            }
        }
    }

    private void afficherStatistiques() {
        List<String> hist = storage.chargerHistorique();
        view.afficherMessage("Parties jouées : " + hist.size());
        // Logique de stats simplifiée pour l'exemple
    }

    private void lancerTests() {
        // Tests unitaires simples
        int ok = 0;
        if ("TEST".indexOf('E') >= 0) ok++;
        if (masquerMot("ABC").length == 3) ok++;
        view.afficherMessage("Tests réussis : " + ok + "/2");
    }

    private String choisirMotAleatoire() {
        return mots[new Random().nextInt(mots.length)];
    }

    private char[] masquerMot(String mot) {
        char[] t = new char[mot.length()];
        for (int i = 0; i < t.length; i++) t[i] = '_';
        return t;
    }

    private void revelerLettre(String mot, char[] cache, char lettre) {
        for (int i = 0; i < mot.length(); i++) {
            if (mot.charAt(i) == lettre) cache[i] = lettre;
        }
    }

    private void finDePartie(String mot, boolean gagne, int vies, long duree, List<String> coups) {
        if (gagne) view.afficherMessageSucces("GAGNÉ ! Mot : " + mot);
        else {
            view.afficherPendu(0);
            view.afficherMessageErreur("PERDU ! Mot : " + mot);
        }
        storage.sauvegarderPartie(mot, gagne, vies, duree, coups);
    }
}
