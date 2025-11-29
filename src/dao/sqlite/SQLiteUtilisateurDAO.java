package dao.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import bdd.C_ConnexionSQLite;
import dao.UtilisateurDAO;
import entites.Utilisateur;

public class SQLiteUtilisateurDAO implements UtilisateurDAO {
    private Connection connexion = C_ConnexionSQLite.getInstance();

    @Override
    public void create(Utilisateur u) throws SQLException {
        String sql = "INSERT INTO UTILISATEUR(identifiant, mot_de_passe) VALUES(?, ?)";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, u.getIdentifiant());
            pstmt.setString(2, u.getMotDePasse());
            pstmt.executeUpdate();
        }
    }

    @Override
    public Utilisateur findById(String identifiant) throws SQLException {
        String sql = "SELECT * FROM UTILISATEUR WHERE identifiant = ?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, identifiant);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Utilisateur(rs.getString("identifiant"), rs.getString("mot_de_passe"));
                }
            }
        }
        return null;
    }
}