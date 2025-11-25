package entites;

/**
 * Représente un Magazine. C'est un Document qui est un TiragePapier.
 */
public class Magazine extends Document implements TiragePapier {

    private int numero;
    private String periodicite;
    private int nombrePages; // Spécifique au numéro
    private String editeur; // L'éditeur du magazine

    public Magazine(String id, String titre, String auteur, String genre, int numero, String periodicite, int nombrePages, String editeur) {
        // L'auteur est souvent nul pour un magazine
        super(id, titre, auteur, genre); 
        this.numero = numero;
        this.periodicite = periodicite;
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
        System.out.println("Numéro: " + numero + ", Périodicité: " + periodicite + ", Pages: " + nombrePages);
    }

    // Getters et Setters spécifiques
    public int getNumero() {
        return numero;
    }

    public void setNumero(int numero) {
        this.numero = numero;
    }

    public String getPeriodicite() {
        return periodicite;
    }

    public void setPeriodicite(String periodicite) {
        this.periodicite = periodicite;
    }
    
    public void setNombrePages(int nombrePages) {
        this.nombrePages = nombrePages;
    }

    public void setEditeur(String editeur) {
        this.editeur = editeur;
    }
}