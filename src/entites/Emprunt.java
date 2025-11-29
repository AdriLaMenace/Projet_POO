package entites;

import java.time.LocalDate;
import java.util.Objects;

/**
 * décris un emprunt fait par un Adherent
 */
public class Emprunt {
    // tout en final pour pas bouger
    private final String idEmprunt;
    private final Document documentEmprunte;
    private final Adherent emprunteur;
    private final LocalDate dateEmprunt;
    private final LocalDate dateRetourPrevue; 

    private LocalDate dateRetourReelle; 

    public Emprunt(String idEmprunt, Document documentEmprunte , Adherent emprunteur) {
         
        this.idEmprunt= Objects.requireNonNull(idEmprunt );
        this.documentEmprunte =Objects.requireNonNull(documentEmprunte);
        this.emprunteur = Objects.requireNonNull( emprunteur);
        
        this.dateEmprunt = LocalDate.now(); //date du jour 'hui
        this.dateRetourPrevue = this.dateEmprunt.plusWeeks(3);  // on a choisi 3 semaines
       
        this.dateRetourReelle =null; //on attend le retour
    }


    public String getIdEmprunt() {
        return idEmprunt ;
    }

    public Document getDocumentEmprunte() {
        return documentEmprunte; 

    }

    public Adherent getEmprunteur() {
        return emprunteur; 
    }

    public LocalDate getDateEmprunt() {
        return dateEmprunt;
    }

    public LocalDate getDateRetourPrevue() {
        return dateRetourPrevue; 

    }

    public LocalDate getDateRetourReelle() {
        return dateRetourReelle ;
    }

    
    /**
     * méthode pour enregistrer la date de retour 
     */
    public void setDateRetourReelle(LocalDate dateRetourReelle) {
        this.dateRetourReelle =dateRetourReelle;
    }
}