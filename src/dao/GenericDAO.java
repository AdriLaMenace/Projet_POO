package dao;

import java.sql.SQLException;
import java.util.List;

/**
 * ici un interface générique pour les opérations CRUD
 * le type T sert pour les entité comme Adherent, doc, emprunt
 */
public interface GenericDAO<T> {

    // toutes opé de bdd peuvent lancer une exception SQL, comme vu au td4

    void save(T entity) throws SQLException;

    T findById(String id ) throws SQLException;
    List<T > findAll() throws SQLException;

 
    void update(T entity) throws SQLException;
    void delete(String id) throws SQLException ;
     
}