/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2009 John Lemcke
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

import com.lemckes.MidiQuickFix.util.StringConverter;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;

/**
 * Handle Midi Meta events.
 *
 * @version $Id$
 */
public class MetaEvent
{

    private static java.text.DecimalFormat twoDigitFormat =
            new java.text.DecimalFormat("00"); // NOI18N
    private static String[] typeNames = {
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
    /**
     * nn=numerator, dd=denominator (2^dd), cc=MIDI clocks/metronome click
     * bb=no. of notated 32nd notes per MIDI quarter note.
     * There are 24 MIDI clocks per quarter note.
     * No I don't understand that last one but it will almost certainly be 8.
     * 06 03 24 08 is 6/8 time, 36 clocks/metronome (2 per bar), 8 1/32nd notes
     * / 1/4note
     */
    public static final int TIME_SIGNATURE = 0x58; //FF 58 04 nn dd cc bb
    public static final int KEY_SIGNATURE = 0x59; //FF 59 02 sf mi
    // -sf=no. of flats +sf=no. of sharps mi=0=major mi=1=minor
    public static final int PROPRIETARY_DATA = 0x7f; //FF 7F len data
    private static final HashMap<String, Integer> mTypeNameToValue;

    static {
        mTypeNameToValue = new HashMap<>(20);
        mTypeNameToValue.put("SEQUENCE_NUMBER", 0x00); // NOI18N
        mTypeNameToValue.put("TEXT", 0x01); // NOI18N
        mTypeNameToValue.put("COPYRIGHT", 0x02); // NOI18N
        mTypeNameToValue.put("TRACK_NAME", 0x03); // NOI18N
        mTypeNameToValue.put("INSTRUMENT", 0x04); // NOI18N
        mTypeNameToValue.put("LYRIC", 0x05); // NOI18N
        mTypeNameToValue.put("MARKER", 0x06); // NOI18N
        mTypeNameToValue.put("CUE_POINT", 0x07); // NOI18N
        mTypeNameToValue.put("PROGRAM_NAME", 0x08); // NOI18N
        mTypeNameToValue.put("DEVICE_NAME", 0x09); // NOI18N
        mTypeNameToValue.put("END_OF_TRACK", 0x2f); // NOI18N
        mTypeNameToValue.put("TEMPO", 0x51); // NOI18N
        mTypeNameToValue.put("SMPTE_OFFSET", 0x54); // NOI18N
        mTypeNameToValue.put("TIME_SIGNATURE", 0x58); // NOI18N
        mTypeNameToValue.put("KEY_SIGNATURE", 0x59); // NOI18N
        mTypeNameToValue.put("PROPRIETARY_DATA", 0x7f); // NOI18N
    }

    /**
     * Get the list of META event type names
     *
     * @return the list of META event type names
     */
    public static String[] getTypeNames() {
        return typeNames;
    }

    /**
     * Get the values that represent the given META event.
     * The returned array consists of <ol>
     * <li>the event name as a String</li>
     * <li>the length of the event data as an Integer</li>
     * <li>the event data as a String</li>
     * </ol>
     *
     * @param mess the META message to format
     * @return the representation of the message
     */
    public static Object[] getMetaStrings(MetaMessage mess) {
        // Some data is String some is a series of bytes others are neither
        boolean dumpText = false;
        boolean dumpBytes = false;

        int type = mess.getType();
        byte[] data = mess.getData();

        // The returned Object array
        // { type name, length, value string }
        Object[] result = {"M:", null, ""}; // NOI18N
        result[1] = Integer.valueOf(data.length);

        switch (type) {
            case SEQUENCE_NUMBER:
                result[0] = "M:SequenceNumber"; // NOI18N
                dumpBytes = true;
                break;
            case TEXT:
                result[0] = "M:Text"; // NOI18N
                dumpText = true;
                break;
            case COPYRIGHT:
                result[0] = "M:Copyright"; // NOI18N
                dumpText = true;
                break;
            case TRACK_NAME:
                result[0] = "M:TrackName"; // NOI18N
                dumpText = true;
                break;
            case INSTRUMENT:
                result[0] = "M:Instrument"; // NOI18N
                dumpText = true;
                break;
            case LYRIC:
                result[0] = "M:Lyric"; // NOI18N
                dumpText = true;
                break;
            case MARKER:
                result[0] = "M:Marker"; // NOI18N
                dumpText = true;
                break;
            case CUE_POINT:
                result[0] = "M:CuePoint"; // NOI18N
                dumpText = true;
                break;
            case PROGRAM_NAME:
                result[0] = "M:ProgramName"; // NOI18N
                dumpText = true;
                break;
            case DEVICE_NAME:
                result[0] = "M:DeviceName"; // NOI18N
                dumpText = true;
                break;
            case SMPTE_OFFSET:
                result[0] = "M:SMPTEOffset"; // NOI18N
                // Hour, Minute, Second, Frame, Field
                // hr mn se fr ff
                result[2] =
                        twoDigitFormat.format(data[0] & 0x00ff) + ":" + // NOI18N
                        twoDigitFormat.format(data[1] & 0x00ff) + ":" + // NOI18N
                        twoDigitFormat.format(data[2] & 0x00ff) + ":" + // NOI18N
                        twoDigitFormat.format(data[3] & 0x00ff) + ":" + // NOI18N
                        twoDigitFormat.format(data[4] & 0x00ff);
                break;
            case TIME_SIGNATURE:
                result[0] = "M:TimeSignature"; // NOI18N
                int nn = (data[0] & 0x00ff);
                int dd = (int)(java.lang.Math.pow(2, (data[1] & 0x00ff)));
                int cc = (data[2] & 0x00ff);
                int bb = (data[3] & 0x00ff);
                result[2] = nn + "/" + dd + " " + cc + "Metr. " + bb + "N/q"; // NOI18N
                //result[2] = nn + "/" + dd;
                break;
            case KEY_SIGNATURE:
                result[0] = "M:KeySignature"; // NOI18N
                result[2] = KeySignatures.getKeyName(data);
                break;
            case TEMPO:
                result[0] = "M:Tempo"; // NOI18N
                int bpm = microSecsToBpm(data);
                //result[2] = bpm + "bpm"; // NOI18N
                result[2] = Integer.toString(bpm);
                break;
            case END_OF_TRACK:
                result[0] = "M:EndOfTrack"; // NOI18N
                break;
            case PROPRIETARY_DATA:
                result[0] = "M:ProprietaryData"; // NOI18N
                dumpBytes = true;
                break;
            default:
                result[0] = "M:" + type; // NOI18N
                dumpBytes = true;
        }

        if (dumpText) {
            try {
                result[2] = StringConverter.convertBytesToString(data);
            } catch (UnsupportedEncodingException ex) {
                result[2] = "Unsupported Character Encoding";
            }
        }

        if (dumpBytes) {
            StringBuilder sb = new StringBuilder(data.length * 6);
            for (int k = 0; k < data.length; ++k) {
                int i = data[k] & 0x00ff;
                if (i > 0) {
                    sb.append(" "); // NOI18N
                }
                sb.append("0x"); // NOI18N
                sb.append(Integer.toHexString(i)); // NOI18N
            }
            result[2] = sb.toString();
        }
        return result;
    }

    // Methods to handle TEMPO events.
    /**
     * Convert the given microsecond period to BeatsPerMinute
     *
     * @param data 3 bytes of data that specify the microsecond period.
     * Calculated as <br>
     * <code>data[0] &lt;&lt; 16 + data[1] &lt;&lt; 8 + data[2]</code>
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

        return (int)(60000000 / t);
    }

    /**
     * Convert the given BeatsPerMinute to a microsecond period
     *
     * @param bpm the BeatsPerMinute to convert
     * @return 3 bytes of data that specify the microsecond period.
     * Calculated as <br>
     * <code>data[0] &lt;&lt; 16 + data[1] &lt;&lt; 8 + data [2]</code>
     */
    public static byte[] bpmToMicroSecs(int bpm) {
        long t = 60000000 / bpm;
        byte[] data = new byte[3];
        data[0] = (byte)((t & 0xff0000) >> 16);
        data[1] = (byte)((t & 0xff00) >> 8);
        data[2] = (byte)((t & 0xff));
        return data;
    }

    /**
     * Parse a tempo string with an optional 'bpm' suffix e.g. 88bpm
     *
     * @param tempoString the string to parse
     * @return the integer part of the string or 60 if the string does not
     * represent a valid integer (with optional 'bpm' suffix)
     */
    public static int parseTempo(String tempoString) {
        int bpmPos = tempoString.toLowerCase().indexOf("bpm"); // NOI18N

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
     *
     * @param timeSigString the string to parse
     * @param ticksPerBeat used to calculate the metronome click
     * @return the data for the event in a byte[]
     */
    public static byte[] parseTimeSignature(String timeSigString,
            int ticksPerBeat) {
        String[] parts = timeSigString.split("/"); // NOI18N
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
                result[0] = getBeatsPerBar(parts[0]);
                result[1] = getBeatValue(parts[1]);
                result[2] = getUsefulClocksPerMetronome(parts[0], parts[1]);
                break;
            case 3:

        }
        return result;
    }

    private static byte getBeatsPerBar(String beats) {
        return safeParseByte(beats);
    }

    private static byte getBeatValue(String value) {
        byte beatValue = safeParseByte(value);
        double log2 = Math.log(beatValue) / Math.log(2);
        return (byte)Math.round(log2);
    }

    private static byte getUsefulClocksPerMetronome(String beats, String value) {
        byte beatsPerBar = getBeatsPerBar(beats);
        byte beatValue = getBeatValue(value);
        // Try to generate a useful metronome click
        // How many MIDI clocks are there in each beat
        // (there are 24 MIDI clocks in a quarter note)
        int clocksPerBeat = (24 * 4) / beatValue;
        int clocksPerMetronome = clocksPerBeat;
        if (beatsPerBar > 0) {
            if (beatsPerBar % 4 == 0) {
                clocksPerMetronome = clocksPerBeat * beatsPerBar / 4;
            } else if (beatsPerBar % 3 == 0) {
                clocksPerMetronome = clocksPerBeat * beatsPerBar / 3;
            } else if (beatsPerBar % 2 == 0) {
                clocksPerMetronome = clocksPerBeat * beatsPerBar / 2;
            }
        }
        return (byte)clocksPerMetronome;
    }

    /**
     * Parse an SMPTE offset string in the form
     * "hours:minutes:seconds:frames:fields"
     *
     * @param smpteString the string to parse
     * @return a byte array with elements representing
     * hours, minutes, seconds, frames, fields
     */
    public static byte[] parseSMPTEOffset(String smpteString) {
        String[] parts = smpteString.split(":"); // NOI18N
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
     *
     * @param mess the message to test
     * @return <code>true</code> if the message data should be
     * represented as a string
     */
    public static boolean isText(MetaMessage mess) {
        int type = mess.getType();
        return (type >= 1 && type <= 9);
    }

    /**
     * test if the message data can be edited in the track editor
     *
     * @param mess the message to test
     * @return <code>true</code> if the message data can be edited
     */
    public static boolean isEditable(MetaMessage mess) {
        int type = mess.getType();
        return ((type >= 1 && type <= 9) || type == TEMPO || type == KEY_SIGNATURE);
    }

    /**
     * Update the data content of the message
     *
     * @param mess the message to update
     * @param value a String that represents the data for the event.<br>
     * This is parsed into a <code>byte[]</code> that becomes the data of
     * the MetaMessage.<br>
     * Most events treat the string as a text value and just convert
     * each character to a <code>byte</code> in the array but the
     * following message types are handled specially
     * <dl>
     * <dt>TEMPO</dt>
     * <dd>an integer value with an optional "bpm" suffix. e.g. 120bpm</dd>
     * <dt>SMPTE_OFFSET</dt>
     * <dd>a string in the form <code>h:m:s:f:d</code> where<br>
     * h=hours m=minutes s=seconds f=frames d=fields<br>
     * If fewer than 5 values are given then the parser treats them as<br>
     * 1. <b>s</b><br>
     * 2. <b>m:s</b><br>
     * 3. <b>h:m:s</b><br>
     * 4. <b>h:m:s:f</b><br>
     * 5. <b>h:m:s:f:d</b><br>
     * and the unspecified values are set to zero.
     * </dd>
     * <dt>TIME_SIGNATURE</dt>
     * <dd>a time signature string in the format <code>n[/d]</code> where<br>
     * n=numerator d=denominator<br>
     * If only <code>n</code> is given then <code>d</code> defaults to 4</dd>
     * <dt>KEY_SIGNATURE</dt>
     * <dd>one of the following key signature strings<br>
     * <b>Cb Gb Db Ab Eb Bb F C G D A E B F# C#</b/></dd>
     * <dt>SEQUENCE_NUMBER and PROPRIETARY_DATA</dt>
     * <dd>a space-separated list of values that are parsed into
     * a <code>byte[]</code> using <code>Byte.decode()</code><br>
     * If any value cannot be parsed into a <code>byte</code>
     * then it is treated as zero</dd>
     * </dl>
     * @param ticksPerBeat the tick resolution of the sequence
     */
    public static void setMetaData(MetaMessage mess, String value,
            int ticksPerBeat) {
        byte[] data;
        int type = mess.getType();
        if (isText(mess)) {
            try {
                data = StringConverter.convertStringToBytes(value);
            } catch (UnsupportedEncodingException ex) {
                // Use the system default encoding
                data = value.getBytes();
            }
        } else if (type == TEMPO) {
            int bpm = parseTempo(value);
            data = bpmToMicroSecs(bpm);
        } else if (type == TIME_SIGNATURE) {
            data = parseTimeSignature(value, ticksPerBeat);
        } else if (type == KEY_SIGNATURE) {
            data = KeySignatures.getKeyValues(value);
        } else if (type == SMPTE_OFFSET) {
            data = parseSMPTEOffset(value);
        } else {
            // treat the string as a space separated list of
            // string representations of byte values
            // Should handle decimal, hexadecimal and octal representations
            // using the java.lang.Byte.decode() method
            String[] strings = value.split("\\p{Space}"); // NOI18N
            int len = strings.length;
            data = new byte[len];
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
                mess.setMessage(type, data, data.length);
            } catch (InvalidMidiDataException e) {
                TraceDialog.addTrace(
                        "Error: MetaEvent.setMetaData(" + value + ") " + e.
                        getMessage()); // NOI18N
            }
        }
    }

    /**
     * Create a Midi Meta event
     *
     * @param type the type of the event as defined by the array returned
     * from getTypeNames()
     * @param data a String that represents the data for the event.<br>
     * see {
     * @see #setMetaData} for details.
     * @param tick the position of the event in the sequence
     * @param ticksPerBeat the tick resolution of the sequence
     * @return the created Midi Meta event
     * @throws javax.sound.midi.InvalidMidiDataException if the
     * MetaMessage.setMessage() parameters are not valid
     */
    public static MidiEvent createMetaEvent(String type, String data,
            long tick, int ticksPerBeat)
            throws InvalidMidiDataException {
        MetaMessage mm = new MetaMessage();
        mm.setMessage(mTypeNameToValue.get(type), null, 0);
        setMetaData(mm, data, ticksPerBeat);
        MidiEvent ev = new MidiEvent(mm, tick);
        return ev;
    }

    private static byte safeParseByte(String s) {
        return safeParseByte(s, (byte)0);
    }

    private static byte safeParseByte(String s, byte defVal) {
        byte t = defVal;
        try {
            t = Byte.parseByte(s);
        } catch (NumberFormatException nfe) {
            // DO NOTHING - just use the default
        }
        return t;
    }
}
