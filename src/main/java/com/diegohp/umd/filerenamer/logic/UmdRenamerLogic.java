/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diegohp.umd.filerenamer.logic;

import com.diegohp.umd.data.Umd;
import com.diegohp.umd.data.UmdDAO;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.IOException;

/**
 * The type Umd renamer logic.
 *
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com></a>
 * @version $Id: $Id
 */
public class UmdRenamerLogic {

    static {
        LogManager.getLogger(UmdDAO.class);
    }

    private UmdDAO umdDAO;

    /**
     * Gets umd.
     *
     * @param file the fileName to set
     * @return the umd
     * @throws java.io.IOException if an I/O error occurs while reading the sector
     */
    public Umd getUmd(File file) throws IOException {
        if (file != null && !file.isDirectory()) {
            return this.umdDAO.getUmd(file);
        }
        return null;
    }

    /**
     * Gets formatted name.
     *
     * @param umd the umd
     * @return the formatted name
     */
    public String getFormattedName(Umd umd) {
        String name = umd.getTitle() + " (" + umd.getId().substring(0, 4) + "-" + umd.getId().substring(4) + ")";
        String invalidChars = "\\/:*?\"<>|™";
        for (Character c : invalidChars.toCharArray()) {
            name = name.replace(c.toString(), "");
        }
        return name;
    }

    /**
     * Rename.
     *
     * @param umd         the umd
     * @param folder      the folder
     * @param newFileName the new file name
     */
    public void rename(Umd umd, String folder, String newFileName) {
        File newFile = new File(folder + File.separator + newFileName + umd.getExtension());

        // Check if the renaming was successful
        if (umd.getFile().renameTo(newFile)) {
            umd.setFile(newFile);
        } else {
            System.err.println("Failed to rename file: " + umd.getFile().getPath());
        }
    }

    /**
     * Sets umd dao.
     *
     * @param umdDAO the umdDAO to set
     */
    public void setUmdDAO(UmdDAO umdDAO) {
        this.umdDAO = umdDAO;
    }
}
