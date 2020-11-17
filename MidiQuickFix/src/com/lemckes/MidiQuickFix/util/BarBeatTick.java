/*
 */
package com.lemckes.MidiQuickFix.util;

import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;

/**
 *
 */
public class BarBeatTick
{

    private final Map<Long, TimeSignature> mTimeSigChanges;
    private final int mResolution;
    private final int mInitialTicksPerBar;
    private final int mInitialBeatsPerBar;
    private static final int TIME_SIGNATURE = 0x58;

    public BarBeatTick(Sequence seq) {
        mTimeSigChanges = new TreeMap<>();
        mResolution = seq.getResolution();
        Track t = seq.getTracks()[0];
        int count = t.size() - 1;
        for (int j = 0; j < count; ++j) {
            MidiEvent ev = t.get(j);
            MidiMessage mess = ev.getMessage();
            int st = mess.getStatus();

            if (st == MetaMessage.META) {
                MetaMessage metaMess = (MetaMessage)mess;
                if (metaMess.getType() == TIME_SIGNATURE) {
                    // System.out.println("-------- BarBeatTick -------");
                    // System.out.println("Tick:       " + ev.getTick());
                    // System.out.println("Resolution: " + mResolution);
                    mTimeSigChanges.put(ev.getTick(), new TimeSignature(metaMess));
                }
            }
        }

        if (mTimeSigChanges.get(0L) == null) {
            // There was no time signature at time zero
            // so create a default 4/4,24,8
            mTimeSigChanges.put(0L, new TimeSignature(4, 0.25f, 24, 8));
        }

        // Add a marker for the end of the sequence
        mTimeSigChanges.put(seq.getTickLength(), new TimeSignature(4, 0.25f, 24, 8));

        mInitialBeatsPerBar = mTimeSigChanges.get(0L).getBeatsPerBar();
        mInitialTicksPerBar = mTimeSigChanges.get(0L).getTicksPerBar(mResolution);
    }

    public SequencePosition getSequencePosition(long tick) {
        int bar = 1;
        int beat = 1;
        int ticksLeft = 0;
        int currTicksPerBar = mInitialTicksPerBar;
        int currBeatsPerBar = mInitialBeatsPerBar;
        long prevChangeTick = 0;
        for (Entry<Long, TimeSignature> timeSigChange : mTimeSigChanges.entrySet()) {
            long changeTick = timeSigChange.getKey();
            if (changeTick > 0) {
                if (tick <= changeTick) {
                    long ticks = tick - prevChangeTick;
                    int bars = (int)(ticks / currTicksPerBar);
                    bar += bars;
                    ticksLeft = (int)(ticks - (bars * currTicksPerBar));
                    int ticksPerBeat = currTicksPerBar / currBeatsPerBar;
                    beat = 1 + ticksLeft / ticksPerBeat;
                    ticksLeft -= (beat - 1) * ticksPerBeat;
                    break;
                } else {
                    long ticks = changeTick - prevChangeTick;
                    bar += ticks / currTicksPerBar;
                    prevChangeTick = changeTick;
                    currTicksPerBar = timeSigChange.getValue().getTicksPerBar(mResolution);
                    currBeatsPerBar = timeSigChange.getValue().getBeatsPerBar();
                }
            }
        }
        return new SequencePosition(bar, beat, ticksLeft);
    }
}
