package main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;

import bdd.C_ConnexionSQLite;
import entites.*;
import service.BibliothequeManager;

/**
 * Script utilitaire pour peupler la BDD avec des données de test.
 * À lancer UNE FOIS pour réinitialiser la démo.
 */
public class DataSeeder {

    public static void main(String[] args) {
        System.out.println("--- DÉMARRAGE DU PEUPLEMENT DE LA BDD ---");
        
        BibliothequeManager manager = new BibliothequeManager();
        
        try {
            // 1. NETTOYAGE TOTAL
            cleanDatabase();

            // 2. CRÉATION DES ADHÉRENTS (10)
            System.out.println("-> Création des adhérents...");
            Adherent[] adhs = {
                new Adherent("ADH-001", "Dupont", "Jean", "jean.dupont@email.com"),
                new Adherent("ADH-002", "Martin", "Sophie", "0601020304"),
                new Adherent("ADH-003", "Garcia", "Luis", "luis.garcia@email.com"),
                new Adherent("ADH-004", "Bernard", "Marie", "marie.b@test.fr"),
                new Adherent("ADH-005", "Petit", "Thomas", "0788996655"),
                new Adherent("ADH-006", "Robert", "Camille", "camille.r@email.com"),
                new Adherent("ADH-007", "Richard", "Lucas", "lucas.richard@test.com"),
                new Adherent("ADH-008", "Durand", "Emma", "emma.d@email.com"),
                new Adherent("ADH-009", "Moreau", "Léa", "lea.moreau@test.fr"),
                new Adherent("ADH-010", "Simon", "Hugo", "hugo.simon@email.com")
            };
            
            for (Adherent a : adhs) manager.ajouterAdherent(a);

            // Simulation d'un mauvais payeur (Pénalité manuelle)
            adhs[9].ajouterPenalite(15.0); // Hugo Simon a une dette
            manager.modifierAdherent(adhs[9]);

            // 3. CRÉATION DES DOCUMENTS (12)
            System.out.println("-> Création des documents...");
            Document[] docs = {
                // Livres
                new Livre("LIV-001", "Les Misérables", "Victor Hugo", "Roman", "978-2253096344", 1400, "Hachette"),
                new Livre("LIV-002", "1984", "George Orwell", "SF", "978-2070368228", 400, "Gallimard"),
                new Livre("LIV-003", "Le Petit Prince", "St Exupéry", "Conte", "978-2070408504", 96, "Folio"),
                new Livre("LIV-004", "Harry Potter 1", "J.K. Rowling", "Fantastique", "978-1408855652", 350, "Bloomsbury"),
                new Livre("LIV-005", "Dune", "Frank Herbert", "SF", "978-2266283292", 800, "Pocket"),
                new Livre("LIV-006", "L'Étranger", "Albert Camus", "Roman", "978-2070360024", 150, "Gallimard"),
                
                // CDs
                new CD("CD-001", "Thriller", "Michael Jackson", "Pop", 42, 9),
                new CD("CD-002", "Random Access Memories", "Daft Punk", "Electro", 74, 13),
                new CD("CD-003", "Back in Black", "AC/DC", "Rock", 42, 10),
                new CD("CD-004", "The Dark Side of the Moon", "Pink Floyd", "Rock Prog", 43, 10),

                // Magazines
                new Magazine("MAG-001", "National Geographic", null, "Science", 254, "Mensuel", 120, "NatGeo Soc"),
                new Magazine("MAG-002", "Vogue", null, "Mode", 890, "Mensuel", 80, "Condé Nast")
            };

            for (Document d : docs) manager.ajouterDocument(d);

            // 4. CRÉATION D'EMPRUNTS (Avec triche sur les dates pour tester les retards)
            System.out.println("-> Génération des emprunts (Normaux et Retards)...");
            
            // Emprunt Normal (En cours)
            manager.emprunter(adhs[0], docs[0]); // Jean emprunte Les Misérables
            manager.emprunter(adhs[1], docs[6]); // Sophie emprunte Thriller

            // Emprunt EN RETARD (On doit tricher via SQL car manager.emprunter met la date d'aujourd'hui)
            // On crée l'emprunt normalement...
            manager.emprunter(adhs[2], docs[1]); // Luis emprunte 1984
            manager.emprunter(adhs[3], docs[2]); // Marie emprunte Le Petit Prince
            
            // ...Puis on hacke la BDD pour changer la date de retour prévue au mois dernier !
            forceLateDate(docs[1].getId()); // Retard sur 1984
            forceLateDate(docs[2].getId()); // Retard sur Le Petit Prince

            // Emprunt TERMINÉ (Rendu)
            manager.emprunter(adhs[4], docs[3]); 
            Emprunt e = manager.listerEmpruntsEnCours().stream()
                        .filter(emp -> emp.getDocumentEmprunte().getId().equals(docs[3].getId()))
                        .findFirst().orElse(null);
            if(e != null) manager.rendre(e); // Thomas a rendu Harry Potter

            System.out.println("--- DONNÉES GÉNÉRÉES AVEC SUCCÈS ! ---");
            System.out.println("Vous pouvez lancer l'application.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Vide les tables
    private static void cleanDatabase() throws SQLException {
        Connection c = C_ConnexionSQLite.getInstance();
        Statement s = c.createStatement();
        s.execute("DELETE FROM EMPRUNT"); // Supprimer emprunts d'abord (clé étrangère)
        s.execute("DELETE FROM DOCUMENT");
        s.execute("DELETE FROM ADHERENT");
        // Reset des séquences d'ID si nécessaire (pas besoin ici car ID générés en Java)
        System.out.println("-> Base de données nettoyée.");
    }

    // Force une date de retour dans le passé pour simuler un retard
    private static void forceLateDate(String idDoc) throws SQLException {
        Connection c = C_ConnexionSQLite.getInstance();
        // On met la date prévue il y a 10 jours
        String sql = "UPDATE EMPRUNT SET date_retour_prevue = ? WHERE id_document = ? AND date_retour_reelle IS NULL";
        PreparedStatement pst = c.prepareStatement(sql);
        pst.setString(1, LocalDate.now().minusDays(10).toString());
        pst.setString(2, idDoc);
        pst.executeUpdate();
    }
}