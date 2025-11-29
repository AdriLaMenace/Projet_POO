package dao.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import bdd.C_ConnexionSQLite;
import dao.UtilisateurDAO;
import entites.Utilisateur;

/**
 * implémentation du dao pour utilisateurs 
 * gère les opérations crud sur la table UTILISATEUR
 */
public class SQLiteUtilisateurDAO implements UtilisateurDAO {
    
    //connexion
    private Connection connexion = C_ConnexionSQLite.getInstance();

    /**
     * nouvel utilisateur dans la bdd
     * lors de l'inscription
     */
    @Override
    public void create(Utilisateur u) throws SQLException {
        String query = "INSERT INTO UTILISATEUR(identifiant, mot_de_passe) VALUES(?, ?)";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, u.getIdentifiant());
            
            pstmt.setString(2, u.getMotDePasse());
            
            pstmt.executeUpdate();
        }
    }

    /**
     * find un utilisateur avec son login
     * check l'existence du compte lors de la connexion
     * @return L'objet Utilisateur si trouvé, null sinon.
     */
    @Override
    public Utilisateur findById(String identifiant) throws SQLException {
        String query = "SELECT * FROM UTILISATEUR WHERE identifiant = ?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, identifiant);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                // on reconstruit l'objet si on trouve une ligne 
                if (rs.next()) {
                    return new Utilisateur(
                        rs.getString("identifiant"), 
                        rs.getString("mot_de_passe")
                    );
                }
            }
        }
        // rien trouvé
        return null;
    }
}