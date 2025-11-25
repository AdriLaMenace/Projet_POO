package dao.sqlite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import bdd.C_ConnexionSQLite;
import dao.DocumentDAO;
import entites.*;

public class SQLiteDocumentDAO implements DocumentDAO {

    private Connection connexion = C_ConnexionSQLite.getInstance();

    @Override
    public void save(Document doc) throws SQLException {
        // Une seule table pour tout le monde, avec des colonnes NULLables
        String sql = "INSERT INTO DOCUMENT(id, titre, auteur, genre, est_emprunte, type_doc, " +
                     "isbn, nb_pages, editeur, numero, periodicite, artiste, duree, pistes) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, doc.getId());
            pstmt.setString(2, doc.getTitre());
            pstmt.setString(3, doc.getAuteur());
            pstmt.setString(4, doc.getGenre());
            pstmt.setBoolean(5, doc.estEmprunte());

            // Gestion du Polymorphisme avec instanceof
            if (doc instanceof Livre) {
                Livre l = (Livre) doc;
                pstmt.setString(6, "LIVRE");
                pstmt.setString(7, l.getIsbn());
                pstmt.setInt(8, l.getNombrePages());
                pstmt.setString(9, l.getEditeur());
                // Les autres champs sont NULL
                pstmt.setNull(10, Types.INTEGER); pstmt.setNull(11, Types.VARCHAR);
                pstmt.setNull(12, Types.VARCHAR); pstmt.setNull(13, Types.INTEGER); pstmt.setNull(14, Types.INTEGER);
            } 
            else if (doc instanceof Magazine) {
                Magazine m = (Magazine) doc;
                pstmt.setString(6, "MAGAZINE");
                pstmt.setNull(7, Types.VARCHAR);
                pstmt.setInt(8, m.getNombrePages());
                pstmt.setString(9, m.getEditeur());
                pstmt.setInt(10, m.getNumero());
                pstmt.setString(11, m.getPeriodicite());
                pstmt.setNull(12, Types.VARCHAR); pstmt.setNull(13, Types.INTEGER); pstmt.setNull(14, Types.INTEGER);
            }
            else if (doc instanceof CD) {
                CD c = (CD) doc;
                pstmt.setString(6, "CD");
                pstmt.setNull(7, Types.VARCHAR); pstmt.setNull(8, Types.INTEGER); pstmt.setNull(9, Types.VARCHAR);
                pstmt.setNull(10, Types.INTEGER); pstmt.setNull(11, Types.VARCHAR);
                pstmt.setString(12, c.getArtistePrincipal());
                pstmt.setInt(13, c.getDureeMinutes());
                pstmt.setInt(14, c.getNombrePistes());
            }
            pstmt.executeUpdate();
        }
    }

    @Override
    public Document findById(String id) throws SQLException {
        String sql = "SELECT * FROM DOCUMENT WHERE id = ?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
        }
        return null;
    }

    @Override
    public List<Document> findAll() throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = "SELECT * FROM DOCUMENT";
        try (Statement stmt = connexion.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                liste.add(mapResultSetToDocument(rs));
            }
        }
        return liste;
    }

    @Override
    public List<Document> findByTitreOrAuteur(String critere) throws SQLException {
        List<Document> liste = new ArrayList<>();
        String sql = "SELECT * FROM DOCUMENT WHERE titre LIKE ? OR auteur LIKE ?";
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, "%" + critere + "%");
            pstmt.setString(2, "%" + critere + "%");
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSetToDocument(rs));
            }
        }
        return liste;
    }

    @Override
    public void update(Document doc) throws SQLException {
        // Mise à jour simple (statut emprunté)
        String sql = "UPDATE DOCUMENT SET est_emprunte=? WHERE id=?";
        try(PreparedStatement pstmt = connexion.prepareStatement(sql)){
            pstmt.setBoolean(1, doc.estEmprunte());
            pstmt.setString(2, doc.getId());
            pstmt.executeUpdate();
        }
    }

    @Override
    public void delete(String id) throws SQLException {
        String sql = "DELETE FROM DOCUMENT WHERE id=?";
        try(PreparedStatement pstmt = connexion.prepareStatement(sql)){
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public int countTotalDocuments() throws SQLException {
        String sql = "SELECT COUNT(*) FROM DOCUMENT";
        try(Statement stmt = connexion.createStatement();
            ResultSet rs = stmt.executeQuery(sql)){
            if(rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // Reconstruction de l'objet Java selon le type stocké en base
    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        String type = rs.getString("type_doc");
        Document doc = null;

        if ("LIVRE".equals(type)) {
            doc = new Livre(rs.getString("id"), rs.getString("titre"), rs.getString("auteur"), rs.getString("genre"),
                            rs.getString("isbn"), rs.getInt("nb_pages"), rs.getString("editeur"));
        } else if ("MAGAZINE".equals(type)) {
            doc = new Magazine(rs.getString("id"), rs.getString("titre"), rs.getString("auteur"), rs.getString("genre"),
                               rs.getInt("numero"), rs.getString("periodicite"), rs.getInt("nb_pages"), rs.getString("editeur"));
        } else if ("CD".equals(type)) {
            doc = new CD(rs.getString("id"), rs.getString("titre"), rs.getString("artiste"), rs.getString("genre"),
                         rs.getInt("duree"), rs.getInt("pistes"));
        }
        
        if (doc != null) {
            doc.setEstEmprunte(rs.getBoolean("est_emprunte"));
        }
        return doc;
    }
}