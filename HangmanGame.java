import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

public class HangmanGame {
    private IView view;
    private List<String> mots;
    
    public HangmanGame(IView view) {
        this.view = view;
        chargerMots();
    }

    private void chargerMots() {
        try {
            this.mots = Files.lines(Paths.get("mots.txt"))
                             .map(String::toUpperCase)
                             .filter(mot -> !mot.trim().isEmpty())
                             .collect(Collectors.toList());
            if (this.mots.isEmpty()) {
                chargerMotsParDefaut();
                view.afficherMessageErreur("Le fichier mots.txt est vide. Utilisation d'une liste par défaut.");
            }
        } catch (IOException e) {
            chargerMotsParDefaut();
            view.afficherMessageErreur("Fichier mots.txt introuvable. Utilisation d'une liste par défaut.");
        }
    }

    private void chargerMotsParDefaut() {
        this.mots = new ArrayList<>(List.of("JAVA", "DEVELOPPEUR", "ALGORITHME"));
    }

    public void start() {
        boolean continuer = true;
        while (continuer) {
            view.afficherMenu(); 
            int choix = view.demanderEntier("Votre choix : ");
            switch (choix) {
                case 1: 
                    jouerPartieSolo(); 
                    break;
                case 2:
                    jouerDuelIA();
                    break;
                case 3:
                    GameTests.lancerTests();
                    break;
                case 4:
                    view.afficherMessage("Au revoir !");
                    continuer = false; 
                    break;
                default: 
                    view.afficherMessageErreur("Choix invalide.");
            }
        }
    }

    private void jouerPartieSolo() {
        String motADeviner = choisirMotAleatoire();
        if (motADeviner == null) {
            view.afficherMessageErreur("Impossible de commencer la partie, aucun mot n'est disponible.");
            return;
        }
        
        char[] motCache = masquerMot(motADeviner);
        int vies = 6;
        List<Character> lettresEssayees = new ArrayList<>();
        boolean gagne = false;
        long startTime = System.currentTimeMillis();

        view.afficherMessageSucces("\n--- DÉBUT DU JEU DU PENDU ---");

        while (vies > 0 && !gagne) {
            view.afficherPendu(vies);
            view.afficherEtatJeu(motCache, vies, lettresEssayees);
            String input = view.demanderSaisie("Proposez une lettre ou devinez le mot : ").toUpperCase();

            if (input.length() > 1) {
                if (input.equals(motADeviner)) {
                    gagne = true;
                    motCache = motADeviner.toCharArray();
                } else {
                    view.afficherMessageErreur("Ce n'est pas le bon mot !");
                    vies--;
                }
            } else if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
                char lettre = input.charAt(0);
                if (lettresEssayees.contains(lettre)) {
                    view.afficherMessage("Vous avez déjà essayé cette lettre.");
                    continue;
                }
                
                lettresEssayees.add(lettre);

                if (motADeviner.indexOf(lettre) >= 0) {
                    view.afficherMessageSucces("Bonne lettre !");
                    revelerLettre(motADeviner, motCache, lettre);
                } else {
                    view.afficherMessageErreur("Mauvaise lettre !");
                    vies--;
                }
            } else {
                view.afficherMessageErreur("Saisie invalide.");
            }

            if (String.valueOf(motCache).equals(motADeviner)) {
                gagne = true;
            }
        }

        long duree = (System.currentTimeMillis() - startTime) / 1000;
        finDePartie(motADeviner, gagne, duree);
    }

    private void jouerDuelIA() {
        view.afficherMessageSucces("\n--- DUEL CONTRE L'IA ---");
        String motADeviner = choisirMotAleatoire();
        if (motADeviner == null) {
            view.afficherMessageErreur("Impossible de commencer, aucun mot disponible.");
            return;
        }
        
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
            
            String coupIA = ia.choisirCoup(lettresEssayees, motADeviner);
            
            if (coupIA.length() > 1) {
                view.afficherMessage("L'IA tente le mot : " + coupIA);
                if (coupIA.equals(motADeviner)) {
                    gagne = true;
                    gagnant = "IA";
                    motCache = motADeviner.toCharArray(); // Révéler le mot
                    break;
                } else {
                    view.afficherMessageErreur("L'IA s'est trompée de mot !");
                }
            } else {
                char lettreIA = coupIA.charAt(0);
                view.afficherMessage("L'IA joue : " + lettreIA);
                
                if (!lettresEssayees.contains(lettreIA)) {
                    lettresEssayees.add(lettreIA);
                    if (motADeviner.indexOf(lettreIA) >= 0) {
                        view.afficherMessageSucces("L'IA a trouvé !");
                        lettresTrouveesIA.add(lettreIA);
                        revelerLettre(motADeviner, motCache, lettreIA);
                    } else {
                        view.afficherMessageErreur("L'IA a raté.");
                    }
                } else {
                     view.afficherMessage("L'IA a essayé une lettre déjà jouée (bizarre...).");
                }
            }

            if (String.valueOf(motCache).equals(motADeviner)) {
                gagne = true;
                gagnant = "IA";
            }
        }
        view.afficherEtatJeuDuel(motCache, 6, lettresEssayees, lettresTrouveesJoueur, lettresTrouveesIA);
        view.afficherMessageSucces("Gagnant : " + gagnant + " (Mot: " + motADeviner + ")");
    }
    
    private String choisirMotAleatoire() {
        if (mots == null || mots.isEmpty()) {
            return null;
        }
        return mots.get(new Random().nextInt(mots.size()));
    }

    char[] masquerMot(String mot) {
        char[] t = new char[mot.length()];
        for (int i = 0; i < t.length; i++) t[i] = '_';
        return t;
    }

    void revelerLettre(String mot, char[] cache, char lettre) {
        for (int i = 0; i < mot.length(); i++) {
            if (mot.charAt(i) == lettre) {
                cache[i] = lettre;
            }
        }
    }

    private void finDePartie(String mot, boolean gagne, long duree) {
        if (gagne) {
            view.afficherMessageSucces("GAGNÉ ! Le mot était : " + mot);
        } else {
            view.afficherPendu(0);
            view.afficherMessageErreur("PERDU ! Le mot était : " + mot);
        }
        view.afficherMessage("Partie terminée en " + duree + " secondes.");
    }
}
