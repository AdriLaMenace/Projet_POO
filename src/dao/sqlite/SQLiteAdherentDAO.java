package dao.sqlite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import bdd.C_ConnexionSQLite;
import dao.AdherentDAO;
import entites.Adherent;
import entites.E_StatutAdherent;

public class SQLiteAdherentDAO implements AdherentDAO {

    private Connection connexion = C_ConnexionSQLite.getInstance();

    @Override
    public void save(Adherent adherent) throws SQLException {
        String sql = "INSERT INTO ADHERENT(id, nom, prenom, coordonnees, statut, montant_penalite) VALUES(?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, adherent.getIdAdherent());
            pstmt.setString(2, adherent.getNom());
            pstmt.setString(3, adherent.getPrenom());
            pstmt.setString(4, adherent.getCoordonnees());
            pstmt.setString(5, adherent.getStatut().name()); // On stocke l'ENUM en texte (ex: "ACTIF")
            pstmt.setDouble(6, adherent.getMontantPenalite());
            pstmt.executeUpdate();
        }
    }

    @Override
    public Adherent findById(String id) throws SQLException {
        String sql = "SELECT * FROM ADHERENT WHERE id = ?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToAdherent(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Adherent> findAll() throws SQLException {
        List<Adherent> liste = new ArrayList<>();
        String sql = "SELECT * FROM ADHERENT";
        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSetToAdherent(rs));
            }
        }
        return liste;
    }
    
    @Override
    public Adherent findByNom(String nom) throws SQLException {
        // Implémentation similaire à findById mais avec le nom
        return null; // (A faire si besoin pour l'UI)
    }

    @Override
    public void update(Adherent adherent) throws SQLException {
        String sql = "UPDATE ADHERENT SET nom=?, prenom=?, coordonnees=?, statut=?, montant_penalite=? WHERE id=?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, adherent.getNom());
            pstmt.setString(2, adherent.getPrenom());
            pstmt.setString(3, adherent.getCoordonnees());
            pstmt.setString(4, adherent.getStatut().name());
            pstmt.setDouble(5, adherent.getMontantPenalite());
            pstmt.setString(6, adherent.getIdAdherent());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM ADHERENT WHERE id=?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    // Méthode utilitaire pour éviter de dupliquer le code de lecture
    private Adherent mapResultSetToAdherent(ResultSet rs) throws SQLException {
        Adherent ad = new Adherent(
            rs.getString("id"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("coordonnees")
        );
        // On remet le statut et la pénalité depuis la BDD
        ad.setStatut(E_StatutAdherent.valueOf(rs.getString("statut")));
        if(rs.getDouble("montant_penalite") > 0) {
            ad.ajouterPenalite(rs.getDouble("montant_penalite"));
        }
        return ad;
    }
}