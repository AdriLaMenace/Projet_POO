package entites;

/**
 * Définit les différents états possibles d'un adhérent en fonction de ses pénalités.
 */
public enum E_StatutAdherent {
    ACTIF,          // Peut emprunter, aucune pénalité.
    AVEC_PENALITE,  // A un retard ou une amende à payer, emprunts bloqués.
    BLOQUE          // Statut permanent (si récidive ou autre problème).
}