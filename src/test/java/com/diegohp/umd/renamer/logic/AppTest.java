package com.diegohp.umd.renamer.logic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 *
 * @author wolf003
 * @version $Id: $Id
 * @since 1.0.2
 */
public class AppTest
        extends TestCase {

    /**
     * Create the test case
     *
     * @param testName name of the test case
     * @since 1.0.2
     */
    public AppTest(String testName) {
        super(testName);
    }

    /**
     * Suite test.
     *
     * @return the suite of tests being tested
     * @since 1.0.2
     */
    public static Test suite() {
        return new TestSuite(AppTest.class);
    }

    /**
     * Rigorous Test :-)
     *
     * @since 1.0.2
     */
    public void testApp() {
        assertTrue(true);
    }
}
