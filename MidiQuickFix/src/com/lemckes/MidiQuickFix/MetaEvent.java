package com.lemckes.MidiQuickFix;

import javax.sound.midi.*;
class MetaEvent {
    // META event types
    static final int sequenceNumber = 0x00; //FF 00 02 ss ss or FF 00 00
    static final int text           = 0x01; //FF 01 len text (arbitrary text)
    static final int copyright      = 0x02; //FF 02 len text
    static final int trackName      = 0x03; //FF 03 len text
    static final int instrument     = 0x04; //FF 04 len text
    static final int lyric          = 0x05; //FF 05 len text
    static final int marker         = 0x06; //FF 06 len text (e.g. Loop point)
    static final int cuePoint       = 0x07; //FF 07 len text (e.g. .wav file name)
    static final int programName    = 0x08; //FF 08 len text (PIANO, FLUTE, ...)
    static final int deviceName     = 0x09; //FF 09 len text (MIDI Out 1, MIDI Out 2)
    static final int endOfTrack     = 0x2f; //FF 2F 00
    static final int tempo          = 0x51; //FF 51 03 tt tt tt microseconds
    static final int SMPTEOffset    = 0x54; //FF 54 05 hr mn se fr ff
    static final int timeSignature  = 0x58; //FF 58 04 nn dd cc bb
    // nn=numerator, dd=denominator (2^dd), cc=MIDI clocks/metronome click
    // bb=no. of notated 32nd notes per MIDI quarter note (24 MIDI clocks).
    // No I don't understand that last one.
    // 06 03 18 08 is 6/8 time, 24 clocks/metronome, 8 1/32ndnotes/1/4note
    
    static final int keySignature   = 0x59; //FF 59 02 sf mi
    // -sf=no. of flats +sf=no. of sharps mi=0=major mi=1=minor
    
    static final int proprietaryData    = 0x7f; //FF 7F len data
    
    static Object[] getMetaStrings(MetaMessage mess) {
        boolean dumpText = false;
        boolean dumpBytes = false;
        
        int type = mess.getType();
        int len = mess.getLength();
        byte[] data = mess.getData();
        
        // The returned Object array
        // { type name, length, value string }
        Object[] result = { "M:", null, "" };
        result[1] = new Integer(data.length);
        
        switch (type) {
            case sequenceNumber:
                result[0] = "M:SequenceNumber";
                dumpBytes = true;
                break;
            case text:
                result[0] = "M:Text";
                dumpText = true;
                break;
            case copyright:
                result[0] = "M:Copyright";
                dumpText = true;
                break;
            case trackName:
                result[0] = "M:TrackName";
                dumpText = true;
                break;
            case instrument:
                result[0] = "M:Instrument";
                dumpText = true;
                break;
            case lyric:
                result[0] = "M:Lyric";
                dumpText = true;
                break;
            case marker:
                result[0] = "M:Marker";
                dumpText = true;
                break;
            case cuePoint:
                result[0] = "M:CuePoint";
                dumpText = true;
                break;
            case programName:
                result[0] = "M:ProgramName";
                dumpText = true;
                break;
            case deviceName:
                result[0] = "M:DeviceName";
                dumpText = true;
                break;
            case SMPTEOffset:
                result[0] = "M:SMPTEOffset";
                //hr mn se fr ff
                result[2] = data[0] + ":" + data[1] + ":" + data[2] + ":" + data[3] + ":" + data[4];
                break;
            case timeSignature:
                result[0] = "M:TimeSignature";
                int nn =  data[0];
                int dd =  (int)(java.lang.Math.pow(2, data[1]));
                int cc =  data[2];
                int bb =  data[3];
                result[2] = nn + "/" + dd + " " + cc + "Metr. " + bb + "N/q";
                //result[2] += nn + "/" + dd;
                break;
            case keySignature:
                result[0] = "M:KeySignature";
                result[2] = KeySignatures.getKeyName(data);
                break;
            case tempo:
                result[0] = "M:Tempo";
                int bpm = microSecsToBpm(data);
                result[2] = bpm + "bpm";
                break;
            case endOfTrack:
                result[0] = "M:EndOfTrack";
                break;
            case proprietaryData:
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
                    chars[k] = (char)b;
                } else {
                    chars[k] = '.';
                }
            }
            result[2] = new String(chars);
        }
        
        if (dumpBytes) {
            for (int k = 0; k < data.length; ++k) {
                byte b = data[k];
                result[2] = "0x" + Integer.toHexString(b) + " ";
            }
        }
        return result;
    }
    
    // Methods to handle tempo events.
    
    public static int microSecsToBpm(byte[] data) {
        long t = data[0] << 16;
        t += data[1] << 8;
        t += data[2];
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
    
    public static int parseTempo(String tempoString) {
        String digits = tempoString.substring(0, tempoString.indexOf("bpm"));
        int t = Integer.parseInt(digits);
        return t;
    }
    
    public static boolean isText(MetaMessage mess) {
        int type = mess.getType();
        return (type >= 1 && type <= 9);
    }
    
    public static boolean isEditable(MetaMessage mess) {
        int type = mess.getType();
        return ((type >= 1 && type <= 9) || type == tempo || type == keySignature);
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
        } else if (type == tempo) {
            int bpm = parseTempo(s);
            data = bpmToMicroSecs(bpm);
        } else if (type == keySignature) {
            data = KeySignatures.getKeyValues(s);
        }
        
        if (data != null) {
            try {
                mess.setMessage(type, data, len);
            } catch(InvalidMidiDataException e) {
                System.out.println("Error: setMetaData. " + e.getMessage());
            }
        }
    }
}
