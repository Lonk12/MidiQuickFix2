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

import com.lemckes.MidiQuickFix.KeySignatures;
import com.lemckes.MidiQuickFix.MetaEvent;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * Transpose a midi Sequence
 * @version $Id$
 */
public class Transposer {
    
    /** Cannot create a new instance of Transposer */
    private Transposer() {
    }
    
    /**
     * Transpose the given sequence by the given number of semitones.
     * If the transposition would cause a note to be outside the valid range
     * for a midi note [0, 127] then the value is adjusted up or down by
     * enough octaves to bring it back into range.
     * @param seq the sequence to transpose
     * @param semitones the number of semitones to transpose
     * @return <code>true</code> if the value of any note would have
     * over/underflowed the valid range [0, 127]
     */
    public static boolean transpose(Sequence seq, int semitones) {
        boolean overflow = false;
        for (Track t : seq.getTracks()) {
            for (int i = 0; i < t.size(); ++i) {
                MidiEvent ev = t.get(i);
                MidiMessage m = ev.getMessage();
                if (m instanceof ShortMessage) {
                    ShortMessage mess = (ShortMessage)m;
                    int d1 = mess.getData1();
                    int d2 = mess.getData2();
                    int st = mess.getStatus();
                    
                    if ((st & 0xf0) <= 0xf0) { // This is a channel message
                        int cmd = mess.getCommand();
                        switch (cmd) {
                            case ShortMessage.NOTE_OFF:
                            case ShortMessage.NOTE_ON:
                                d1 += semitones;
                                while (d1 < 0) {
                                    d1 += 12;
                                    overflow = true;
                                }
                                while (d1 > 127) {
                                    d1 -= 12;
                                    overflow = true;
                                }
                                try {
                                    int channel = (int)(mess.getChannel() & 0xff);
                                    mess.setMessage(cmd, channel, d1, d2);
                                } catch(InvalidMidiDataException e) {
                                    TraceDialog.addTrace(
                                        "Transposer invalid note: " + e.getMessage());
                                }
                                break;
                            default:
                                // DO NOTHING
                        }
                    }
                } else if (m instanceof MetaMessage) {
                    MetaMessage mess = (MetaMessage)m;
                    int type = mess.getType();
                    byte[] data = mess.getData();
                    if (type == MetaEvent.KEY_SIGNATURE) {
                        data[0] = adjustKeySig(data[0], semitones);
                        try {
                            mess.setMessage(type, data, 2);
                        } catch(InvalidMidiDataException e) {
                            TraceDialog.addTrace(
                                "Transposer invalid key sig: " + e.getMessage());
                        }
                    }
                }
            }
        }
        return overflow;
    }
    
    private static byte adjustKeySig(byte sharps, int semitones) {
        byte key = sharpsToKey[sharps + 7];
        key += semitones;
        key %= 12;
        if (key < 0) key += 12;
        return keyToSharps[key];
    }
    
    public static void main(String args[]) {
        // Test the key sig conversion
        byte[] data = new byte[2];
        data[1] = 0;
        for (byte sig = -7; sig < 8; ++sig) {
            data[0] = sig;
            System.out.print(  "Key " + KeySignatures.getKeyName(data));
            data[0] = adjustKeySig(sig, -3);
            System.out.println(" -- " + KeySignatures.getKeyName(data));
        }
    }
    
    // Map a key signature to the number of sharps or flats
    // Index 0 = C, 1 = Db ... 11 = Bb, 12 = B
    //                           0   1  2   3  4   5  6  7   8  9  10 11
    static byte[] keyToSharps = {0, -5, 2, -3, 4, -1, 6, 1, -4, 3, -2, 5};
    // Map the number of sharps or flats to a key signature
    // Index 0 = 7 flats(-7) - 14 = 7 sharps(+7)
    // Convert from midi key sig with idx=data[0]+7
    static byte[] sharpsToKey = {11, 6, 1, 8, 3, 10, 5, 0, 7, 2, 9, 4, 11, 6, 1};
}
