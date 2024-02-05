/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diegohp.umd.data;

import jpcsp.filesystems.umdiso.UmdIsoFile;
import jpcsp.filesystems.umdiso.UmdIsoReader;
import jpcsp.format.PSF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * The type Umd dao.
 *
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com></a>
 * @version $Id: $Id
 */
public class UmdDAO {

    private static final Logger logger = LogManager.getLogger(UmdDAO.class);

    /**
     * Instantiates a new Umd dao.
     */
    public UmdDAO() {
    }

    /**
     * Gets umd.
     *
     * @param file the file
     * @return the umd
     * @throws java.io.IOException the io exception
     */
    public Umd getUmd(File file) throws IOException {

        try {

            if (!file.isDirectory()) {

                Umd umd = new Umd();
                PSF psf = new PSF();

                logger.info("Reading file: " + file.getName());
                UmdIsoReader iso = new UmdIsoReader(file.getPath());

                UmdIsoFile paramSfo = iso.getFile("PSP_GAME/param.sfo");
                byte[] sfo = new byte[(int) paramSfo.length()];
                int bytesRead = paramSfo.read(sfo);
                if (bytesRead == -1) {
                    // Handle the case where no bytes were read
                    throw new IOException("Failed to read from param.sfo");
                }
                paramSfo.close();
                ByteBuffer buf = ByteBuffer.wrap(sfo);
                psf.read(buf);

                byte[] icon0 = null;
                try {
                    UmdIsoFile icon0umd = iso.getFile("PSP_GAME/ICON0.PNG");
                    icon0 = new byte[(int) icon0umd.length()];
                    int bytesReadIcon0 = icon0umd.read(icon0);
                    if (bytesReadIcon0 == -1) {
                        // Handle the case where no bytes were read
                        throw new IOException("Failed to read from ICON0.PNG");
                    }
                    icon0umd.close();
                } catch (FileNotFoundException e) {
                    logger.warn(e.getMessage());
                    //assign the default icon
                }

                String title = psf.getString("TITLE");
                String id = psf.getString("DISC_ID");
                String version = psf.getString("DISC_VERSION");
                String firmware = psf.getString("PSP_SYSTEM_VER");

                umd.setId(id);
                umd.setTitle(title);
                umd.setVersion(version);
                umd.setFirmware(firmware);
                umd.setIcon0(icon0);
                umd.setFile(file);

                return umd;

            }
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            // default icon
            // icons[rowIndex] = new ImageIcon(getClass().getResource("/jpcsp/images/icon0.png"));
        }

        return null;
    }
}
