package dao;

import entites.Document;
import java.sql.SQLException;
import java.util.List;

/**
 * Interface DAO spécifique aux documents.
 */
public interface DocumentDAO extends GenericDAO<Document> {
    // Méthodes spécifiques au document
    List<Document> findByTitreOrAuteur(String critere) throws SQLException;
    int countTotalDocuments() throws SQLException;
}