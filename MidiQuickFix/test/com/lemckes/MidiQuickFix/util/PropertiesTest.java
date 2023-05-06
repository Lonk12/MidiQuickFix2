package com.lemckes.MidiQuickFix.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import junit.framework.TestCase;

/**
 *
 * @author john
 */
public class PropertiesTest
    extends TestCase
{

    private static final String PROPS_FILE = "/tmp/props";
    private static final Properties mProps = new Properties();
    
    public PropertiesTest() {
    }

    public PropertiesTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        //mProps.setProperty("date.yesterday", "v\u010Dera");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testProperties() {
        try {
            mProps.load(new FileReader(PROPS_FILE));
            String v = mProps.getProperty("date.yesterday");
            System.out.println("Value = |" + v + "|");
            v = mProps.getProperty("date.today");
            System.out.println("Value = |" + v + "|");
        } catch (FileNotFoundException ex) {
            // Shame really
        } catch (IOException ex) {
            // oops
        }
//    }
//
//    public static void testWriteProperties() {
        try {
            mProps.store(new FileWriter(PROPS_FILE),
                "MidiQuickFix properties"); //NOI18N
        } catch (FileNotFoundException ex) {
            // Shame really
        } catch (IOException ex) {
            // oops
        }
    }
}
