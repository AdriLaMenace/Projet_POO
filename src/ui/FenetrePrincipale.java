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
        
        // TODO: Tu pourras coder le bouton Ajouter Adhérent ici (JDialog)
        
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