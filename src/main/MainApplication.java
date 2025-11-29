package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import service.BibliothequeManager;
import ui.FenetreLogin; // On importe la nouvelle fenêtre

public class MainApplication {

    public static void main(String[] args) {
        
        BibliothequeManager manager = new BibliothequeManager();

        // On lance l'interface graphique
        SwingUtilities.invokeLater(() -> {
            try {
                // Look Nimbus pour que ce soit joli
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {}
            
            // CHANGEMENT ICI : On lance la fenêtre de Login
            FenetreLogin login = new FenetreLogin(manager);
            login.setVisible(true);
        });
    }
}