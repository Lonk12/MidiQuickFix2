/** ************************************************************
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
 ************************************************************* */
package com.lemckes.MidiQuickFix;

import static com.lemckes.MidiQuickFix.ShortEvent.isChannelMessage;
import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.UiStrings;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.table.AbstractTableModel;

/**
 * The model for the track summary table.
 *
 * @version $Id: TrackSummaryTableModel.java,v 1.19 2012/09/09 03:56:05 jostle
 * Exp $
 */
public class TrackSummaryTableModel
    extends AbstractTableModel
{

    static final long serialVersionUID = -5109111307767764175L;
//    transient private Synthesizer mSynth;
//    private final transient MidiChannel[] mChannels;
    private final transient Sequencer mSeq;
    
    /**
     * The Sequence that is loaded.
     */
    private final transient MqfSequence mSequence;
    
    /**
     * The resolution of the sequence
     */
    private final int mRes;
    
    /**
     * The tracks in the sequence
     */
    transient private Track[] mTracks;

    /**
     * The info about a track
     */
    static class TrackInfo
    {

        String mName;
        long mStart;
        long mEnd;
        int mLowNote;
        int mHighNote;
        int mChannel;
        boolean mSolo;
        boolean mMute;
        boolean mLyrics;
    }

    /**
     * The track info for each track
     */
    transient private TrackInfo[] mInfo;

    /**
     * Creates a new instance of a TrackSummaryTableModel
     *
     * @param s
     * @param sequencer
     */
    public TrackSummaryTableModel(MqfSequence s, Sequencer sequencer) {
//        try {
//            mSynth = MidiSystem.getSynthesizer();
//        }
//        catch (MidiUnavailableException e) {
//            TraceDialog.addTrace("No Synthesiser available." + // NOI18N
//                " (Could make playing tricky.)"); // NOI18N
//        }

//        mChannels = MidiQuickFix.getSynth().getChannels();
        MidiQuickFix.getSynth().getChannels()[0].allSoundOff();

        mSeq = sequencer;
        mSequence = s;
        mRes = s.getResolution();

        updateInfo();
    }

    public final void updateInfo() {
        mTracks = mSequence.getTracks();
        int numTracks = mTracks.length;

        mInfo = new TrackInfo[numTracks];

        for (int i = 0; i < numTracks; ++i) {
            mInfo[i] = new TrackInfo();
            mInfo[i].mSolo = false;
            mSeq.setTrackSolo(i, false);
            mInfo[i].mMute = false;
            mSeq.setTrackMute(i, false);
            mInfo[i].mLyrics = false;
            Track t = mTracks[i];
            mInfo[i].mEnd = t.ticks();
            mInfo[i].mStart = -1;
            mInfo[i].mLowNote = Integer.MAX_VALUE;
            mInfo[i].mHighNote = Integer.MIN_VALUE;
            mInfo[i].mChannel = -1;

            /* Only treat M:Text events as lyrics if there are more than 4 of them */
            int textEventCount = 0;
            for (int e = 0; e < t.size(); ++e) {
                MidiEvent me = t.get(e);
                long tick = me.getTick();

                MidiMessage mm = me.getMessage();
                if (mm.getStatus() == MetaMessage.META) {
                    Object[] str = MetaEvent.getMetaStrings((MetaMessage)mm);
                    if (str[0].equals("M:TrackName")) {
                        mInfo[i].mName = (String)str[2];
                    }
                    if (str[0].equals("M:Lyric")) {
                        mInfo[i].mLyrics = true;
                    }
                    if (str[0].equals("M:Text")) {
                        ++textEventCount;
                        if (textEventCount > 4) {
                            mInfo[i].mLyrics = true;
                        }
                    }
                }

                if (mm instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mm;
                    if (isChannelMessage(sm)) {
                        mInfo[i].mChannel = sm.getChannel();

                        // The first NOTE_ON is the start of
                        // the track.
                        if (sm.getCommand() == ShortMessage.NOTE_ON) {
                            if (mInfo[i].mStart == -1) {
                                mInfo[i].mStart = tick;
                            }
                            int noteNum = sm.getData1();
                            mInfo[i].mLowNote
                                = Math.min(noteNum, mInfo[i].mLowNote);
                            mInfo[i].mHighNote
                                = Math.max(noteNum, mInfo[i].mHighNote);
                        }
                    }
                }
            }
        }
    }

    public boolean showLyrics(int trackNum) {
        return mInfo[trackNum].mLyrics;
    }

    @Override
    public int getRowCount() {
        return mTracks.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object result;
        switch (column) {
            case 0:
                result = row;
                break;
            case 1:
                result = mInfo[row].mName;
                break;
            case 2:
                if (mInfo[row].mStart == -1) {
                    // Didn't find a NOTE_ON event
                    result = null;
                } else {
                    result = Formats.formatTicks(mInfo[row].mStart, mRes, true);
                }
                break;
            case 3:
                if (mInfo[row].mStart == -1) {
                    // Didn't find a NOTE_ON event
                    result = null;
                } else {
                    result = Formats.formatTicks(mInfo[row].mEnd, mRes, true);
                }
                break;
            case 4:
                if (mInfo[row].mLowNote == Integer.MAX_VALUE) {
                    // Didn't find a NOTE_ON event
                    result = null;
                } else {
                    // Display the lowest note in flats
                    result = NoteNames.getNoteName(
                        mInfo[row].mLowNote, true);
                }
                break;
            case 5:
                if (mInfo[row].mHighNote == Integer.MIN_VALUE) {
                    // Didn't find a NOTE_ON event
                    result = null;
                } else {
                    // Display the highest note in sharps
                    result = NoteNames.getNoteName(
                        mInfo[row].mHighNote, false);
                }
                break;
            case 6:
                if (mInfo[row].mChannel == -1) {
                    // Didn't find a channel event
                    result = null;
                } else {
                    result = mInfo[row].mChannel;
                }
                break;
            case 7:
                result = mInfo[row].mSolo;
                break;
            case 8:
                result = mInfo[row].mMute;
                break;
            case 9:
                result = mInfo[row].mLyrics;
                break;
            default:
                result = "";
        }
        return result;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        switch (column) {
            case 7:
                // Solo
                mInfo[row].mSolo = ((Boolean)value);
                mSeq.setTrackSolo(row, mInfo[row].mSolo);
                boolean soloed = mSeq.getTrackSolo(row);
                if (soloed != mInfo[row].mSolo) {
                    TraceDialog.addTrace(
                        "Sequencer Solo not supported. (Set to " + value // NOI18N
                        + " is actually " + soloed + ")"); // NOI18N
                }
                break;
            case 8:
                // Mute
                mInfo[row].mMute = ((Boolean)value);
                mSeq.setTrackMute(row, mInfo[row].mMute);
                boolean muted = mSeq.getTrackMute(row);
                if (muted != mInfo[row].mMute) {
                    TraceDialog.addTrace(
                        "Sequencer Mute not supported. (Set to " + value // NOI18N
                        + " is actually " + muted + ")"); // NOI18N
                }
                break;
            case 9:
                // Show Lyrics
                mInfo[row].mLyrics = ((Boolean)value);
                fireTableCellUpdated(row, column);
                break;
            default:
            // Do Nothing
        }
    }
    Class<?>[] types = new Class<?>[]{
        java.lang.Integer.class,
        java.lang.String.class,
        java.lang.Object.class,
        java.lang.Object.class,
        java.lang.Object.class,
        java.lang.Object.class,
        java.lang.Integer.class,
        java.lang.Boolean.class,
        java.lang.Boolean.class,
        java.lang.Boolean.class
    };

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return types[columnIndex];
    }
    String[] columnNames = new String[]{
        UiStrings.getString("no."),
        UiStrings.getString("name"),
        UiStrings.getString("start"),
        UiStrings.getString("end"),
        UiStrings.getString("low_note"),
        UiStrings.getString("high_note"),
        UiStrings.getString("channel_abbrev"),
        UiStrings.getString("solo"),
        UiStrings.getString("mute"),
        UiStrings.getString("lyrics")
    };

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
    boolean[] canEdit = new boolean[]{
        false,
        false,
        false,
        false,
        false,
        false,
        false,
        true,
        true,
        true
    };

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        boolean ret = canEdit[columnIndex];
        if (columnIndex > 6 && columnIndex < 8 && mInfo[rowIndex].mChannel == -1) {
            ret = false;
        }
        return ret;
    }
}
