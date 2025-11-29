package dao;

import entites.Document;
import java.sql.SQLException;
import java.util.List;

public interface DocumentDAO extends GenericDAO<Document> {
    // Nouvelle méthode de recherche avec le type (LIVRE, CD, TOUT)
    List<Document> findByCriteria(String critere, String typeDoc) throws SQLException;
    int countTotalDocuments() throws SQLException;
}