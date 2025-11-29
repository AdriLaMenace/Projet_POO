package main;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import service.BibliothequeManager;
import ui.FenetreLogin; 

public class MainApplication {

    public static void main(String[] args) {
        
        BibliothequeManager manager =new BibliothequeManager();

        //lancement de l'interface graphique
        SwingUtilities.invokeLater(() -> {
            try {
                
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) { // on utilise Nimbus : rendu sympa et pro
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            } catch (Exception e) {}
            
            
            FenetreLogin login= new FenetreLogin(manager ); //lancement login
            login.setVisible(true) ;
             
        });
    }
}