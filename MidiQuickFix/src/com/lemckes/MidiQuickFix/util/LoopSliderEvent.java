/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2023 John Lemcke
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
 * The event that is passed to LoopSliderListener implementors.
 * @see LoopSliderListener
 */
public class LoopSliderEvent
{
    /**
     * Create a LoopSliderEvent.
     * @param value The value of the slider
     * @param inPoint The position of the start of the loop
     * @param outPoint The position of the end of the loop.
     * @param valueIsAdjusting True if the slider is being adjusted.
     */
    public LoopSliderEvent(long value, long inPoint, long outPoint, boolean valueIsAdjusting)
    {
        mValue = value;
        mInPoint = inPoint;
        mOutPoint = outPoint;
        mValueIsAdjusting = valueIsAdjusting;
    }


    private long mValue;
    private long mInPoint;
    private long mOutPoint;
    private boolean mValueIsAdjusting;

    public long getValue() {
        return mValue;
    }

    public long getInPoint() {
        return mInPoint;
    }

    public long getOutPoint() {
        return mOutPoint;
    }

    public boolean getValueIsAdjusting() {
        return mValueIsAdjusting;
    }
}

