/*
 */
package com.lemckes.MidiQuickFix.util;

import javax.sound.midi.MetaMessage;

/**
 *
 */
class TimeSignature
{

    /**
     * The numerator (top number) of the time signature
     */
    private final int mBeatsPerBar;
    /**
     * Log2 of the denominator (bottom number) of the time signature
     */
    private final float mBeatLength;
    /**
     * MIDI clocks/metronome click
     */
    private final int mClocksPerMetronome;
    /**
     * no. of notated 32nd notes per MIDI quarter note
     */
    private final int mNum32In4;

    public TimeSignature(MetaMessage metaMess) {
        byte[] data = metaMess.getData();
        mBeatsPerBar = (data[0] & 0x00ff);
        mBeatLength = 1.0f / (int)(Math.pow(2, (data[1] & 0x00ff)));
        mClocksPerMetronome = (data[2] & 0x00ff);
        mNum32In4 = (data[3] & 0x00ff);

    }

    public TimeSignature(int beatsPerBar, float beatLength,
        int clocksPerMetronome, int num32In4) {
        mBeatsPerBar = beatsPerBar;
        mBeatLength = beatLength;
        mClocksPerMetronome = clocksPerMetronome;
        mNum32In4 = num32In4;
    }

    public int getTicksPerBar(int resolution) {
        float qNotesPerBar = 4 * mBeatLength * mBeatsPerBar;
        float ticksPerBar = qNotesPerBar * resolution;
        return (int)ticksPerBar;
    }

    public int getBeatsPerBar() {
        return mBeatsPerBar;
    }

    public float getBeatLength() {
        return mBeatLength;
    }

    public int getClocksPerMetronome() {
        return mClocksPerMetronome;
    }

    public int getNum32In4() {
        return mNum32In4;
    }
}