package bdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gère la connexion à la base de données SQLite (bibliotheque.db).
 * Utilise le pattern Singleton (vu en cours) pour garantir qu'une seule
 * instance de connexion soit active dans l'application.
 */
public class C_ConnexionSQLite {

    // Chemin d'accès au fichier de la base de données.
    private static final String URL = "jdbc:sqlite:bibliotheque.db";

    // L'unique instance de la connexion (Singleton).
    private static Connection connexion = null;

    /**
     * Constructeur privé pour empêcher l'instanciation directe (Singleton).
     */
    private C_ConnexionSQLite() {
    }

    /**
     * Point d'accès global pour obtenir la connexion.
     * @return L'instance Connection, créée si elle n'existe pas.
     */
    public static Connection getInstance() {
        if (connexion == null) {
            try {
                // AJOUTE CETTE LIGNE ICI :
                // Elle force le chargement du pilote SQLite en mémoire
                Class.forName("org.sqlite.JDBC");

                // Établissement de la connexion.
                connexion = DriverManager.getConnection(URL);
                System.out.println("BDD-INFO: Connexion SQLite établie.");
            
            } catch (ClassNotFoundException e) {
                System.err.println("ERREUR: Pilote SQLite introuvable ! Vérifiez le Build Path.");
                e.printStackTrace();
            } catch (SQLException e) {
                System.err.println("ERREUR BDD: Impossible d'établir la connexion.");
                e.printStackTrace();
            }
        }
        return connexion;
    }

    /**
     * Ferme proprement la connexion à la fin du programme.
     */
    public static void fermer() {
        if (connexion != null) {
            try {
                connexion.close();
                connexion = null;
                System.out.println("BDD-INFO: Connexion SQLite fermée.");
            } catch (SQLException e) {
                System.err.println("ERREUR BDD: Échec de la fermeture de la connexion.");
                e.printStackTrace();
            }
        }
    }
}