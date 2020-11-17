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
 */
public class TrackSummaryTableModel
    extends AbstractTableModel
{

    static final long serialVersionUID = -5109111307767764175L;
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
        boolean mHasLyrics;
        boolean mShowLyrics;
    }

    /**
     * The track info for each track
     */
    transient private TrackInfo[] mInfo;

    /**
     * Enum to hold info for each column in the table
     */
    enum ColumnInfo
    {
        TRACK_NUM(0, UiStrings.getString("no."), Integer.class),
        TRACK_NAME(1, UiStrings.getString("name"), String.class),
        START_TICK(2, UiStrings.getString("start"), Object.class),
        END_TICK(3, UiStrings.getString("end"), Object.class),
        LOW_NOTE(4, UiStrings.getString("low_note"), Object.class),
        HIGH_NOTE(5, UiStrings.getString("high_note"), Object.class),
        CHANNEL(6, UiStrings.getString("channel_abbrev"), Integer.class),
        MUTE(7, UiStrings.getString("solo"), Boolean.class),
        SOLO(8, UiStrings.getString("mute"), Boolean.class),
        SHOW_LYRICS(9, UiStrings.getString("lyrics"), Boolean.class);

        private final int mIndex;
        private final String mName;
        private final Class<?> mClass;

        ColumnInfo(int columnIndex, String columnName, Class<?> columnClass) {
            mIndex = columnIndex;
            mName = columnName;
            mClass = columnClass;
        }

        public int getIndex() { return mIndex; }
        public String getName() { return mName; }
        public Class<?> getColumnClass() { return mClass; }
    };


    private final ColumnInfo[] columnOrder = new ColumnInfo[] {
        ColumnInfo.TRACK_NUM,
        ColumnInfo.TRACK_NAME,
        ColumnInfo.START_TICK,
        ColumnInfo.END_TICK,
        ColumnInfo.LOW_NOTE,
        ColumnInfo.HIGH_NOTE,
        ColumnInfo.CHANNEL,
        ColumnInfo.SOLO,
        ColumnInfo.MUTE,
        ColumnInfo.SHOW_LYRICS
    };
    /**
     * Creates a new instance of a TrackSummaryTableModel
     *
     * @param s
     * @param sequencer
     */
    public TrackSummaryTableModel(MqfSequence s, Sequencer sequencer) {
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
            mInfo[i].mShowLyrics = false;
            Track t = mTracks[i];
            mInfo[i].mEnd = t.ticks();
            mInfo[i].mStart = -1;
            mInfo[i].mLowNote = Integer.MAX_VALUE;
            mInfo[i].mHighNote = Integer.MIN_VALUE;
            mInfo[i].mChannel = -1;

            /* Only treat M:Text events as lyrics if there are more than 8 of them */
            int textEventCount = 0;
            for (int e = 0; e < t.size(); ++e) {
                MidiEvent me = t.get(e);
                long tick = me.getTick();

                MidiMessage mm = me.getMessage();
                if (mm.getStatus() == MetaMessage.META) {
                    Object[] str = MetaEvent.getMetaStrings((MetaMessage)mm);
                    if (str[0].equals("M:TrackName")) {
                        mInfo[i].mName = (String)str[2];
                    } else if (str[0].equals("M:Lyric")) {
                        mInfo[i].mHasLyrics = true;
                        mInfo[i].mShowLyrics = true;
                    } else if (str[0].equals("M:Text")) {
                        ++textEventCount;
                        if (textEventCount > 8) {
                            mInfo[i].mHasLyrics = true;
                            mInfo[i].mShowLyrics = true;
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
        return mInfo[trackNum].mShowLyrics;
    }

    @Override
    public int getRowCount() {
        return mTracks.length;
    }

    @Override
    public int getColumnCount() {
        return columnOrder.length;
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object result = null;
        switch (columnOrder[column]) {
            case TRACK_NUM:
                result = row;
                break;
            case TRACK_NAME:
                result = mInfo[row].mName;
                break;
            case START_TICK:
                if (mInfo[row].mStart > -1) {
                    result = Formats.formatBeatsTicks(mInfo[row].mStart, mRes, true);
                }
                break;

            case END_TICK:
                if (mInfo[row].mStart > -1) {
                    result = Formats.formatBeatsTicks(mInfo[row].mEnd, mRes, true);
                }
                break;

            case LOW_NOTE:
                if (mInfo[row].mLowNote != Integer.MAX_VALUE) {
                    // Display the lowest note in flats
                    result = NoteNames.getNoteName(
                        mInfo[row].mLowNote, true);
                }
                break;

            case HIGH_NOTE:
                if (mInfo[row].mHighNote != Integer.MIN_VALUE) {
                    // Display the highest note in sharps
                    result = NoteNames.getNoteName(
                        mInfo[row].mHighNote, false);
                }
                break;

            case CHANNEL:
                if (mInfo[row].mChannel > -1) {
                    result = mInfo[row].mChannel;
                }
                break;

            case SOLO:
                result = mInfo[row].mSolo;
                break;
            case MUTE:
                result = mInfo[row].mMute;
                break;
            case SHOW_LYRICS:
                result = mInfo[row].mShowLyrics;
                break;
        }
        return result;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        switch (columnOrder[column]) {
            case SOLO:
                mInfo[row].mSolo = ((Boolean)value);
                mSeq.setTrackSolo(row, mInfo[row].mSolo);
                boolean soloed = mSeq.getTrackSolo(row);
                if (soloed != mInfo[row].mSolo) {
                    TraceDialog.addTrace(
                        "Sequencer Solo not supported. (Set to " + value // NOI18N
                        + " is actually " + soloed + ")"); // NOI18N
                }
                break;
            case MUTE:
                mInfo[row].mMute = ((Boolean)value);
                mSeq.setTrackMute(row, mInfo[row].mMute);
                boolean muted = mSeq.getTrackMute(row);
                if (muted != mInfo[row].mMute) {
                    TraceDialog.addTrace(
                        "Sequencer Mute not supported. (Set to " + value // NOI18N
                        + " is actually " + muted + ")"); // NOI18N
                }
                break;
            case SHOW_LYRICS:
                mInfo[row].mShowLyrics = ((Boolean)value);
                fireTableCellUpdated(row, column);
                break;
            default:
            // Do Nothing
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        return columnOrder[columnIndex].getColumnClass();
    }

    @Override
    public String getColumnName(int columnIndex) {
        return columnOrder[columnIndex].getName();
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
        boolean ret;
        if (columnIndex == 9) {
            ret = mInfo[rowIndex].mHasLyrics;
        } else {
            ret = canEdit[columnIndex] && mInfo[rowIndex].mChannel > -1;
        }
        return ret;
    }
}
