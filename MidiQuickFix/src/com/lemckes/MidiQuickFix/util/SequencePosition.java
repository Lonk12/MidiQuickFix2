/*
 */
package com.lemckes.MidiQuickFix.util;

public class SequencePosition
{

    private int mBar;
    private int mBeat;
    private int mTick;

    public SequencePosition(int bar, int beat, int tick) {
        mBar = bar;
        mBeat = beat;
        mTick = tick;
    }

    public int getBar() {
        return mBar;
    }

    public int getBeat() {
        return mBeat;
    }

    public int getTick() {
        return mTick;
    }
}
