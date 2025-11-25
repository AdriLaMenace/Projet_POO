package entites;

/**
 * Représente un Livre. C'est un Document qui est un TiragePapier.
 */
public class Livre extends Document implements TiragePapier {

    private String isbn;
    private int nombrePages;
    private String editeur;

    // Constructeur principal
    public Livre(String id, String titre, String auteur, String genre, String isbn, int nombrePages, String editeur) {
        super(id, titre, auteur, genre); // Appel au constructeur de la classe mère
        this.isbn = isbn;
        this.nombrePages = nombrePages;
        this.editeur = editeur;
    }

    // Implémentation des méthodes de l'interface TiragePapier
    @Override
    public int getNombrePages() {
        return nombrePages;
    }

    @Override
    public String getEditeur() {
        return editeur;
    }

    // Implémentation de la méthode abstraite de Document
    @Override
    public void afficherDetailsSpecifiques() {
        System.out.println("ISBN: " + isbn + ", Pages: " + nombrePages + ", Éditeur: " + editeur);
    }
    
    // Getters et Setters spécifiques
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setNombrePages(int nombrePages) {
        this.nombrePages = nombrePages;
    }

    public void setEditeur(String editeur) {
        this.editeur = editeur;
    }
}