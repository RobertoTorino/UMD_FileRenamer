package com.diegohp.swing;

import javax.swing.*;
import java.io.File;

/**
 * This class brings some shortcuts of Swing User-Interface components.
 *
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com></a>
 * @version 1.0
 */
public final class SwingHelper {

    /**
     * Displays a {@link javax.swing.JOptionPane} as an error message.
     *
     * @param title   The title of the dialog.
     * @param message The message inside the dialog.
     */
    public static void showErrorMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Displays a {@link javax.swing.JOptionPane} as a warning message.
     *
     * @param title   The title of the dialog.
     * @param message The message inside the dialog.
     */
    public static void showWarningMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.WARNING_MESSAGE);
    }

    /**
     * Displays a {@link javax.swing.JOptionPane} as an information message.
     *
     * @param title   The title of the dialog.
     * @param message The message inside the dialog.
     */
    public static void showInformationMessage(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Displays a {@link javax.swing.JFileChooser} to select a directory.
     *
     * @param title          The title of the dialog.
     * @param startDirectory The directory where the dialog is initialed opened.
     * @return The {@link java.io.File} object selected, returns null if no directory was selected.
     */
    public static File chooseDirectory(String title, File startDirectory) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setDialogTitle(title);

        if (startDirectory != null) {
            chooser.setCurrentDirectory(startDirectory);
        }

        int status = chooser.showOpenDialog(null);

        if (status == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile();
        }

        return null;
    }

}
