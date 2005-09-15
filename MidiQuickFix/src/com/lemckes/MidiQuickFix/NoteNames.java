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

package com.lemckes.MidiQuickFix;

/**
 * Convert between Midi note numbers and textual note names.
 * @todo This probably should be i18n'd.
 * @version $Id$
 */ 
class NoteNames {
    
    static double[] limits =
    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    static String[] sharpNames = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    static String[] flatNames = {
        "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"
    };
    
    static java.text.ChoiceFormat cf =
    new java.text.ChoiceFormat(limits, sharpNames);
    
    static String getNoteName(int note, boolean flats) {
        
        if (flats) {
            cf.setChoices(limits, flatNames);
        } else {
            cf.setChoices(limits, sharpNames);
        }
        return cf.format(note % 12) + (int)(note / 12);
    }
    
    public static int getNoteNumber(String name) {
        int octave = Integer.parseInt(name.substring(name.length() - 1, name.length()));
        int noteNum = octave * 12;
        String note = name.substring(0, name.length() - 1);
        //StringBuffer sb = new StringBuffer(note);
        //sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        // System.out.println("getNoteNumber name=" + name + " note=" + note + " oct=" + octave);
        boolean found = false;
        for (int i = 0; i < flatNames.length; ++i) {
            if (note.equalsIgnoreCase(flatNames[i])) {
                noteNum += i;
                found = true;
                break;
            }
        }
        if (!found) {
            for (int i = 0; i < sharpNames.length; ++i) {
                if (note.equalsIgnoreCase(sharpNames[i])) {
                    noteNum += i;
                    break;
                }
            }
        }
        
        return noteNum;
    }
}
