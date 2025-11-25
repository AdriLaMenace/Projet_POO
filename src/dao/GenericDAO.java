package dao;

import java.sql.SQLException;
import java.util.List;

/**
 * Interface générique pour les opérations CRUD de base.
 * Le type T représente l'Entité (Adherent, Document, Emprunt).
 */
public interface GenericDAO<T> {

    // On déclare que toutes les opérations de BDD peuvent lancer une exception SQL (vu en TD4)
    void save(T entity) throws SQLException;
    T findById(String id) throws SQLException;
    List<T> findAll() throws SQLException;
    void update(T entity) throws SQLException;
    void delete(String id) throws SQLException;
}