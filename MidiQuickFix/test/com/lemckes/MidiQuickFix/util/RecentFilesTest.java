/*
 * The Artistic License
 *
 * Copyright (C) 2004-2023 John Lemcke
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
 */
package com.lemckes.MidiQuickFix.util;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author john
 */
public class RecentFilesTest
{

    static RecentFiles globalInstance;

    public RecentFilesTest() {
    }

    @BeforeClass
    public static void setUpClass() {
        globalInstance = new RecentFiles();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getFiles method, of class RecentFiles.
     */
    @Test
    public void testGetFiles() {
        System.out.println("getFiles");
        RecentFiles instance = new RecentFiles();
        List<String> expResult = new ArrayList<>(RecentFiles.MAX_ENTRIES + 1);
        List<String> result = instance.getFiles();
        assertEquals(expResult, result);
    }

    /**
     * Test of useFile method, of class RecentFiles.
     */
    @Test
    public void testUseFile() {
        System.out.println("useFile");
        String fileName = "/tmp/tmpUsedFile0.midi";
        globalInstance.useFile(fileName);
        assertTrue("/tmp/tmpUsedFile0.midi is in RecentFiles",
            globalInstance.getFiles().contains(fileName));

        // Use enough files to push out the oldest ones from the list
        for (int i = 1; i < RecentFiles.MAX_ENTRIES + 10; ++i) {
            String fn = "/tmp/tmpUsedFile" + i + ".midi";
            globalInstance.useFile(fn);
        }

        assertEquals(RecentFiles.MAX_ENTRIES, globalInstance.getFiles().size());

        List<String> expectedList = new ArrayList<>(RecentFiles.MAX_ENTRIES + 1);
        for (int j = RecentFiles.MAX_ENTRIES - 1; j >= 0; --j) {
            String fn = "/tmp/tmpUsedFile" + (j + 10) + ".midi";
            expectedList.add(fn);
        }
        assertEquals(expectedList, globalInstance.getFiles());

        // Reuse file number 14
        String reused = "/tmp/tmpUsedFile14.midi";
        globalInstance.useFile(reused);

        expectedList.remove(reused);
        expectedList.add(0, reused);

        assertEquals(expectedList, globalInstance.getFiles());
    }

    /**
     * Test of toPropertyString method, of class RecentFiles.
     */
    @Test
    public void testToPropertyString() {
        System.out.println("toPropertyString");
        String expResult = "/tmp/tmpUsedFile14.midi#/tmp/tmpUsedFile17.midi#"
            + "/tmp/tmpUsedFile16.midi#/tmp/tmpUsedFile15.midi#"
            + "/tmp/tmpUsedFile13.midi#/tmp/tmpUsedFile12.midi#"
            + "/tmp/tmpUsedFile11.midi#/tmp/tmpUsedFile10.midi";

        String result = globalInstance.toPropertyString();
        assertEquals(expResult, result);
    }

    /**
     * Test of fromPropertyString method, of class RecentFiles.
     */
    @Test
    public void testFromPropertyString() {
        System.out.println("fromPropertyString");
        String propertyString = "/tmp/tmpUsedFile17.midi#/tmp/tmpUsedFile16.midi#"
            + "/tmp/tmpUsedFile15.midi#/tmp/tmpUsedFile14.midi#"
            + "/tmp/tmpUsedFile13.midi#/tmp/tmpUsedFile12.midi#"
            + "/tmp/tmpUsedFile11.midi#/tmp/tmpUsedFile10.midi";

        RecentFiles fromString = new RecentFiles();
        fromString.fromPropertyString(propertyString);

        List<String> expectedList = new ArrayList<>(RecentFiles.MAX_ENTRIES + 1);
        for (int j = RecentFiles.MAX_ENTRIES - 1; j >= 0; --j) {
            String fn = "/tmp/tmpUsedFile" + (j + 10) + ".midi";
            expectedList.add(fn);
        }
        assertEquals(expectedList, fromString.getFiles());
    }

}
