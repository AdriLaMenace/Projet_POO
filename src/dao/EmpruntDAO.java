package dao;

import entites.Adherent;
import entites.Document;
import entites.Emprunt;
import java.sql.SQLException;
import java.util.List;

public interface EmpruntDAO extends GenericDAO<Emprunt> {
    // Méthodes spécifiques : trouver les emprunts non rendus, ou par adhérent
    List<Emprunt> findEncours() throws SQLException;
    List<Emprunt> findByAdherent(Adherent adherent) throws SQLException;
    List<Emprunt> findRetards() throws SQLException;
}