/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.lemckes.MidiQuickFix.util;

import java.nio.charset.Charset;
import java.util.Map.Entry;
import java.util.SortedMap;
import junit.framework.TestCase;

/**
 *
 * @author john
 */
public class StringConverterTest extends TestCase
{

    private String s = "Aa Æ©ÜĀƀḀⱠ꜠﹐ԱႠἀ፳亼人鰱 zZabc\u5639\u563b";
//        String requestedCharsetName = "LATIN";
//        String requestedCharsetName = "JP";
//        String requestedCharsetName = "X-UTF-32BE-BOM";
        String requestedCharsetName = "UTF-8";
//        String requestedCharsetName = "UTF-16";
    byte[] bytes;

    public StringConverterTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        bytes = StringConverter.convertStringToBytes(s);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSetCharsetName() throws Exception {
        boolean ok = StringConverter.setCharsetName(requestedCharsetName);
        System.out.println(requestedCharsetName
            + " becomes " + StringConverter.getCharsetName());
        assertTrue(requestedCharsetName +" is not a supported charSet", ok);
    }

    public void testAvailableCharsets() throws Exception {
        System.out.println("testAvailableCharsets");
        SortedMap<String, Charset> available = Charset.availableCharsets();
        for (Entry<String, Charset> e : available.entrySet()) {
            System.out.println(e.getKey() + " = " + e.getValue().toString());
        }
        System.out.println("Default = " + Charset.defaultCharset());
    }

    /**
     * Test of convertBytesToString method, of class StringConverter.
     */
    public void testConvertBytesToString() throws Exception {
        System.out.println("convertBytesToString");
        String result = StringConverter.convertBytesToString(bytes);
        System.out.println(result);
    }

    /**
     * Test of convertStringToBytes method, of class StringConverter.
     */
    public void testConvertRoundTrip() throws Exception {
        System.out.println("convertStringToBytes");
        byte[] result = StringConverter.convertStringToBytes(s);
        assertEquals(bytes.length, result.length);
        String backAgain = StringConverter.convertBytesToString(result);
        System.out.println("Original  :" + s + ":");
        System.out.println("Converted :" + backAgain + ":");
        System.out.println("s.getBytes");
        for (byte b : s.getBytes()) {
            System.out.print(" " + Integer.toHexString(b & 0xff));
        }
        System.out.println("");
        System.out.println("bytes");
        for (byte b : bytes) {
            System.out.print(" " + Integer.toHexString(b & 0xff));
        }
        System.out.println("");
        System.out.println("StringConverter.convertStringToBytes(s)");
        for (byte b : result) {
            System.out.print(" " + Integer.toHexString(b & 0xff));
        }
        System.out.println("");
    }
}
