package entites;

import java.util.Objects; // Nécessaire pour Objects.requireNonNull

/**
 * Classe abstraite représentant un document générique dans la bibliothèque.
 * Elle est la classe de base pour tous les types de documents (Livre, Magazine, CD...).
 */
public abstract class Document {

    // Attributs communs à tous les documents
    private final String id; // ID est final car il ne change pas après la création (bonne pratique TD3)
    private String titre;
    private String auteur;
    private String genre;
    private boolean estEmprunte; 

    // Constructeur
    public Document(String id, String titre, String auteur, String genre) {
        // Utilisation de Objects.requireNonNull pour la robustesse (vu en TD3)
        this.id = Objects.requireNonNull(id, "L'ID ne peut pas être null.");
        this.titre = Objects.requireNonNull(titre, "Le titre ne peut pas être null.");
        this.auteur = auteur; // L'auteur peut être null pour certains documents (ex: Magazine)
        this.genre = genre;
        this.estEmprunte = false;
    }

    // Méthode abstraite pour forcer l'affichage des détails spécifiques à chaque type
    public abstract void afficherDetailsSpecifiques(); 


    // --- Getters et Setters ---
    
    public String getId() {
        return id;
    }

    public String getTitre() {
        return titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur = auteur;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public boolean estEmprunte() {
        return estEmprunte;
    }

    public void setEstEmprunte(boolean estEmprunte) {
        this.estEmprunte = estEmprunte;
    }
}