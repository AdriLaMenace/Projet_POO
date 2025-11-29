package dao;

import java.sql.SQLException;
import java.util.List;

import entites.Document;

public interface DocumentDAO extends GenericDAO<Document> {
    //pour rechercher avec le type. comme livre, cd, tout 
    List<Document> findByCriteria(String critere,String typeDoc) throws SQLException;
    int countTotalDocuments() throws SQLException ;
}