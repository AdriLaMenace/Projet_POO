package entites;

import java.util.Objects;

/**
 * Représente un adhérent de la bibliothèque.
 * Intègre le statut de pénalité et le montant dû.
 */
public class Adherent {

    private final String idAdherent; // ID unique, final (ne change pas)
    private String nom;
    private String prenom;
    private String coordonnees;
    private E_StatutAdherent statut;
    private double montantPenalite; // Pour l'amélioration: montant dû en €

    // Constructeur
    public Adherent(String idAdherent, String nom, String prenom, String coordonnees) {
        // Robustesse: Objects.requireNonNull (vu en TD3)
        this.idAdherent = Objects.requireNonNull(idAdherent, "L'ID adhérent est obligatoire.");
        this.nom = Objects.requireNonNull(nom, "Le nom est obligatoire.");
        this.prenom = Objects.requireNonNull(prenom, "Le prénom est obligatoire.");
        this.coordonnees = coordonnees;
        this.statut = E_StatutAdherent.ACTIF; // Statut par défaut
        this.montantPenalite = 0.0;
    }

    // --- Getters et Setters ---

    public String getIdAdherent() {
        return idAdherent;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getCoordonnees() {
        return coordonnees;
    }

    public void setCoordonnees(String coordonnees) {
        this.coordonnees = coordonnees;
    }

    public E_StatutAdherent getStatut() {
        return statut;
    }

    public void setStatut(E_StatutAdherent statut) {
        this.statut = statut;
    }

    public double getMontantPenalite() {
        return montantPenalite;
    }

    /**
     * Méthode pour ajouter une pénalité au montant total.
     */
    public void ajouterPenalite(double montant) {
        if (montant > 0) {
            this.montantPenalite += montant;
            this.statut = E_StatutAdherent.AVEC_PENALITE;
        }
    }
    
    /**
     * Méthode pour régler une pénalité (remet le montant à zéro et le statut à ACTIF si tout est payé).
     */
    public void reglerPenalite() {
        this.montantPenalite = 0.0;
        this.statut = E_StatutAdherent.ACTIF;
    }
}