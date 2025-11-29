package dao.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import bdd.C_ConnexionSQLite;
import dao.AdherentDAO;
import dao.DAOFactory;
import dao.DocumentDAO;
import dao.EmpruntDAO;
import entites.Adherent;
import entites.Document;
import entites.Emprunt;

/**
 * implémentation dao pour Emprunts
 * lien entre adhérents et Documents
 */
public class SQLiteEmpruntDAO implements EmpruntDAO {

    //connexion 
    private Connection connexion = C_ConnexionSQLite.getInstance();

    // on a besoin des autres DAO
    private AdherentDAO adherentDAO = DAOFactory.getAdherentDAO();
    private DocumentDAO documentDAO = DAOFactory.getDocumentDAO();

    /**
     * save un nouvel emprunt en bdd
     * dates en String pour le stockage SQLite.
     */
    @Override
    public void save(Emprunt emprunt) throws SQLException {
        String query = "INSERT INTO EMPRUNT(id, id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle) " +
                       "VALUES(?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, emprunt.getIdEmprunt());
            pstmt.setString(2, emprunt.getDocumentEmprunte().getId());
            pstmt.setString(3, emprunt.getEmprunteur().getIdAdherent());
            
            // LocalDate en String
            pstmt.setString(4, emprunt.getDateEmprunt().toString());
            pstmt.setString(5, emprunt.getDateRetourPrevue().toString());
            
            //date de retour réelle est NULL au début
            pstmt.setString(6, null);
            
            pstmt.executeUpdate();
        }
    }

    /**
     * MAJ emprunt
     */
    @Override
    public void update(Emprunt emprunt) throws SQLException {
        String query = "UPDATE EMPRUNT SET date_retour_reelle=? WHERE id=?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            //on enregistre la date si il y a un rendu, sinon NULL
            if (emprunt.getDateRetourReelle() != null) {
                pstmt.setString(1, emprunt.getDateRetourReelle().toString());
            } else {
                pstmt.setString(1, null);
            }
            pstmt.setString(2, emprunt.getIdEmprunt());
            
            pstmt.executeUpdate();
        }
    }

    /**
     * Find emprunt par son ID
     */
    @Override
    public Emprunt findById(String id) throws SQLException {
        String query = "SELECT * FROM EMPRUNT WHERE id = ?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEmprunt(rs);
                }
            }
        }
        return null;
    }

    /**
     * historique des emprunts
     */
    @Override
    public List<Emprunt> findAll() throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String query = "SELECT * FROM EMPRUNT";
        
        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Emprunt e = mapResultSetToEmprunt(rs);
                //sécurité : ajoute que si l'emprunt est valide
                if (e != null) {
                    liste.add(e);
                }
            }
        }
        return liste;
    }

    /**
     * uniquement les emprunts en cours (ceux qui n'ont pas de date de retour réelle)
     */
    @Override
    public List<Emprunt> findEncours() throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String query = "SELECT * FROM EMPRUNT WHERE date_retour_reelle IS NULL";
        
        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Emprunt e = mapResultSetToEmprunt(rs);
                if (e != null) liste.add(e);
            }
        }
        return liste;
    }
    
    /**
     * find emprunts liés à un adhérent 
     */
    @Override
    public List<Emprunt> findByAdherent(Adherent adherent) throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String query = "SELECT * FROM EMPRUNT WHERE id_adherent = ?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, adherent.getIdAdherent());
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Emprunt e = mapResultSetToEmprunt(rs);
                    if (e != null) liste.add(e);
                }
            }
        }
        return liste;
    }

    /**
     * find les emprunts en retard 
     * on prend la fonction date('now') de SQLite
     */
    @Override
    public List<Emprunt> findRetards() throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String query = "SELECT * FROM EMPRUNT WHERE date_retour_reelle IS NULL AND date_retour_prevue < date('now')";
        
        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                Emprunt e = mapResultSetToEmprunt(rs);
                if (e != null) liste.add(e);
            }
        }
        return liste;
    }

    @Override
    public void delete(String id) throws SQLException {
        String query = "DELETE FROM EMPRUNT WHERE id=?";
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    /**
     * methode qui reconstruit l'objet Emprunt
     * va chercher dans Document et Adherent avec l'id
     */
    private Emprunt mapResultSetToEmprunt(ResultSet rs) throws SQLException {
        String idDoc = rs.getString("id_document");
        String idAdh = rs.getString("id_adherent");

        //récupération des objets liés
        Document doc = documentDAO.findById(idDoc);
        Adherent adh = adherentDAO.findById(idAdh);

        // vérification 
        if (doc == null || adh == null) {
            return null; 
        }

        //reconstruction de l'Emprunt
        Emprunt e = new Emprunt(rs.getString("id"), doc, adh);
        
        //les dates
        String dateRetourReelleStr = rs.getString("date_retour_reelle");
        if (dateRetourReelleStr != null) {
            e.setDateRetourReelle(LocalDate.parse(dateRetourReelleStr));
        }
        
        return e;
    }
}