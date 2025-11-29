package dao;

import java.sql.SQLException;

import entites.Adherent;

/**
 * interface dao pour adhérents.
 */
public interface AdherentDAO extends GenericDAO<Adherent > {
    Adherent findByNom(String nom ) throws SQLException;
}