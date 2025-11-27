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
 * Utilise Swing pour l'interface graphique.
 * Communique uniquement avec le BibliothequeManager (Contrôleur/Service).
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
        setTitle("SGEB - Système de Gestion de Bibliothèque");
        setSize(1200, 750); // Taille ajustée pour bien voir les colonnes
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centrer à l'écran

        // Création des onglets
        onglets = new JTabbedPane();
        
        onglets.addTab("Gestion Documents", createPanelDocuments());
        onglets.addTab("Gestion Adhérents", createPanelAdherents());
        onglets.addTab("Emprunts & Retours", createPanelEmprunts());

        this.add(onglets);
    }

    // =================================================================================
    // ONGLET 1 : GESTION DES DOCUMENTS
    // =================================================================================
    private JPanel createPanelDocuments() {
        JPanel panel = new JPanel(new BorderLayout());

        // --- Barre d'outils (Haut) ---
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JTextField champRecherche = new JTextField(20);
        JButton btnRechercher = new JButton("Rechercher");
        
        // Séparateur visuel
        JSeparator separator = new JSeparator(SwingConstants.VERTICAL);
        
        JButton btnAjouterLivre = new JButton("Nouveau Livre");
        JButton btnAjouterCD = new JButton("Nouveau CD");
        JButton btnSupprimer = new JButton("Supprimer"); // Bouton demandé par l'énoncé

        toolbar.add(new JLabel("Recherche (Titre, Auteur, ISBN, Genre) :"));
        toolbar.add(champRecherche);
        toolbar.add(btnRechercher);
        toolbar.add(Box.createHorizontalStrut(15)); // Espace
        toolbar.add(btnAjouterLivre);
        toolbar.add(btnAjouterCD);
        toolbar.add(btnSupprimer);

        // --- Tableau (Centre) ---
        // La colonne "Détails" affichera les infos spécifiques (ISBN pour livre, Durée pour CD)
        String[] colonnes = {"ID", "Type", "Titre", "Auteur/Artiste", "Genre", "Détails (ISBN, Pages...)", "Statut"};
        modeleDocuments = new DefaultTableModel(colonnes, 0) {
            @Override // Rend les cellules non éditables directement
            public boolean isCellEditable(int row, int column) { return false; }
        };
        tableDocuments = new JTable(modeleDocuments);
        JScrollPane scrollPane = new JScrollPane(tableDocuments);

        // --- ACTIONS ---

        // 1. Recherche
        btnRechercher.addActionListener(e -> rafraichirDocuments(champRecherche.getText()));

        // 2. Supprimer un document
        btnSupprimer.addActionListener(e -> {
            int row = tableDocuments.getSelectedRow();
            if (row >= 0) {
                String idDoc = (String) modeleDocuments.getValueAt(row, 0);
                String titre = (String) modeleDocuments.getValueAt(row, 2);
                
                int reponse = JOptionPane.showConfirmDialog(this, 
                    "Voulez-vous vraiment supprimer le document '" + titre + "' ?", 
                    "Confirmation de suppression", JOptionPane.YES_NO_OPTION);
                
                if (reponse == JOptionPane.YES_OPTION) {
                    try {
                        // Vérification métier avant suppression
                        Document doc = manager.recupererDocumentParId(idDoc);
                        if (doc != null && doc.estEmprunte()) {
                            JOptionPane.showMessageDialog(this, "Impossible de supprimer : ce document est actuellement emprunté.", "Erreur", JOptionPane.ERROR_MESSAGE);
                        } else {
                            manager.supprimerDocument(idDoc);
                            JOptionPane.showMessageDialog(this, "Document supprimé avec succès.");
                            rafraichirDocuments("");
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Erreur lors de la suppression : " + ex.getMessage());
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner une ligne à supprimer.");
            }
        });

        // 3. Ajouter un Livre (Formulaire complet)
        btnAjouterLivre.addActionListener(e -> {
            JTextField titreField = new JTextField();
            JTextField auteurField = new JTextField();
            JTextField genreField = new JTextField();
            JTextField isbnField = new JTextField();
            JTextField pagesField = new JTextField();
            JTextField editeurField = new JTextField();

            Object[] message = {
                "Titre :", titreField,
                "Auteur :", auteurField,
                "Genre :", genreField,
                "ISBN :", isbnField,
                "Nombre de pages :", pagesField,
                "Éditeur :", editeurField
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Nouveau Livre", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    if (titreField.getText().isEmpty()) throw new IllegalArgumentException("Le titre est obligatoire.");
                    
                    String id = "LIV-" + System.currentTimeMillis();
                    int nbPages = Integer.parseInt(pagesField.getText()); // Peut lever NumberFormatException

                    Livre nouveauLivre = new Livre(id, titreField.getText(), auteurField.getText(), genreField.getText(), 
                                                   isbnField.getText(), nbPages, editeurField.getText());
                    
                    manager.ajouterDocument(nouveauLivre);
                    JOptionPane.showMessageDialog(this, "Livre ajouté !");
                    rafraichirDocuments("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Le nombre de pages doit être un entier.", "Erreur de format", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // 4. Ajouter un CD
        btnAjouterCD.addActionListener(e -> {
            JTextField titreField = new JTextField();
            JTextField artisteField = new JTextField();
            JTextField genreField = new JTextField();
            JTextField dureeField = new JTextField();
            JTextField pistesField = new JTextField();

            Object[] message = {
                "Titre album :", titreField,
                "Artiste principal :", artisteField,
                "Genre musical :", genreField,
                "Durée (minutes) :", dureeField,
                "Nombre de pistes :", pistesField
            };

            int option = JOptionPane.showConfirmDialog(this, message, "Nouveau CD", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    if (titreField.getText().isEmpty()) throw new IllegalArgumentException("Le titre est obligatoire.");
                    
                    String id = "CD-" + System.currentTimeMillis();
                    int duree = Integer.parseInt(dureeField.getText());
                    int pistes = Integer.parseInt(pistesField.getText());

                    CD nouveauCD = new CD(id, titreField.getText(), artisteField.getText(), genreField.getText(), duree, pistes);
                    
                    manager.ajouterDocument(nouveauCD);
                    JOptionPane.showMessageDialog(this, "CD ajouté !");
                    rafraichirDocuments("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "La durée et le nombre de pistes doivent être des entiers.", "Erreur de format", JOptionPane.ERROR_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Chargement initial des données
        rafraichirDocuments("");

        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    // =================================================================================
    // ONGLET 2 : GESTION DES ADHÉRENTS
    // =================================================================================
    private JPanel createPanelAdherents() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAjouter = new JButton("Nouvel Adhérent");
        JButton btnRafraichir = new JButton("Actualiser");
        
        toolbar.add(btnAjouter);
        toolbar.add(btnRafraichir);

        String[] colonnes = {"ID", "Nom", "Prénom", "Coordonnées", "Statut", "Amende"};
        modeleAdherents = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableAdherents = new JTable(modeleAdherents);
        
        btnRafraichir.addActionListener(e -> rafraichirAdherents());
        
        // Action : Ajouter Adhérent
        btnAjouter.addActionListener(e -> {
            JTextField nomField = new JTextField();
            JTextField prenomField = new JTextField();
            JTextField coordField = new JTextField();

            Object[] message = { "Nom :", nomField, "Prénom :", prenomField, "Email / Tel :", coordField };

            int option = JOptionPane.showConfirmDialog(this, message, "Nouvel Adhérent", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                try {
                    if (nomField.getText().isEmpty() || prenomField.getText().isEmpty()) 
                        throw new IllegalArgumentException("Nom et Prénom obligatoires.");
                    
                    String id = "ADH-" + System.currentTimeMillis();
                    Adherent adh = new Adherent(id, nomField.getText(), prenomField.getText(), coordField.getText());
                    
                    manager.ajouterAdherent(adh);
                    JOptionPane.showMessageDialog(this, "Adhérent ajouté avec succès !");
                    rafraichirAdherents();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Erreur : " + ex.getMessage());
                }
            }
        });
        
        rafraichirAdherents();
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableAdherents), BorderLayout.CENTER);
        return panel;
    }

    // =================================================================================
    // ONGLET 3 : EMPRUNTS & RETOURS
    // =================================================================================
    private JPanel createPanelEmprunts() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnEmprunter = new JButton("Enregistrer Emprunt");
        JButton btnRetourner = new JButton("Enregistrer Retour");
        
        toolbar.add(btnEmprunter);
        toolbar.add(btnRetourner);

        String[] colonnes = {"ID Emprunt", "Document", "Adhérent", "Date Prêt", "Retour Prévu", "Retard ?"};
        modeleEmprunts = new DefaultTableModel(colonnes, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        tableEmprunts = new JTable(modeleEmprunts);

        // Action : Emprunter (Par ID)
        // --- ACTION : EMPRUNTER AVEC RECHERCHE ET FILTRE ---
        btnEmprunter.addActionListener(e -> {
            try {
                // 1. Préparation des listes (String) pour la recherche
                List<Adherent> tousAdherents = manager.listerAdherents();
                List<Document> tousDocuments = manager.rechercherDocuments("");

                // A. Préparer la liste des Adhérents (Texte "ID - Nom")
                List<String> textesAdherents = new java.util.ArrayList<>();
                for (Adherent a : tousAdherents) {
                    // On peut ajouter une alerte visuelle si pénalité
                    String penalite = (a.getMontantPenalite() > 0) ? " [AMENDE !]" : "";
                    textesAdherents.add(a.getIdAdherent() + " - " + a.getNom() + " " + a.getPrenom() + penalite);
                }

                // B. Préparer la liste des Documents (UNIQUEMENT LES DISPONIBLES)
                List<String> textesDocuments = new java.util.ArrayList<>();
                for (Document d : tousDocuments) {
                    // --- CORRECTION DEMANDÉE : On ne montre que les dispo ---
                    if (!d.estEmprunte()) {
                        textesDocuments.add(d.getId() + " - " + d.getTitre());
                    }
                }

                if (textesAdherents.isEmpty() || textesDocuments.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Aucun adhérent ou aucun document disponible !");
                    return;
                }

                // 2. Création des composants graphiques
                JComboBox<String> comboAdh = new JComboBox<>();
                JComboBox<String> comboDoc = new JComboBox<>();

                // On utilise notre méthode "Helper" pour créer les panneaux avec recherche
                JPanel panelAdh = creerPanelRecherche(textesAdherents, comboAdh);
                JPanel panelDoc = creerPanelRecherche(textesDocuments, comboDoc);

                // 3. Construction du message global
                Object[] message = {
                    "Sélectionner l'Adhérent :", panelAdh,
                    "Sélectionner le Document :", panelDoc
                };

                // 4. Affichage
                int option = JOptionPane.showConfirmDialog(this, message, "Nouvel Emprunt", JOptionPane.OK_CANCEL_OPTION);

                // 5. Validation
                if (option == JOptionPane.OK_OPTION) {
                    String selAdh = (String) comboAdh.getSelectedItem();
                    String selDoc = (String) comboDoc.getSelectedItem();

                    if (selAdh != null && selDoc != null) {
                        // Extraction des ID (tout ce qui est avant le " - ")
                        String idAdh = selAdh.split(" - ")[0];
                        String idDoc = selDoc.split(" - ")[0];

                        Adherent adh = manager.rechercherAdherent(idAdh);
                        Document doc = manager.recupererDocumentParId(idDoc);

                        // Tentative d'emprunt
                        if (manager.emprunter(adh, doc)) {
                            JOptionPane.showMessageDialog(this, "Emprunt validé !");
                            rafraichirTout();
                        } else {
                            JOptionPane.showMessageDialog(this, "Erreur lors de l'emprunt (Quota ou Pénalité).");
                        }
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });


        // Action : Retourner
        btnRetourner.addActionListener(e -> {
            int row = tableEmprunts.getSelectedRow();
            if (row >= 0) {
                List<Emprunt> encours = manager.listerEmpruntsEnCours();
                // Sécurité : s'assurer que l'index correspond
                if (row < encours.size()) {
                    Emprunt emp = encours.get(row);
                    manager.rendre(emp);
                    JOptionPane.showMessageDialog(this, "Retour enregistré.");
                    rafraichirTout();
                }
            } else {
                JOptionPane.showMessageDialog(this, "Veuillez sélectionner un emprunt dans la liste pour le rendre.");
            }
        });

        rafraichirEmprunts();
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableEmprunts), BorderLayout.CENTER);
        return panel;
    }

    // =================================================================================
    // MÉTHODES DE RAFRAÎCHISSEMENT (Mise à jour des tableaux)
    // =================================================================================

    private void rafraichirTout() {
        rafraichirDocuments("");
        rafraichirAdherents();
        rafraichirEmprunts();
    }

    private void rafraichirDocuments(String critere) {
        modeleDocuments.setRowCount(0); // Vider le tableau
        List<Document> resultats = manager.rechercherDocuments(critere);
        
        for (Document d : resultats) {
            String type = "Autre";
            String details = "";

            // Utilisation du polymorphisme et de instanceof pour formater l'affichage
            if (d instanceof Livre) {
                type = "Livre";
                Livre l = (Livre) d;
                // Affichage de l'ISBN demandé
                details = "ISBN: " + l.getIsbn() + " | " + l.getNombrePages() + " p. | Ed: " + l.getEditeur();
            } else if (d instanceof CD) {
                type = "CD";
                CD c = (CD) d;
                details = c.getDureeMinutes() + " min | " + c.getNombrePistes() + " pistes";
            } else if (d instanceof Magazine) {
                type = "Magazine";
                Magazine m = (Magazine) d;
                details = "N°" + m.getNumero() + " (" + m.getPeriodicite() + ")";
            }
            
            modeleDocuments.addRow(new Object[]{
                d.getId(), 
                type, 
                d.getTitre(), 
                d.getAuteur(), 
                d.getGenre(), 
                details,
                d.estEmprunte() ? "Non Dispo" : "Dispo"
            });
        }
    }

    private void rafraichirAdherents() {
        try {
            modeleAdherents.setRowCount(0);
            List<Adherent> liste = manager.listerAdherents();
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
        List<Emprunt> liste = manager.listerEmpruntsEnCours();
        for (Emprunt e : liste) {
            boolean enRetard = LocalDate.now().isAfter(e.getDateRetourPrevue());
            modeleEmprunts.addRow(new Object[]{
                e.getIdEmprunt(), 
                e.getDocumentEmprunte().getTitre(), 
                e.getEmprunteur().getNom(),
                e.getDateEmprunt(), 
                e.getDateRetourPrevue(), 
                enRetard ? "RETARD !" : "Non"
            });
        }
    }

    /**
     * Crée un panneau contenant une barre de recherche et un menu déroulant filtrable.
     * @param elements La liste complète des textes à afficher (Ex: "ID - Titre")
     * @param comboBox Le JComboBox vide qui sera rempli/manipulé
     * @return Le JPanel à afficher dans le popup
     */
    private JPanel creerPanelRecherche(List<String> elements, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JTextField champRecherche = new JTextField(15);
        
        // Remplissage initial
        for (String s : elements) comboBox.addItem(s);

        // Ajout d'un écouteur sur le clavier : dès qu'on tape, on filtre !
        champRecherche.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String texte = champRecherche.getText().toLowerCase();
                comboBox.removeAllItems(); // On vide tout
                
                for (String s : elements) {
                    // Si le texte correspond à la recherche, on l'ajoute
                    if (s.toLowerCase().contains(texte)) {
                        comboBox.addItem(s);
                    }
                }
                // Si la liste n'est pas vide, on ouvre le menu pour montrer les résultats
                if (comboBox.getItemCount() > 0) {
                    comboBox.showPopup();
                }
            }
        });

        JPanel haut = new JPanel(new BorderLayout());
        haut.add(new JLabel("Filtrer (Nom/Titre) :"), BorderLayout.NORTH);
        haut.add(champRecherche, BorderLayout.CENTER);

        panel.add(haut, BorderLayout.NORTH);
        panel.add(comboBox, BorderLayout.CENTER);
        
        return panel;
    }
}