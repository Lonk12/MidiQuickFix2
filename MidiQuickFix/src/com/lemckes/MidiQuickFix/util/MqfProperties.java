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
package com.lemckes.MidiQuickFix.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/**
 *
 */
public class MqfProperties
{
    static String mPropertiesFileName =
        System.getProperty("user.home")
        + System.getProperty("file.separator")
        + "MQF.properties";
    static Properties mProps = new Properties();

    public static final String LAST_PATH_KEY = "lastpath";

    public static String getProperty(String key) {
        return mProps.getProperty(key);
    }

    public static void setProperty(String key, String value) {
        mProps.setProperty(key, value);
    }

    public static void readProperties() {
        try {
            mProps.load(new FileReader(mPropertiesFileName));
        } catch (FileNotFoundException ex) {
            // Shame really
        } catch (IOException ex) {
            // oops
        }
    }

    public static void writeProperties() {
        try {
            mProps.store(new FileWriter(mPropertiesFileName),
                "MidiQuickFix properties");
        } catch (FileNotFoundException ex) {
            // Shame really
        } catch (IOException ex) {
            // oops
        }
    }
}
