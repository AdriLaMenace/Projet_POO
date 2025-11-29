package dao;

import dao.sqlite.SQLiteAdherentDAO;
import dao.sqlite.SQLiteDocumentDAO;
import dao.sqlite.SQLiteEmpruntDAO;
import dao.sqlite.SQLiteUtilisateurDAO;

/**
 * Fabrique pour instancier les DAO.
 * Permet de changer facilement de type de base de données (ex: passer à MySQL)
 * sans toucher au reste du code.
 */
public class DAOFactory {

    public static AdherentDAO getAdherentDAO() {
        return new SQLiteAdherentDAO();
    }

    public static DocumentDAO getDocumentDAO() {
        return new SQLiteDocumentDAO();
    }
    
    // On ajoutera getEmpruntDAO plus tard
    
    public static EmpruntDAO getEmpruntDAO() {
        return new SQLiteEmpruntDAO();
    }

    public static UtilisateurDAO getUtilisateurDAO() {
            return new SQLiteUtilisateurDAO();
        }
}