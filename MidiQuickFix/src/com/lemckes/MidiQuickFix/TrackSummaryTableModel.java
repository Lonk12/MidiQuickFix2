/** ************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2021 John Lemcke
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

import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.MidiQuickFix.util.UiStrings;
import javax.sound.midi.Sequencer;
import javax.swing.table.AbstractTableModel;

/**
 * The model for the track summary table.
 */
public class TrackSummaryTableModel
    extends AbstractTableModel
{

    static final long serialVersionUID = -5109111307767764175L;
    private final transient Sequencer mSequencer;

    /**
     * The Sequence that is loaded.
     */
    private final transient MqfSequence mSequence;

    /**
     * The resolution of the sequence
     */
    private final int mRes;

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
        CHANNEL(6, UiStrings.getString("channel_abbrev"), Integer.class);

        private final int mIndex;
        private final String mName;
        private final Class<?> mClass;

        ColumnInfo(int columnIndex, String columnName, Class<?> columnClass) {
            mIndex = columnIndex;
            mName = columnName;
            mClass = columnClass;
        }

        public int getIndex() {
            return mIndex;
        }

        public String getName() {
            return mName;
        }

        public Class<?> getColumnClass() {
            return mClass;
        }
    };

    private final ColumnInfo[] columnOrder = new ColumnInfo[]{
        ColumnInfo.TRACK_NUM,
        ColumnInfo.TRACK_NAME,
        ColumnInfo.START_TICK,
        ColumnInfo.END_TICK,
        ColumnInfo.LOW_NOTE,
        ColumnInfo.HIGH_NOTE,
        ColumnInfo.CHANNEL
    };

    /**
     * Creates a new instance of a TrackSummaryTableModel
     *
     * @param s
     * @param sequencer
     */
    public TrackSummaryTableModel(MqfSequence s, Sequencer sequencer) {
        mSequencer = sequencer;
        mSequence = s;
        mRes = s.getResolution();
    }

    @Override
    public int getRowCount() {
        return mSequence.getTracks().length;
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
                result = mSequence.getTrackName(row);
                break;
            case START_TICK:
                long start = mSequence.getTrackFirstTick(row);
                if (start > -1) {
                    result = Formats.formatBeatsTicks(start, mRes, true);
                }
                break;

            case END_TICK:
                long end = mSequence.getTrackLastTick(row);
                if (end > -1) {
                    result = Formats.formatBeatsTicks(end, mRes, true);
                }
                break;

            case LOW_NOTE:
                var low = mSequence.getTrackLowestNote(row);
                if (low != Integer.MAX_VALUE) {
                    // Display the lowest note in flats
                    result = NoteNames.getNoteName(low, true);
                }
                break;

            case HIGH_NOTE:
                var high = mSequence.getTrackHighestNote(row);
                if (high != Integer.MIN_VALUE) {
                    // Display the highest note in sharps
                    result = NoteNames.getNoteName(high, false);
                }
                break;

            case CHANNEL:
                int channel = mSequence.getTrackChannel(row);
                if (channel > -1) {
                    result = channel;
                }
                break;
        }
        return result;
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
        false
    };

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }
}
