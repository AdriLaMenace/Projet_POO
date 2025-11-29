package dao.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import bdd.C_ConnexionSQLite;
import dao.DocumentDAO;
import entites.CD;
import entites.Document;
import entites.Livre;
import entites.Magazine;

/**
 * implémentation du dao pour Documents 
 * ici on utilise la strat "Single Table Inheritance" : 
 * tous les types de documents sont stockés dans la même table DOCUMENT avec une colonne discriminante "type_doc"
 */
public class SQLiteDocumentDAO implements DocumentDAO {

    //connexion
    private Connection connexion =C_ConnexionSQLite.getInstance();

    /**
     * Sauvegarde un nouveau document dans la bdd
     * Gère les colonnes en mettant NULL si non applicable
     */
    @Override
    public void save(Document doc) throws SQLException {
        //contient toutes les colonnes possibles de tous les types de documents
        String query = "INSERT INTO DOCUMENT(id, titre, auteur, genre, est_emprunte, type_doc, " +
                       "isbn, nb_pages, editeur, numero, periodicite, artiste, duree, pistes) " +
                       "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            //paramètres communs
            pstmt.setString(1, doc.getId());
            pstmt.setString(2, doc.getTitre());
            pstmt.setString(3, doc.getAuteur());
            pstmt.setString(4, doc.getGenre());
            pstmt.setBoolean(5, doc.estEmprunte());

            //les autres parametres
            //"instanceof" pour avoir le type réel de l'objet
            if (doc instanceof Livre) {
                Livre l = (Livre) doc;
                pstmt.setString(6, "LIVRE");
                pstmt.setString(7, l.getIsbn());
                pstmt.setInt(8, l.getNombrePages());
                pstmt.setString(9, l.getEditeur());
                //Magazine et CD sont à NULL
                setNullParams(pstmt, 10, 14);
            } 
            else if (doc instanceof Magazine) {
                Magazine m = (Magazine) doc;
                pstmt.setString(6, "MAGAZINE");
                pstmt.setNull(7, Types.VARCHAR); // l'ISBN null
                pstmt.setInt(8, m.getNombrePages());
                pstmt.setString(9, m.getEditeur());
                pstmt.setInt(10, m.getNumero());
                pstmt.setString(11, m.getPeriodicite());
                //CD à NULL
                setNullParams(pstmt, 12, 14);
            }
            else if (doc instanceof CD) {
                CD c = (CD) doc;
                pstmt.setString(6, "CD");
                //livre et magazine à NULL
                setNullParams(pstmt, 7, 11);
                pstmt.setString(12, c.getArtistePrincipal());
                pstmt.setInt(13, c.getDureeMinutes());
                pstmt.setInt(14, c.getNombrePistes());
            }
            
            pstmt.executeUpdate();
        }
    }

    /**
     * recherche avec ID
     */
    @Override
    public Document findById(String id) throws SQLException {
        String query = "SELECT * FROM DOCUMENT WHERE id = ?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, id);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDocument(rs);
                }
            }
        }
        return null;
    }

    /**
     * récupère tous les doc de la bdd
     */
    @Override
    public List<Document> findAll() throws SQLException {
        //on use encore la méthode de recherche générique sans filtre
        return findByCriteria("", "TOUT");
    }

    /**
     * Recherche multicritères
     * @param critere Le texte à chercher.
     * @param typeDoc Le type de document ("LIVRE", "CD", "MAGAZINE" ou "TOUT").
     */
    @Override
    public List<Document> findByCriteria(String critere, String typeDoc) throws SQLException {
        List<Document> liste = new ArrayList<>();
        
        //construction de la requête SQL
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM DOCUMENT WHERE (titre LIKE ? OR auteur LIKE ? OR isbn LIKE ? OR genre LIKE ? OR id LIKE ?)");
        
        //filtre
        if (typeDoc != null && !"TOUT".equals(typeDoc)) {
            sb.append(" AND type_doc = ?");
        }

        try (PreparedStatement pstmt = connexion.prepareStatement(sb.toString())) {
            String searchPattern = "%" + critere + "%";
            
            //5 paramètres 
            for (int i = 1; i <= 5; i++) {
                pstmt.setString(i, searchPattern);
            }
            
            if (typeDoc != null && !"TOUT".equals(typeDoc)) { // le 6eme 
                pstmt.setString(6, typeDoc);
            }
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    liste.add(mapResultSetToDocument(rs));
                }
            }
        }
        return liste;
    }

    /**
     * MAJ des informations d'un document 
     */
    @Override
    public void update(Document doc) throws SQLException {
        String query = "UPDATE DOCUMENT SET titre=?, auteur=?, genre=?, est_emprunte=?, " +
                       "isbn=?, nb_pages=?, editeur=?, numero=?, periodicite=?, artiste=?, duree=?, pistes=? " +
                       "WHERE id=?";
        
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            //communs
            pstmt.setString(1, doc.getTitre());
            pstmt.setString(2, doc.getAuteur());
            pstmt.setString(3, doc.getGenre());
            pstmt.setBoolean(4, doc.estEmprunte());

            //les autres
            if (doc instanceof Livre) {
                Livre l = (Livre) doc;
                pstmt.setString(5, l.getIsbn());
                pstmt.setInt(6, l.getNombrePages());
                pstmt.setString(7, l.getEditeur());
                setNullParams(pstmt, 8, 12); 
            } else if (doc instanceof Magazine) {
                Magazine m = (Magazine) doc;
                pstmt.setNull(5, Types.VARCHAR); // l'ISBN NULL
                pstmt.setInt(6, m.getNombrePages());
                pstmt.setString(7, m.getEditeur());
                pstmt.setInt(8, m.getNumero());
                pstmt.setString(9, m.getPeriodicite());
                setNullParams(pstmt, 10, 12); // CD NULL
            } else if (doc instanceof CD) {
                CD c = (CD) doc;
                setNullParams(pstmt, 5, 9); // Livre/Mag NULL
                pstmt.setString(10, c.getArtistePrincipal());
                pstmt.setInt(11, c.getDureeMinutes());
                pstmt.setInt(12, c.getNombrePistes());
            } else {
                //pour la sécu
                setNullParams(pstmt, 5, 12);
            }

            pstmt.setString(13, doc.getId());
            
            pstmt.executeUpdate();
        }
    }

    /**
     * Supprime un document de la bdd
     */
    @Override
    public void delete(String id) throws SQLException {
        String query = "DELETE FROM DOCUMENT WHERE id=?";
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        }
    }

    @Override
    public int countTotalDocuments() throws SQLException {
        return 0;
    }

    /**
     * méthode utilitaire pour reconstruire l'objet à partir de résultat SQL
     */
    private Document mapResultSetToDocument(ResultSet rs) throws SQLException {
        String type = rs.getString("type_doc");
        Document doc = null;

        if ("LIVRE".equals(type)) {
            doc = new Livre(
                rs.getString("id"), rs.getString("titre"), rs.getString("auteur"), rs.getString("genre"),
                rs.getString("isbn"), rs.getInt("nb_pages"), rs.getString("editeur")
            );
        } else if ("MAGAZINE".equals(type)) {
            doc = new Magazine(
                rs.getString("id"), rs.getString("titre"), rs.getString("auteur"), rs.getString("genre"),
                rs.getInt("numero"), rs.getString("periodicite"), rs.getInt("nb_pages"), rs.getString("editeur")
            );
        } else if ("CD".equals(type)) {
            doc = new CD(
                rs.getString("id"), rs.getString("titre"), rs.getString("artiste"), rs.getString("genre"),
                rs.getInt("duree"), rs.getInt("pistes")
            );
        }
        
        if (doc != null) {
            doc.setEstEmprunte(rs.getBoolean("est_emprunte"));
        }
        return doc;
    }
    
    /**
     * mettre plusieurs parametres à null dans  PreparedStatement
     */
    private void setNullParams(PreparedStatement pstmt, int start, int end) throws SQLException {
        for (int i = start; i <= end; i++) {
            pstmt.setNull(i, Types.VARCHAR);
        }
    }
}