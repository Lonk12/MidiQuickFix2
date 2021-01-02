/** ************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2018 John Lemcke
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
 ************************************************************* */
package com.lemckes.MidiQuickFix.util;

import com.lemckes.MidiQuickFix.MetaEvent;
import static com.lemckes.MidiQuickFix.ShortEvent.isChannelMessage;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;

/**
 * This is a wrapper around <code>javax.sound.midi.Sequence</code>
 * to allow access to the Vector of Tracks in the Sequence.
 * The original implementation only provides access through the
 * <code>public Track[] getTracks()</code> method which returns an array
 * containing the Tracks. This prevents any manipulation of the order
 * of Tracks in the Sequence. I would like to be able to, for instance,
 * insert a new Track at a specific place in the order.
 *
 * Information about each Track is stored in TrackInfo objects.
 *
 * Any Song Information (as defined by http://www.midi.org/techspecs/rp26.php)
 * is also stored in this class.
 */
public class MqfSequence
    extends Sequence
{

    /**
     * The info about a track
     */
    private class TrackInfo
    {

        String mName;
        long mFirstTick;
        long mLastTick;
        int mLowestNote;
        int mHighestNote;
        boolean mHasNotes;
        int mChannel;
        int mVolume;
        boolean mSoloing;
        boolean mMuted;
        boolean mHasLyrics;
        boolean mShowLyrics;

        TrackInfo() {
            mName = null;
            mFirstTick = -1;
            mLastTick = -1;
            mLowestNote = Integer.MAX_VALUE;
            mHighestNote = Integer.MIN_VALUE;
            mHasNotes = false;
            mChannel = -1;
            mVolume = 100;
            mSoloing = false;
            mMuted = false;
            mHasLyrics = false;
            mShowLyrics = false;
        }
    }

    /**
     * The track info for each track
     */
    transient private List<TrackInfo> mTrackInfos;

    private final Map<String, String> mSongInfo = new LinkedHashMap<>(4);

    /**
     * Construct an MqfSequence from the given Sequence
     *
     * @param seq
     * @throws InvalidMidiDataException
     */
    public MqfSequence(Sequence seq) throws
        InvalidMidiDataException {
        super(seq.getDivisionType(), seq.getResolution());

        for (Track t : seq.getTracks()) {
            tracks.addElement(t);
        }

        buildTrackInfos();
    }

    private void buildTrackInfos() {

        mTrackInfos = new ArrayList<>(getTracks().length);

        int index = 0;
        for (Track t : getTracks()) {
            createTrackInfo(t, index++);
        }
    }

    /**
     * Create a Track at the specified index in the Sequence.
     * Need to do some hackiness because the Track() constructor is not public.
     * The only way to create a Track is with the existing createTrack()
     * method that places the track at the end of the existing tracks.
     *
     * @param index the index at which to create the new Track
     * @return the newly created Track
     */
    public Track createTrack(int index) {
        synchronized (this) {
            // Create a new Track at the end of the sequence
            Track track = super.createTrack();
            // Add the same new Track at the given index
            tracks.add(index, track);
            // and remove the one from the end.
            tracks.remove(tracks.size() - 1);

            createTrackInfo(track, index);

            return track;
        }
    }

    @Override
    public boolean deleteTrack(Track track) {
        boolean deleted = super.deleteTrack(track);

        if (deleted) {
            buildTrackInfos();
        }

        return deleted;
    }

    public String getTrackName(int trackIndex) {
        return mTrackInfos.get(trackIndex).mName;
    }

    public void setTrackName(int trackIndex, String mName) {
        mTrackInfos.get(trackIndex).mName = mName;
    }

    public long getTrackFirstTick(int trackIndex) {
        return mTrackInfos.get(trackIndex).mFirstTick;
    }

    public void setTrackFirstTick(int trackIndex, long mFirstTick) {
        mTrackInfos.get(trackIndex).mFirstTick = mFirstTick;
    }

    public long getTrackLastTick(int trackIndex) {
        return mTrackInfos.get(trackIndex).mLastTick;
    }

    public void setTrackLastTick(int trackIndex, long mLastTick) {
        mTrackInfos.get(trackIndex).mLastTick = mLastTick;
    }

    public int getTrackLowestNote(int trackIndex) {
        return mTrackInfos.get(trackIndex).mLowestNote;
    }

    public void setTrackLowestNote(int trackIndex, int mLowestNote) {
        mTrackInfos.get(trackIndex).mLowestNote = mLowestNote;
    }

    public int getTrackHighestNote(int trackIndex) {
        return mTrackInfos.get(trackIndex).mHighestNote;
    }

    public void setTrackHighestNote(int trackIndex, int mHighestNote) {
        mTrackInfos.get(trackIndex).mHighestNote = mHighestNote;
    }

    public boolean trackHasNotes(int trackIndex) {
        return mTrackInfos.get(trackIndex).mHasNotes;
    }

    public int getTrackChannel(int trackIndex) {
        return mTrackInfos.get(trackIndex).mChannel;
    }

    public void setTrackChannel(int trackIndex, int mChannel) {
        mTrackInfos.get(trackIndex).mChannel = mChannel;
    }

    public boolean isTrackMuted(int trackIndex) {
        return mTrackInfos.get(trackIndex).mMuted;
    }

    public void muteTrack(int trackIndex, boolean mute) {
        mTrackInfos.get(trackIndex).mMuted = mute;
    }

    public boolean isTrackSoloing(int trackIndex) {
        return mTrackInfos.get(trackIndex).mSoloing;
    }

    public void soloTrack(int trackIndex, boolean solo) {
        mTrackInfos.get(trackIndex).mSoloing = solo;
    }

    public boolean trackLyricsShown(int trackIndex) {
        return mTrackInfos.get(trackIndex).mShowLyrics;
    }

    public void showTrackLyrics(int trackIndex, boolean show) {
        mTrackInfos.get(trackIndex).mShowLyrics = show;
    }

    public boolean getTrackHasLyrics(int trackIndex) {
        return mTrackInfos.get(trackIndex).mHasLyrics;
    }

    public void setTrackLyrics(int trackIndex, boolean hasLyrics) {
        mTrackInfos.get(trackIndex).mHasLyrics = hasLyrics;
    }

    public int getTrackVolume(int trackIndex) {
        return mTrackInfos.get(trackIndex).mVolume;
    }

    public void setTrackVolume(int trackIndex, int volume) {
        mTrackInfos.get(trackIndex).mVolume = volume;
    }

    private void createTrackInfo(Track t, int index) {
        TrackInfo ti = new TrackInfo();

        int textEventCount = 0;
        for (int e = 0; e < t.size(); ++e) {
            MidiEvent me = t.get(e);

            MidiMessage mm = me.getMessage();

            if (mm.getStatus() == MetaMessage.META) {
                handleMetaMessage(ti, (MetaMessage)mm, textEventCount);
            }

            if (mm instanceof ShortMessage) {
                handleShortMessage(ti, (ShortMessage)mm, me.getTick());
            }
        }

        if (ti.mFirstTick != -1) {
            ti.mLastTick = getTickLength();
            ti.mHasNotes = true;
        }

        mTrackInfos.add(index, ti);
    }

    private void handleMetaMessage(TrackInfo ti, MetaMessage mm, int textEventCount) {
        Object[] str = MetaEvent.getMetaStrings((MetaMessage)mm);
        if (str[0].equals("M:TrackName")) {
            ti.mName = (String)str[2];
        } else if (str[0].equals("M:Lyric")) {
            ti.mHasLyrics = true;
            ti.mShowLyrics = true;
        } else if (str[0].equals("M:Text")) {
            ++textEventCount;
            if (textEventCount > 8) {
                ti.mHasLyrics = true;
                ti.mShowLyrics = true;
            }
        }
    }

    private void handleShortMessage(TrackInfo ti, ShortMessage sm, long tick) {
        if (isChannelMessage(sm)) {
            ti.mChannel = sm.getChannel();

            switch (sm.getCommand()) {
                case ShortMessage.NOTE_ON:
                    // Treat the first NOTE_ON as the start of the track.
                    if (ti.mFirstTick == -1) {
                        ti.mFirstTick = tick;
                    }
                    int noteNum = sm.getData1();
                    ti.mLowestNote
                        = Math.min(noteNum, ti.mLowestNote);
                    ti.mHighestNote
                        = Math.max(noteNum, ti.mHighestNote);
                    break;

                case ShortMessage.CONTROL_CHANGE:
                    int d1 = sm.getData1();
                    int d2 = sm.getData2();
                    if (d1 == 7) {
                        ti.mVolume = d2;
                    }
                    break;
                default:
                    // Ignore other messages
                    break;
            }
        }
    }

    public void putSongInfo(String key, String value) {
        mSongInfo.put(key, value);
    }

    public String getSongInfoItem(String key) {
        return mSongInfo.get(key);
    }

    public Map<String, String> getSongInfo() {
        return mSongInfo;
    }
}
