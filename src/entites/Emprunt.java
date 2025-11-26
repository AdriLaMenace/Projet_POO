package entites;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Représente l'acte d'emprunt d'un Document par un Adherent.
 */
public class Emprunt {

    private final String idEmprunt;
    private final Document documentEmprunte;
    private final Adherent emprunteur;
    private final LocalDate dateEmprunt; // Date de départ
    private final LocalDate dateRetourPrevue; // Date calculée (+3 semaines)
    private LocalDate dateRetourReelle; // Date d'arrivée (null par défaut)

    public Emprunt(String idEmprunt, Document documentEmprunte, Adherent emprunteur) {
        this.idEmprunt = Objects.requireNonNull(idEmprunt);
        this.documentEmprunte = Objects.requireNonNull(documentEmprunte);
        this.emprunteur = Objects.requireNonNull(emprunteur);
        
        this.dateEmprunt = LocalDate.now(); // Date d'aujourd'hui
        // Calcul de la date de retour prévue (+3 semaines)
        this.dateRetourPrevue = this.dateEmprunt.plusWeeks(3); 
       
        this.dateRetourReelle = null; // En attente de retour
    }

    // --- Getters ---

    public String getIdEmprunt() {
        return idEmprunt;
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
        return dateRetourReelle;
    }

    // --- Setter ---
    
    /**
     * Enregistre la date de retour effective (méthode de fin de transaction).
     */
    public void setDateRetourReelle(LocalDate dateRetourReelle) {
        this.dateRetourReelle = dateRetourReelle;
    }
}