package dao;

import java.sql.SQLException;
import java.util.List;

import entites.Adherent;
import entites.Emprunt;

public interface EmpruntDAO extends GenericDAO<Emprunt > {
    //trouver les emprunts non rendus ou par adhérent
    List<Emprunt > findEncours() throws SQLException;
     
    List<Emprunt > findByAdherent(Adherent adherent ) throws SQLException;
    List<Emprunt > findRetards() throws SQLException;
}