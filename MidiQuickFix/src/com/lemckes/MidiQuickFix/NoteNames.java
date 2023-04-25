/** ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2023 John Lemcke
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
 ************************************************************* */
package com.lemckes.MidiQuickFix;

/**
 * Convert between Midi note numbers and textual note names.
 * todo This probably should be i18n'd.
 */
class NoteNames {

    static final double[] limits = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
    static final String[] sharpNames = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    }; // NOI18N
    static final String[] flatNames = {
        "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"
    }; // NOI18N
    static final String[] bothNames = {
        "C", "Db/C#", "D", "Eb/D#", "E", "F", "Gb/F#", "G", "Ab/G#", "A",
        "Bb/A#", "B"
    }; // NOI18N
    static java.text.ChoiceFormat cf =
        new java.text.ChoiceFormat(limits, sharpNames);

    static String getNoteName(int note, boolean flats) {

        if (flats) {
            cf.setChoices(limits, flatNames);
        } else {
            cf.setChoices(limits, sharpNames);
        }
        return cf.format(note % 12) + (note / 12);
    }

    static String getBothNoteNames(int note) {

        cf.setChoices(limits, bothNames);
        return cf.format(note % 12) + (note / 12);
    }

    public static int getNoteNumber(String name) {
        int octave =
            Integer.parseInt(name.substring(name.length() - 1, name.length()));
        int noteNum = octave * 12;
        String note = name.substring(0, name.length() - 1);
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

    public static String[] getFlatsNoteNamesArray() {
        return flatNames;
    }

    public static String[] getSharpNoteNamesArray() {
        return sharpNames;
    }

    public static String[] getBothNoteNamesArray() {
        return bothNames;
    }
}
