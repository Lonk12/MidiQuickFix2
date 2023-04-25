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

import java.util.Map.Entry;
import java.util.Set;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/**
 *
 * @author john
 */
public class FontSizer
{

    public static void scaleFont(double scale) {

        UIDefaults uidef = UIManager.getDefaults();
        Set<Entry<Object, Object>> set = uidef.entrySet();

        for (Entry<Object, Object> e : set) {
            Object val = e.getValue();
            if (val != null && val instanceof FontUIResource) {
                FontUIResource fui = (FontUIResource)val;
                int newSize = (int)((double)fui.getSize() * scale);
                System.out.println("Changing Font " + fui.getFontName() + "/" + fui.getStyle()
                    + " from " + fui.getSize() + " to " + newSize);
                uidef.put(e.getKey(), new FontUIResource(fui.getName(), fui.getStyle(), newSize));
            }
        }

        UIDefaults uidef2 = UIManager.getLookAndFeelDefaults();
        Set<Entry<Object, Object>> set2 = uidef2.entrySet();

        for (Entry<Object, Object> e : set2) {
            Object val = e.getValue();
            if (val != null && val instanceof FontUIResource) {
                FontUIResource fui = (FontUIResource)val;
                int newSize = (int)((double)fui.getSize() * scale);
                System.out.println("Changing Font " + fui.getFontName() + "/" + fui.getStyle()
                    + " from " + fui.getSize() + " to " + newSize);
                uidef2.put(e.getKey(), new FontUIResource(fui.getName(), fui.getStyle(), newSize));
            }
        }
    }
}
