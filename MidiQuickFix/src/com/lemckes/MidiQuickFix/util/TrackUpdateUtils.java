/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2010 John Lemcke
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

import com.lemckes.MidiQuickFix.MetaEvent;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 *
 */
public class TrackUpdateUtils
{

    public static void convertNoteOnZeroToNoteOff(Track track) {
        for (int e = 0; e < track.size(); ++e) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage)message;
                int st = sm.getStatus();

                // Check that this is a channel message
                if ((st & 0xf0) <= 0xf0) {
                    int channel = sm.getChannel();
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int noteNum = sm.getData1();
                        int velocity = sm.getData2();
                        if (velocity == 0) {
                            try {
                                sm.setMessage(ShortMessage.NOTE_OFF, channel, noteNum, 0);
                            }
                            catch (InvalidMidiDataException ex) {
                                System.err.println("Can't convert to NOTE_OFF");
                            }
                        }
                    }
                }
            }
        }
    }

    public static void convertTextToLyric(Track track, int[] rows) {
        for (int e : rows) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message.getStatus() == MetaMessage.META) {
                MetaMessage metaMess = (MetaMessage)message;
                Object[] str = MetaEvent.getMetaStrings((MetaMessage)message);
                if (str[0].equals("M:Text")) {
                    try {
                        metaMess.setMessage(MetaEvent.LYRIC, metaMess.getData(), metaMess.
                            getData().length);
                    }
                    catch (InvalidMidiDataException ex) {
                        System.err.println("Can't convert TEXT to LYRIC");
                    }
                }
            }
        }
    }

    public static void removeNotesFromTrack(Track track) {
        for (int e = track.size() -1; e >= 0; --e) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage)message;
                int st = sm.getStatus();

                // Check that this is a channel message
                if ((st & 0xf0) <= 0xf0) {
                    int command = sm.getCommand();
                    if (command == ShortMessage.NOTE_ON ||
                        command == ShortMessage.NOTE_OFF) {
                        track.remove(event);
                    }
                }
            }
        }
    }
}
