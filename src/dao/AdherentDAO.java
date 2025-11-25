package dao;

import entites.Adherent;
import java.sql.SQLException;

/**
 * Interface DAO spécifique aux adhérents.
 */
public interface AdherentDAO extends GenericDAO<Adherent> {
    // Méthodes spécifiques à l'adhérent, si nécessaire
    Adherent findByNom(String nom) throws SQLException;
}