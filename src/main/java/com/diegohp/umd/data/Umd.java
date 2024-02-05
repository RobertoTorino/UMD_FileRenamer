/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diegohp.umd.data;

import java.io.File;

/**
 * The type Umd.
 *
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com></a>
 * @version $Id: $Id
 */
public class Umd {

    private String id;
    private String title;
    private String version;
    private String firmware;
    private byte[] icon0;
    private File file;


    /**
     * Instantiates a new Umd.
     */
    public Umd() {
        this.id = "";
        this.title = "";
        this.version = "";
        this.firmware = "";
        this.icon0 = null;
        this.file = null;
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the UMD.
     *
     * @param title the title to set; must not be null.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version of the UMD.
     *
     * @param version the version to set; must not be null.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Gets firmware.
     *
     * @return the firmware
     */
    public String getFirmware() {
        return firmware;
    }

    /**
     * Sets the firmware version of the UMD.
     *
     * @param firmware the firmware version to set; must not be null.
     */
    public void setFirmware(String firmware) {
        this.firmware = firmware;
    }

    /**
     * Get icon 0 byte [ ].
     *
     * @return the icon0
     */
    public byte[] getIcon0() {
        return icon0;
    }

    /**
     * Sets the icon0 image of the UMD.
     *
     * @param icon0 the icon0 image to set; must not be null.
     */
    public void setIcon0(byte[] icon0) {
        this.icon0 = icon0;
    }

    /**
     * Gets file.
     *
     * @return the fileName
     */
    public File getFile() {
        return this.file;
    }

    /**
     * Sets the icon0 image of the UMD.
     *
     * @param file the fileName to set; must not be null.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * Gets extension.
     *
     * @return the extension
     */
    public String getExtension() {
        String extension = null;
        if (this.getFile().getPath().endsWith(".iso")) {
            extension = ".iso";
        } else if (this.getFile().getPath().endsWith(".cso")) {
            extension = ".cso";
        }
        return extension;
    }


}
