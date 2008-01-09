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
import java.util.HashMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

/**
 * Handle Midi Meta events.
 * @version $Id$
 */
public class MetaEvent {

    static java.text.DecimalFormat twoDigitFormat = new java.text.DecimalFormat("00");
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
    public static final int TEXT = 0x01; //FF 01 len TEXT (arbitrary TEXT)
    public static final int COPYRIGHT = 0x02; //FF 02 len TEXT
    public static final int TRACK_NAME = 0x03; //FF 03 len TEXT
    public static final int INSTRUMENT = 0x04; //FF 04 len TEXT
    public static final int LYRIC = 0x05; //FF 05 len TEXT
    public static final int MARKER = 0x06; //FF 06 len TEXT (e.g. Loop point)
    public static final int CUE_POINT = 0x07; //FF 07 len TEXT (e.g. .wav file name)
    public static final int PROGRAM_NAME = 0x08; //FF 08 len TEXT (PIANO, FLUTE, ...)
    public static final int DEVICE_NAME = 0x09; //FF 09 len TEXT (MIDI Out 1, MIDI Out 2)
    public static final int END_OF_TRACK = 0x2f; //FF 2F 00
    public static final int TEMPO = 0x51; //FF 51 03 tt tt tt microseconds
    public static final int SMPTE_OFFSET = 0x54; //FF 54 05 hr mn se fr ff
    public static final int TIME_SIGNATURE = 0x58; //FF 58 04 nn dd cc bb
    // nn=numerator, dd=denominator (2^dd), cc=MIDI clocks/metronome click
    // bb=no. of notated 32nd notes per MIDI quarter note (24 MIDI clocks).
    // No I don't understand that last one.
    // 06 03 18 08 is 6/8 time, 24 clocks/metronome, 8 1/32ndnotes/1/4note
    public static final int KEY_SIGNATURE = 0x59; //FF 59 02 sf mi
    // -sf=no. of flats +sf=no. of sharps mi=0=major mi=1=minor
    public static final int PROPRIETARY_DATA = 0x7f; //FF 7F len data
    private static HashMap<String, Integer> mTypeNameToValue;

    static {
        mTypeNameToValue = new HashMap<String, Integer>();
        mTypeNameToValue.put("SEQUENCE_NUMBER", 0x00);
        mTypeNameToValue.put("TEXT", 0x01);
        mTypeNameToValue.put("COPYRIGHT", 0x02);
        mTypeNameToValue.put("TRACK_NAME", 0x03);
        mTypeNameToValue.put("INSTRUMENT", 0x04);
        mTypeNameToValue.put("LYRIC", 0x05);
        mTypeNameToValue.put("MARKER", 0x06);
        mTypeNameToValue.put("CUE_POINT", 0x07);
        mTypeNameToValue.put("PROGRAM_NAME", 0x08);
        mTypeNameToValue.put("DEVICE_NAME", 0x09);
        mTypeNameToValue.put("END_OF_TRACK", 0x2f);
        mTypeNameToValue.put("TEMPO", 0x51);
        mTypeNameToValue.put("SMPTE_OFFSET", 0x54);
        mTypeNameToValue.put("TIME_SIGNATURE", 0x58);
        mTypeNameToValue.put("KEY_SIGNATURE", 0x59);
        mTypeNameToValue.put("PROPRIETARY_DATA", 0x7f);
    }

    /**
     * Get the list of META event type names
     * @return the list of META event type names
     */
    public static String[] getTypeNames() {
        return typeNames;
    }

    /**
     * Get the values that represent the given META event.
     * The returned array consists of <ol>
     *    <li>the event name as a String</li>
     *    <li>the length of the event data as an Integer</li>
     *    <li>the event data as a String</li>
     * </ol>
     * @param mess the META message to format
     * @return the representation of the message
     */
    public static Object[] getMetaStrings(MetaMessage mess) {
        boolean dumpText = false;
        boolean dumpBytes = false;

        int type = mess.getType();
        byte[] data = mess.getData();

        // The returned Object array
        // { type name, length, value string }
        Object[] result = {"M:", null, ""};
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
                result[2] =
                        twoDigitFormat.format(data[0] & 0x00ff) + ":" +
                        twoDigitFormat.format(data[1] & 0x00ff) + ":" +
                        twoDigitFormat.format(data[2] & 0x00ff) + ":" +
                        twoDigitFormat.format(data[3] & 0x00ff) + ":" +
                        twoDigitFormat.format(data[4] & 0x00ff);
                break;
            case TIME_SIGNATURE:
                result[0] = "M:TimeSignature";
                int nn = (data[0] & 0x00ff);
                int dd = (int) (java.lang.Math.pow(2, (data[1] & 0x00ff)));
                int cc = (data[2] & 0x00ff);
                int bb = (data[3] & 0x00ff);
                result[2] = nn + "/" + dd + " " + cc + "Metr. " + bb + "N/q";
                //result[2] = nn + "/" + dd;
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
                    chars[k] = (char) b;
                } else {
                    chars[k] = '.';
                }
            }
            result[2] = new String(chars);
        }

        if (dumpBytes) {
            result[2] = "";
            for (int k = 0; k < data.length; ++k) {
                int i = data[k] & 0x00ff;
                result[2] = result[2] + "0x" + Integer.toHexString(i) + " ";
            }
        }
        return result;
    }

    // Methods to handle TEMPO events.
    /**
     * Convert the given microsecond period to BeatsPerMinute
     * @param data 3 bytes of data that specifiy the microsecond period.
     * Calculated as <br>
     * <code>data[0] << 16 + data[1] << 8 + data [2]</code>
     * @return the BeatsPerMinute equivalent to the given microsecond period
     */
    public static int microSecsToBpm(byte[] data) {
        // Coerce the bytes into ints
        int[] ints = new int[3];
        ints[0] = data[0] & 0x00ff;
        ints[1] = data[1] & 0x00ff;
        ints[2] = data[2] & 0x00ff;

        long t = ints[0] << 16;
        t += ints[1] << 8;
        t += ints[2];

        return (int) (60000000 / t);
    }

    /**
     * Convert the given BeatsPerMinute to a microsecond period
     * @param bpm the BeatsPerMinute to convert
     * @return the microsecond period as described for {@see microSecsToBpm}
     */
    public static byte[] bpmToMicroSecs(int bpm) {
        long t = 60000000 / bpm;
        byte[] data = new byte[3];
        data[0] = (byte) ((t & 0xff0000) >> 16);
        data[1] = (byte) ((t & 0xff00) >> 8);
        data[2] = (byte) ((t & 0xff));
        return data;
    }

    /**
     * Parse a tempo string with an optional 'bpm' suffix e.g. 88bpm
     * @param tempoString the string to parse
     * @return the integer part of the string or 60 if the string does not
     * represent a valid integer (with optional 'bpm' suffix)
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
        } catch (NumberFormatException nfe) {
        // DO NOTHING - just use the default
        }
        return t;
    }

    /**
     * Parse a time signature string in the format nn[/dd]
     * nn=numerator, dd=denominator
     * If only nn is given then dd defaults to 4
     * @param timeSigString the string to parse
     * @param ticksPerBeat used to calculate the metronome click
     * @return the data for the event in a byte[]
     */
    public static byte[] parseTimeSignature(String timeSigString, int ticksPerBeat) {
        String[] parts = timeSigString.split("/");
        // default to 4/4 
        byte[] result = {4, 2, (byte)(ticksPerBeat / 4), 8};
        switch (parts.length) {
            case 0:
                break;
            case 1:
                // Assume beats
                result[0] = safeParseByte(parts[0]);
                break;
            case 2:
                // Beats per bar / Beat note duration
                result[0] = safeParseByte(parts[0]);
                byte dur = safeParseByte(parts[1]);
                double log2 = Math.log(dur) / Math.log(2);
                result[1] = (byte) Math.round(log2);
                // One metronome click per beat
                result[2] = (byte)(ticksPerBeat / dur);

                break;
        }
        return result;
    }

    /**
     * Parse an SMPTE offset string with an optional 'bpm' suffix e.g. 88bpm
     * @param tempoString the string to parse
     * @return the integer part of the string or 60 if the string does not
     * represent a valid integer (with optional 'bpm' suffix)
     */
    public static byte[] parseSMPTEOffset(String smpteString) {
        String[] parts = smpteString.split(":");
        byte[] result = {0, 0, 0, 0, 0};
        switch (parts.length) {
            case 0:
                break;
            case 1:
                // Assume secs
                result[2] = safeParseByte(parts[0]);
                break;
            case 2:
                // Assume mins secs
                result[1] = safeParseByte(parts[0]);
                result[2] = safeParseByte(parts[1]);
                break;
            case 3:
                // Assume hrs mins secs
                result[0] = safeParseByte(parts[0]);
                result[1] = safeParseByte(parts[1]);
                result[2] = safeParseByte(parts[2]);
                break;
            case 4:
                // Assume hrs mins secs frames
                result[0] = safeParseByte(parts[0]);
                result[1] = safeParseByte(parts[1]);
                result[2] = safeParseByte(parts[2]);
                result[3] = safeParseByte(parts[3]);
                break;
            case 5:
                // must be hrs mins secs frames fields
                result[0] = safeParseByte(parts[0]);
                result[1] = safeParseByte(parts[1]);
                result[2] = safeParseByte(parts[2]);
                result[3] = safeParseByte(parts[3]);
                result[4] = safeParseByte(parts[4]);
                break;
        }
        return result;
    }

    /**
     * test if the message data should be treated as a string
     * @param mess the messaage to test
     * @return <code>true</code> if the message data should be
     * represented as a string
     */
    public static boolean isText(MetaMessage mess) {
        int type = mess.getType();
        return (type >= 1 && type <= 9);
    }

    /**
     * test if the message data can be edited in the track editor
     * @param mess the message to test
     * @return <code>true</code> if the message data can be edited
     */
    public static boolean isEditable(MetaMessage mess) {
        int type = mess.getType();
        return ((type >= 1 && type <= 9) || type == TEMPO || type == KEY_SIGNATURE);
    }

    /**
     * Update the data content of the message
     * @param mess the message to update
     * @param s a String representation of the data.<br>
     * This can be just a String for text type messages,
     * a TEMPO value, a KEY_SIGNATURE value or a space separated
     * list of byte value strings such as "0x04".
     */
    public static void setMetaData(MetaMessage mess, String s, int ticksPerBeat) {
        byte[] data = null;
        int type = mess.getType();
        int len = mess.getData().length; // Beware of variable length messages!
        if (isText(mess)) {
            len = s.length();
            data = new byte[len];
            for (int i = 0; i < len; ++i) {
                data[i] = (byte) s.charAt(i);
            }
        } else if (type == TEMPO) {
            int bpm = parseTempo(s);
            data = bpmToMicroSecs(bpm);
            len = data.length;
        } else if (type == TIME_SIGNATURE) {
            data = parseTimeSignature(s, ticksPerBeat);
            len = data.length;
        } else if (type == KEY_SIGNATURE) {
            data = KeySignatures.getKeyValues(s);
            len = data.length;
        } else if (type == SMPTE_OFFSET) {
            data = parseSMPTEOffset(s);
            len = data.length;
        } else {
            // treat the string as a space separated list of
            // string representations of byte values
            // Should handle decimal, hexadecimal and octal representations
            // using the java.lang.Byte.decode() method
            String[] strings = s.split("\\p{Space}");
            data = new byte[strings.length];
            len = strings.length;
            for (int i = 0; i < len; ++i) {
                try {
                    data[i] = Byte.decode(strings[i]);
                } catch (NumberFormatException nfe) {
                    data[i] = 0;
                }
            }
        }

        if (data != null) {
            try {
                mess.setMessage(type, data, len);
            } catch (InvalidMidiDataException e) {
                TraceDialog.addTrace(
                        "Error: MetaEvent.setMetaData(" + s + ") " + e.getMessage());
            }
        }
    }

    /**
     * Create a Midi Meta event
     * @param type the type of the event as defined by the array returned from getTypeNames()
     * @param data the data for the event. This can be a string, for text events such as LYRIC
     * a TEMPO, a KEY_SIGNATURE or a space-separated list of values that are parsed into
     * a <code>byte[]</code> using <code>Byte.decode()</code>
     * @param tick the position of the event in the sequence
     * @return the created Midi Meta event
     * @throws javax.sound.midi.InvalidMidiDataException if the MetaMessage.setMessage()
     * parameters are not valid
     */
    public static MidiEvent createMetaEvent(
            String type, String data, long tick, int ticksPerBeat)
            throws InvalidMidiDataException {
        MetaMessage mm = new MetaMessage();
        mm.setMessage(mTypeNameToValue.get(type), null, 0);
        setMetaData(mm, data, ticksPerBeat);
        MidiEvent ev = new MidiEvent(mm, tick);
        return ev;
    }

    private static byte safeParseByte(String s) {
        return safeParseByte(s, (byte) 0);
    }

    private static byte safeParseByte(String s, byte defVal) {
        byte t = defVal;
        // Default value is 60bpm
        try {
            t = Byte.parseByte(s);
        } catch (NumberFormatException nfe) {
        // DO NOTHING - just use the default
        }
        return t;
    }
}
