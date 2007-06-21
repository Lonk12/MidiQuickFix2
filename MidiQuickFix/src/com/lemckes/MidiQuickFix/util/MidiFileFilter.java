/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2005 John Lemcke
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

/**
 * A FileFilter to select just midi files (.mid and .kar)
 * @version $Id$
 */
public class MidiFileFilter extends javax.swing.filechooser.FileFilter {
    /**
     * Check if the given file matches the acceptable files.
     * @param f The file to check
     * @return True if the file is either *.mid or *.kar.
     */
    public boolean accept(java.io.File f) {
        boolean acc = false;
        if (f.isDirectory()) {
            acc = true;
        } else {
            String ext = null;
            String s = f.getName();
            int i = s.lastIndexOf('.');
            if (i > 0 && i < s.length() - 1) {
                ext = s.substring(i + 1).toLowerCase();
            }
            if (ext != null) {
                if (ext.equals("mid") || ext.equals("kar")) {
                    acc = true;
                }
            }
        }
        return acc;
    }
    
    /**
     * Get the description to display in the FileChooser.
     * @return The description of this filter
     */
    public String getDescription() {
        return UiStrings.getString("midi_file_filter_description");
    }
}

