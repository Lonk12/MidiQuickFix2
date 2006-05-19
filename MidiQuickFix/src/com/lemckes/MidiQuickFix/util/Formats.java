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
public class Formats {
    
    /** Creates a new instance of Formats */
    public Formats() {
    }
    
    /**
     * Format the given ticks into a beats:ticks string.
     * @param ticks the number of ticks
     * @param ticksPerBeat the number of ticks per beat (a.k.a. resolution)
     */
    public static String formatTicks(long ticks, int ticksPerBeat) {
        java.text.DecimalFormat beatF = new java.text.DecimalFormat("0");
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
