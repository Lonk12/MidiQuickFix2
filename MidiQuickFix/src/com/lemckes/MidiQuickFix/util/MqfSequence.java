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
package com.lemckes.MidiQuickFix.util;

import java.util.Vector;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 * This is a simple wrapper around <code>javax.sound.midi.Sequence</code>
 * to allow access to the Vector of Tracks in the Sequence.
 * The original implementation only provides access through the <>
 * <code>public Track[] getTracks()</code> method which returns an array
 * containing the Tracks. This prevents any manipulation of the order
 * of Tracks in the Sequence. I would like to be able to, for instance,
 * insert a new Track at a specific place in the order.
 */
public class MqfSequence extends Sequence
{

    public MqfSequence() throws InvalidMidiDataException {
        this(Sequence.PPQ, 96);
    }

    public MqfSequence(float divisionType, int resolution, int numTracks) throws
        InvalidMidiDataException {
        super(divisionType, resolution, numTracks);
    }

    public MqfSequence(float divisionType, int resolution) throws
        InvalidMidiDataException {
        super(divisionType, resolution);
    }

    public MqfSequence createMqfSequence(Sequence seq) throws
        InvalidMidiDataException {
        MqfSequence mqfs = new MqfSequence(
            seq.getDivisionType(), seq.getResolution());
        for (Track t : seq.getTracks()) {
            mqfs.getTracksVector().addElement(t);
        }
        return mqfs;
    }

    /**
     * Gets the actual Vector that contains the Tracks
     * @return the actual Vector that contains the Tracks
     */
    public Vector<Track> getTracksVector() {
        return tracks;
    }

    /**
     * Create a Track at the specified index in the Sequence.
     * Need to do some hackiness because the Track() constructor is not public.
     * The only way to create a Track is with the existing createTrack()
     * method that places the track at the end of the existing tracks.
     * @param index the index at which to create the new Track
     * @return the newly created Track
     */
    public Track createTrack(int index) {
        synchronized (tracks) {
            Track track = createTrack();
            tracks.add(index, track);
            tracks.remove(tracks.size() - 1);
            return track;
        }
    }
}