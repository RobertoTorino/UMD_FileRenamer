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
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com>
 */
public class UmdRenamerLogic {

    static {
        LogManager.getLogger(UmdDAO.class);
    }

    private UmdDAO umdDAO;

    /**
     * @param file the fileName to set
     * @throws IOException if an I/O error occurs while reading the sector
     */
    public Umd getUmd(File file) throws IOException {
        if (file != null && !file.isDirectory()) {
            return this.umdDAO.getUmd(file);
        }
        return null;
    }

    public String getFormattedName(Umd umd) {
        String name = umd.getTitle() + " (" + umd.getId().substring(0, 4) + "-" + umd.getId().substring(4) + ")";
        String invalidChars = "\\/:*?\"<>|™";
        for (Character c : invalidChars.toCharArray()) {
            name = name.replace(c.toString(), "");
        }
        return name;
    }

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
     * @param umdDAO the umdDAO to set
     */
    public void setUmdDAO(UmdDAO umdDAO) {
        this.umdDAO = umdDAO;
    }
}
