package dao.sqlite;

import bdd.C_ConnexionSQLite;
import dao.DocumentDAO;
import entites.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDocumentDAO implements DocumentDAO {

    private Connection connexion = C_ConnexionSQLite.getInstance();

    @Override
    public void save(Document doc) throws SQLException {
        String sql = "INSERT INTO DOCUMENT(id, titre, auteur, genre, est_emprunte, type_doc, " +
                     "isbn, nb_pages, editeur, numero, periodicite, artiste, duree, pistes) " +
                     "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, doc.getId());
            pstmt.setString(2, doc.getTitre());
            pstmt.setString(3, doc.getAuteur());
            pstmt.setString(4, doc.getGenre());
            pstmt.setBoolean(5, doc.estEmprunte());

            if (doc instanceof Livre) {
                Livre l = (Livre) doc;
                pstmt.setString(6, "LIVRE");
                pstmt.setString(7, l.getIsbn());
                pstmt.setInt(8, l.getNombrePages());
                pstmt.setString(9, l.getEditeur());
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
                if (rs.next()) return mapResultSetToDocument(rs);
            }
        }
        return null;
    }

    @Override
    public List<Document> findAll() throws SQLException {
        return findByCriteria("", "TOUT");
    }

    @Override
    public List<Document> findByCriteria(String critere, String typeDoc) throws SQLException {
        List<Document> liste = new ArrayList<>();
        
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM DOCUMENT WHERE (titre LIKE ? OR auteur LIKE ? OR isbn LIKE ? OR genre LIKE ? OR id LIKE ?)");
        
        if (typeDoc != null && !typeDoc.equals("TOUT")) {
            sb.append(" AND type_doc = ?");
        }

        try (PreparedStatement pstmt = connexion.prepareStatement(sb.toString())) {
            String search = "%" + critere + "%";
            pstmt.setString(1, search);
            pstmt.setString(2, search);
            pstmt.setString(3, search);
            pstmt.setString(4, search);
            pstmt.setString(5, search);
            
            if (typeDoc != null && !typeDoc.equals("TOUT")) {
                pstmt.setString(6, typeDoc);
            }
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                liste.add(mapResultSetToDocument(rs));
            }
        }
        return liste;
    }

    @Override
    public void update(Document doc) throws SQLException {
        // Mise à jour complète (Titre, Auteur, Genre + Spécifiques)
        String sql = "UPDATE DOCUMENT SET titre=?, auteur=?, genre=?, est_emprunte=?, " +
                     "isbn=?, nb_pages=?, editeur=?, numero=?, periodicite=?, artiste=?, duree=?, pistes=? " +
                     "WHERE id=?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(sql)) {
            pstmt.setString(1, doc.getTitre());
            pstmt.setString(2, doc.getAuteur());
            pstmt.setString(3, doc.getGenre());
            pstmt.setBoolean(4, doc.estEmprunte());

            // Gestion des champs spécifiques (NULL si pas concerné)
            if (doc instanceof Livre) {
                Livre l = (Livre) doc;
                pstmt.setString(5, l.getIsbn());
                pstmt.setInt(6, l.getNombrePages());
                pstmt.setString(7, l.getEditeur());
                pstmt.setNull(8, Types.INTEGER); pstmt.setNull(9, Types.VARCHAR); // Mag
                pstmt.setNull(10, Types.VARCHAR); pstmt.setNull(11, Types.INTEGER); pstmt.setNull(12, Types.INTEGER); // CD
            } else if (doc instanceof Magazine) {
                Magazine m = (Magazine) doc;
                pstmt.setNull(5, Types.VARCHAR); pstmt.setInt(6, m.getNombrePages()); pstmt.setString(7, m.getEditeur());
                pstmt.setInt(8, m.getNumero());
                pstmt.setString(9, m.getPeriodicite());
                pstmt.setNull(10, Types.VARCHAR); pstmt.setNull(11, Types.INTEGER); pstmt.setNull(12, Types.INTEGER);
            } else if (doc instanceof CD) {
                CD c = (CD) doc;
                pstmt.setNull(5, Types.VARCHAR); pstmt.setNull(6, Types.INTEGER); pstmt.setNull(7, Types.VARCHAR);
                pstmt.setNull(8, Types.INTEGER); pstmt.setNull(9, Types.VARCHAR);
                pstmt.setString(10, c.getArtistePrincipal());
                pstmt.setInt(11, c.getDureeMinutes());
                pstmt.setInt(12, c.getNombrePistes());
            } else {
                // Cas par défaut (ne devrait pas arriver)
                for(int i=5; i<=12; i++) pstmt.setNull(i, Types.VARCHAR);
            }

            pstmt.setString(13, doc.getId()); // WHERE ID = ?
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
    public int countTotalDocuments() throws SQLException { return 0; } // Pas utilisé ici

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
        
        if (doc != null) doc.setEstEmprunte(rs.getBoolean("est_emprunte"));
        return doc;
    }
}