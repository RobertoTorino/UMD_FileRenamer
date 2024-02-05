/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.diegohp.umd.renamer.logic;

import com.diegohp.umd.data.Umd;
import com.diegohp.umd.data.UmdDAO;
import com.diegohp.umd.filerenamer.logic.UmdRenamerLogic;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;
import java.io.IOException;

/**
 * <p>UmdRenamerLogicTest class.</p>
 *
 * @author diegohp (Diego Hernandez Perez) - <a href="mailto:hp.diego@gmail.com">hp.diego@gmail.com></a>
 * @version $Id: $Id
 * @since 1.0.2
 */
public class UmdRenamerLogicTest extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public UmdRenamerLogicTest(String testName) {
        super(testName);
    }

    /**
     * <p>suite.</p>
     *
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite(UmdRenamerLogicTest.class);
    }

    /**
     * Rigorous Test :-)
     *
     * @throws java.io.IOException if any.
     */
    public void testUmdRenamerLogic() throws IOException {

        String cubeFileFolder = "target/test-classes/com/diegohp/umd/filerenamer/logic/";
        String cubeFileName = "cube.cso";
        String cubeFilePath = cubeFileFolder + cubeFileName;
        System.out.println("Reading: " + cubeFilePath);
        File file = new File(cubeFilePath);
        assertTrue(file.exists());

        UmdRenamerLogic umdRenamerLogic = new UmdRenamerLogic();
        UmdDAO umdDAO = new UmdDAO();
        umdRenamerLogic.setUmdDAO(umdDAO);

        Umd umd = umdRenamerLogic.getUmd(file);
        assertNotNull(umd);

        assertEquals("Cube sample", umd.getTitle());
        System.out.println("Title: " + umd.getTitle());

        assertEquals("UCJS10041", umd.getId());
        System.out.println("ID: " + umd.getId());

        assertEquals("1.00", umd.getVersion());
        System.out.println("Version: " + umd.getVersion());

        assertEquals("1.50", umd.getFirmware());
        System.out.println("Firmware: " + umd.getFirmware());

        String formattedName = umdRenamerLogic.getFormattedName(umd);

        assertEquals("Cube sample (UCJS-10041)", formattedName);
        System.out.println("Formatted name: " + formattedName);

        umdRenamerLogic.rename(umd, cubeFileFolder, formattedName);
        assertFalse((new File(cubeFilePath)).exists());
        assertTrue(umd.getFile().exists());
        System.out.println("New file: " + umd.getFile().getPath());
    }
}
