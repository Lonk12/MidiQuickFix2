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

import com.lemckes.MidiQuickFix.util.TraceDialog;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;

/**
 * Handle Midi Meta events.
 * @version $Id$
 */
public class MetaEvent {
    static String[] typeNames = {
        "SEQUENCE_NUMBER",
        "TEXT",
        "COPYRIGHT",
        "TRACK_NAME",
        "INSTRUMENT",
        "LYRIC",
        "MARKER",
        "CUE_POINT",
        "PROGRAM_NAME",
        "DEVICE_NAME",
        "END_OF_TRACK",
        "TEMPO",
        "SMPTE_OFFSET",
        "TIME_SIGNATURE",
        "KEY_SIGNATURE",
        "PROPRIETARY_DATA"
    };
    
    // META event types
    public static final int SEQUENCE_NUMBER = 0x00; //FF 00 02 ss ss or FF 00 00
    public static final int TEXT            = 0x01; //FF 01 len TEXT (arbitrary TEXT)
    public static final int COPYRIGHT       = 0x02; //FF 02 len TEXT
    public static final int TRACK_NAME      = 0x03; //FF 03 len TEXT
    public static final int INSTRUMENT      = 0x04; //FF 04 len TEXT
    public static final int LYRIC           = 0x05; //FF 05 len TEXT
    public static final int MARKER          = 0x06; //FF 06 len TEXT (e.g. Loop point)
    public static final int CUE_POINT       = 0x07; //FF 07 len TEXT (e.g. .wav file name)
    public static final int PROGRAM_NAME    = 0x08; //FF 08 len TEXT (PIANO, FLUTE, ...)
    public static final int DEVICE_NAME     = 0x09; //FF 09 len TEXT (MIDI Out 1, MIDI Out 2)
    public static final int END_OF_TRACK    = 0x2f; //FF 2F 00
    public static final int TEMPO           = 0x51; //FF 51 03 tt tt tt microseconds
    public static final int SMPTE_OFFSET    = 0x54; //FF 54 05 hr mn se fr ff
    public static final int TIME_SIGNATURE  = 0x58; //FF 58 04 nn dd cc bb
    // nn=numerator, dd=denominator (2^dd), cc=MIDI clocks/metronome click
    // bb=no. of notated 32nd notes per MIDI quarter note (24 MIDI clocks).
    // No I don't understand that last one.
    // 06 03 18 08 is 6/8 time, 24 clocks/metronome, 8 1/32ndnotes/1/4note
    
    public static final int KEY_SIGNATURE   = 0x59; //FF 59 02 sf mi
    // -sf=no. of flats +sf=no. of sharps mi=0=major mi=1=minor
    
    public static final int PROPRIETARY_DATA = 0x7f; //FF 7F len data
    
    public static String[] getNameArray() {
        return typeNames;
    }
    
    public static Object[] getMetaStrings(MetaMessage mess) {
        boolean dumpText = false;
        boolean dumpBytes = false;
        
        int type = mess.getType();
        // int len = mess.getLength();
        byte[] data = mess.getData();
        
        // The returned Object array
        // { type name, length, value string }
        Object[] result = { "M:", null, "" };
        result[1] = Integer.valueOf(data.length);
        
        switch (type) {
            case SEQUENCE_NUMBER:
                result[0] = "M:SequenceNumber";
                dumpBytes = true;
                break;
            case TEXT:
                result[0] = "M:Text";
                dumpText = true;
                break;
            case COPYRIGHT:
                result[0] = "M:Copyright";
                dumpText = true;
                break;
            case TRACK_NAME:
                result[0] = "M:TrackName";
                dumpText = true;
                break;
            case INSTRUMENT:
                result[0] = "M:Instrument";
                dumpText = true;
                break;
            case LYRIC:
                result[0] = "M:Lyric";
                dumpText = true;
                break;
            case MARKER:
                result[0] = "M:Marker";
                dumpText = true;
                break;
            case CUE_POINT:
                result[0] = "M:CuePoint";
                dumpText = true;
                break;
            case PROGRAM_NAME:
                result[0] = "M:ProgramName";
                dumpText = true;
                break;
            case DEVICE_NAME:
                result[0] = "M:DeviceName";
                dumpText = true;
                break;
            case SMPTE_OFFSET:
                result[0] = "M:SMPTEOffset";
                //hr mn se fr ff
                result[2] = (data[0] & 0x00ff)
                + ":" + (data[1] & 0x00ff)
                + ":" + (data[2] & 0x00ff)
                + ":" + (data[3] & 0x00ff)
                + ":" + (data[4] & 0x00ff);
                break;
            case TIME_SIGNATURE:
                result[0] = "M:TimeSignature";
                int nn =  (data[0] & 0x00ff);
                int dd =  (int)(java.lang.Math.pow(2, (data[1] & 0x00ff)));
                int cc =  (data[2] & 0x00ff);
                int bb =  (data[3] & 0x00ff);
                //result[2] = nn + "/" + dd + " " + cc + "Metr. " + bb + "N/q";
                result[2] = nn + "/" + dd;
                break;
            case KEY_SIGNATURE:
                result[0] = "M:KeySignature";
                result[2] = KeySignatures.getKeyName(data);
                break;
            case TEMPO:
                result[0] = "M:Tempo";
                int bpm = microSecsToBpm(data);
                //result[2] = bpm + "bpm";
                result[2] = Integer.toString(bpm);
                break;
            case END_OF_TRACK:
                result[0] = "M:EndOfTrack";
                break;
            case PROPRIETARY_DATA:
                result[0] = "M:ProprietaryData";
                dumpBytes = true;
                break;
            default:
                result[0] = "" + type;
                dumpBytes = true;
        }
        
        if (dumpText) {
            char chars[] = new char[data.length];
            for (int k = 0; k < data.length; ++k) {
                byte b = data[k];
                if (b > 31 && b < 128) {
                    // Printable character.
                    chars[k] = (char)b;
                } else {
                    chars[k] = '.';
                }
            }
            result[2] = new String(chars);
        }
        
        if (dumpBytes) {
            for (int k = 0; k < data.length; ++k) {
                int i = data[k] & 0x00ff;
                result[2] = "0x" + Integer.toHexString(i) + " ";
            }
        }
        return result;
    }
    
    // Methods to handle TEMPO events.
    
    public static int microSecsToBpm(byte[] data) {
        // Coerce the bytes into ints
        int[] ints = new int[3];
        ints[0] = data[0] & 0x00ff;
        ints[1] = data[1] & 0x00ff;
        ints[2] = data[2] & 0x00ff;
        
        long t = ints[0] << 16;
        t += ints[1] << 8;
        t += ints[2];
        
        return (int)(60000000 / t);
    }
    
    public static byte[] bpmToMicroSecs(int bpm) {
        long t = 60000000 / bpm;
        byte[] data = new byte[3];
        data[0] = (byte)((t & 0xff0000) >> 16);
        data[1] = (byte)((t & 0xff00) >> 8);
        data[2] = (byte)((t & 0xff));
        return data;
    }
    
    /**
     * Parse a tempo string e.g. 88bpm
     * @param tempoString 
     * @return 
     */
    public static int parseTempo(String tempoString) {
        int bpmPos = tempoString.toLowerCase().indexOf("bpm");
        
        // Default value is 60bpm
        int t = 60;
        if (bpmPos != -1) {
            tempoString = tempoString.substring(0, bpmPos);
        }
        try {
            t = Integer.parseInt(tempoString);
        } catch(NumberFormatException nfe) {
            // DO NOTHING - just use the default
        }
        return t;
    }
    
    public static boolean isText(MetaMessage mess) {
        int type = mess.getType();
        return (type >= 1 && type <= 9);
    }
    
    public static boolean isEditable(MetaMessage mess) {
        int type = mess.getType();
        return ((type >= 1 && type <= 9) || type == TEMPO || type == KEY_SIGNATURE);
    }
    
    public static void setMetaData(MetaMessage mess, String s) {
        byte[] data = null;
        int type = mess.getType();
        int len = mess.getData().length; // Beware of variable length messages!
        if (isText(mess)) {
            len = s.length();
            data = new byte[len];
            for (int i = 0; i < len; ++i) {
                data[i] = (byte)s.charAt(i);
            }
        } else if (type == TEMPO) {
            int bpm = parseTempo(s);
            data = bpmToMicroSecs(bpm);
        } else if (type == KEY_SIGNATURE) {
            data = KeySignatures.getKeyValues(s);
        }
        
        if (data != null) {
            try {
                mess.setMessage(type, data, len);
            } catch(InvalidMidiDataException e) {
                TraceDialog.addTrace(
                    "Error: MetaEvent.setMetaData(" + s + ") " + e.getMessage());
            }
        }
    }
}
