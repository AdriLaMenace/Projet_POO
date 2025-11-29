package ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import service.BibliothequeManager;

public class FenetreLogin extends JFrame {

    private BibliothequeManager manager;
    
    public FenetreLogin(BibliothequeManager manager) {
        this.manager = manager;
        
        setTitle("Connexion SGEB");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        JButton btnLogin = new JButton("Se connecter");
        JButton btnRegister = new JButton("Créer un compte");
        
        panel.add(new JLabel("Identifiant :"));
        panel.add(userField);
        panel.add(new JLabel("Mot de passe :"));
        panel.add(passField);
        
        JPanel btnPanel = new JPanel(new FlowLayout());
        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);
        
        this.add(panel, BorderLayout.CENTER);
        this.add(btnPanel, BorderLayout.SOUTH);
        
        // connexion
        btnLogin.addActionListener(e -> {
            String id = userField.getText();
            String mdp = new String(passField.getPassword());
            
            if (manager.seConnecter(id, mdp)) {
                // On ferme cette fenêtre et on ouvre la principale
                this.dispose(); 
                SwingUtilities.invokeLater(() -> {
                    new FenetrePrincipale(manager).setVisible(true);
                });
            } else {
                JOptionPane.showMessageDialog(this, "Identifiant ou mot de passe incorrect.", "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // l'inscription
        btnRegister.addActionListener(e -> {
            String id = userField.getText();
            String mdp = new String(passField.getPassword());
            
            if (id.isEmpty() || mdp.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Veuillez remplir tous les champs.");
                return;
            }
            
            if (manager.inscrire(id, mdp)) {
                JOptionPane.showMessageDialog(this, "Compte créé ! Vous pouvez vous connecter.");
            } else {
                JOptionPane.showMessageDialog(this, "Cet identifiant existe déjà.", "Erreur", JOptionPane.WARNING_MESSAGE);
            }
        });
    }
}