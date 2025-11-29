package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import entites.Adherent;
import entites.CD;
import entites.Document;
import entites.E_StatutAdherent;
import entites.Emprunt;
import entites.Livre;
import entites.Magazine;
import service.BibliothequeManager;

public class FenetrePrincipale extends JFrame {

    private BibliothequeManager manager;
    private JTabbedPane onglets;
    
    // Tableaux et Modèles
    private JTable tableDocuments, tableAdherents, tableEmprunts;
    private DefaultTableModel modeleDocuments, modeleAdherents, modeleEmprunts;
    
    // Filtres
    private JComboBox<String> comboTypeDoc;
    private JComboBox<String> comboStatutAdh;
    private JComboBox<String> comboEtatEmp;
    
    // Format des dates
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public FenetrePrincipale(BibliothequeManager manager) {
        this.manager = manager;

        setTitle("SGEB - Gestion de Bibliothèque");
        setSize(1300, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        onglets = new JTabbedPane();
        onglets.addTab("  📚 Documents  ", createPanelDocuments());
        onglets.addTab("  👤 Adhérents  ", createPanelAdherents());
        onglets.addTab("  🔄 Emprunts  ", createPanelEmprunts());

        this.add(onglets);
    }

    // =================================================================================
    // ONGLET 1 : DOCUMENTS
    // =================================================================================
    private JPanel createPanelDocuments() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JTextField champRecherche = new JTextField(12);
        comboTypeDoc = new JComboBox<>(new String[]{"TOUT", "LIVRE", "CD", "MAGAZINE"});
        

        JButton btnRech = new JButton("🔍 Rechercher");
        JButton btnAddLivre = new JButton("＋Livre");
        JButton btnAddCD = new JButton("＋CD");
        JButton btnAddMag = new JButton("＋Mag");
        JButton btnModif = new JButton("✎ Modifier");
        JButton btnSuppr = new JButton("🗑 Suppr.");

        toolbar.add(new JLabel("Rech:")); toolbar.add(champRecherche); toolbar.add(comboTypeDoc); toolbar.add(btnRech);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(btnAddLivre); toolbar.add(btnAddCD); toolbar.add(btnAddMag);
        toolbar.add(btnModif); toolbar.add(btnSuppr); 

        String[] cols = {"ID", "Type", "Titre", "Auteur/Artiste", "Genre", "ISBN", "Détails (Pages/Durée/Num)", "Statut"};
        modeleDocuments = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        tableDocuments = new JTable(modeleDocuments);
        styliserTable(tableDocuments);

        // Actions
        btnRech.addActionListener(e -> rafraichirDocuments(champRecherche.getText(), (String) comboTypeDoc.getSelectedItem()));
        
        btnSuppr.addActionListener(e -> {
            int row = tableDocuments.getSelectedRow();
            if (row >= 0) {
                String id = (String) modeleDocuments.getValueAt(row, 0);
                if (JOptionPane.showConfirmDialog(this, "Supprimer ?", "Confirm", JOptionPane.YES_NO_OPTION) == 0) {
                    try {
                        Document d = manager.recupererDocumentParId(id);
                        if (d.estEmprunte()) JOptionPane.showMessageDialog(this, "Impossible : document emprunté !");
                        else { manager.supprimerDocument(id); rafraichirTout(); }
                    } catch (Exception ex) { ex.printStackTrace(); }
                }
            } else JOptionPane.showMessageDialog(this, "Sélectionnez une ligne.");
        });

        
        // ACTION MODIFIER (LE CODE EST LÀ MAINTENANT)
        btnModif.addActionListener(e -> {
            int row = tableDocuments.getSelectedRow();
            if (row >= 0) {
                try {
                    String id = (String) modeleDocuments.getValueAt(row, 0);
                    Document d = manager.recupererDocumentParId(id);
                    if (d != null) modifierDocument(d);
                } catch (Exception ex) { ex.printStackTrace(); }
            } else JOptionPane.showMessageDialog(this, "Sélectionnez un document.");
        });

        btnAddLivre.addActionListener(e -> ajouterLivre());
        btnAddCD.addActionListener(e -> ajouterCD());
        btnAddMag.addActionListener(e -> ajouterMagazine());

        
        rafraichirDocuments("", "TOUT");
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableDocuments), BorderLayout.CENTER);
        return panel;

    }

    // =================================================================================
    // ONGLET 2 : ADHÉRENTS
    // =================================================================================
    private JPanel createPanelAdherents() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField champRech = new JTextField(12);
        
        comboStatutAdh = new JComboBox<>(new String[]{"TOUT", "ACTIF", "AVEC_PENALITE"});
        
        JButton btnRech = new JButton("Rechercher");
        JButton btnAdd = new JButton("＋ Adhérent");
        JButton btnModif = new JButton("✎ Modifier");
        JButton btnHist = new JButton("📜 Historique");
        JButton btnSuppr = new JButton("Supprimer");
        JButton btnRefresh = new JButton("Actualiser");
        JButton btnPayer = new JButton("💰 Régler Dettes");

        toolbar.add(new JLabel("Nom/ID:")); toolbar.add(champRech); 
        toolbar.add(new JLabel("Filtre:")); toolbar.add(comboStatutAdh);
        toolbar.add(btnRech);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(btnAdd); toolbar.add(btnModif); toolbar.add(btnHist); toolbar.add(btnSuppr); toolbar.add(btnRefresh); toolbar.add(btnPayer);

        String[] cols = {"ID", "Nom", "Prénom", "Coordonnées", "Statut", "Amende"};
        modeleAdherents = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        tableAdherents = new JTable(modeleAdherents);
        styliserTable(tableAdherents);

        Runnable actionRefresh = () -> rafraichirAdherents(champRech.getText(), (String) comboStatutAdh.getSelectedItem());
        
        btnRech.addActionListener(e -> actionRefresh.run());
        btnRefresh.addActionListener(e -> { champRech.setText(""); comboStatutAdh.setSelectedIndex(0); actionRefresh.run(); });
        btnAdd.addActionListener(e -> ajouterAdherent());
        
        // ACTION MODIFIER ADHERENT (LE CODE EST LÀ)
        btnModif.addActionListener(e -> {
            int row = tableAdherents.getSelectedRow();
            if (row >= 0) {
                String id = (String) modeleAdherents.getValueAt(row, 0);
                Adherent a = manager.rechercherAdherent(id);
                if (a != null) modifierAdherent(a);
            } else JOptionPane.showMessageDialog(this, "Sélectionnez un adhérent.");
        });

        btnHist.addActionListener(e -> {
            int row = tableAdherents.getSelectedRow();
            if (row >= 0) {
                String id = (String) modeleAdherents.getValueAt(row, 0);
                Adherent a = manager.rechercherAdherent(id);
                afficherHistorique(a);
            } else JOptionPane.showMessageDialog(this, "Sélectionnez un adhérent.");
        });

        // Action Supprimer
        btnSuppr.addActionListener(e -> {
            int row = tableAdherents.getSelectedRow();
            if (row >= 0) {
                String id = (String) modeleAdherents.getValueAt(row, 0);
                String nom = (String) modeleAdherents.getValueAt(row, 1);
                
                int rep = JOptionPane.showConfirmDialog(this, 
                    "Supprimer l'adhérent " + nom + " ?", 
                    "Confirmation", JOptionPane.YES_NO_OPTION);
                
                if (rep == JOptionPane.YES_OPTION) {
                    try {
                        manager.supprimerAdherent(id);
                        JOptionPane.showMessageDialog(this, "Adhérent supprimé.");
                        // Rafraîchir la liste (utilise ta méthode de rafraichissement habituelle)
                        // Si tu as 'actionRefresh', utilise : actionRefresh.run();
                        // Sinon : 
                        rafraichirAdherents("", "TOUT"); 
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(this, "Erreur : Impossible de supprimer (vérifiez s'il a des emprunts)");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez une ligne.");
            }
        });

        btnPayer.addActionListener(e -> {
            int row = tableAdherents.getSelectedRow();
            if (row >= 0) {
                String id = (String) modeleAdherents.getValueAt(row, 0);
                Adherent a = manager.rechercherAdherent(id);
            
                if (a.getMontantPenalite() > 0) {
                    int rep = JOptionPane.showConfirmDialog(this, 
                        "L'adhérent doit " + a.getMontantPenalite() + " €.\nA-t-il réglé cette somme ?", 
                        "Règlement", JOptionPane.YES_NO_OPTION);
                
                    if (rep == JOptionPane.YES_OPTION) {
                        try {
                            manager.reglerPenalite(a); // Appel au manager
                            JOptionPane.showMessageDialog(this, "Dette réglée. Compte réactivé !");
                            actionRefresh.run(); // Rafraîchir le tableau
                        } catch (Exception ex) { ex.printStackTrace(); }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Cet adhérent n'a aucune dette.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Sélectionnez un adhérent.");
            }
        });

        actionRefresh.run();
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableAdherents), BorderLayout.CENTER);
        return panel;
    }

    // =================================================================================
    // ONGLET 3 : EMPRUNTS
    // =================================================================================
    private JPanel createPanelEmprunts() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        JTextField champRechercheEmp = new JTextField(15);
        JButton btnRechEmp = new JButton("Rechercher");
        
        comboEtatEmp = new JComboBox<>(new String[]{"EN COURS", "RETARD", "TOUT"});
        JButton btnFiltrer = new JButton("Filtrer");
        
        JButton btnEmp = new JButton("📤 Emprunter");
        JButton btnRet = new JButton("📥 Retourner");

        toolbar.add(new JLabel("Rech:")); toolbar.add(champRechercheEmp); toolbar.add(btnRechEmp);
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(new JLabel("État:")); toolbar.add(comboEtatEmp); toolbar.add(btnFiltrer);
        toolbar.add(Box.createHorizontalStrut(20));
        toolbar.add(btnEmp); toolbar.add(btnRet);

        String[] cols = {"ID", "Document", "Adhérent", "Date Prêt", "Retour Prévu", "Retour Réel", "État"};
        modeleEmprunts = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        tableEmprunts = new JTable(modeleEmprunts);
        styliserTable(tableEmprunts);

        btnFiltrer.addActionListener(e -> rafraichirEmprunts((String) comboEtatEmp.getSelectedItem(), champRechercheEmp.getText()));
        btnRechEmp.addActionListener(e -> rafraichirEmprunts((String) comboEtatEmp.getSelectedItem(), champRechercheEmp.getText()));

        // ACTION EMPRUNTER (CORRIGÉE ET COMPLÈTE)
        btnEmp.addActionListener(e -> {
            Vector<String> ads = new Vector<>(), docs = new Vector<>();
            try {
                for(Adherent a : manager.listerAdherents()) 
                    if(a.getStatut() == E_StatutAdherent.ACTIF) ads.add(a.getIdAdherent()+" - "+a.getNom());
                
                for(Document d : manager.rechercherDocuments("","TOUT")) 
                    if(!d.estEmprunte()) docs.add(d.getId()+" - "+d.getTitre());
            } catch(Exception x){}
            
            // Sécurité si listes vides
            if(ads.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun adhérent actif trouvé."); return; }
            if(docs.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun document disponible."); return; }
            
            JComboBox<String> ca = new JComboBox<>(ads);
            JComboBox<String> cd = new JComboBox<>(docs);
            
            if(JOptionPane.showConfirmDialog(this, new Object[]{"Adhérent:", ca, "Document:", cd}, "Emprunter", JOptionPane.OK_CANCEL_OPTION)==0) {
                try {
                    // Le split est sûr ici car on a construit les chaînes nous-mêmes juste au-dessus
                    String idA = ((String)ca.getSelectedItem()).split(" - ")[0];
                    String idD = ((String)cd.getSelectedItem()).split(" - ")[0];
                    
                    if(manager.emprunter(manager.rechercherAdherent(idA), manager.recupererDocumentParId(idD))) {
                        JOptionPane.showMessageDialog(this, "Emprunt validé !"); 
                        rafraichirTout();
                    } else {
                        JOptionPane.showMessageDialog(this, "Echec : Quota atteint ou Document indisponible.", "Erreur", JOptionPane.WARNING_MESSAGE);
                    }
                } catch(Exception x) { x.printStackTrace(); }
            }
        });

        btnRet.addActionListener(e -> {
            int r = tableEmprunts.getSelectedRow();
            if(r>=0) {
                String idEmp = (String) modeleEmprunts.getValueAt(r, 0);
                List<Emprunt> all = manager.listerEmpruntsEnCours();
                Emprunt cible = all.stream().filter(emp -> emp.getIdEmprunt().equals(idEmp)).findFirst().orElse(null);
                
                if (cible == null) JOptionPane.showMessageDialog(this, "Cet emprunt est déjà terminé ou introuvable.");
                else {
                    manager.rendre(cible);
                    JOptionPane.showMessageDialog(this, "Retour enregistré.");
                    rafraichirTout();
                }
            } else JOptionPane.showMessageDialog(this, "Sélectionnez un emprunt en cours.");
        });

        rafraichirEmprunts("EN COURS", "");
        panel.add(toolbar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tableEmprunts), BorderLayout.CENTER);
        return panel;
    }

    // =========================================================
    // LOGIQUE FORMULAIRES (AJOUT & MODIF)
    // =========================================================

    private void ajouterLivre() {
        JTextField t1=new JTextField(), t2=new JTextField(), t3=new JTextField(), t4=new JTextField(), t5=new JTextField(), t6=new JTextField();
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Titre:",t1,"Auteur:",t2,"Genre:",t3,"ISBN:",t4,"Pages:",t5,"Editeur:",t6}, "Nouveau Livre", 2)==0) {
            try {
                manager.ajouterDocument(new Livre("LIV-"+System.currentTimeMillis(), t1.getText(), t2.getText(), t3.getText(), t4.getText(), Integer.parseInt(t5.getText()), t6.getText()));
                rafraichirTout();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout : Vérifiez que les données sont valides svpp", "Erreur de saisie", JOptionPane.ERROR_MESSAGE); }
        }
    }
    
    private void ajouterCD() {
        JTextField t1=new JTextField(), t2=new JTextField(), t3=new JTextField(), t4=new JTextField(), t5=new JTextField();
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Titre:",t1,"Artiste:",t2,"Genre:",t3,"Durée (min):",t4,"Pistes:",t5}, "Nouveau CD", 2)==0) {
            try {
                manager.ajouterDocument(new CD("CD-"+System.currentTimeMillis(), t1.getText(), t2.getText(), t3.getText(), Integer.parseInt(t4.getText()), Integer.parseInt(t5.getText())));
                rafraichirTout();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout : Vérifiez que les données sont valides svpp", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);  }
        }
    }

    private void ajouterMagazine() {
        JTextField t1=new JTextField(), t2=new JTextField(), t3=new JTextField(), t4=new JTextField(), t5=new JTextField(), t6=new JTextField();
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Titre:",t1,"Genre:",t2,"Numéro:",t3,"Périodicité:",t4,"Pages:",t5,"Editeur:",t6}, "Nouveau Mag", 2)==0) {
            try {
                manager.ajouterDocument(new Magazine("MAG-"+System.currentTimeMillis(), t1.getText(), null, t2.getText(), Integer.parseInt(t3.getText()), t4.getText(), Integer.parseInt(t5.getText()), t6.getText()));
                rafraichirTout();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout : Vérifiez que les données sont valides svpp", "Erreur de saisie", JOptionPane.ERROR_MESSAGE);  }
        }
    }

    private void ajouterAdherent() {
        JTextField t1=new JTextField(), t2=new JTextField(), t3=new JTextField();
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Nom:",t1,"Prénom:",t2,"Contact:",t3}, "Nouvel Adhérent", 2)==0) {
            try {
                manager.ajouterAdherent(new Adherent("ADH-"+System.currentTimeMillis(), t1.getText(), t2.getText(), t3.getText()));
                rafraichirTout();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur lors de l'ajout : Vérifiez que les données sont valides svpp", "Erreur de saisie", JOptionPane.ERROR_MESSAGE); }
        }
    }

    // --- MODIFICATION ADHÉRENT (CODE COMPLET) ---
    private void modifierAdherent(Adherent a) {
        JTextField t1 = new JTextField(a.getNom());
        JTextField t2 = new JTextField(a.getPrenom());
        JTextField t3 = new JTextField(a.getCoordonnees());
        
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Nom:",t1,"Prénom:",t2,"Contact:",t3}, "Modifier Adhérent", 2)==0) {
            try {
                a.setNom(t1.getText()); a.setPrenom(t2.getText()); a.setCoordonnees(t3.getText());
                manager.modifierAdherent(a);
                rafraichirTout();
            } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur modif"); }
        }
    }

    // --- MODIFICATION DOCUMENT (CODE COMPLET) ---
    private void modifierDocument(Document d) {
        JTextField tTit = new JTextField(d.getTitre());
        JTextField tAut = new JTextField(d.getAuteur());
        JTextField tGen = new JTextField(d.getGenre());
        
        Object[] msg = null;
        if(d instanceof Livre) {
            Livre l = (Livre)d;
            JTextField tIsbn = new JTextField(l.getIsbn()), tPages = new JTextField(""+l.getNombrePages()), tEdit = new JTextField(l.getEditeur());
            msg = new Object[]{"Titre:",tTit,"Auteur:",tAut,"Genre:",tGen,"ISBN:",tIsbn,"Pages:",tPages,"Editeur:",tEdit};
            if(JOptionPane.showConfirmDialog(this, msg, "Modifier Livre", 2)==0) {
                l.setTitre(tTit.getText()); l.setAuteur(tAut.getText()); l.setGenre(tGen.getText());
                l.setIsbn(tIsbn.getText()); l.setNombrePages(Integer.parseInt(tPages.getText())); l.setEditeur(tEdit.getText());
                try { manager.modifierDocument(l); } catch(Exception e) { e.printStackTrace(); }
            }
        } else if(d instanceof CD) {
            CD c = (CD)d;
            JTextField tDur = new JTextField(""+c.getDureeMinutes()), tPist = new JTextField(""+c.getNombrePistes());
            msg = new Object[]{"Titre:",tTit,"Artiste:",tAut,"Genre:",tGen,"Durée:",tDur,"Pistes:",tPist};
            if(JOptionPane.showConfirmDialog(this, msg, "Modifier CD", 2)==0) {
                c.setTitre(tTit.getText()); c.setArtistePrincipal(tAut.getText()); c.setGenre(tGen.getText());
                c.setDureeMinutes(Integer.parseInt(tDur.getText())); c.setNombrePistes(Integer.parseInt(tPist.getText()));
                try { manager.modifierDocument(c); } catch(Exception e) { e.printStackTrace(); }
            }
        } else if(d instanceof Magazine) {
            Magazine m = (Magazine)d;
            JTextField tNum = new JTextField(""+m.getNumero()), tPer = new JTextField(m.getPeriodicite()), tPag = new JTextField(""+m.getNombrePages()), tEd = new JTextField(m.getEditeur());
            msg = new Object[]{"Titre:",tTit,"Genre:",tGen,"Numéro:",tNum,"Périodicité:",tPer,"Pages:",tPag,"Editeur:",tEd};
            if(JOptionPane.showConfirmDialog(this, msg, "Modifier Mag", 2)==0) {
                m.setTitre(tTit.getText()); m.setGenre(tGen.getText());
                m.setNumero(Integer.parseInt(tNum.getText())); m.setPeriodicite(tPer.getText()); m.setNombrePages(Integer.parseInt(tPag.getText())); m.setEditeur(tEd.getText());
                try { manager.modifierDocument(m); } catch(Exception e) { e.printStackTrace(); }
            }
        }
        rafraichirTout();
    }

    // --- AFFICHAGE HISTORIQUE (CODE COMPLET) ---
    private void afficherHistorique(Adherent a) {
        List<Emprunt> history = manager.recupererHistorique(a);
        String[] cols = {"Doc", "Emprunté le", "Prévu le", "Rendu le", "État"};
        DefaultTableModel model = new DefaultTableModel(cols, 0);
        
        for(Emprunt e : history) {
            String rendu = (e.getDateRetourReelle() == null) ? "Non rendu" : e.getDateRetourReelle().format(fmt);
            String etat = (e.getDateRetourReelle() == null) ? "En cours" : "Terminé";
            if (e.getDateRetourReelle() == null && LocalDate.now().isAfter(e.getDateRetourPrevue())) etat = "RETARD";
            
            model.addRow(new Object[]{
                e.getDocumentEmprunte().getTitre(), 
                e.getDateEmprunt().format(fmt), 
                e.getDateRetourPrevue().format(fmt), 
                rendu, 
                etat
            });
        }
        
        JTable table = new JTable(model);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(700, 300));
        JOptionPane.showMessageDialog(this, scroll, "Historique : " + a.getNom(), JOptionPane.INFORMATION_MESSAGE);
    }

    // --- HELPERS ---
    private void rafraichirTout() {
        String typeActuel = (comboTypeDoc != null) ? (String) comboTypeDoc.getSelectedItem() : "TOUT";
        rafraichirDocuments("", typeActuel);
        
        String statutActuel = (comboStatutAdh != null) ? (String) comboStatutAdh.getSelectedItem() : "TOUT";
        rafraichirAdherents("", statutActuel);
        
        String etatActuel = (comboEtatEmp != null) ? (String) comboEtatEmp.getSelectedItem() : "EN COURS";
        rafraichirEmprunts(etatActuel, "");
    }

    private void rafraichirDocuments(String critere, String type) {
        modeleDocuments.setRowCount(0);
        for(Document d : manager.rechercherDocuments(critere, type)) {
            String det="", typ="", isbn="";
            if(d instanceof Livre) { typ="Livre"; isbn=((Livre)d).getIsbn(); det=((Livre)d).getNombrePages()+" p. | "+((Livre)d).getEditeur(); }
            else if(d instanceof CD) { typ="CD"; det=((CD)d).getDureeMinutes()+"min"; }
            else if(d instanceof Magazine) { typ="Mag"; det="N°"+((Magazine)d).getNumero()+" ("+((Magazine)d).getPeriodicite()+")"; }
            modeleDocuments.addRow(new Object[]{d.getId(), typ, d.getTitre(), d.getAuteur(), d.getGenre(), isbn, det, d.estEmprunte()?"Pris":"Dispo"});
        }
    }

    private void rafraichirAdherents(String txt, String filtreStatut) {
        modeleAdherents.setRowCount(0);
        txt = txt.toLowerCase();
        for (Adherent a : manager.listerAdherents()) {
            boolean matchNom = (txt.isEmpty() || a.getNom().toLowerCase().contains(txt) || a.getIdAdherent().toLowerCase().contains(txt));
            boolean matchStatut = (filtreStatut.equals("TOUT") || a.getStatut().name().equals(filtreStatut));
            
            if (matchNom && matchStatut) {
                modeleAdherents.addRow(new Object[]{a.getIdAdherent(), a.getNom(), a.getPrenom(), a.getCoordonnees(), a.getStatut(), a.getMontantPenalite()+" €"});
            }
        }
    }

    private void rafraichirEmprunts(String filtreEtat, String texteRecherche) {
        modeleEmprunts.setRowCount(0);
        String txt = texteRecherche.toLowerCase();
        
        try {
            List<Emprunt> liste = manager.listerEmpruntsEnCours(); // Optimisation possible : lister TOUT si filtre terminé
            if(filtreEtat.equals("TOUT") || filtreEtat.equals("TERMINÉS")) {
                // Pour voir les terminés, on devrait charger tout l'historique, mais ici on reste simple
                // Si tu veux tout, il faut une méthode manager.findAllEmprunts()
            }
            
            for (Emprunt e : liste) {
                boolean isRetard = LocalDate.now().isAfter(e.getDateRetourPrevue());
                boolean matchEtat = false;
                if (filtreEtat.equals("TOUT")) matchEtat = true;
                else if (filtreEtat.equals("RETARD") && isRetard) matchEtat = true;
                else if (filtreEtat.equals("EN COURS") && !isRetard) matchEtat = true;
                
                boolean matchText = true;
                if (!txt.isEmpty()) {
                    matchText = e.getIdEmprunt().toLowerCase().contains(txt) || e.getEmprunteur().getNom().toLowerCase().contains(txt) || e.getDocumentEmprunte().getTitre().toLowerCase().contains(txt);
                }

                if (matchEtat && matchText) {
                    modeleEmprunts.addRow(new Object[]{
                        e.getIdEmprunt(), e.getDocumentEmprunte().getTitre(), e.getEmprunteur().getNom(),
                        e.getDateEmprunt().format(fmt), e.getDateRetourPrevue().format(fmt), null, isRetard ? "⚠️ RETARD" : "OK"
                    });
                }
            }
        } catch (Exception e) {}
    }

    private void styliserTable(JTable table) {
        table.setRowHeight(25);
        table.getTableHeader().setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD));
        table.setShowVerticalLines(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
}