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
import java.util.Arrays;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * Utility methods to change a track
 */
public class TrackUpdateUtils
{

    /**
     * Convert all NOTE_ON events with a velocity of zero to NOTE_OFF events
     *
     * @param track the track to convert
     */
    public static void convertNoteOnZeroToNoteOff(Track track) {
        for (int e = 0; e < track.size(); ++e) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage)message;
                if (ShortEvent.isChannelMessage(sm)) {
                    int channel = sm.getChannel();
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int noteNum = sm.getData1();
                        int velocity = sm.getData2();
                        if (velocity == 0) {
                            try {
                                sm.setMessage(ShortMessage.NOTE_OFF, channel, noteNum, 0);
                            } catch (InvalidMidiDataException ex) {
                                System.err.println("Can't convert to NOTE_OFF");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Set the velocity of all NOTE_ON events with a velocity greater than zero
     * to the given value. {@code velocity } must be greater than 1 and less
     * than 128
     *
     * @param track the track to convert
     */
    public static void setNoteOnVelocity(Track track, int velocity) {
        if (velocity < 1 || velocity > 127) {
            System.err.println(
                "Can't set NOTE_ON velocity < 1 or > 127");
            return;
        }
        for (int e = 0; e < track.size(); ++e) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage)message;
                if (ShortEvent.isChannelMessage(sm)) {
                    int channel = sm.getChannel();
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int noteNum = sm.getData1();
                        if (sm.getData2() > 0) {
                            try {
                                sm.setMessage(ShortMessage.NOTE_ON, channel, noteNum, velocity);
                            } catch (InvalidMidiDataException ex) {
                                System.err.println(
                                    "Can't set NOTE_ON velocity (channel=" + channel
                                    + ", note=" + noteNum + ", velocity=" + velocity + ")");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adjust the velocity of all NOTE_ON events with a velocity greater than
     * zero
     * by the given factor. {@code factor} must be greater than 0.0f and less
     * than 4.0f
     *
     * @param track the track to convert
     */
    public static void adjustNoteOnVelocity(Track track, float factor) {
        if (factor <= 0.0f || factor > 4.0f) {
            System.err.println(
                "Can't set NOTE_ON factor <= 0.0 or > 4.0");
            return;
        }

        for (int e = 0; e < track.size(); ++e) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage)message;
                if (ShortEvent.isChannelMessage(sm)) {
                    int channel = sm.getChannel();
                    if (sm.getCommand() == ShortMessage.NOTE_ON) {
                        int noteNum = sm.getData1();
                        int velocity = sm.getData2();
                        if (velocity > 0) {
                            try {
                                // Adjust velocity and clamp to valid values
                                velocity = Math.round(velocity * factor);
                                velocity = Math.max(1, velocity);
                                velocity = Math.min(127, velocity);
                                sm.setMessage(ShortMessage.NOTE_ON, channel, noteNum, velocity);
                            } catch (InvalidMidiDataException ex) {
                                System.err.println(
                                    "Can't set NOTE_ON velocity (channel=" + channel
                                    + ", note=" + noteNum + ", velocity=" + velocity + ")");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Convert any TEXT events at the positions in
     * <code>eventIndices</code>
     * into LYRIC events
     *
     * @param track the track to convert
     * @param eventIndices the event indices in <code>track</code>
     */
    public static void convertTextToLyric(Track track, int[] eventIndices) {
        for (int e : eventIndices) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message.getStatus() == MetaMessage.META) {
                MetaMessage metaMess = (MetaMessage)message;
                Object[] str = MetaEvent.getMetaStrings((MetaMessage)message);
                if (str[0].equals("M:Text")) {
                    try {
                        metaMess.setMessage(MetaEvent.LYRIC, metaMess.getData(), metaMess.
                            getData().length);
                    } catch (InvalidMidiDataException ex) {
                        System.err.println("Can't convert TEXT to LYRIC");
                    }
                }
            }
        }
    }

    /**
     * Add a space to any LYRIC events at the positions in
     * <code>eventIndices</code>
     *
     * @param track the track to change
     * @param eventIndices the event indices in <code>track</code>
     */
    public static void addSpaceToLyric(Track track, int[] eventIndices) {
        for (int e : eventIndices) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message.getStatus() == MetaMessage.META) {
                MetaMessage metaMess = (MetaMessage)message;
                Object[] str = MetaEvent.getMetaStrings((MetaMessage)message);
                if (str[0].equals("M:Lyric")) {
                    byte[] lyric = metaMess.getData();
                    if (lyric[lyric.length - 1] != ' ') {
                        byte[] newLyric = Arrays.copyOf(lyric, lyric.length + 1);
                        newLyric[lyric.length] = ' ';
                        try {
                            metaMess.setMessage(MetaEvent.LYRIC, newLyric, newLyric.length);
                        } catch (InvalidMidiDataException ex) {
                            System.err.println("Can't add space to LYRIC");
                        }
                    }
                }
            }
        }
    }

    /**
     * Remove all NOTE_ON and NOTE_OFF events from the
     * <code>track</code>
     *
     * @param track the track from which to remove the notes
     */
    public static void removeNotesFromTrack(Track track) {
        for (int e = track.size() - 1; e >= 0; --e) {
            MidiEvent event = track.get(e);

            MidiMessage message = event.getMessage();
            if (message instanceof ShortMessage) {
                ShortMessage sm = (ShortMessage)message;
                if (ShortEvent.isChannelMessage(sm)) {
                    int command = sm.getCommand();
                    if (command == ShortMessage.NOTE_ON
                        || command == ShortMessage.NOTE_OFF) {
                        track.remove(event);
                    }
                }
            }
        }
    }

    /**
     * Shift any events at the positions in
     * <code>eventIndices</code> by <code>offset</code> ticks.
     *
     * @param track the track to convert
     * @param eventIndices the event indices in <code>track</code>
     * @param targetTick the new position for the first event.
     */
    public static void shiftEvents(Track track, int[] eventIndices, long targetTick) {
        MidiEvent event = track.get(eventIndices[0]);
        long offset = targetTick - event.getTick();
        if (offset == 0) {
            return;
        }

        for (int i : eventIndices) {
            event = track.get(i);
            long oldTick = event.getTick();
            long newTick = oldTick + offset;
            event.setTick(newTick);
        }
    }
}
