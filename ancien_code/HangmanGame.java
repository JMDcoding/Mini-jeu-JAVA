import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

// Classe principale qui gère la logique du jeu
public class HangmanGame {
    private IView view; // L'interface pour l'affichage (Console ou Graphique)
    private List<String> mots; // La liste des mots possibles
    
    public HangmanGame(IView view) {
        this.view = view;
        chargerMots();
    }

    // Charge les mots depuis le fichier texte
    private void chargerMots() {
        try {
            // Lecture de toutes les lignes du fichier
            List<String> lignes = Files.readAllLines(Paths.get("mots.txt"));
            this.mots = new ArrayList<>();
            
            // On garde seulement les mots valides (pas vides) et on les met en majuscules
            for (String ligne : lignes) {
                if (!ligne.trim().isEmpty()) {
                    this.mots.add(ligne.trim().toUpperCase());
                }
            }

            // Si la liste est vide, on charge des mots par défaut
            if (this.mots.isEmpty()) {
                chargerMotsParDefaut();
                view.afficherMessageErreur("Le fichier mots.txt est vide. Utilisation d'une liste par défaut.");
            }
        } catch (IOException e) {
            // En cas d'erreur de lecture (fichier absent), on charge les mots par défaut
            chargerMotsParDefaut();
            view.afficherMessageErreur("Fichier mots.txt introuvable. Utilisation d'une liste par défaut.");
        }
    }

    private void chargerMotsParDefaut() {
        this.mots = new ArrayList<>();
        this.mots.add("JAVA");
        this.mots.add("DEVELOPPEUR");
        this.mots.add("ALGORITHME");
    }

    // Méthode principale qui lance le menu du jeu
    public void start() {
        boolean continuer = true;
        while (continuer) {
            view.afficherMenu(); 
            int choix = view.demanderEntier("Votre choix : ");
            switch (choix) {
                case 1: 
                    do {
                        jouerPartieSolo(); 
                    } while (view.demanderRejouer());
                    break;
                case 2:
                    do {
                        jouerDuelIA();
                    } while (view.demanderRejouer());
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

    // Logique d'une partie en solo
    private void jouerPartieSolo() {
        String motADeviner = choisirMotAleatoire();
        if (motADeviner == null) {
            view.afficherMessageErreur("Impossible de commencer la partie, aucun mot n'est disponible.");
            return;
        }
        
        char[] motCache = masquerMot(motADeviner); // Crée le tableau de tirets (ex: _ _ _ _)
        int vies = 6;
        List<Character> lettresEssayees = new ArrayList<>();
        boolean gagne = false;
        long startTime = System.currentTimeMillis();

        view.afficherMessageSucces("\n--- DÉBUT DU JEU DU PENDU ---");

        // Boucle principale du jeu
        while (vies > 0 && !gagne) {
            view.afficherPendu(vies);
            view.afficherEtatJeu(motCache, vies, lettresEssayees);
            
            // Demande une lettre ou un mot au joueur
            String input = view.demanderSaisie("Proposez une lettre ou devinez le mot : ").toUpperCase();

            // Si le joueur propose un mot complet
            if (input.length() > 1) {
                if (input.equals(motADeviner)) {
                    gagne = true;
                    motCache = motADeviner.toCharArray(); // On révèle tout le mot
                } else {
                    view.afficherMessageErreur("Ce n'est pas le bon mot !");
                    vies--; // Pénalité
                    view.afficherPendu(vies); // Mise à jour immédiate du dessin
                }
            } 
            // Si le joueur propose une seule lettre
            else if (input.length() == 1 && Character.isLetter(input.charAt(0))) {
                char lettre = input.charAt(0);
                
                // Vérifie si la lettre a déjà été jouée
                if (lettresEssayees.contains(lettre)) {
                    view.afficherMessage("Vous avez déjà essayé cette lettre.");
                    continue; // On passe au tour suivant sans pénalité
                }
                
                lettresEssayees.add(lettre);

                // Vérifie si la lettre est dans le mot
                if (motADeviner.indexOf(lettre) >= 0) {
                    view.afficherMessageSucces("Bonne lettre !");
                    revelerLettre(motADeviner, motCache, lettre);
                } else {
                    view.afficherMessageErreur("Mauvaise lettre !");
                    vies--;
                    view.afficherPendu(vies); // Mise à jour immédiate du dessin
                }
            } else {
                view.afficherMessageErreur("Saisie invalide.");
            }

            // Vérifie si le mot est entièrement découvert
            if (String.valueOf(motCache).equals(motADeviner)) {
                gagne = true;
            }
        }

        long duree = (System.currentTimeMillis() - startTime) / 1000;
        finDePartie(motADeviner, gagne, duree);
    }

    // Logique du duel contre l'IA
    private void jouerDuelIA() {
        view.afficherMessageSucces("\n--- DUEL CONTRE L'IA ---");
        String motADeviner = choisirMotAleatoire();
        if (motADeviner == null) {
            view.afficherMessageErreur("Impossible de commencer, aucun mot disponible.");
            return;
        }
        
        // Choix de la difficulté de l'IA
        view.afficherMessage("1. Simple\n2. Sophistiquée");
        int choix = view.demanderEntier("Niveau IA : ");
        IAIStrategy ia;
        if (choix == 2) {
            ia = new SmartAI();
        } else {
            ia = new RandomAI();
        }

        char[] motCache = masquerMot(motADeviner);
        List<Character> lettresEssayees = new ArrayList<>();
        List<Character> lettresTrouveesJoueur = new ArrayList<>();
        List<Character> lettresTrouveesIA = new ArrayList<>();
        
        boolean gagne = false;
        String gagnant = "";

        while (!gagne) {
            // --- Tour du Joueur ---
            view.afficherEtatJeuDuel(motCache, 6, lettresEssayees, lettresTrouveesJoueur, lettresTrouveesIA);
            String input = view.demanderSaisie("Votre tour (lettre ou mot) : ").toUpperCase();
            
            if (input.length() > 1) {
                if (input.equals(motADeviner)) {
                    gagne = true;
                    gagnant = "JOUEUR";
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

            // --- Tour de l'IA ---
            view.afficherMessage("\nL'IA (" + ia.getNom() + ") réfléchit...");
            try { Thread.sleep(1000); } catch (Exception e) {} // Petite pause pour le réalisme
            
            String coupIA = ia.choisirCoup(lettresEssayees, motADeviner);
            
            if (coupIA.length() > 1) {
                view.afficherMessage("L'IA tente le mot : " + coupIA);
                if (coupIA.equals(motADeviner)) {
                    gagne = true;
                    gagnant = "IA";
                    motCache = motADeviner.toCharArray();
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
        Random random = new Random();
        int index = random.nextInt(mots.size());
        return mots.get(index);
    }

    // Remplace chaque lettre du mot par un tiret '_'
    char[] masquerMot(String mot) {
        char[] tableau = new char[mot.length()];
        for (int i = 0; i < tableau.length; i++) {
            tableau[i] = '_';
        }
        return tableau;
    }

    // Révèle les occurrences de la lettre trouvée dans le mot caché
    void revelerLettre(String mot, char[] motCache, char lettre) {
        for (int i = 0; i < mot.length(); i++) {
            if (mot.charAt(i) == lettre) {
                motCache[i] = lettre;
            }
        }
    }

    private void finDePartie(String mot, boolean gagne, long duree) {
        if (gagne) {
            view.afficherMessageSucces("GAGNÉ ! Le mot était : " + mot);
        } else {
            view.afficherPendu(0);
            view.afficherMessageErreur("MATCH NUL ! Le mot était : " + mot);
        }
        view.afficherMessage("Partie terminée en " + duree + " secondes.");
    }
}
