package service;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

import bdd.C_ConnexionSQLite;
import dao.AdherentDAO;
import dao.DAOFactory;
import dao.DocumentDAO;
import dao.EmpruntDAO;
import entites.Adherent;
import entites.Document;
import entites.E_StatutAdherent;
import entites.Emprunt;

/**
 * Cœur du système (Service Layer).
 * Cette classe orchestre les interactions entre l'interface utilisateur et la base de données.
 * Elle contient toute la "Logique Métier" (Business Logic).
 */
public class BibliothequeManager {

    // On récupère nos outils d'accès aux données via la Factory
    private AdherentDAO adherentDAO = DAOFactory.getAdherentDAO();
    private DocumentDAO documentDAO = DAOFactory.getDocumentDAO();
    private EmpruntDAO empruntDAO = DAOFactory.getEmpruntDAO();

    /**
     * Constructeur : Initialise la BDD au démarrage.
     */
    public BibliothequeManager() {
        initialiserBaseDeDonnees();
    }

    /**
     * Crée les tables SQL si elles n'existent pas encore.
     * C'est ce qui rend l'application "portable" : elle s'auto-installe.
     */
    private void initialiserBaseDeDonnees() {
        Connection c = C_ConnexionSQLite.getInstance();
        try (Statement stmt = c.createStatement()) {
            
            // 1. Table ADHERENT
            String sqlAdherent = "CREATE TABLE IF NOT EXISTS ADHERENT (" +
                                 "id TEXT PRIMARY KEY, " +
                                 "nom TEXT NOT NULL, " +
                                 "prenom TEXT NOT NULL, " +
                                 "coordonnees TEXT, " +
                                 "statut TEXT NOT NULL, " +
                                 "montant_penalite REAL)";
            stmt.execute(sqlAdherent);

            // 2. Table DOCUMENT (Unique pour Livre, Magazine, CD - Single Table Inheritance)
            // On met plein de colonnes NULLables pour gérer tous les types
            String sqlDoc = "CREATE TABLE IF NOT EXISTS DOCUMENT (" +
                            "id TEXT PRIMARY KEY, " +
                            "titre TEXT NOT NULL, " +
                            "auteur TEXT, " +
                            "genre TEXT, " +
                            "est_emprunte BOOLEAN, " +
                            "type_doc TEXT NOT NULL, " + // 'LIVRE', 'CD', 'MAGAZINE'
                            "isbn TEXT, nb_pages INTEGER, editeur TEXT, " + // Spécifique Livre
                            "numero INTEGER, periodicite TEXT, " + // Spécifique Magazine
                            "artiste TEXT, duree INTEGER, pistes INTEGER)"; // Spécifique CD
            stmt.execute(sqlDoc);

            // 3. Table EMPRUNT
            String sqlEmprunt = "CREATE TABLE IF NOT EXISTS EMPRUNT (" +
                                "id TEXT PRIMARY KEY, " +
                                "id_document TEXT NOT NULL, " +
                                "id_adherent TEXT NOT NULL, " +
                                "date_emprunt TEXT NOT NULL, " +
                                "date_retour_prevue TEXT NOT NULL, " +
                                "date_retour_reelle TEXT, " +
                                "FOREIGN KEY(id_document) REFERENCES DOCUMENT(id), " +
                                "FOREIGN KEY(id_adherent) REFERENCES ADHERENT(id))";
            stmt.execute(sqlEmprunt);

            System.out.println("MANAGER: Tables de la base de données vérifiées/créées.");

        } catch (SQLException e) {
            System.err.println("ERREUR CRITIQUE: Impossible d'initialiser la BDD.");
            e.printStackTrace();
        }
    }

    // --- LOGIQUE MÉTIER : EMPRUNTS ---

    /**
     * Tente d'enregistrer un nouvel emprunt.
     * Applique toutes les règles de gestion (Quota, Disponibilité, Pénalités).
     * @return true si l'emprunt a réussi, false sinon (avec message console).
     */
    public boolean emprunter(Adherent adherent, Document document) {
        try {
            // Règle 1 : Disponibilité
            if (document.estEmprunte()) {
                System.out.println("REFUSÉ: Le document est déjà emprunté.");
                return false;
            }

            // Règle 2 : Statut de l'adhérent
            if (adherent.getStatut() != E_StatutAdherent.ACTIF) {
                System.out.println("REFUSÉ: L'adhérent est bloqué ou a des pénalités.");
                return false;
            }

            // Règle 3 : Quota (max 5 emprunts en cours)
            List<Emprunt> empruntsEnCours = empruntDAO.findByAdherent(adherent).stream()
                    .filter(e -> e.getDateRetourReelle() == null) // On filtre ceux non rendus
                    .collect(Collectors.toList()); // Utilisation des Streams (TD7)

            if (empruntsEnCours.size() >= 5) {
                System.out.println("REFUSÉ: Quota de 5 emprunts atteint.");
                return false;
            }

            // Tout est OK : On crée l'emprunt
            String idUnique = "EMP-" + System.currentTimeMillis();
            Emprunt nouvelEmprunt = new Emprunt(idUnique, document, adherent);

            // TRANSACTION : On sauvegarde l'emprunt ET on met à jour le document
            empruntDAO.save(nouvelEmprunt);
            
            document.setEstEmprunte(true);
            documentDAO.update(document); // Met à jour le statut en BDD

            System.out.println("SUCCÈS: Emprunt enregistré pour " + adherent.getNom());
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Gère le retour d'un document.
     * Calcule les pénalités éventuelles.
     */
    public void rendre(Emprunt emprunt) {
        try {
            // 1. Mettre à jour la date de retour
            emprunt.setDateRetourReelle(LocalDate.now());
            empruntDAO.update(emprunt);

            // 2. Libérer le document
            Document doc = emprunt.getDocumentEmprunte();
            doc.setEstEmprunte(false);
            documentDAO.update(doc);

            // 3. Vérifier le retard (Logique Date TD7)
            long joursRetard = ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), emprunt.getDateRetourReelle());
            
            if (joursRetard > 0) {
                double amende = joursRetard * 0.50;
                System.out.println("RETARD DÉTECTÉ: " + joursRetard + " jours. Pénalité: " + amende + "€");
                
                // Mise à jour de l'adhérent
                Adherent adh = emprunt.getEmprunteur();
                adh.ajouterPenalite(amende);
                adherentDAO.update(adh);
            } else {
                System.out.println("Retour enregistré à l'heure. Merci.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- MÉTHODES DE CONSULTATION (PASSE-PLAT VERS DAO) ---
    // Ces méthodes permettent à l'UI d'accéder aux données sans toucher aux DAO directement.

    public void ajouterDocument(Document d) throws SQLException {
        documentDAO.save(d);
    }
    
    public void ajouterAdherent(Adherent a) throws SQLException {
        adherentDAO.save(a);
    }

    public List<Document> rechercherDocuments(String critere) {
        try {
            // Si critère vide, on renvoie tout, sinon on cherche
            if (critere == null || critere.isEmpty()) {
                return documentDAO.findAll();
            }
            return documentDAO.findByTitreOrAuteur(critere);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of(); // Retourne liste vide en cas d'erreur
        }
    }
    
    public Adherent rechercherAdherent(String id) {
        try {
            return adherentDAO.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<Emprunt> listerEmpruntsEnCours() {
        try {
            return empruntDAO.findEncours();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }
    /**
     * Récupère la liste complète des adhérents depuis la BDD.
     */
    public List<Adherent> listerAdherents() {
        try {
            return adherentDAO.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of(); // Retourne une liste vide si erreur
        }
    }
    /**
     * Recherche un document précis par son ID.
     */
    public Document recupererDocumentParId(String id) {
        try {
            return documentDAO.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}