public class GameTests {

    public static void lancerTests() {
        System.out.println("\n--- DÉBUT DES TESTS UNITAIRES ---");
        int testsReussis = 0;
        int testsTotal = 0;

        testsTotal++;
        if (testMasquerMot()) {
            System.out.println("[OK] testMasquerMot");
            testsReussis++;
        } else {
            System.out.println("[FAIL] testMasquerMot");
        }

        testsTotal++;
        if (testRevelerLettre()) {
            System.out.println("[OK] testRevelerLettre");
            testsReussis++;
        } else {
            System.out.println("[FAIL] testRevelerLettre");
        }

        System.out.println("Résultat : " + testsReussis + "/" + testsTotal + " tests réussis.");
        System.out.println("--- FIN DES TESTS UNITAIRES ---");
    }

    private static boolean testMasquerMot() {
        HangmanGame game = new HangmanGame(new ConsoleView());
        char[] result = game.masquerMot("TEST");
        if (result.length != 4) return false;
        for (char c : result) {
            if (c != '_') return false;
        }
        return true;
    }

    private static boolean testRevelerLettre() {
        HangmanGame game = new HangmanGame(new ConsoleView());
        char[] cache = {'_', '_', '_', '_'};
        game.revelerLettre("TEST", cache, 'E');
        
        if (cache[0] != '_') return false;
        if (cache[1] != 'E') return false;
        if (cache[2] != '_') return false;
        if (cache[3] != '_') return false;
        
        return true;
    }
}
