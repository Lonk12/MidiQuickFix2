/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2021 John Lemcke
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
 *************************************************************
 */
package com.lemckes.MidiQuickFix;

/**
 * Methods associated with key signatures.
 */
public class KeySignatures
{

    /**
     * The names of the Major key signatures.
     */
    static String[] majorKeyNames = {
        "Cb",
        "Gb",
        "Db",
        "Ab",
        "Eb",
        "Bb",
        "F",
        "C",
        "G",
        "D",
        "A",
        "E",
        "B",
        "F#",
        "C#"
    };
    /**
     * The names of the Minor key signatures.
     */
    static String[] minorKeyNames = {
        "Ab",
        "Eb",
        "Bb",
        "F",
        "C",
        "G",
        "D",
        "A",
        "E",
        "B",
        "F#",
        "C#",
        "G#",
        "D#",
        "A#"
    };

    /**
     * Get the name of the key.
     *
     * @return The String representation of the key signature.
     * e.g. "Ab or Dm"
     * @param data data[0] defines the number of sharps/ flats.<br>
     * Negative values represent flats;
     * Positive values represent sharps.
     * i.e. -7=7flats +7=7sharps.<br>
     * data[1] defines whether the key is major or minor; 0=major, 1=minor.
     */
    public static String getKeyName(byte data[]) {
        StringBuilder result = new StringBuilder(4);
        int k = data[0] + 7;
        int m = data[1];
        if (m == 1) {
            result.append(minorKeyNames[k]).append("m"); // NOI18N
        } else {
            result.append(majorKeyNames[k]);
        }
        return result.toString();
    }

    /**
     * Get the key and mode for the given string.
     *
     * @param keyName The String version of the key signature
     * as returned from getKeyName.
     * @return data[0] defines the number of sharps/ flats.<br>
     * Negative values represent flats;
     * Positive values represent sharps.
     * i.e. -7=7flats +7=7sharps.<br>
     * data[1] whether the key is major or minor; 0=major, 1=minor.
     */
    public static byte[] getKeyValues(String keyName) {
        byte[] result = {0, 0};
        // Check for a minor key
        int mPos = keyName.indexOf('m'); // NOI18N
        if (mPos != -1) {
            result[1] = 1;
            // and remove the trailing "m"
            keyName = keyName.substring(0, mPos);
        }

        String[] keyNames = result[1] == 1 ? minorKeyNames : majorKeyNames;

        for (byte i = 0; i < keyNames.length; ++i) {
            if (keyName.equals(keyNames[i])) {
                result[0] = (byte)(i - 7);
                break;
            }
        }
        return result;
    }

    /**
     * Check if the notes in the key should be displayed as flats or sharps.
     *
     * @param keyName The String version of the key signature
     * as returned by getKeyName.
     * @return True if the notes should be displayed as flats.
     */
    public static boolean isInFlats(String keyName) {
        if (keyName == null) {
            return false;
        }

        byte[] data = getKeyValues(keyName);
        return isInFlats(data[0]);
    }

    /**
     * Check if the notes in the key should be displayed as flats or sharps.
     *
     * @param keyNum The key number. Positive values define
     * the number of sharps in the key signature. Negative
     * values define the number of flats.
     * @return True if the notes should be displayed as flats.
     */
    public static boolean isInFlats(int keyNum) {
        return keyNum < 0;
    }

    private KeySignatures() {
    }
}
