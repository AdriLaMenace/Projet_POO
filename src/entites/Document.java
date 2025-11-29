package entites;

import java.util.Objects;

/**
 * classe abstraite pour document générique
 * classe de base pour tous types de documents
 */
public abstract class Document {

    //ceci est commun à tous les documents
    private final String id; //en final
    private String titre;
    private String auteur;
    private String genre ;
    private boolean estEmprunte ; 

    public Document(String id, String titre, String auteur, String genre) {
        this.id= Objects.requireNonNull(id, "L'ID ne peut pas être null."); //pour robustesse
        this.titre = Objects.requireNonNull(titre, "Le titre ne peut pas être null.");
        this.auteur =auteur; //on met comme si ça pouvait êrte NULL
        this.genre= genre;
        this.estEmprunte = false;
    }

    //méthode abstraite pour affichage
    public abstract void afficherDetailsSpecifiques(); 

    
    public String getId() {
        return id; 

    }

    public String getTitre() {
        return titre ;
    }

    public void setTitre(String titre) {
        this.titre =titre;
    }

    public String getAuteur() {
        return auteur;
    }

    public void setAuteur(String auteur) {
        this.auteur= auteur;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre =genre;
    }

    public boolean estEmprunte() {
        return estEmprunte;
        
    }

    public void setEstEmprunte(boolean estEmprunte) {
        this.estEmprunte= estEmprunte;
    }
}