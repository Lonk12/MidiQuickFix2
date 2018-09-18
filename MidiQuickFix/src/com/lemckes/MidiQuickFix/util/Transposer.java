/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2018 John Lemcke
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

import com.lemckes.MidiQuickFix.MetaEvent;
import com.lemckes.MidiQuickFix.ShortEvent;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * Transpose a midi Sequence
 */
public class Transposer
{

    /**
     * Map a key signature to the number of sharps or flats
     * Index 0 = C, 1 = Db ... 10 = Bb, 11 = B
     * 0 1 2 3 4 5 6 7 8 9 10 11
     */
    static byte[] keyToSharps = {0, -5, 2, -3, 4, -1, 6, 1, -4, 3, -2, 5};
    /**
     *
     * Map the number of sharps or flats to a key signature
     * Index 0 = 7 flats(-7)p; 14 = 7 sharps(+7)
     * Convert from midi key sig with idx=data[0]+7
     */
    static byte[] sharpsToKey = {11, 6, 1, 8, 3, 10, 5, 0, 7, 2, 9, 4, 11, 6, 1};

    /**
     * Cannot create a new instance of Transposer
     */
    private Transposer() {
    }

    /**
     * Transpose the given sequence by the given number of semitones
     * without transposing the drum track.
     * If the transposition would cause a note to be outside the valid range
     * for a midi note [0, 127] then the value is adjusted up or down by
     * enough octaves to bring it back into range.
     *
     * @return <code>true</code> if the value of any note would have
     * over/underflowed the valid range [0, 127]
     * @param seq the sequence to transpose
     * @param semitones the number of semitones to transpose
     */
    public static boolean transpose(MqfSequence seq, int semitones) {
        return transpose(seq, semitones, false);
    }

    /**
     * Transpose the given sequence by the given number of semitones.
     * If <code>doDrums</code> is false then note events for the default
     * drum track, channel 9, are not transposed.
     * If the transposition would cause a note to be outside the valid range
     * for a midi note [0, 127] then the value is adjusted up or down by
     * enough octaves to bring it back into range.
     *
     * @return <code>true</code> if the value of any note would have
     * over/underflowed the valid range [0, 127]
     * @param doDrums if <code>true</code> then the drum track, channel 9, will
     * be transposed
     * @param seq the sequence to transpose
     * @param semitones the number of semitones to transpose
     */
    public static boolean transpose(MqfSequence seq, int semitones,
        boolean doDrums) {
        boolean overflow = false;
        for (Track t : seq.getTracks()) {
            for (int i = 0; i < t.size(); ++i) {
                MidiEvent ev = t.get(i);
                MidiMessage message = ev.getMessage();
                if (message instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)message;
                    int d1 = sm.getData1();
                    int d2 = sm.getData2();
                    if (ShortEvent.isChannelMessage(sm)
                        && (doDrums || sm.getChannel() != 9)) {
                        int cmd = sm.getCommand();
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
                                    int channel = sm.getChannel() & 0xff;
                                    sm.setMessage(cmd, channel, d1, d2);
                                } catch (InvalidMidiDataException e) {
                                    TraceDialog.addTrace(
                                        "Transposer invalid note: "
                                        + e.getMessage());
                                }
                                break;
                            default:
                            // DO NOTHING
                            }
                    }
                } else if (message instanceof MetaMessage) {
                    MetaMessage mess = (MetaMessage)message;
                    int type = mess.getType();
                    byte[] data = mess.getData();
                    if (type == MetaEvent.KEY_SIGNATURE) {
                        data[0] = adjustKeySig(data[0], semitones);
                        try {
                            mess.setMessage(type, data, 2);
                        } catch (InvalidMidiDataException e) {
                            TraceDialog.addTrace(
                                "Transposer invalid key sig: " + e.getMessage());
                        }
                    }
                }
            }
        }
        return overflow;
    }

    static byte adjustKeySig(byte sharps, int semitones) {
        byte key = sharpsToKey[sharps + 7];
        key += semitones;
        key %= 12;
        if (key < 0) {
            key += 12;
        }
        return keyToSharps[key];
    }
}
