/*
 * The Artistic License
 *
 * Copyright (C) 2004-2023 john.
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

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author john
 */
public class FontSizerTest
{

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    public FontSizerTest() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of scaleFont method, of class FontSizer.
     */
    @Test
    public void testScaleFont() {
        System.out.println("scaleFont");
        MqfProperties.readProperties();
        try {
            String lafName = MqfProperties.getStringProperty(
                MqfProperties.LOOK_AND_FEEL_NAME, "Nimbus");
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (lafName.equals(info.getName())) {
                    System.out.println(info.getClassName());
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // lafName is not available, use the default look and feel.
        }

        float scale = 2.0f;
        FontSizer.scaleFont(scale);
        System.out.println("####################################################");
        FontSizer.scaleFont(scale);

    }

}
