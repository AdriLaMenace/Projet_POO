package entites;

/**
 * Interface pour les documents qui ont une structure physique papier.
 */
public interface TiragePapier {
    
    // Attributs/Méthodes spécifiques aux documents imprimés
    int getNombrePages();
    String getEditeur();
}