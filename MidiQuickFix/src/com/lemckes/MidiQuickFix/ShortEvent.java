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

import javax.sound.midi.*;
import javax.sound.midi.ShortMessage.*;

/**
 * Handle Midi Short events.
 * @version $Id$
 */
class ShortEvent {
    static Object[] getShortStrings(ShortMessage mess, boolean inFlats) {
        // Event, Note, Value, Patch, Text, Channel
        Object[] result = { "", "", null, "", "", null };
        int st = mess.getStatus();
        int len = mess.getLength();
        int d1 = mess.getData1();
        int d2 = mess.getData2();
        
        if ((st & 0xf0) <= 0xf0) { // This is a channel message
            int cmd = mess.getCommand();
            switch (cmd) {
                case javax.sound.midi.ShortMessage.CHANNEL_PRESSURE:
                    result[0] = "CHANNEL_PRESSURE";
                    result[2] = "" + d1;
                    break;
                case javax.sound.midi.ShortMessage.CONTROL_CHANGE:
                    //result[0] = "CONTROL_CHANGE";
                    result[0] = Controls.getControlName(d1);
                    result[2] = Controls.getControlValue(d2, false);
                    break;
                case javax.sound.midi.ShortMessage.NOTE_OFF:
                    result[0] = "NOTE_OFF";
                    result[1] = NoteNames.getNoteName(d1, inFlats);
                    result[2] = new Integer(d2);
                    break;
                case javax.sound.midi.ShortMessage.NOTE_ON:
                    result[0] = "NOTE_ON ";
                    result[1] = NoteNames.getNoteName(d1, inFlats);
                    result[2] = new Integer(d2);
                    break;
                case javax.sound.midi.ShortMessage.PITCH_BEND:
                    result[0] = "PITCH_BEND";
                    result[2] = new Integer(d1 + (d2 << 7));
                    break;
                case javax.sound.midi.ShortMessage.POLY_PRESSURE:
                    result[0] = "POLY_PRESSURE";
                    result[1] = NoteNames.getNoteName(d1, inFlats);
                    result[2] = new Integer(d2);
                    break;
                case javax.sound.midi.ShortMessage.PROGRAM_CHANGE:
                    result[0] = "PATCH ";
                    result[3] = InstrumentNames.getName(d1);
                    break;
                default:
                    result[0] = "UNKNOWN";
            }
            int chan = mess.getChannel();
            result[5] = new Integer(chan);
        } else { // This is a system message
            switch (st) {
                case javax.sound.midi.ShortMessage.ACTIVE_SENSING:
                    result[0] = "ACTIVE_SENSING";
                    break;
                case javax.sound.midi.ShortMessage.CONTINUE:
                    result[0] = "CONTINUE";
                    break;
                case javax.sound.midi.ShortMessage.END_OF_EXCLUSIVE:
                    result[0] = "END_OF_EXCLUSIVE";
                    break;
                case javax.sound.midi.ShortMessage.MIDI_TIME_CODE:
                    result[0] = "MIDI_TIME_CODE";
                    break;
                case javax.sound.midi.ShortMessage.SONG_POSITION_POINTER:
                    result[0] = "SONG_POSITION_POINTER";
                    result[2]  = new Integer(d1 + (d2 << 7));
                    break;
                case javax.sound.midi.ShortMessage.SONG_SELECT:
                    result[0] = "SONG_SELECT";
                    result[2] = "" + d1;
                    break;
                case javax.sound.midi.ShortMessage.START:
                    result[0] = "START";
                    break;
                case javax.sound.midi.ShortMessage.STOP:
                    result[0] = "STOP";
                    break;
                case javax.sound.midi.ShortMessage.SYSTEM_RESET:
                    result[0] = "RESET";
                    break;
                case javax.sound.midi.ShortMessage.TIMING_CLOCK:
                    result[0] = "TIMING_CLOCK";
                    break;
                case javax.sound.midi.ShortMessage.TUNE_REQUEST:
                    result[0] = "TUNE_REQUEST";
                    break;
                default:
                    result[0] = "UNDEFINED";
            }
        }
        return result;
    }
}
