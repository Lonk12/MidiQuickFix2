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

package com.lemckes.MidiQuickFix;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;

/**
 * Handle Midi Short events.
 * @version $Id$
 */
class ShortEvent {
    static Object[] getShortStrings(ShortMessage mess, boolean inFlats) {
        // Event, Note, Value, Patch, Text, Channel
        Object[] result = { "", "", null, "", "", null }; // NOI18N
        int st = mess.getStatus();
        //int len = mess.getLength();
        int d1 = mess.getData1();
        int d2 = mess.getData2();
        
        if ((st & 0xf0) <= 0xf0) { // This is a channel message
            int cmd = mess.getCommand();
            switch (cmd) {
                case ShortMessage.CHANNEL_PRESSURE:
                    result[0] = "CHANNEL_PRESSURE"; // NOI18N
                    result[2] = "" + d1;
                    break;
                case ShortMessage.CONTROL_CHANGE:
                    result[0] = Controllers.getControlName(d1);
                    result[2] = Controllers.getControlValue(d2, false);
                    break;
                case ShortMessage.NOTE_OFF:
                    result[0] = "NOTE_OFF"; // NOI18N
                    result[1] = NoteNames.getNoteName(d1, inFlats);
                    result[2] = Integer.valueOf(d2);
                    break;
                case ShortMessage.NOTE_ON:
                    result[0] = "NOTE_ON "; // NOI18N
                    result[1] = NoteNames.getNoteName(d1, inFlats);
                    result[2] = Integer.valueOf(d2);
                    break;
                case ShortMessage.PITCH_BEND:
                    result[0] = "PITCH_BEND"; // NOI18N
                    result[2] = Integer.valueOf(d1 + (d2 << 7));
                    break;
                case ShortMessage.POLY_PRESSURE:
                    result[0] = "POLY_PRESSURE"; // NOI18N
                    result[1] = NoteNames.getNoteName(d1, inFlats);
                    result[2] = Integer.valueOf(d2);
                    break;
                case ShortMessage.PROGRAM_CHANGE:
                    result[0] = "PATCH "; // NOI18N
                    result[3] = InstrumentNames.getName(d2, d1);
                    break;
                default:
                    result[0] = "UNKNOWN"; // NOI18N
            }
            int chan = mess.getChannel();
            result[5] = Integer.valueOf(chan);
        } else { // This is a system message
            switch (st) {
                case ShortMessage.ACTIVE_SENSING:
                    result[0] = "ACTIVE_SENSING"; // NOI18N
                    break;
                case ShortMessage.CONTINUE:
                    result[0] = "CONTINUE"; // NOI18N
                    break;
                case ShortMessage.END_OF_EXCLUSIVE:
                    result[0] = "END_OF_EXCLUSIVE"; // NOI18N
                    break;
                case ShortMessage.MIDI_TIME_CODE:
                    result[0] = "MIDI_TIME_CODE"; // NOI18N
                    break;
                case ShortMessage.SONG_POSITION_POINTER:
                    result[0] = "SONG_POSITION_POINTER"; // NOI18N
                    result[2]  = Integer.valueOf(d1 + (d2 << 7));
                    break;
                case ShortMessage.SONG_SELECT:
                    result[0] = "SONG_SELECT"; // NOI18N
                    result[2] = "" + d1;
                    break;
                case ShortMessage.START:
                    result[0] = "START"; // NOI18N
                    break;
                case ShortMessage.STOP:
                    result[0] = "STOP"; // NOI18N
                    break;
                case ShortMessage.SYSTEM_RESET:
                    result[0] = "RESET"; // NOI18N
                    break;
                case ShortMessage.TIMING_CLOCK:
                    result[0] = "TIMING_CLOCK"; // NOI18N
                    break;
                case ShortMessage.TUNE_REQUEST:
                    result[0] = "TUNE_REQUEST"; // NOI18N
                    break;
                default:
                    result[0] = "UNDEFINED"; // NOI18N
            }
        }
        return result;
    }
    
    public static MidiEvent createShortEvent(
        int status, long tick)
        throws InvalidMidiDataException {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(status);
        MidiEvent ev = new MidiEvent(sm, tick);
        return ev;
    }
    
    public static MidiEvent createShortEvent(
        int status, int d1, int d2, long tick)
        throws InvalidMidiDataException {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(status, d1, d2);
        MidiEvent ev = new MidiEvent(sm, tick);
        return ev;
    }
    
    public static MidiEvent createShortEvent(
        int status, int channel, int d1, int d2, long tick)
        throws InvalidMidiDataException {
        ShortMessage sm = new ShortMessage();
        sm.setMessage(status, channel, d1, d2);
        MidiEvent ev = new MidiEvent(sm, tick);
        return ev;
    }
}
