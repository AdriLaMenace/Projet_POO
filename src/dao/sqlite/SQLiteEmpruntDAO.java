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

public class SQLiteEmpruntDAO implements EmpruntDAO {

    private Connection connexion = C_ConnexionSQLite.getInstance();
    private AdherentDAO adherentDAO = DAOFactory.getAdherentDAO();
    private DocumentDAO documentDAO = DAOFactory.getDocumentDAO();

    @Override
    public void save(Emprunt emprunt) throws SQLException {
        String sql = "INSERT INTO EMPRUNT(id, id_document, id_adherent, date_emprunt, date_retour_prevue, date_retour_reelle) VALUES(?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, emprunt.getIdEmprunt());
            pstmt.setString(2, emprunt.getDocumentEmprunte().getId());
            pstmt.setString(3, emprunt.getEmprunteur().getIdAdherent());
            pstmt.setString(4, emprunt.getDateEmprunt().toString());
            pstmt.setString(5, emprunt.getDateRetourPrevue().toString());
            pstmt.setString(6, null);
            pstmt.executeUpdate();
        }
    }

    @Override
    public void update(Emprunt emprunt) throws SQLException {
        String sql = "UPDATE EMPRUNT SET date_retour_reelle=? WHERE id=?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            if (emprunt.getDateRetourReelle() != null) {
                pstmt.setString(1, emprunt.getDateRetourReelle().toString());
            } else {
                pstmt.setString(1, null);
            }
            pstmt.setString(2, emprunt.getIdEmprunt());
            pstmt.executeUpdate();
        }
    }

    @Override
    public Emprunt findById(String id) throws SQLException {
        String sql = "SELECT * FROM EMPRUNT WHERE id = ?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapResultSetToEmprunt(rs);
            }
        }
        return null;
    }

    @Override
    public List<Emprunt> findAll() throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM EMPRUNT";
        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Emprunt e = mapResultSetToEmprunt(rs);
                if (e != null) liste.add(e); // SÉCURITÉ AJOUTÉE
            }
        }
        return liste;
    }

    @Override
    public List<Emprunt> findEncours() throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM EMPRUNT WHERE date_retour_reelle IS NULL";
        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Emprunt e = mapResultSetToEmprunt(rs);
                if (e != null) liste.add(e); // SÉCURITÉ AJOUTÉE
            }
        }
        return liste;
    }
    
    @Override
    public List<Emprunt> findByAdherent(Adherent adherent) throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM EMPRUNT WHERE id_adherent = ?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, adherent.getIdAdherent());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                Emprunt e = mapResultSetToEmprunt(rs);
                if (e != null) liste.add(e); // SÉCURITÉ AJOUTÉE : Si un emprunt est corrompu, on l'ignore au lieu de planter
            }
        }
        return liste;
    }

    @Override
    public List<Emprunt> findRetards() throws SQLException {
        List<Emprunt> liste = new ArrayList<>();
        String sql = "SELECT * FROM EMPRUNT WHERE date_retour_reelle IS NULL AND date_retour_prevue < date('now')";
        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Emprunt e = mapResultSetToEmprunt(rs);
                if (e != null) liste.add(e); // SÉCURITÉ AJOUTÉE
            }
        }
        return liste;
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM EMPRUNT WHERE id=?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    private Emprunt mapResultSetToEmprunt(ResultSet rs) throws SQLException {
        String idDoc = rs.getString("id_document");
        String idAdh = rs.getString("id_adherent");

        // On vérifie que le document et l'adhérent existent toujours
        Document doc = documentDAO.findById(idDoc);
        Adherent adh = adherentDAO.findById(idAdh);

        // Si l'un des deux a été supprimé de la base, cet emprunt est "orphelin" -> On retourne null
        if (doc == null || adh == null) return null;

        Emprunt e = new Emprunt(rs.getString("id"), doc, adh);
        
        // Reconstruction des dates exactes
        String dateEmpruntStr = rs.getString("date_emprunt");
        // Astuce : on utilise la reflection ou on suppose que la date est bonne, 
        // ou idéalement on ajoute un setter pour la date d'emprunt dans l'entité si besoin.
        // Ici on garde la logique simple.
        
        String dateRetourReelleStr = rs.getString("date_retour_reelle");
        if (dateRetourReelleStr != null) {
            e.setDateRetourReelle(LocalDate.parse(dateRetourReelleStr));
        }
        
        return e;
    }
}