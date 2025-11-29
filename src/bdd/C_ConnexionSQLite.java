package bdd;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * s'occupe de la connexion avec la bdd 
 * on utilise ici les singleton
 */
public class C_ConnexionSQLite {

    private static final String URL = "jdbc:sqlite:bibliotheque.db";

    //singleton
    private static Connection connexion = null;

    /**
     * empeche l'instanciation directe
     */
    private C_ConnexionSQLite() {
    }

    /**
     * la connexion.
     * return l'instance Connection
     */
    public static Connection getInstance() {
        if (connexion ==null) {
            try {
                
                Class.forName("org.sqlite.JDBC");

                //la connexion
                connexion = DriverManager.getConnection(URL);
                System.out.println(" la connexion SQLite vient tout juste d'etre établie ");
            
            } catch (ClassNotFoundException e) {
                System.err.println("ERREUR : introuvable !");
                e.printStackTrace() ;

            } catch (SQLException e) {

                System.err.println("ERREUR BDD : Impossible de faire la connexion !! ");
                e.printStackTrace() ; 
            }
        }
        return connexion;
    }

    /**
     * on ferme à la fin du programme
     */
    public static void fermer() {
        if (connexion !=null) {
            try {
                connexion.close(); 
                connexion =null;
                System.out.println("Connexion fermée ") ;

            } catch (SQLException e) {
                System.err.println("ERREUR BDD : échec de la fermeture de la connexion !!");
                e.printStackTrace() ; 
                 
            }
        }
    }
}