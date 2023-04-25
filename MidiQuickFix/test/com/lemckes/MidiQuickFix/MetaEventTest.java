/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2023 John Lemcke
 * jostle@users.sourceforge.net
 *
 * This program is free software; you can redistribute it
 * and/or modify it under the terms of the Artistic License
 * as published by Larry Wall, either version 2.0,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Artistic License for more details.
 *
 * You should have received a copy of the Artistic License with this Kit,
 * in the file named "Artistic.clarified".
 * If not, I'll be glad to provide one.
 *
 *************************************************************
 */
package com.lemckes.MidiQuickFix;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author john
 */
public class MetaEventTest
    extends TestCase
{

    public MetaEventTest(String testName) {
        super(testName);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() throws Exception {
    }

    @After
    @Override
    public void tearDown() throws Exception {
    }

    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite
            = new junit.framework.TestSuite(MetaEventTest.class);

        return suite;
    }

    /**
     * Test of tempo conversion methods, of class
     * com.lemckes.MidiQuickFix.MetaEvent.
     */
    @Test
    public void testTempoConversions() {
        System.out.println("testTempoConversions");

        String bpmString;
        int bpmExpect;
        // Test conversion from bpm to microseconds and back again.
        // Select some random bpms in a typical range
        for (int i = 0; i < 50; ++i) {
            bpmExpect = 37 + (int)(Math.random() * 300);
            bpmString = bpmExpect + "bpm";

            byte[] data = MetaEvent.bpmToMicroSecs(bpmExpect);
            int bpm2 = MetaEvent.microSecsToBpm(data);
//            System.out.println(bpmExpect + " = {"
//                + (data[0] & 0x00ff) + ", "
//                + (data[1] & 0x00ff) + ", "
//                + (data[2] & 0x00ff) + "} = " + bpm2);

            assertEquals(bpmExpect, bpm2);
        }
    }

    /**
     * Test of metaDataToText method, of class MetaEvent.
     */
    @Test
    public void testMetaDataToText() {
        System.out.println("metaDataToText");
        byte[] data = {0x30, 0x31, 0x32, 0x41, 0x42, 0x43};
        String expResult = "012ABC";
        String result = MetaEvent.metaDataToText(data);
        assertEquals(expResult, result);
    }

    /**
     * Test of metaDataToHexBytesString method, of class MetaEvent.
     */
    @Test
    public void testMetaDataToHexBytesString() {
        System.out.println("metaDataToHexBytesString");
        byte[] data = {0x30, 0x31, 0x32, 0x41, 0x42, 0x43};
        String expResult = "0x30 0x31 0x32 0x41 0x42 0x43";
        String result = MetaEvent.metaDataToHexBytesString(data);
        assertEquals(expResult, result);
    }

    /**
     * Test of metaDataToXfChordString method, of class MetaEvent.
     */
    @Test
    public void testMetaDataToXfChordString() {
        System.out.println("metaDataToXfChordString");
        byte[] data = {0x43, 0x7b, 0x01, 0x31, 0x00, 0x51, 0x00};
        String expResult = "C/C##";
        String result = MetaEvent.metaDataToXfChordString(data);
        assertEquals(expResult, result);

        data = new byte[]{0x43, 0x7b, 0x01, 0x32, 0x10, 0x7f, 0x00};
        expResult = "D minMaj7(9)";
        result = MetaEvent.metaDataToXfChordString(data);
        assertEquals(expResult, result);
    }

    /**
     * Test of xfChordToMetaData method, of class MetaEvent.
     */
    @Test
    public void testXfChordToMetaData() {
        System.out.println("xfChordToMetaData");
        String chordNoteName = "C";
        String chordTypeName = "Maj";
        String bassNoteName = "C";
        String bassTypeName = "Maj";
        String expResult = "0x43 0x7b 0x1 0x31 0x0 0x31 0x0";
        String result = MetaEvent.xfChordToMetaData(chordNoteName, chordTypeName, bassNoteName, bassTypeName);
        assertEquals(expResult, result);

        chordNoteName = "D";
        chordTypeName = "minMaj7(9)";
        bassNoteName = "None";
        bassTypeName = "Maj";
        expResult = "0x43 0x7b 0x1 0x32 0x10 0x7f 0x0";
        result = MetaEvent.xfChordToMetaData(chordNoteName, chordTypeName, bassNoteName, bassTypeName);
        assertEquals(expResult, result);

        chordNoteName = "Cbbb";
        chordTypeName = "min";
        bassNoteName = "None";
        bassTypeName = "Maj";
        expResult = "0x43 0x7b 0x1 0x1 0x8 0x7f 0x0";
        result = MetaEvent.xfChordToMetaData(chordNoteName, chordTypeName, bassNoteName, bassTypeName);
        assertEquals(expResult, result);

        chordNoteName = "E#";
        chordTypeName = "Maj";
        bassNoteName = "None";
        bassTypeName = "Maj";
        expResult = "0x43 0x7b 0x1 0x43 0x0 0x7f 0x0";
        result = MetaEvent.xfChordToMetaData(chordNoteName, chordTypeName, bassNoteName, bassTypeName);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseTempo method, of class MetaEvent.
     */
    @Test
    public void testParseTempo() {
        System.out.println("parseTempo");
        String tempoString = "361bpm";
        int expResult = 361;
        int result = MetaEvent.parseTempo(tempoString);
        assertEquals(expResult, result);

        tempoString = "361";
        expResult = 361;
        result = MetaEvent.parseTempo(tempoString);
        assertEquals(expResult, result);

        tempoString = "howdsl361bpm";
        expResult = 60;
        result = MetaEvent.parseTempo(tempoString);
        assertEquals(expResult, result);
    }

    /**
     * Test of parseTimeSignature method, of class MetaEvent.
     */
    @Test
    public void testParseTimeSignature() {
        System.out.println("parseTimeSignature");
        String timeSigString = "3/4";
        int ticksPerBeat = 96;
        byte[] expResult = {3, 2, 48, 8};
        byte[] result = MetaEvent.parseTimeSignature(timeSigString, ticksPerBeat);
        assertArrayEquals(expResult, result);

        timeSigString = "9/8";
        ticksPerBeat = 96;
        expResult = new byte[]{9, 3, 96, 8};
        result = MetaEvent.parseTimeSignature(timeSigString, ticksPerBeat);
        assertArrayEquals(expResult, result);

        timeSigString = "2/2";
        ticksPerBeat = 96;
        expResult = new byte[]{2, 1, 96, 8};
        result = MetaEvent.parseTimeSignature(timeSigString, ticksPerBeat);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of parseSMPTEOffset method, of class MetaEvent.
     */
    @Test
    public void testParseSMPTEOffset() {
        System.out.println("parseSMPTEOffset");
        String smpteString = "1:2:3:4:5";
        byte[] expResult = {1, 2, 3, 4, 5};
        byte[] result = MetaEvent.parseSMPTEOffset(smpteString);
        assertArrayEquals(expResult, result);
    }

}
