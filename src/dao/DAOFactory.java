package dao;

import dao.sqlite.SQLiteAdherentDAO;
import dao.sqlite.SQLiteDocumentDAO;
import dao.sqlite.SQLiteEmpruntDAO;
import dao.sqlite.SQLiteUtilisateurDAO;

/**
 * c'est une fabrique pour les dao
 * ça aide pour sql
 * comme ça on touche pas au reste du code
 */
public class DAOFactory {

    public static AdherentDAO getAdherentDAO() {
        return new SQLiteAdherentDAO();
    }

    public static DocumentDAO getDocumentDAO() {
        return new SQLiteDocumentDAO();
    }
        
    public static EmpruntDAO getEmpruntDAO() {
        return new SQLiteEmpruntDAO();
    }

    public static UtilisateurDAO getUtilisateurDAO() {
            return new SQLiteUtilisateurDAO();
        }
}