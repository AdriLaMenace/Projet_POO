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
        super(id, titre, auteur, genre); // Appel constr de la classe mère
        this.isbn = isbn;
        this.nombrePages =nombrePages;
        this.editeur= editeur;
    }

    // Imp des méthodes de l'interface
    @Override
    public int getNombrePages() {
        return nombrePages;
    }

    @Override
    public String getEditeur() {
        return editeur;
    }

    // Imp méthode abstraite de Document
    @Override
    public void afficherDetailsSpecifiques() {
        System.out.println("ISBN: " + isbn + ", Pages: " + nombrePages + ", Éditeur: " + editeur);
    }
    
    // Getters et Setters
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