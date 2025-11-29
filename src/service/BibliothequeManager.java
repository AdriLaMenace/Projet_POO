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
import dao.UtilisateurDAO; 
import entites.Adherent;
import entites.Document;
import entites.E_StatutAdherent;
import entites.Emprunt;
import entites.Utilisateur;

/**
 * cette classe gere toute la logique metier entre l'interface et la bdd
 * elle contient les regles de gestion
 */
public class BibliothequeManager {

    // recuperation des outils d'acces aux donnees via la Factory
    private AdherentDAO adherentDAO = DAOFactory.getAdherentDAO();
    private DocumentDAO documentDAO = DAOFactory.getDocumentDAO();
    private EmpruntDAO empruntDAO = DAOFactory.getEmpruntDAO();
    private UtilisateurDAO utilisateurDAO = DAOFactory.getUtilisateurDAO();

    /**
     * constructeur : init la bdd au demarrage
     */
    public BibliothequeManager() {
        initialiserBaseDeDonnees();
    }

    /**
     * creation des tables sql si elles existent pas encore
     */
    private void initialiserBaseDeDonnees() {
        Connection c = C_ConnexionSQLite.getInstance();
        try (Statement stmt = c.createStatement()) {
            
            // table ADHERENT
            String sqlAdherent = "CREATE TABLE IF NOT EXISTS ADHERENT (" +
                                 "id TEXT PRIMARY KEY, " +
                                 "nom TEXT NOT NULL, " +
                                 "prenom TEXT NOT NULL, " +
                                 "coordonnees TEXT, " +
                                 "statut TEXT NOT NULL, " +
                                 "montant_penalite REAL)";
            stmt.execute(sqlAdherent);

            // table DOCUMENT 
            // on met plein de colonnes NULLables pour gerer tous les types
            String sqlDoc = "CREATE TABLE IF NOT EXISTS DOCUMENT (" +
                            "id TEXT PRIMARY KEY, " +
                            "titre TEXT NOT NULL, " +
                            "auteur TEXT, " +
                            "genre TEXT, " +
                            "est_emprunte BOOLEAN, " +
                            "type_doc TEXT NOT NULL, " + 
                            "isbn TEXT, nb_pages INTEGER, editeur TEXT, " + // specifique Livre
                            "numero INTEGER, periodicite TEXT, " + // specifique Mag
                            "artiste TEXT, duree INTEGER, pistes INTEGER)"; // specifique CD
            stmt.execute(sqlDoc);

            // table EMPRUNT
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

            //table UTILISATEUR (pour la connexion)
            String sqlUser = "CREATE TABLE IF NOT EXISTS UTILISATEUR (" +
                             "identifiant TEXT PRIMARY KEY, " +
                             "mot_de_passe TEXT NOT NULL)";
            stmt.execute(sqlUser);

            System.out.println("MANAGER: verif des tables ok.");

        } catch (SQLException e) {
            System.err.println("ERREUR CRITIQUE: pb init BDD.");
            e.printStackTrace();
        }
    }


    /**
     * tente d'enregistrer un nouvel emprunt.
     * applique les regles : dispo, statut, quota.
     * @return true si ok, false sinon
     */
    public boolean emprunter(Adherent adherent, Document document) {
        try {
            // Regle 1 : dispo
            if (document.estEmprunte()) {
                System.out.println("REFUS: doc deja emprunte.");
                return false;
            }

            // Regle 2 : statut adherent
            if (adherent.getStatut() != E_StatutAdherent.ACTIF) {
                System.out.println("REFUS: adherent bloque ou avec penalites.");
                return false;
            }

            // Regle 3 : quota (max 5 emprunts en cours)
            List<Emprunt> empruntsEnCours = empruntDAO.findByAdherent(adherent).stream()
                    .filter(e -> e.getDateRetourReelle() == null) // filtre ceux non rendus
                    .collect(Collectors.toList());

            if (empruntsEnCours.size() >= 5) {
                System.out.println("REFUS: quota de 5 atteint.");
                return false;
            }

            // tout est bon, on cree l'emprunt
            String idUnique = "EMP-" + System.currentTimeMillis();
            Emprunt nouvelEmprunt = new Emprunt(idUnique, document, adherent);

            // TRANSACTION : save l'emprunt + update le document
            empruntDAO.save(nouvelEmprunt);
            
            document.setEstEmprunte(true);
            documentDAO.update(document); // maj le statut en bdd

            System.out.println("SUCCES: emprunt ok pour " + adherent.getNom());
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * gere le retour d'un doc
     * calcule les penalites si besoin
     */
    public void rendre(Emprunt emprunt) {
        try {
            //ate de retour
            emprunt.setDateRetourReelle(LocalDate.now());
            empruntDAO.update(emprunt);

            //liberer le doc
            Document doc = emprunt.getDocumentEmprunte();
            doc.setEstEmprunte(false);
            documentDAO.update(doc);

            //verif retard
            long joursRetard = ChronoUnit.DAYS.between(emprunt.getDateRetourPrevue(), emprunt.getDateRetourReelle());
            
            if (joursRetard > 0) {
                double amende = joursRetard * 0.50; // 0.50 cts par jour
                System.out.println("RETARD: " + joursRetard + " jours. Amende: " + amende + "e");
                
                // update adherent
                Adherent adh = emprunt.getEmprunteur();
                adh.ajouterPenalite(amende);
                adherentDAO.update(adh);
            } else {
                System.out.println("Retour a l'heure.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void ajouterDocument(Document d) throws SQLException {
        documentDAO.save(d);
    }
    
    public void ajouterAdherent(Adherent a) throws SQLException {
        adherentDAO.save(a);
    }

    // recherche avec filtre par type
    public List<Document> rechercherDocuments(String critere, String type) {
        try {
            String typeFiltre = (type == null) ? "TOUT" : type;
            return documentDAO.findByCriteria(critere, typeFiltre);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
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
     * recupere tous les adherents de la bdd
     */
    public List<Adherent> listerAdherents() {
        try {
            return adherentDAO.findAll();
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    /**
     * cherche un doc par son ID precis
     */
    public Document recupererDocumentParId(String id) {
        try {
            return documentDAO.findById(id);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void supprimerDocument(String id) throws SQLException {
        documentDAO.delete(id);
    }
    

    public void modifierDocument(Document d) throws SQLException {
        documentDAO.update(d);
    }

    public void modifierAdherent(Adherent a) throws SQLException {
        adherentDAO.update(a);
    }

    /**
     * recupere TOUS les emprunts (meme rendus) pour l'historique
     */
    public List<Emprunt> recupererHistorique(Adherent a) {
        try {
            return empruntDAO.findByAdherent(a);
        } catch (SQLException e) {
            e.printStackTrace();
            return List.of();
        }
    }


    public boolean inscrire(String identifiant, String mdp) {
        try {
            if (utilisateurDAO.findById(identifiant) != null) {
                System.out.println("Erreur: ID deja pris.");
                return false;
            }
            utilisateurDAO.create(new Utilisateur(identifiant, mdp));
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean seConnecter(String identifiant, String mdp) {
        try {
            Utilisateur u = utilisateurDAO.findById(identifiant);
            if (u != null && u.getMotDePasse().equals(mdp)) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * regle la dette d'un adherent et le remet ACTIF
     */
    public void reglerPenalite(Adherent a) throws SQLException {
        a.reglerPenalite(); 
        adherentDAO.update(a); 
    }

    public void supprimerAdherent(String id) throws SQLException {
        // la bdd devrait bloquer si y'a des emprunts
        adherentDAO.delete(id);
    }
}