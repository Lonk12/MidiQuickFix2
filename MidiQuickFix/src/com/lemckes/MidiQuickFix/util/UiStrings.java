/**************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2018 John Lemcke
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
 **************************************************************/
package com.lemckes.MidiQuickFix.util;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Provide access to the UIStrings resources bundle.
 */
public class UiStrings
{

    static ResourceBundle mStringBundle;

    static {
        mStringBundle = java.util.ResourceBundle.getBundle(
                "com/lemckes/MidiQuickFix/resources/UIStrings");
    }

    static public String getString(String key) {
        String value = key;
        try {
            value = mStringBundle.getString(key);
        } catch (MissingResourceException mre) {
            TraceDialog.addTrace("No UIString found for " + key);
        }
        return value;
    }
}
