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

import java.awt.Font;
import java.util.Set;
import javax.swing.UIDefaults;
import javax.swing.UIManager;

/**
 *
 * @author john
 */
public class FontSizer
{

    public static void scaleFont(float scale) {
        UIDefaults uid = UIManager.getLookAndFeelDefaults();
        Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
        System.out.println("uid="+uid.size()+"\n keyset="+keySet.size());
        UIDefaults uid2 = UIManager.getDefaults();
        Set<Object> keySet2 = UIManager.getDefaults().keySet();
        System.out.println("uid2="+uid2.size()+"\n keyset2="+keySet2.size());
        var keys = keySet.toArray(Object[]::new);
        for (Object key : keys) {
//            System.out.println("Key : " + key.toString());
//            if (key != null && key.toString().toLowerCase().contains("font")) {
            if (key != null) {
                Font font = uid.getFont(key);
//                Font font = UIManager.getDefaults().getFont(key);
                if (font != null) {
                    float defaultSize = font.getSize2D();
                    float size = defaultSize * scale;
                    String family = font.getFamily();
                    System.out.println(key + ":     \t family = " + family + " \tfrom = " + defaultSize + ", \tto = " + size);
                    Font newFont = font.deriveFont(size);
                    UIManager.put(key, newFont);
                }
            }
        }
    }

//    public static void scaleFont(double scale) {
//
//        UIDefaults uidef = UIManager.getDefaults();
//        Set<Entry<Object, Object>> set = uidef.entrySet();
//        System.out.println("FontSizer.scaleFont() UIManager.getDefaults();");
//        for (Entry<Object, Object> e : set) {
//            Object val = e.getValue();
//            Object key = e.getKey();
////            if (key != null && key instanceof String && ((String)key).contains("font")) {
//            if (val != null && val instanceof FontUIResource) {
//                FontUIResource fui = (FontUIResource)val;
//                int newSize = (int)((double)fui.getSize() * scale);
//                System.out.println("Changing Font " + fui.getFontName() + "/" + fui.getStyle()
//                    + " from " + fui.getSize() + " to " + newSize);
//                uidef.put(e.getKey(), new FontUIResource(fui.getName(), fui.getStyle(), newSize));
//            }
//        }
//
//        UIDefaults uidef2 = UIManager.getLookAndFeelDefaults();
//        Set<Entry<Object, Object>> set2 = uidef2.entrySet();
//        System.out.println("FontSizer.scaleFont() UIManager.getLookAndFeelDefaults();");
//
//        for (Entry<Object, Object> e : set2) {
//            Object val = e.getValue();
//            Object key = e.getKey();
//            if (val != null && val instanceof FontUIResource) {
//                FontUIResource fui = (FontUIResource)val;
//                int newSize = (int)((double)fui.getSize() * scale);
//                System.out.println("Changing Font " + fui.getFontName() + "/" + fui.getStyle()
//                    + " from " + fui.getSize() + " to " + newSize);
//                uidef2.put(e.getKey(), new FontUIResource(fui.getName(), fui.getStyle(), newSize));
//            }
//        }
//    }
}
