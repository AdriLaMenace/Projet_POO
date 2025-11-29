package entites;

/**
 * ici on décrit un CD : c'est un document et un SupportMultimedia.
 */
public class CD extends Document implements SupportMultimedia {

    private String Auteur; 
    private int dureeMinutes ;
    private int nombrePistes; 

    public CD(String id,String titre, String Auteur , String genre, int dureeMinutes, int nombrePistes) {
        super(id, titre, Auteur,genre );
        this.Auteur =Auteur;
        this.dureeMinutes= dureeMinutes;
        this.nombrePistes = nombrePistes;
  
        super.setAuteur( Auteur);
    }

    //méthodes de l'interface SupportMultimedia
    @Override
    public int getDureeMinutes() {
        return dureeMinutes ; 

    }

    @Override
    public int getNombrePistes() {
        return nombrePistes; 
    }

    //méthode abstraite 
    @Override
    public void afficherDetailsSpecifiques() {
         
        System.out.println("l'artiste : " + Auteur + ", la durée : " + dureeMinutes + " min, pistes : " + nombrePistes) ; 
    }
    
    //getters et setters
    public String getArtistePrincipal() {
        return Auteur; 
    }

    public void setArtistePrincipal(String Auteur) {
        this.Auteur =Auteur;
    }

    public void setDureeMinutes(int dureeMinutes) {
        this.dureeMinutes= dureeMinutes;
    }

    public void setNombrePistes(int nombrePistes) {
        this.nombrePistes =nombrePistes; 
        
    }
}