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
 * Utilities to return formatted strings for various types of values.
 * @version $Id$
 */
public final class Formats {
    
    /**
     * Format the given ticks into a beats:ticks string.
     * @param ticks the number of ticks
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     * @param longFormat if true the 'beats' part is formatted with at least 5 digits
     */
    public static String formatTicks(long ticks, int ticksPerBeat, boolean longFormat) {
        java.text.DecimalFormat beatF;
        if (longFormat) {
            beatF = new java.text.DecimalFormat("00000");
        } else {
            beatF = new java.text.DecimalFormat("0");
        }
        java.text.DecimalFormat tickF = new java.text.DecimalFormat("000");
        long beat = 0;
        long tick = 0;
        if (ticksPerBeat > 0) {
            beat = ticks / ticksPerBeat;
            tick = ticks % ticksPerBeat;
        }
        return beatF.format(beat) + ":" + tickF.format(tick);
    }
    
    /**
     * Convert a string in beats:ticks format to its value as a number of ticks
     * @param tickString the beats:ticks string
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     * @return the number of ticks represented by the tickString
     * or -1 if the string cannot be parsed
     */
    public static int parseTicks(String tickString, int ticksPerBeat) {
        int r;
        String ticks = "X";
        String beats = "X";
        try {
            int colonPos = tickString.indexOf(':');
            if (colonPos == -1 ||
              (colonPos == tickString.length() - 1 &&
              tickString.length() > 1)) {
                // No colon found or found at end; parse the string as beats
                ticks = tickString.substring(colonPos + 1);
                beats = "0";
            } else if (colonPos == 0 && tickString.length() > 1) {
                // Colon at start; parse as ticks
                ticks = tickString.substring(colonPos + 1);
                beats = "0";
            } else {
                // Something each side of the colon; parse both
                ticks = tickString.substring(colonPos + 1);
                beats = tickString.substring(0, colonPos);
            }
            int b = Integer.parseInt(beats);
            int t = Integer.parseInt(ticks);
            r = b * ticksPerBeat + t;
        } catch(NumberFormatException nfe) {
            r = -1;
        }
        return r;
    }
    
    /**
     * Format the given ticks into a beats string.
     * @param ticks the number of ticks
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     */
    public static String formatBeats(long ticks, int ticksPerBeat) {
        java.text.DecimalFormat beatF = new java.text.DecimalFormat("0");
        long beat = 0;
        if (ticksPerBeat > 0) {
            beat = ticks / ticksPerBeat;
        }
        return beatF.format(beat);
    }
    
    /**
     * Format the given number of seconds into a minutes:seconds string.
     * @parm seconds the number of seconds to format
     */
    public static String formatSeconds(long seconds) {
        java.text.DecimalFormat minF = new java.text.DecimalFormat("0");
        java.text.DecimalFormat secF = new java.text.DecimalFormat("00");
        long secs = seconds % 60;
        long mins = seconds / 60;
        return minF.format(mins) + ":" + secF.format(secs);
    }
}
