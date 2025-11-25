package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import entites.Adherent;
import entites.Livre;
import entites.CD;
import service.BibliothequeManager;
import ui.FenetrePrincipale;

/**
 * Point d'entrée de l'application.
 */
public class MainApplication {

    public static void main(String[] args) {
        
        // 1. On démarre le "Cerveau" (qui initialise la BDD)
        BibliothequeManager manager = new BibliothequeManager();

        // 2. (Optionnel) On ajoute des données de test si c'est vide
        // Cela permet d'avoir une démo prête pour la soutenance
        if (manager.rechercherDocuments("").isEmpty()) {
            peuplerBaseDeDonnees(manager);
        }

        // 3. On lance l'interface graphique dans le "Thread" dédié à l'affichage
        SwingUtilities.invokeLater(() -> {
            try {
                // On essaie de mettre le look "système" (plus joli que le look Java par défaut)
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            // On crée et affiche la fenêtre
            FenetrePrincipale fenetre = new FenetrePrincipale(manager);
            fenetre.setVisible(true);
        });
    }

    private static void peuplerBaseDeDonnees(BibliothequeManager manager) {
        System.out.println("MAIN: Génération de données de test...");
        try {
            manager.ajouterAdherent(new Adherent("ADH-001", "Dupont", "Jean", "0601020304"));
            manager.ajouterAdherent(new Adherent("ADH-002", "Martin", "Sophie", "sophie@email.com"));
            
            manager.ajouterDocument(new Livre("LIV-001", "Les Misérables", "Victor Hugo", "Roman", "978-2253096344", 1500, "Hachette"));
            manager.ajouterDocument(new Livre("LIV-002", "Le Petit Prince", "Saint-Exupéry", "Jeunesse", "978-2070408504", 96, "Gallimard"));
            manager.ajouterDocument(new CD("CD-001", "Thriller", "Michael Jackson", "Pop", 42, 9));
            
            System.out.println("MAIN: Données insérées.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}