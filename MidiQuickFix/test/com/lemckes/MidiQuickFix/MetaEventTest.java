/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2009 John Lemcke
 *   jostle@users.sourceforge.net
 *
 *   This program is free software; you can redistribute it
 *   and/or modify it under the terms of the Artistic License
 *   as published by Larry Wall, either version 2.0,
 *   or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   See the Artistic License for more details.
 *
 *   You should have received a copy of the Artistic License with this Kit,
 *   in the file named "Artistic.clarified".
 *   If not, I'll be glad to provide one.
 *
 **************************************************************/
package com.lemckes.MidiQuickFix;

import junit.framework.TestCase;

/**
 *
 * @author john
 */
public class MetaEventTest extends TestCase {
    
    public MetaEventTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws java.lang.Exception {
    }
    
    @Override
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite =
            new junit.framework.TestSuite(MetaEventTest.class);
        
        return suite;
    }
    
    /**
     * Test of tempo string parsing, of class com.lemckes.MidiQuickFix.MetaEvent.
     */
    public void testTempoParsing() {
        System.out.println("testTempoParsing");
        
        String bpmString = "361bpm";
        int bpmExpect = 361;
        
        // Test the parsing of a tempo string.
        int bpm1 = MetaEvent.parseTempo(bpmString);
        System.out.println("parseTempo(" + bpmString + ") gives " + bpm1);
        assertEquals(bpmExpect, bpm1);
        
    }
    
    /**
     * Test of tempo conversion methods, of class com.lemckes.MidiQuickFix.MetaEvent.
     */
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
            System.out.println(bpmExpect + " = {" +
                (data[0] & 0x00ff) + ", " +
                (data[1] & 0x00ff) + ", " +
                (data[2] & 0x00ff) + "} = " + bpm2);
            
            assertEquals(bpmExpect, bpm2);
        }
    }
    
    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    
}
