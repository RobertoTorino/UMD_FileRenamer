package com.diegohp.umd.filerenamer;

import com.diegohp.umd.data.UmdDAO;
import com.diegohp.umd.renamer.ui.LanguageSelectorJDialog;
import com.diegohp.umd.renamer.ui.RenamerJFrame;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.*;

/**
 * Hello world!
 *
 * @author wolf003
 * @version $Id: $Id
 */
public class App {

    private static final Logger logger = LogManager.getLogger(UmdDAO.class);

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        // Set the system property to opt-out of secure coding for restorable state
        System.setProperty("apple.awt.application.supportsSecureRestorableState", "false");

        // Your application logic go
        logger.info("Starting UMD_FileRenamer");

        //<editor-fold default state="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            javax.swing.UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(RenamerJFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(() -> {
            LanguageSelectorJDialog languageSelectorJFrame = new LanguageSelectorJDialog(new JFrame(), true);
            languageSelectorJFrame.setVisible(true);

            RenamerJFrame renamerJFrame = new RenamerJFrame();
            renamerJFrame.setVisible(true);

            renamerJFrame.askStartDirectory();
        });
    }
}
