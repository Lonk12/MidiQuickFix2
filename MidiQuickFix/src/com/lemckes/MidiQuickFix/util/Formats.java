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

import java.text.DecimalFormat;

/**
 * Utilities to return formatted strings for various types of values.
 * @version $Id$
 */
public final class Formats {
    public static final String TICK_BEAT_RE = "\\p{Digit}*:\\p{Digit}{0,3}";
    public static final String MIN_SEC_RE = "\\p{Digit}*:\\p{Digit}{0,2}";
    private static final DecimalFormat beatFormatShort =
        new java.text.DecimalFormat("0");
    private static final DecimalFormat beatFormatLong =
        new java.text.DecimalFormat("00000");
    private static final DecimalFormat tickFormat =
        new java.text.DecimalFormat("000");
    private static final DecimalFormat minFormat =
        new java.text.DecimalFormat("0");
    private static final DecimalFormat secFormat =
        new java.text.DecimalFormat("00");

    /**
     * Format the given ticks into a beats:ticks string.
     * @param ticks the number of ticks
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     * @param longFormat if true the 'beats' part is formatted with at least 5 digits
     * @return a string representing the ticks in beats:ticks format
     */
    public static String formatTicks(long ticks, int ticksPerBeat,
                                       boolean longFormat) {
        DecimalFormat beatF;
        if (longFormat) {
            beatF = beatFormatLong;
        } else {
            beatF = beatFormatShort;
        }
        long beat = 0;
        long tick = 0;
        if (ticksPerBeat > 0) {
            beat = ticks / ticksPerBeat;
            tick = ticks % ticksPerBeat;
        }
        return beatF.format(beat) + ":" + tickFormat.format(tick);
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
        } catch (NumberFormatException nfe) {
            r = -1;
        }
        return r;
    }

    /**
     * Format the given ticks into a beats string.
     * @param ticks the number of ticks
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     * @return a string representing the beats part of the ticks value
     */
    public static String formatBeats(long ticks, int ticksPerBeat) {
        long beat = 0;
        if (ticksPerBeat > 0) {
            beat = ticks / ticksPerBeat;
        }
        return beatFormatShort.format(beat);
    }

    /**
     * Format the given number of seconds into a minutes:seconds string.
     * @param seconds the number of seconds to format
     * @return a string representing the seconds in mins:secs format
     */
    public static String formatSeconds(long seconds) {
        long secs = seconds % 60;
        long mins = seconds / 60;
        return minFormat.format(mins) + ":" + secFormat.format(secs);
    }
}
