package dao.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import bdd.C_ConnexionSQLite;
import dao.AdherentDAO;
import entites.Adherent;
import entites.E_StatutAdherent;

/**
 *implémentation du dao pour : gestion adhérents
 * fait le lien entre aherent et la table sql adherent
 */
public class SQLiteAdherentDAO implements AdherentDAO {

    // pattern Singleton
    private Connection connexion =C_ConnexionSQLite.getInstance();

    /**
     * enregistre un nouvel adhérent dans la bdd
     */
    @Override
    public void save(Adherent adherent) throws SQLException {
        // Utilisation de ? pour éviter les injections SQL
        String query = "INSERT INTO ADHERENT(id, nom, prenom, coordonnees, statut, montant_penalite) VALUES(?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, adherent.getIdAdherent());
            pstmt.setString(2, adherent.getNom());
            pstmt.setString(3, adherent.getPrenom());
            pstmt.setString(4, adherent.getCoordonnees());
            
            pstmt.setString(5, adherent.getStatut().name());
            pstmt.setDouble(6, adherent.getMontantPenalite());
            
            pstmt.executeUpdate();
        }
    }

    /**
     * recherche un adhérent par son id 
     */
    @Override
    public Adherent findById(String id) throws SQLException {
        String query = "SELECT * FROM ADHERENT WHERE id = ?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // si y'a un match : on la convertit en objet
                if (rs.next()) {
                    return mapResultSetToAdherent(rs);
                }
            }
        }
        return null; // null si l'id n'existe dans la bdd
    }

    /**
     * récupere tous les adhérents en liste
     */
    @Override
    public List<Adherent> findAll() throws SQLException {
        List<Adherent> liste = new ArrayList<>();
        String query = "SELECT * FROM ADHERENT";
        
        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            while (rs.next()) {
                liste.add(mapResultSetToAdherent(rs));
            }
        }
        return liste;
    }
    
    @Override
    public Adherent findByNom(String nom) throws SQLException {
        return null; 
    }

    /**
     * MAJ les informations d'un adhérent existant
     */
    @Override
    public void update(Adherent adherent) throws SQLException {
        String query = "UPDATE ADHERENT SET nom=?, prenom=?, coordonnees=?, statut=?, montant_penalite=? WHERE id=?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, adherent.getNom());
            pstmt.setString(2, adherent.getPrenom());
            pstmt.setString(3, adherent.getCoordonnees());
            pstmt.setString(4, adherent.getStatut().name());
            pstmt.setDouble(5, adherent.getMontantPenalite());
            
            pstmt.setString(6, adherent.getIdAdherent());
            
            pstmt.executeUpdate();
        }
    }

    /**
     * Supprime un adhérent de la base de données.
     */
    @Override
    public void delete(String id) throws SQLException {
        String query = "DELETE FROM ADHERENT WHERE id=?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    
    private Adherent mapResultSetToAdherent(ResultSet rs) throws SQLException {
        //objet de base
        Adherent ad = new Adherent(
            rs.getString("id"),
            rs.getString("nom"),
            rs.getString("prenom"),
            rs.getString("coordonnees")
        );
        
        //vers l'enum java
        String statutStr = rs.getString("statut");
        if (statutStr != null) {
            ad.setStatut(E_StatutAdherent.valueOf(statutStr));
        }

        // Si l'adhérent avait une pénalité enregistrée, on la remet
        double penalite = rs.getDouble("montant_penalite");
        if (penalite > 0) {
            ad.ajouterPenalite(penalite);
        }
        
        return ad;
    }
}