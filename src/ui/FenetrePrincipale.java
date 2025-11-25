package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import entites.*;
import service.BibliothequeManager;

/**
 * La fenêtre principale de l'application (Vue).
 * Elle utilise Swing pour afficher des onglets et des tableaux.
 */
public class FenetrePrincipale extends JFrame {

    private BibliothequeManager manager;

    // Composants graphiques
    private JTabbedPane onglets;
    private JTable tableDocuments, tableAdherents, tableEmprunts;
    private DefaultTableModel modeleDocuments, modeleAdherents, modeleEmprunts;

    public FenetrePrincipale(BibliothequeManager manager) {
        this.manager = manager;

        // Configuration de la fenêtre
        setTitle("SGEB - Gestion de Bibliothèque");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrer

        // Création des onglets
        onglets = new JTabbedPane();
        
        onglets.addTab("Gestion Documents", createPanelDocuments());
        onglets.addTab("Gestion Adhérents", createPanelAdherents());
        onglets.addTab("Emprunts & Retours", createPanelEmprunts());

        this.add(onglets);
    }

    // --- ONGLET 1 : DOCUMENTS ---
    private JPanel createPanelDocuments() {
        JPanel panel = new JPanel(new BorderLayout());

        // Barre d'outils (Haut)
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField champRecherche = new JTextField(20);
        JButton btnRechercher = new JButton("Rechercher");
        JButton btnAjouterLivre = new JButton("Nouveau Livre");
        JButton btnAjouterCD = new JButton("Nouveau CD");

        toolbar.add(new JLabel("Recherche :"));
        toolbar.add(champRecherche);
        toolbar.add(btnRechercher);
        toolbar.add(Box.createHorizontalStrut(20)); // Espace
        toolbar.add(btnAjouterLivre);
        toolbar.add(btnAjouterCD);

        // Tableau (Centre)
        String[] colonnes = {"ID", "Type", "Titre", "Auteur/Artiste", "Genre", "Détails", "Statut"};
        modeleDocuments = new DefaultTableModel(colonnes, 0);
        tableDocuments = new JTable(modeleDocuments);
        JScrollPane scrollPane = new JScrollPane(tableDocuments);

        // Actions
        btnRechercher.addActionListener(e -> rafraichirDocuments(champRecherche.getText()));

        // --- ACTION : AJOUTER CD ---
        btnAjouterCD.addActionListener(e -> {
            // 1. Champs de saisie
            JTextField titreField = new JTextField();
            JTextField artisteField = new JTextField();
            JTextField genreField = new JTextField();
            JTextField dureeField = new JTextField();
            JTextField pistesField = new JTextField();

            Object[] message = {
                "Titre de l'album :", titreField,
                "Artiste principal :", artisteField,
                "Genre musical :", genreField,
                "Durée (minutes) :", dureeField,
                "Nombre de pistes :", pistesField
            };

            // 2. Affichage
            int option = JOptionPane.showConfirmDialog(this, message, "Nouveau CD", JOptionPane.OK_CANCEL_OPTION);

            // 3. Validation et Création
            if (option == JOptionPane.OK_OPTION) {
                try {
                    if (titreField.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Le titre est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    // Génération ID unique
                    String id = "CD-" + System.currentTimeMillis();
                    
                    // Conversion des chiffres (Attention aux erreurs de frappe)
                    int duree = Integer.parseInt(dureeField.getText());
                    int nbPistes = Integer.parseInt(pistesField.getText());

                    // 4. Instanciation de l'objet CD (Correspond exactement à votre classe)
                    CD nouveauCD = new CD(
                        id,
                        titreField.getText(),
                        artisteField.getText(),
                        genreField.getText(),
                        duree,
                        nbPistes
                    );

                    // 5. Sauvegarde via le Manager
                    manager.ajouterDocument(nouveauCD);

                    // 6. Feedback utilisateur
                    JOptionPane.showMessageDialog(this, "CD ajouté avec succès !");
                    rafraichirDocuments("");

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "La durée et le nombre de pistes doivent être des nombres entiers.", "Erreur de format", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
                }
            }
        });

        // --- ACTION : AJOUTER LIVRE (Corrigé avec Éditeur) ---
        btnAjouterLivre.addActionListener(e -> {
            // 1. Création des champs de saisie
            JTextField titreField = new JTextField();
            JTextField auteurField = new JTextField();
            JTextField genreField = new JTextField();
            JTextField isbnField = new JTextField();
            JTextField pagesField = new JTextField();
            JTextField editeurField = new JTextField(); // <--- NOUVEAU CHAMP

            Object[] message = {
                "Titre :", titreField,
                "Auteur :", auteurField,
                "Genre :", genreField,
                "ISBN :", isbnField,
                "Nombre de pages :", pagesField,
                "Éditeur :", editeurField // <--- AJOUT DANS LE POPUP
            };

            // 2. Affichage du popup
            int option = JOptionPane.showConfirmDialog(this, message, "Nouveau Livre", JOptionPane.OK_CANCEL_OPTION);

            // 3. Traitement
            if (option == JOptionPane.OK_OPTION) {
                try {
                    // Validation simple
                    if (titreField.getText().isEmpty()) {
                        JOptionPane.showMessageDialog(this, "Le titre est obligatoire.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        return;
                    }

                    String id = "LIV-" + System.currentTimeMillis();
                    int nbPages = Integer.parseInt(pagesField.getText());

                    // 4. Création de l'objet Livre avec les 7 ARGUMENTS
                    Livre nouveauLivre = new Livre(
                        id, 
                        titreField.getText(), 
                        auteurField.getText(), 
                        genreField.getText(), 
                        isbnField.getText(), 
                        nbPages,
                        editeurField.getText() // <--- ON PASSE L'ÉDITEUR ICI
                    );

                    // 5. Envoi au Manager
                    manager.ajouterDocument(nouveauLivre);

                    JOptionPane.showMessageDialog(this, "Livre ajouté avec succès !");
                    rafraichirDocuments(""); 

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Le nombre de pages doit être un entier valid.", "Erreur", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
                }
            }
        });
        
        // Chargement initial
        rafraichirDocuments("");

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // --- ONGLET 2 : ADHÉRENTS ---
    private JPanel createPanelAdherents() {
        JPanel panel = new JPanel(new BorderLayout());

        // Barre d'outils
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAjouter = new JButton("Nouvel Adhérent");
        JButton btnRafraichir = new JButton("Actualiser");
        
        toolbar.add(btnAjouter);
        toolbar.add(btnRafraichir);

        // Tableau
        String[] colonnes = {"ID", "Nom", "Prénom", "Coordonnées", "Statut", "Amende"};
        modeleAdherents = new DefaultTableModel(colonnes, 0);
        tableAdherents = new JTable(modeleAdherents);
        
        // Actions
        btnRafraichir.addActionListener(e -> rafraichirAdherents());
        
        // --- ACTION : AJOUTER ADHÉRENT ---
        btnAjouter.addActionListener(e -> {
            // 1. Create a simple form using JTextFields
            JTextField nomField = new JTextField();
            JTextField prenomField = new JTextField();
            JTextField emailField = new JTextField();

            Object[] message = {
                "Nom :", nomField,
                "Prénom :", prenomField,
                "Email / Tel :", emailField
            };

            // 2. Show the popup (Dialog)
            int option = JOptionPane.showConfirmDialog(this, message, "Nouvel Adhérent", JOptionPane.OK_CANCEL_OPTION);

            // 3. If User clicks OK
            if (option == JOptionPane.OK_OPTION) {
                try {
                    // Generate a unique ID (Simple method for student project)
                    String id = "ADH-" + System.currentTimeMillis(); 
                    String nom = nomField.getText();
                    String prenom = prenomField.getText();
                    String coords = emailField.getText();

                    if (!nom.isEmpty() && !prenom.isEmpty()) {
                        // Create the object
                        // Note: Assumes your Adherent constructor is (id, nom, prenom, coords, statut, penalite)
                        // If your constructor is different, adjust parameters below.
                        Adherent nouveau = new Adherent(id, nom, prenom, coords);
                        
                        // 4. Send to Manager -> DAO -> DB
                        manager.ajouterAdherent(nouveau);
                        
                        // 5. Update the UI
                        JOptionPane.showMessageDialog(this, "Adhérent ajouté avec succès !");
                        rafraichirAdherents(); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Le nom et le prénom sont obligatoires.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Erreur lors de l'enregistrement : " + ex.getMessage());
                }
            }
        });
        
        rafraichirAdherents();
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableAdherents), BorderLayout.CENTER);
        return panel;
    }

    // --- ONGLET 3 : EMPRUNTS ---
    private JPanel createPanelEmprunts() {
        JPanel panel = new JPanel(new BorderLayout());

        // Barre d'outils
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnEmprunter = new JButton("Enregistrer Emprunt");
        JButton btnRetourner = new JButton("Enregistrer Retour");
        
        toolbar.add(btnEmprunter);
        toolbar.add(btnRetourner);

        // Tableau
        String[] colonnes = {"ID Emprunt", "Livre", "Adhérent", "Date Prêt", "Retour Prévu", "Retard ?"};
        modeleEmprunts = new DefaultTableModel(colonnes, 0);
        tableEmprunts = new JTable(modeleEmprunts);

        // --- ACTION : EMPRUNTER ---
        btnEmprunter.addActionListener(e -> {
            // Boîte de dialogue simple pour saisir les IDs
            JTextField idAdhField = new JTextField();
            JTextField idDocField = new JTextField();
            Object[] message = {"ID Adhérent:", idAdhField, "ID Document:", idDocField};

            int option = JOptionPane.showConfirmDialog(this, message, "Nouvel Emprunt", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
            	try {
                    // CORRECTION ICI : On cherche par ID, pas par titre !
                    Adherent adh = manager.rechercherAdherent(idAdhField.getText());
                    Document doc = manager.recupererDocumentParId(idDocField.getText());

                    if (adh != null && doc != null) {
                        boolean succes = manager.emprunter(adh, doc);
                        if (succes) {
                            JOptionPane.showMessageDialog(this, "Emprunt validé !");
                            rafraichirTout();
                        } else {
                            JOptionPane.showMessageDialog(this, "Emprunt refusé (Document déjà pris ou Quota atteint).", "Erreur", JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        JOptionPane.showMessageDialog(this, "ID Adhérent ou ID Document introuvable.");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        // --- ACTION : RETOURNER ---
        btnRetourner.addActionListener(e -> {
            int row = tableEmprunts.getSelectedRow();
            if (row >= 0) {
                // On récupère l'objet Emprunt caché dans la ligne (astuce : il faut le retrouver via le manager idéalement)
                // Ici pour simplifier, on va recharger les emprunts du manager
                List<Emprunt> encours = manager.listerEmpruntsEnCours();
                if (row < encours.size()) {
                    Emprunt emp = encours.get(row);
                    manager.rendre(emp);
                    JOptionPane.showMessageDialog(this, "Retour enregistré.");
                    rafraichirTout();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un emprunt dans la liste.");
            }
        });

        rafraichirEmprunts();
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableEmprunts), BorderLayout.CENTER);
        return panel;
    }

    // --- MÉTHODES DE RAFRAÎCHISSEMENT ---

    private void rafraichirTout() {
        rafraichirDocuments("");
        rafraichirAdherents();
        rafraichirEmprunts();
    }

    private void rafraichirDocuments(String critere) {
        modeleDocuments.setRowCount(0); // Vider la table
        for (Document d : manager.rechercherDocuments(critere)) {
            String type = (d instanceof Livre) ? "Livre" : (d instanceof CD) ? "CD" : "Autre";
            String details = "";
            if(d instanceof Livre) details = ((Livre)d).getNombrePages() + " p.";
            if(d instanceof CD) details = ((CD)d).getDureeMinutes() + " min.";
            
            modeleDocuments.addRow(new Object[]{
                d.getId(), type, d.getTitre(), d.getAuteur(), d.getGenre(), details,
                d.estEmprunte() ? "Non Dispo" : "Dispo"
            });
        }
    }

    private void rafraichirAdherents() {
        try {
            // 1. On vide la table
            modeleAdherents.setRowCount(0);
            
            // 2. On demande la liste au Manager (qui demande au DAO, qui demande à SQLite)
            List<Adherent> liste = manager.listerAdherents(); 
            
            // 3. On remplit la table ligne par ligne
            for (Adherent a : liste) {
                modeleAdherents.addRow(new Object[]{
                    a.getIdAdherent(),
                    a.getNom(),
                    a.getPrenom(),
                    a.getCoordonnees(),
                    a.getStatut(),
                    a.getMontantPenalite() + " €"
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void rafraichirEmprunts() {
        modeleEmprunts.setRowCount(0);
        for (Emprunt e : manager.listerEmpruntsEnCours()) {
            boolean enRetard = LocalDate.now().isAfter(e.getDateRetourPrevue());
            modeleEmprunts.addRow(new Object[]{
                e.getIdEmprunt(),
                e.getDocumentEmprunte().getTitre(),
                e.getEmprunteur().getNom(),
                e.getDateEmprunt(),
                e.getDateRetourPrevue(),
                enRetard ? "OUI" : "Non"
            });
        }
    }
}