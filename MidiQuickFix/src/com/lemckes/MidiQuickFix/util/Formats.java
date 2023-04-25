/**
 * ************************************************************
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
 *************************************************************
 */
package com.lemckes.MidiQuickFix.util;

import java.text.DecimalFormat;

/**
 * Utilities to return formatted strings for various types of values.
 */
public final class Formats
{

    public static final String BEATS_TICKS_RE = "\\p{Digit}*:\\p{Digit}{0,3}";
    public static final String MINS_SECS_RE = "\\p{Digit}*:\\p{Digit}{0,2}";
    public static final String BARS_BEATS_RE = "\\p{Digit}*:\\p{Digit}{0,2}";
    private static final DecimalFormat beatFormatShort
        = new java.text.DecimalFormat("0");
    private static final DecimalFormat beatFormatLong
        = new java.text.DecimalFormat("00000");
    private static final DecimalFormat tickFormat
        = new java.text.DecimalFormat("000");
    private static final DecimalFormat minFormat
        = new java.text.DecimalFormat("0");
    private static final DecimalFormat secFormat
        = new java.text.DecimalFormat("00");
    private static final DecimalFormat number_1_1_Format
        = new java.text.DecimalFormat("0.0");

    /**
     * Format the given ticks into a beats:ticks string.
     *
     * @param ticks the number of ticks
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     * @param longFormat if true the 'beats' part is formatted with at least 5
     * digits
     * @return a string representing the ticks in beats:ticks format
     */
    public static String formatBeatsTicks(long ticks, int ticksPerBeat,
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
     *
     * @param beatTickString the beats:ticks string
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     * @return the number of ticks represented by the tickString
     * or -1 if the string cannot be parsed
     */
    public static int parseBeatsTicks(String beatTickString, int ticksPerBeat) {
        int ticks;
        String tickString = "X";
        String beatString = "X";
        try {
            int colonPos = beatTickString.indexOf(':');
            if (colonPos == -1) {
                // No colon found; parse the string as beats
                tickString = "0";
                beatString = beatTickString;
            } else if (colonPos == beatTickString.length() - 1 && beatTickString.length() > 1) {
                // Colon found at end; parse the string as beats
                tickString = "0";
                beatString = beatTickString.substring(0, colonPos);
            } else if (colonPos == 0 && beatTickString.length() > 1) {
                // Colon at start; parse as ticks
                tickString = beatTickString.substring(colonPos + 1);
                beatString = "0";
            } else {
                // Something each side of the colon; parse both
                tickString = beatTickString.substring(colonPos + 1);
                beatString = beatTickString.substring(0, colonPos);
            }
            int b = Integer.parseInt(beatString);
            int t = Integer.parseInt(tickString);
            ticks = b * ticksPerBeat + t;
        } catch (NumberFormatException nfe) {
            ticks = -1;
        }
        return ticks;
    }

    /**
     * Format the given ticks into a bars:beats string.
     *
     * @param ticks the number of ticks
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     * @return a string representing the ticks in bars:beats format
     *
     * WARNING - This needs to be implemented properly to allow for
     * variable time signatures.
     */
    public static String formatBarsBeats(long ticks, int ticksPerBeat) {
        int beatsPerBar = 4;
        long bar = 1;
        long beat = 1;
        if (ticksPerBeat > 0) {
            beat = ticks / ticksPerBeat;
            bar = beat / beatsPerBar;
        }

        return beatFormatShort.format(bar) + ":" + tickFormat.format(beat);
    }

    /**
     * Convert a string in bars:beats format to its value as a number of ticks
     *
     * @param barBeatString the bar:beats string
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     * @return the number of ticks represented by the barBeatString
     * or -1 if the string cannot be parsed
     *
     * WARNING - This needs to be implemented properly to allow for
     * variable time signatures.
     */
    public static int parseBarsBeats(String barBeatString, int ticksPerBeat) {
        int beatsPerBar = 4;
        int ticks;
        String barString = "X";
        String beatString = "X";
        try {
            int colonPos = barBeatString.indexOf(':');
            if (colonPos == -1) {
                // No colon found; parse the string as bars
                beatString = "1";
                barString = barBeatString;
            } else if (colonPos == barBeatString.length() - 1 && barBeatString.length() > 1) {
                // Colon found at end; parse the string as bars
                beatString = "1";
                barString = barBeatString.substring(0, colonPos);
            } else if (colonPos == 0 && barBeatString.length() > 1) {
                // Colon at start; parse as beats
                beatString = barBeatString.substring(colonPos + 1);
                barString = "1";
            } else {
                // Something each side of the colon; parse both
                beatString = barBeatString.substring(colonPos + 1);
                barString = barBeatString.substring(0, colonPos);
            }
            int bars = Integer.parseInt(barString) - 1;
            int beats = Integer.parseInt(beatString) - 1;
            ticks = (bars * beatsPerBar + beats) * ticksPerBeat;
        } catch (NumberFormatException nfe) {
            ticks = -1;
        }
        return ticks;
    }

    /**
     * Format the given ticks into a beats string.
     *
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
     *
     * @param seconds the number of seconds to format
     * @return a string representing the seconds in mins:secs format
     */
    public static String formatSeconds(long seconds) {
        long secs = seconds % 60;
        long mins = seconds / 60;
        return minFormat.format(mins) + ":" + secFormat.format(secs);
    }

    /**
     * Format the given number decimal string with 1 leading zero and 1 decimal
     * place.
     *
     * @param value the value to format
     * @return a string representing the seconds in decimal format
     */
    public static String formatNumber_1_1(Number value) {
        return number_1_1_Format.format(value);
    }
}
