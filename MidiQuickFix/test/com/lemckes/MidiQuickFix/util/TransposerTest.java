/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.lemckes.MidiQuickFix.util;

import com.lemckes.MidiQuickFix.KeySignatures;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author john
 */
public class TransposerTest {

    public TransposerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }


    /**
     * Test of main method, of class Transposer.
     */
    @Test
    public void testAdjustKeySig() {
        System.out.println("adjustKeySig - Transpose down 3 semitones");
        // Test the key sig conversion
        byte[] data = new byte[2];
        data[1] = 0;
        for (byte sig = -7; sig < 8; ++sig) {
            data[0] = sig;
            System.out.print("From " + KeySignatures.getKeyName(data) + " to ");
            data[0] = Transposer.adjustKeySig(sig, -3);
            System.out.println(KeySignatures.getKeyName(data));
        }
    }

}