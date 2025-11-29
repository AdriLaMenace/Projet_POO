package dao;

import java.sql.SQLException;

import entites.Utilisateur;

public interface UtilisateurDAO {
    void create(Utilisateur u) throws SQLException;
    Utilisateur findById(String identifiant) throws SQLException;
}