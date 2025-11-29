package ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;

import entites.*;
import service.BibliothequeManager;

public class FenetrePrincipale extends JFrame {

    // --- THEME DARK MODERN ---
    private static final Color BG_DARK = new Color(18, 18, 24);
    private static final Color PANEL_BG = new Color(30, 30, 40);
    private static final Color ACCENT = new Color(108, 92, 231);
    private static final Color SUCCESS = new Color(0, 184, 148);
    private static final Color DANGER = new Color(214, 48, 49);
    private static final Color WARNING = new Color(253, 203, 110);
    private static final Color TEXT_MAIN = new Color(240, 240, 240);
    private static final Color TEXT_MUTED = new Color(160, 160, 170);
    
    private static final Font FONT_TITLE = new Font("SansSerif", Font.BOLD, 24);
    private static final Font FONT_REGULAR = new Font("SansSerif", Font.PLAIN, 13);

    private BibliothequeManager manager;
    private JPanel mainContainer;
    private CardLayout navigator;
    
    // Data Models
    private DefaultTableModel modelDocs, modelAdhs, modelEmps;
    private JTable tableDocs, tableAdhs, tableEmps;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Inputs (Standards pour fonctionnalité max)
    private JTextField txtSearchDoc, txtSearchAdh, txtSearchEmp;
    private JComboBox<String> comboType, comboStatut, comboEtat;

    public FenetrePrincipale(BibliothequeManager manager) {
        this.manager = manager;
        setTitle("SGEB • Nexus Dashboard");
        setSize(1450, 900);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        setContentPane(root);

        // 1. SIDEBAR
        JPanel sidebar = new GradientPanel(new Color(25, 25, 35), new Color(15, 15, 20));
        sidebar.setPreferredSize(new Dimension(260, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(new EmptyBorder(30, 20, 30, 20));

        JLabel logo = new JLabel("⚡ SGEB");
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("SansSerif", Font.BOLD, 32));
        logo.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(logo);
        sidebar.add(Box.createVerticalStrut(50));

        addMenuButton(sidebar, "Catalogue", "DOCS", "📚");
        addMenuButton(sidebar, "Membres", "ADHS", "👥");
        addMenuButton(sidebar, "Transactions", "EMPS", "🔄");

        // 2. MAIN CONTENT
        navigator = new CardLayout();
        mainContainer = new JPanel(navigator);
        mainContainer.setOpaque(false);
        mainContainer.setBorder(new EmptyBorder(20, 30, 20, 30));

        mainContainer.add(buildDocsView(), "DOCS");
        mainContainer.add(buildAdhsView(), "ADHS");
        mainContainer.add(buildEmpsView(), "EMPS");

        root.add(sidebar, BorderLayout.WEST);
        root.add(mainContainer, BorderLayout.CENTER);

        rafraichirTout();
    }

    // =================================================================================
    // VUE 1 : DOCUMENTS
    // =================================================================================
    private JPanel buildDocsView() {
        JPanel view = new JPanel(new BorderLayout(0, 15));
        view.setOpaque(false);

        view.add(createHeader("Catalogue", "Gestion complète du fonds documentaire."), BorderLayout.NORTH);
        
        // Toolbar
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        
        txtSearchDoc = new JTextField(15);
        comboType = new JComboBox<>(new String[]{"TOUT", "LIVRE", "CD", "MAGAZINE"});
        
        top.add(new JLabel("Rech: ")); top.add(txtSearchDoc);
        top.add(comboType);
        top.add(new ModernButton("🔍", ACCENT, e -> refreshDocs()));
        top.add(Box.createHorizontalStrut(20));
        top.add(new ModernButton("＋ Livre", PANEL_BG, e -> ajouterLivre())); // VRAIE METHODE V1
        top.add(new ModernButton("＋ CD", PANEL_BG, e -> ajouterCD()));       // VRAIE METHODE V1
        top.add(new ModernButton("＋ Mag", PANEL_BG, e -> ajouterMagazine())); // VRAIE METHODE V1
        top.add(new ModernButton("✎", PANEL_BG, e -> modifierDocument()));    // VRAIE METHODE V1
        top.add(new ModernButton("🗑", DANGER, e -> deleteDoc()));

        // Table
        String[] cols = {"ID", "Type", "Titre", "Auteur", "Genre", "ISBN", "Détails", "Statut"};
        modelDocs = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        tableDocs = new ModernTable(modelDocs);
        tableDocs.getColumnModel().getColumn(7).setCellRenderer(new BadgeRenderer());

        view.add(top, BorderLayout.CENTER);
        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(top, BorderLayout.NORTH);
        content.add(new ModernScrollPane(tableDocs), BorderLayout.CENTER);
        
        view.add(content, BorderLayout.CENTER);
        return view;
    }

    // =================================================================================
    // VUE 2 : ADHERENTS
    // =================================================================================
    private JPanel buildAdhsView() {
        JPanel view = new JPanel(new BorderLayout(0, 15));
        view.setOpaque(false);

        view.add(createHeader("Membres", "Gestion des inscriptions et pénalités."), BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        
        txtSearchAdh = new JTextField(15);
        comboStatut = new JComboBox<>(new String[]{"TOUT", "ACTIF", "AVEC_PENALITE"});
        
        top.add(new JLabel("Nom/ID: ")); top.add(txtSearchAdh);
        top.add(comboStatut);
        top.add(new ModernButton("🔍", ACCENT, e -> refreshAdhs()));
        top.add(Box.createHorizontalStrut(20));
        top.add(new ModernButton("＋ Adhérent", SUCCESS, e -> ajouterAdherent())); // VRAIE METHODE V1
        top.add(new ModernButton("✎", PANEL_BG, e -> modifierAdherent()));        // VRAIE METHODE V1
        top.add(new ModernButton("📜 Historique", PANEL_BG, e -> afficherHistorique())); // VRAIE METHODE V1
        top.add(new ModernButton("💰 Régler", WARNING, e -> payDette()));
        top.add(new ModernButton("🗑", DANGER, e -> deleteAdherent()));

        String[] cols = {"ID", "Nom", "Prénom", "Coordonnées", "Statut", "Amende"};
        modelAdhs = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        tableAdhs = new ModernTable(modelAdhs);
        tableAdhs.getColumnModel().getColumn(4).setCellRenderer(new BadgeRenderer());

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(top, BorderLayout.NORTH);
        content.add(new ModernScrollPane(tableAdhs), BorderLayout.CENTER);
        
        view.add(content, BorderLayout.CENTER);
        return view;
    }

    // =================================================================================
    // VUE 3 : EMPRUNTS
    // =================================================================================
    private JPanel buildEmpsView() {
        JPanel view = new JPanel(new BorderLayout(0, 15));
        view.setOpaque(false);

        view.add(createHeader("Transactions", "Suivi des prêts et retours."), BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.setOpaque(false);
        
        txtSearchEmp = new JTextField(15);
        comboEtat = new JComboBox<>(new String[]{"EN COURS", "RETARD", "TOUT"});
        
        top.add(new JLabel("Rech: ")); top.add(txtSearchEmp);
        top.add(comboEtat);
        top.add(new ModernButton("Actualiser", ACCENT, e -> refreshEmps()));
        top.add(Box.createHorizontalStrut(30));
        top.add(new ModernButton("📤 Emprunter", ACCENT, e -> nouveauPret())); // VRAIE METHODE V1
        top.add(new ModernButton("📥 Retourner", SUCCESS, e -> returnLoan()));

        String[] cols = {"ID", "Document", "Adhérent", "Date Prêt", "Date Prévue", "Date Réelle", "État"};
        modelEmps = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r, int c) { return false; } };
        tableEmps = new ModernTable(modelEmps);
        tableEmps.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer());

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setOpaque(false);
        content.add(top, BorderLayout.NORTH);
        content.add(new ModernScrollPane(tableEmps), BorderLayout.CENTER);
        
        view.add(content, BorderLayout.CENTER);
        return view;
    }

    // =================================================================================
    // LOGIQUE MÉTIER & ACTIONS (RESTITUTION EXACTE DE LA V1)
    // =================================================================================
    
    private void rafraichirTout() { refreshDocs(); refreshAdhs(); refreshEmps(); }

    // --- REFRESH TABLES ---
    private void refreshDocs() {
        modelDocs.setRowCount(0);
        String type = (String) comboType.getSelectedItem();
        String txt = txtSearchDoc.getText();
        for(Document d : manager.rechercherDocuments(txt, type)) {
            String isbn = (d instanceof Livre) ? ((Livre)d).getIsbn() : "-";
            String info = (d instanceof Livre) ? ((Livre)d).getNombrePages()+"p" : (d instanceof CD) ? ((CD)d).getDureeMinutes()+"min" : "Mag";
            modelDocs.addRow(new Object[]{d.getId(), d.getClass().getSimpleName(), d.getTitre(), d.getAuteur(), d.getGenre(), isbn, info, d.estEmprunte()?"PRIS":"DISPO"});
        }
    }
    private void refreshAdhs() {
        modelAdhs.setRowCount(0);
        String st = (String) comboStatut.getSelectedItem();
        String txt = txtSearchAdh.getText().toLowerCase();
        for(Adherent a : manager.listerAdherents()) {
            if((st.equals("TOUT") || a.getStatut().name().equals(st)) && (txt.isEmpty() || a.getNom().toLowerCase().contains(txt)))
                modelAdhs.addRow(new Object[]{a.getIdAdherent(), a.getNom(), a.getPrenom(), a.getCoordonnees(), a.getStatut().toString(), a.getMontantPenalite()+" €"});
        }
    }
    private void refreshEmps() {
        modelEmps.setRowCount(0);
        String st = (String) comboEtat.getSelectedItem();
        String txt = txtSearchEmp.getText().toLowerCase();
        for(Emprunt e : manager.listerEmpruntsEnCours()) {
            boolean ret = LocalDate.now().isAfter(e.getDateRetourPrevue());
            String dateReelle = (e.getDateRetourReelle() != null) ? e.getDateRetourReelle().format(fmt) : "-";
            boolean isTermine = (e.getDateRetourReelle() != null);
            boolean afficher = false;
            if(st.equals("TOUT")) afficher = true;
            else if(st.equals("EN COURS") && !isTermine) afficher = true;
            else if(st.equals("RETARD") && !isTermine && ret) afficher = true;

            if(afficher && (txt.isEmpty() || e.getDocumentEmprunte().getTitre().toLowerCase().contains(txt))) {
                String etat = isTermine ? "RENDU" : (ret ? "RETARD" : "EN COURS");
                modelEmps.addRow(new Object[]{e.getIdEmprunt(), e.getDocumentEmprunte().getTitre(), e.getEmprunteur().getNom(), e.getDateEmprunt().format(fmt), e.getDateRetourPrevue().format(fmt), dateReelle, etat});
            }
        }
    }

    // --- FORMULAIRES V1 (RESTITUTION) ---

    private void ajouterLivre() {
        JTextField t1=new JTextField(), t2=new JTextField(), t3=new JTextField(), t4=new JTextField(), t5=new JTextField(), t6=new JTextField();
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Titre:",t1,"Auteur:",t2,"Genre:",t3,"ISBN:",t4,"Pages:",t5,"Editeur:",t6}, "Nouveau Livre", 2)==0) {
            try { manager.ajouterDocument(new Livre("LIV-"+System.currentTimeMillis(), t1.getText(), t2.getText(), t3.getText(), t4.getText(), Integer.parseInt(t5.getText()), t6.getText())); refreshDocs(); } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur saisie"); }
        }
    }
    private void ajouterCD() {
        JTextField t1=new JTextField(), t2=new JTextField(), t3=new JTextField(), t4=new JTextField(), t5=new JTextField();
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Titre:",t1,"Artiste:",t2,"Genre:",t3,"Durée (min):",t4,"Pistes:",t5}, "Nouveau CD", 2)==0) {
            try { manager.ajouterDocument(new CD("CD-"+System.currentTimeMillis(), t1.getText(), t2.getText(), t3.getText(), Integer.parseInt(t4.getText()), Integer.parseInt(t5.getText()))); refreshDocs(); } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur saisie"); }
        }
    }
    private void ajouterMagazine() {
        JTextField t1=new JTextField(), t2=new JTextField(), t3=new JTextField(), t4=new JTextField(), t5=new JTextField(), t6=new JTextField();
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Titre:",t1,"Genre:",t2,"Numéro:",t3,"Périodicité:",t4,"Pages:",t5,"Editeur:",t6}, "Nouveau Mag", 2)==0) {
            try { manager.ajouterDocument(new Magazine("MAG-"+System.currentTimeMillis(), t1.getText(), null, t2.getText(), Integer.parseInt(t3.getText()), t4.getText(), Integer.parseInt(t5.getText()), t6.getText())); refreshDocs(); } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur saisie"); }
        }
    }
    private void ajouterAdherent() {
        JTextField t1=new JTextField(), t2=new JTextField(), t3=new JTextField();
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Nom:",t1,"Prénom:",t2,"Contact:",t3}, "Nouvel Adhérent", 2)==0) {
            try { manager.ajouterAdherent(new Adherent("ADH-"+System.currentTimeMillis(), t1.getText(), t2.getText(), t3.getText())); refreshAdhs(); } catch(Exception e) { JOptionPane.showMessageDialog(this, "Erreur saisie"); }
        }
    }

    private void modifierDocument() {
        int r = tableDocs.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un document."); return; }
        String id = (String) modelDocs.getValueAt(r, 0);
        try {
            Document d = manager.recupererDocumentParId(id);
            JTextField tTit = new JTextField(d.getTitre()), tAut = new JTextField(d.getAuteur()), tGen = new JTextField(d.getGenre());
            Object[] msg = null;
            if(d instanceof Livre) {
                Livre l = (Livre)d;
                JTextField tIsbn = new JTextField(l.getIsbn()), tPages = new JTextField(""+l.getNombrePages()), tEdit = new JTextField(l.getEditeur());
                msg = new Object[]{"Titre:",tTit,"Auteur:",tAut,"Genre:",tGen,"ISBN:",tIsbn,"Pages:",tPages,"Editeur:",tEdit};
                if(JOptionPane.showConfirmDialog(this, msg, "Modifier Livre", 2)==0) {
                    l.setTitre(tTit.getText()); l.setAuteur(tAut.getText()); l.setGenre(tGen.getText());
                    l.setIsbn(tIsbn.getText()); l.setNombrePages(Integer.parseInt(tPages.getText())); l.setEditeur(tEdit.getText());
                    manager.modifierDocument(l);
                }
            } else if(d instanceof CD) {
                CD c = (CD)d;
                JTextField tDur = new JTextField(""+c.getDureeMinutes()), tPist = new JTextField(""+c.getNombrePistes());
                msg = new Object[]{"Titre:",tTit,"Artiste:",tAut,"Genre:",tGen,"Durée:",tDur,"Pistes:",tPist};
                if(JOptionPane.showConfirmDialog(this, msg, "Modifier CD", 2)==0) {
                    c.setTitre(tTit.getText()); c.setArtistePrincipal(tAut.getText()); c.setGenre(tGen.getText());
                    c.setDureeMinutes(Integer.parseInt(tDur.getText())); c.setNombrePistes(Integer.parseInt(tPist.getText()));
                    manager.modifierDocument(c);
                }
            } else if(d instanceof Magazine) {
                Magazine m = (Magazine)d;
                JTextField tNum = new JTextField(""+m.getNumero()), tPer = new JTextField(m.getPeriodicite()), tPag = new JTextField(""+m.getNombrePages()), tEd = new JTextField(m.getEditeur());
                msg = new Object[]{"Titre:",tTit,"Genre:",tGen,"Numéro:",tNum,"Périodicité:",tPer,"Pages:",tPag,"Editeur:",tEd};
                if(JOptionPane.showConfirmDialog(this, msg, "Modifier Mag", 2)==0) {
                    m.setTitre(tTit.getText()); m.setGenre(tGen.getText());
                    m.setNumero(Integer.parseInt(tNum.getText())); m.setPeriodicite(tPer.getText()); m.setNombrePages(Integer.parseInt(tPag.getText())); m.setEditeur(tEd.getText());
                    manager.modifierDocument(m);
                }
            }
            refreshDocs();
        } catch(Exception e) { e.printStackTrace(); }
    }

    private void modifierAdherent() {
        int r = tableAdhs.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un adhérent."); return; }
        String id = (String) modelAdhs.getValueAt(r, 0);
        try {
            Adherent a = manager.rechercherAdherent(id);
            JTextField t1 = new JTextField(a.getNom()), t2 = new JTextField(a.getPrenom()), t3 = new JTextField(a.getCoordonnees());
            if(JOptionPane.showConfirmDialog(this, new Object[]{"Nom:",t1,"Prénom:",t2,"Contact:",t3}, "Modifier Adhérent", 2)==0) {
                a.setNom(t1.getText()); a.setPrenom(t2.getText()); a.setCoordonnees(t3.getText());
                manager.modifierAdherent(a); refreshAdhs();
            }
        } catch(Exception e){}
    }

    private void nouveauPret() {
        Vector<String> ads=new Vector<>(), docs=new Vector<>();
        try {
            for(Adherent a:manager.listerAdherents()) if(a.getStatut()==E_StatutAdherent.ACTIF) ads.add(a.getIdAdherent()+" - "+a.getNom());
            for(Document d:manager.rechercherDocuments("","TOUT")) if(!d.estEmprunte()) docs.add(d.getId()+" - "+d.getTitre());
        } catch(Exception e){}
        
        if(ads.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun adhérent actif."); return; }
        if(docs.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucun document dispo."); return; }

        JComboBox<String> ca=new JComboBox<>(ads), cd=new JComboBox<>(docs);
        if(JOptionPane.showConfirmDialog(this, new Object[]{"Qui ?", ca, "Quoi ?", cd}, "Emprunt", 2)==0) {
            try {
                String ida = ((String)ca.getSelectedItem()).split(" - ")[0];
                String idd = ((String)cd.getSelectedItem()).split(" - ")[0];
                if(manager.emprunter(manager.rechercherAdherent(ida), manager.recupererDocumentParId(idd))) {
                    JOptionPane.showMessageDialog(this, "Emprunt validé !"); rafraichirTout();
                } else JOptionPane.showMessageDialog(this, "Erreur (Quota)");
            } catch(Exception e){}
        }
    }

    private void afficherHistorique() {
        int r = tableAdhs.getSelectedRow();
        if(r >= 0) {
            String id = (String) modelAdhs.getValueAt(r, 0);
            Adherent a = manager.rechercherAdherent(id);
            List<Emprunt> hist = manager.recupererHistorique(a);
            
            String[] hCols = {"Doc", "Emprunté le", "Prévu le", "Rendu le", "État"};
            DefaultTableModel hModel = new DefaultTableModel(hCols, 0);
            for(Emprunt e : hist) {
                String rendu = (e.getDateRetourReelle() == null) ? "Non rendu" : e.getDateRetourReelle().format(fmt);
                String etat = (e.getDateRetourReelle() == null && LocalDate.now().isAfter(e.getDateRetourPrevue())) ? "RETARD" : "OK";
                hModel.addRow(new Object[]{e.getDocumentEmprunte().getTitre(), e.getDateEmprunt().format(fmt), e.getDateRetourPrevue().format(fmt), rendu, etat});
            }
            JTable t = new ModernTable(hModel);
            JScrollPane s = new ModernScrollPane(t);
            s.setPreferredSize(new Dimension(800, 400));
            JOptionPane.showMessageDialog(this, s, "Historique : " + a.getNom(), JOptionPane.PLAIN_MESSAGE);
        } else JOptionPane.showMessageDialog(this, "Sélectionnez un adhérent.");
    }

    private void deleteDoc() {
        int r = tableDocs.getSelectedRow();
        if(r>=0 && JOptionPane.showConfirmDialog(this, "Supprimer ?", "Confirm", JOptionPane.YES_NO_OPTION)==0) {
            try {
                String id=(String)modelDocs.getValueAt(r,0);
                if(manager.recupererDocumentParId(id).estEmprunte()) JOptionPane.showMessageDialog(this, "Impossible: Emprunté");
                else { manager.supprimerDocument(id); refreshDocs(); }
            } catch(Exception e){}
        }
    }
    private void deleteAdherent() {
        int r = tableAdhs.getSelectedRow();
        if(r>=0 && JOptionPane.showConfirmDialog(this, "Supprimer ?", "Confirm", JOptionPane.YES_NO_OPTION)==0) {
            try { manager.supprimerAdherent((String)modelAdhs.getValueAt(r,0)); refreshAdhs(); } catch(Exception e){ JOptionPane.showMessageDialog(this, "Erreur: A des emprunts"); }
        }
    }
    private void payDette() {
        int r = tableAdhs.getSelectedRow();
        if(r>=0) {
            Adherent a = manager.rechercherAdherent((String)modelAdhs.getValueAt(r, 0));
            if(a.getMontantPenalite()>0 && JOptionPane.showConfirmDialog(this, "Régler "+a.getMontantPenalite()+"€ ?", "Pay", 0)==0) {
                try { manager.reglerPenalite(a); refreshAdhs(); } catch(Exception e){}
            }
        }
    }
    private void returnLoan() {
        int r = tableEmps.getSelectedRow();
        if(r>=0) {
            String id = (String) modelEmps.getValueAt(r, 0);
            Emprunt e = manager.listerEmpruntsEnCours().stream().filter(em->em.getIdEmprunt().equals(id)).findFirst().orElse(null);
            if(e!=null) { manager.rendre(e); rafraichirTout(); JOptionPane.showMessageDialog(this, "Retour Validé"); }
        }
    }

    // =================================================================================
    // UI COMPONENTS
    // =================================================================================

    private JPanel createHeader(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(false);
        JLabel l1 = new JLabel(title); l1.setFont(FONT_TITLE); l1.setForeground(Color.WHITE);
        JLabel l2 = new JLabel(subtitle); l2.setFont(new Font("SansSerif", Font.PLAIN, 14)); l2.setForeground(TEXT_MUTED);
        p.add(l1, BorderLayout.NORTH); p.add(l2, BorderLayout.CENTER);
        p.setBorder(new EmptyBorder(0, 0, 15, 0));
        return p;
    }

    private void addMenuButton(JPanel p, String text, String key, String icon) {
        JButton b = new JButton(icon + "  " + text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (getModel().isRollover()) { g2.setColor(new Color(255, 255, 255, 20)); g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10); }
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(TEXT_MAIN);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(12, 15, 12, 0));
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(240, 45));
        b.addActionListener(e -> navigator.show(mainContainer, key));
        p.add(b); p.add(Box.createVerticalStrut(5));
    }

    class GradientPanel extends JPanel {
        private Color c1, c2;
        public GradientPanel(Color c1, Color c2) { this.c1=c1; this.c2=c2; }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }
    class ModernButton extends JButton {
        private Color color;
        public ModernButton(String text, Color c, ActionListener act) {
            super(text); this.color = c; addActionListener(act);
            setFont(new Font("SansSerif", Font.BOLD, 12)); setForeground(Color.WHITE);
            setBorder(new EmptyBorder(8, 15, 8, 15)); setFocusPainted(false);
            setContentAreaFilled(false); setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getModel().isRollover() ? color.brighter() : color);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            super.paintComponent(g);
        }
    }
    class ModernTable extends JTable {
        public ModernTable(TableModel m) {
            super(m); setRowHeight(45); setFont(FONT_REGULAR); setForeground(TEXT_MAIN); setBackground(PANEL_BG);
            setShowVerticalLines(false); setGridColor(new Color(255, 255, 255, 10));
            setSelectionBackground(new Color(108, 92, 231, 50)); setSelectionForeground(Color.WHITE);
            getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
            getTableHeader().setBackground(BG_DARK); getTableHeader().setForeground(TEXT_MUTED);
            getTableHeader().setBorder(null);
        }
    }
    class ModernScrollPane extends JScrollPane {
        public ModernScrollPane(Component c) { super(c); getViewport().setBackground(BG_DARK); setBorder(null); setOpaque(false); }
    }
    class BadgeRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String s = (String) value;
            JLabel l = new JLabel(s); l.setFont(new Font("SansSerif", Font.BOLD, 11));
            l.setHorizontalAlignment(CENTER); l.setForeground(Color.WHITE);
            Color bg = Color.GRAY;
            if(s.contains("DISPO") || s.contains("ACTIF") || s.contains("EN COURS") || s.equals("OK")) bg = SUCCESS;
            if(s.contains("PRIS") || s.contains("PENALITE") || s.contains("RETARD")) bg = DANGER;
            JPanel p = new JPanel(new GridBagLayout()) {
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D)g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(isSelected ? table.getSelectionBackground() : PANEL_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.setColor(getBackground()); g2.fillRoundRect(10, 12, getWidth()-20, getHeight()-24, 15, 15);
                }
            };
            p.setBackground(bg); p.add(l);
            return p;
        }
    }
}