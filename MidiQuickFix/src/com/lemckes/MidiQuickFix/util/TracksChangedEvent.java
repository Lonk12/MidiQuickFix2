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

/**
 * The event that is passed to TracksChangedListener implementors to
 * signal the addition or deletion of Tracks from a Sequence.
 * @see TracksChangedListener
 * @version $Id$
 */
public class TracksChangedEvent
{

    public enum TrackChangeType
    {

        TRACK_ADDED,
        TRACK_DELETED
    }
    private TrackChangeType mChangeType;

    /**
     * Create a TracksChangedEvent of the given type.
     * @param changeType the type of change
     */
    public TracksChangedEvent(TrackChangeType changeType) {
        mChangeType = changeType;
    }

    public TrackChangeType getChangeType(){
        return mChangeType;
    }
}

