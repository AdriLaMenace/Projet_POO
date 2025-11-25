package entites;

import java.util.List;

/**
 * Représente un CD. C'est un Document qui est un SupportMultimedia.
 */
public class CD extends Document implements SupportMultimedia {

    private String artistePrincipal; // L'auteur est souvent l'artiste principal pour un CD
    private int dureeMinutes;
    private int nombrePistes;

    public CD(String id, String titre, String artistePrincipal, String genre, int dureeMinutes, int nombrePistes) {
        super(id, titre, artistePrincipal, genre);
        this.artistePrincipal = artistePrincipal;
        this.dureeMinutes = dureeMinutes;
        this.nombrePistes = nombrePistes;
        // On met à jour l'auteur du document avec l'artiste principal
        super.setAuteur(artistePrincipal);
    }

    // Implémentation des méthodes de l'interface SupportMultimedia
    @Override
    public int getDureeMinutes() {
        return dureeMinutes;
    }

    @Override
    public int getNombrePistes() {
        return nombrePistes;
    }

    // Implémentation de la méthode abstraite de Document
    @Override
    public void afficherDetailsSpecifiques() {
        System.out.println("Artiste: " + artistePrincipal + ", Durée: " + dureeMinutes + " min, Pistes: " + nombrePistes);
    }
    
    // Getters et Setters spécifiques
    public String getArtistePrincipal() {
        return artistePrincipal;
    }

    public void setArtistePrincipal(String artistePrincipal) {
        this.artistePrincipal = artistePrincipal;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes = dureeMinutes;
    }

    public void setNombrePistes(int nombrePistes) {
        this.nombrePistes = nombrePistes;
    }
}