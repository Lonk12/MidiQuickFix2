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
package com.lemckes.MidiQuickFix;

import static com.lemckes.MidiQuickFix.ShortEvent.isChannelMessage;
import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import javax.swing.table.DefaultTableModel;

/**
 * The model for the main track table.
 */
class TrackTableModel extends DefaultTableModel {
    static final long serialVersionUID = 5464614685967695539L;
    /** The Track that is being displayed. */
    transient Track mTrack;
    /** The Ticks/Beat resolution of this track. */
    int mResolution;
    /** Whether to display notes as flats. */
    boolean mInFlats = true;
    /** Whether to show the NOTE_ON/NOTE_OFF events. */
    boolean mShowNotes;
    /** The number of NOTE_ON/NOTE_OFF events in this track. */
    int mNumNotes;
    /** Maps table row to track index when mShowNotes is false. */
    ArrayList<Integer> mNoNotesRowMap;

    /**
     * Enum to hold info for each column in the table
     */
    enum ColumnInfo
    {
        BEAT_TICK(0, UiStrings.getString("beat:tick"), Object.class),
        EVENT_NAME(1, UiStrings.getString("event"), Object.class),
        NOTE(2, UiStrings.getString("note"), Object.class),
        VALUE(3, UiStrings.getString("value"), Integer.class),
        PATCH(4, UiStrings.getString("patch"), Object.class),
        TEXT(5, UiStrings.getString("text"), Object.class),
        CHANNEL(6, UiStrings.getString("channel_abbrev"), Integer.class);

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

    /**
     * Map column number to columnId enum.
     */
    private final ColumnInfo[] columnOrder = new ColumnInfo[] {
        ColumnInfo.BEAT_TICK,
        ColumnInfo.EVENT_NAME,
        ColumnInfo.NOTE,
        ColumnInfo.VALUE,
        ColumnInfo.PATCH,
        ColumnInfo.TEXT,
        ColumnInfo.CHANNEL
    };

    TrackTableModel(Track t, int res, boolean showNotes, boolean inFlats) {
        mTrack = t;
        mResolution = res;
        mInFlats = inFlats;
        mShowNotes = showNotes;
        mNumNotes = 0;

        if (mTrack != null) {
            buildNoNotesRowMap();
        } else {
            mNoNotesRowMap = new ArrayList<>(4);
        }
    }

    /**
     * Called when a track is changed programmatically for instance
     * by one of the methods in TrackUpdateUtils
     */
    public void trackModified() {
        buildNoNotesRowMap();
        fireTableDataChanged();
    }

    /**
     * Build the mNoNotesRowMap vector. When mShowNotes is false
     * this provides the mapping from table row to the index of
     * the event in the track.
     */
    private void buildNoNotesRowMap() {
        mNumNotes = 0;
        mNoNotesRowMap = new ArrayList<>(mTrack.size());
        for (int i = 0; i < mTrack.size(); ++i) {
            MidiMessage mess = mTrack.get(i).getMessage();
            if (mess instanceof ShortMessage) {
                int cmd = ((ShortMessage)mess).getCommand();
                if (cmd == ShortMessage.NOTE_OFF
                    || cmd == ShortMessage.NOTE_ON) {
                    mNumNotes++;
                } else {
                    mNoNotesRowMap.add(i);
                }
            } else {
                mNoNotesRowMap.add(i);
            }
        }
    }

    public void setShowNotes(boolean show) {
        mShowNotes = show;
        fireTableStructureChanged();
    }

    @Override
    public int getRowCount() {
        if (mTrack == null) {
            return 0;
        } else if (mShowNotes) {
            return mTrack.size();
        } else {
            return mTrack.size() - mNumNotes;
        }
    }

    @Override
    public int getColumnCount() {
        return columnOrder.length;
    }

    private MidiEvent getEventForRow(int row) {
        int eventIndex = row;
        // Adjust the index if notes are not being displayed
        if (!mShowNotes) {
            eventIndex = mNoNotesRowMap.get(row);
        }
        return mTrack.get(eventIndex);
    }

    public long getTickForRow(int row) {
        return getEventForRow(row).getTick();
    }

    @Override
    public Object getValueAt(int row, int column) {
        Object result = "";
        MidiEvent event = getEventForRow(row);
        MidiMessage mess = event.getMessage();
        long tick = event.getTick();
        switch (columnOrder[column]) {
            case BEAT_TICK:
                result = Formats.formatBeatsTicks(tick, mResolution, true);
                break;
            case EVENT_NAME:
                result = getMessageArray(mess)[0];
                break;
            case NOTE:
                result = getMessageArray(mess)[1];
                break;
            case VALUE:
                result = getMessageArray(mess)[2];
                break;
            case PATCH:
                result = getMessageArray(mess)[3];
                break;
            case TEXT:
                result = getMessageArray(mess)[4];
                break;
            case CHANNEL:
                result = getMessageArray(mess)[5];
                break;
        }
        return result;
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        boolean result = false;
        MidiMessage mess = getEventForRow(row).getMessage();
        switch (columnOrder[column]) {
            case BEAT_TICK:
                result = true;
                break;
            case EVENT_NAME:
                result = false;
                break;
            case NOTE:
                if (mess instanceof ShortMessage) {
                    int cmd = ((ShortMessage)mess).getCommand();
                    if (cmd == ShortMessage.NOTE_OFF
                        || cmd == ShortMessage.NOTE_ON) {
                        result = true;
                    }
                }
                break;
            case VALUE:
                if (mess instanceof ShortMessage) {
                    int cmd = ((ShortMessage)mess).getCommand();
                    if (cmd != ShortMessage.PROGRAM_CHANGE) {
                        result = true;
                    }
                }
                break;
            case PATCH:
                if (mess instanceof ShortMessage) {
                    int cmd = ((ShortMessage)mess).getCommand();
                    if (cmd == ShortMessage.PROGRAM_CHANGE) {
                        result = true;
                    }
                }
                break;
            case TEXT:
                if (mess instanceof MetaMessage) {
                    result = MetaEvent.isEditable((MetaMessage)mess);
                }
                break;
            case CHANNEL:
                if (mess instanceof ShortMessage) {
                    if (isChannelMessage((ShortMessage)mess)) {
                        result = true;
                    }
                }
                break;
        }
        return result;
    }

    @Override
    public void setValueAt(Object value, int row, int column) {
        // Don't bother if the value hasn't changed.
        Object oldVal = getValueAt(row, column);
        // Need to compare String versions of the Objects
        if (value.toString().equals(oldVal.toString())) {
            return;
        }

        MidiEvent ev = getEventForRow(row);
        MidiMessage mess = ev.getMessage();
        switch (columnOrder[column]) {
            case BEAT_TICK:
                ev.setTick(Formats.parseBeatsTicks(value.toString(), mResolution));
                fireTableCellUpdated(row, column);
                break;
            case EVENT_NAME:
                TraceDialog.addTrace(
                    "Error: TrackTableModel.setValueAt EVENT_NAME column should not be editable."); // NOI18N
                break;
            case NOTE:
                if (mess instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mess;
                    int command = sm.getCommand() & 0xff;
                    int channel = sm.getChannel() & 0xff;
                    // int d1 = sm.getData1() & 0xff;
                    int d2 = sm.getData2() & 0xff;
                    try {
                        updateMessage(ev, command, channel,
                            NoteNames.getNoteNumber((String)value), d2);
                        fireTableDataChanged();
                    } catch (InvalidMidiDataException e) {
                        TraceDialog.addTrace(
                            "Error: TrackTableModel.setValueAt NOTE column. " + // NOI18N
                            e.getMessage());
                    }
                }
                break;
            case VALUE:
                if (mess instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mess;
                    int channel = sm.getChannel();
                    int d1 = sm.getData1();
                    int d2 = sm.getData2();
                    int command = sm.getCommand() & 0xff;

                    if (isChannelMessage(sm)) {
                        switch (command) {
                            case ShortMessage.CHANNEL_PRESSURE:
                                d1 = (Integer)value;
                                break;
                            case ShortMessage.CONTROL_CHANGE:
                            case ShortMessage.NOTE_OFF:
                            case ShortMessage.NOTE_ON:
                            case ShortMessage.POLY_PRESSURE:
                                d2 = (Integer)value;
                                break;
                            case ShortMessage.PITCH_BEND:
                                int val = (Integer)value;
                                d1 = val & 0x7f;
                                d2 = (val - d1) >> 7;
                                break;
                            case ShortMessage.PROGRAM_CHANGE:
                                // Should not get here. PROGRAM_CHANGE
                                // is handled in the PATCH column
                                TraceDialog.addTrace("TrackTableModel - "); // NOI18N
                                TraceDialog.addTrace(
                                    "Got to a PROGRAM_CHANGE event in the VALUE column."); // NOI18N
                                break;
                            default:
                                // Should not get here
                                TraceDialog.addTrace("TrackTableModel - "); // NOI18N
                                TraceDialog.addTrace(
                                    "Got to a default case in the VALUE column."); // NOI18N
                        }
                    }

                    try {
                        updateMessage(ev, command, channel, d1, d2);
                        fireTableDataChanged();
                    } catch (InvalidMidiDataException e) {
                        TraceDialog.addTrace(
                            "Error: TrackTableModel.setValueAt VALUE column. " + // NOI18N
                            e.getMessage());
                    }
                }
                break;
            case PATCH:
                if (mess instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mess;
                    int command = sm.getCommand() & 0xff;
                    int channel = sm.getChannel() & 0xff;
                    int d1 =
                        InstrumentNames.getInstance().getInstrumentNumber((String)value) & 0xff;
                    int d2 =
                        InstrumentNames.getInstance().getInstrumentBank((String)value) & 0xff;
                    try {
                        updateMessage(ev, command, channel, d1, d2);
                        fireTableDataChanged();
                    } catch (InvalidMidiDataException e) {
                        TraceDialog.addTrace("Error: setValueAt - PATCH column. " + // NOI18N
                            e.getMessage());
                    }
                }
                break;
            case TEXT:
                if (mess instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage)mess;
                    MetaEvent.setMetaData(mm, value.toString(), mResolution);
                    fireTableCellUpdated(row, column);
                }
                break;
            case CHANNEL:
                if (mess instanceof ShortMessage) {
                    int channel = (Integer)value;
                    int answer = javax.swing.JOptionPane.showConfirmDialog(null,
                        UiStrings.getString("set_channel_question") + channel);
                    if (answer == javax.swing.JOptionPane.YES_OPTION) {
                        setTrackChannel(channel);
                    } else if (answer == javax.swing.JOptionPane.NO_OPTION) {
                        ShortMessage sm = (ShortMessage)mess;
                        int command = sm.getCommand();
                        int d1 = sm.getData1();
                        int d2 = sm.getData2();

                        try {
                            updateMessage(ev, command, channel, d1, d2);
                            fireTableDataChanged();
                        } catch (InvalidMidiDataException e) {
                            TraceDialog.addTrace(
                                "Error: setValueAt - CHANNEL column. " + e.getMessage()); // NOI18N
                        }
                    }
                }
                break;
        }
    }

    private void updateMessage(
        MidiEvent ev, int command, int channel, int d1, int d2)
        throws InvalidMidiDataException {

        try {
            if (MidiQuickFix.VERSION_1_4_2_BUG) {
                // HACK to fix BUG where data2 is set to data1 in jdk 1.4.2
                //
                // Create a new message, set its values to those of
                // the old message, remove the old message and
                // add the new message.
                ShortMessage sm = new ShortMessage();
                sm.setMessage(command, channel, d1, d2);
                MidiEvent newEv = new MidiEvent(sm, ev.getTick());
                mTrack.remove(ev);
                mTrack.add(newEv);

                // Need to rebuild the map of non-note events.
                // The remove()/add() calls may have re-ordered the events.
                buildNoNotesRowMap();

                //End ugly HACK
            } else {
                ShortMessage mess = (ShortMessage)ev.getMessage();
                mess.setMessage(command, channel, d1, d2);
            }
        } catch (InvalidMidiDataException e) {
            throw (e);
        }
    }

    public void deleteEvents(int[] rows) {
        ArrayList<MidiEvent> events = new ArrayList<>(rows.length);
        for (int i = 0; i < rows.length; ++i) {
            events.add(getEventForRow(rows[i]));
        }

        for (MidiEvent e : events) {
            mTrack.remove(e);
        }
        trackModified();
    }

    public void insertEvent(MidiEvent event) {
        mTrack.add(event);
        if (event.getTick() == 0) {
            sortTickZeroEvents();
        }
        trackModified();
    }

    /**
     * Make sure that all the events that occur at tick zero
     * are sorted in a suitable order.<br>
     * The order is :
     * <ol>
     * <li>MetaMessage.TRACK_NAME</li>
     * <li>MetaMessage.TEXT</li>
     * <li>MetaMessage.COPYRIGHT</li>
     * <li>Other MetaMessages</li>
     * <li>System Exclusive messages</li>
     * <li>ShortMessage System messages</li>
     * <li>Other ShortMessages</li>
     * <li>MetaMessage.KEY_SIGNATURE</li>
     * <li>MetaMessage.TIME_SIGNATURE</li>
     * <li>MetaMessage.TEMPO</li>
     * <li>ShortMessage.PROGRAM_CHANGE</li>
     * <li>ShortMessage.NOTE_ON</li>
     * <li>ShortMessage.NOTE_OFF (or NOTE_ON/0)</li>
     * <li>MetaMessage.LYRIC</li>
     * <li>Anything else ...</li>
     * </ol>
     */
    private void sortTickZeroEvents() {
        final int TRACK_NAME_PRIORITY = 101;
        final int TEXT_PRIORITY = 102;
        final int COPYRIGHT_PRIORITY = 103;
        final int OTHER_META_PRIORITY = 200;
        final int SYSEX_PRIORITY = 210;
        final int SHORT_SYSTEM_PRIORITY = 301;
        final int OTHER_SHORT_PRIORITY = 302;
        final int KEY_SIGNATURE_PRIORITY = 401;
        final int TIME_SIGNATURE_PRIORITY = 402;
        final int TEMPO_PRIORITY = 403;
        final int PROGRAM_CHANGE_PRIORITY = 501;
        final int NOTE_ON_PRIORITY = 511;
        final int NOTE_OFF_PRIORITY = 512;
        final int LYRIC_PRIORITY = 521;
        final int OTHER_PRIORITY = Integer.MAX_VALUE;

        Comparator<MidiEvent> comparator = new Comparator<MidiEvent>() {
            @Override
            public int compare(MidiEvent o1, MidiEvent o2) {
                return getSortPriority(o1) - getSortPriority(o2);
            }

            private int getSortPriority(MidiEvent me) {
                int priority = OTHER_PRIORITY;
                MidiMessage mess = me.getMessage();
                if (mess instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage)mess;
                    int type = mm.getType();
                    switch (type) {
                        case MetaEvent.TRACK_NAME:
                            priority = TRACK_NAME_PRIORITY;
                            break;
                        case MetaEvent.TEXT:
                            priority = TEXT_PRIORITY;
                            break;
                        case MetaEvent.COPYRIGHT:
                            priority = COPYRIGHT_PRIORITY;
                            break;
                        case MetaEvent.KEY_SIGNATURE:
                            priority = KEY_SIGNATURE_PRIORITY;
                            break;
                        case MetaEvent.TIME_SIGNATURE:
                            priority = TIME_SIGNATURE_PRIORITY;
                            break;
                        case MetaEvent.TEMPO:
                            priority = TEMPO_PRIORITY;
                            break;
                        case MetaEvent.LYRIC:
                            priority = LYRIC_PRIORITY;
                            break;
                        default:
                            priority = OTHER_META_PRIORITY;
                    }
                } else if (mess instanceof SysexMessage) {
                    priority = SYSEX_PRIORITY;
                } else if (mess instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mess;
                    int command = sm.getCommand() & 0xff;
                    if (isChannelMessage(sm)) {
                        switch (command) {
                            case ShortMessage.PROGRAM_CHANGE:
                                priority = PROGRAM_CHANGE_PRIORITY;
                                break;
                            case ShortMessage.NOTE_ON:
                                if (sm.getData2() != 0) {
                                    priority = NOTE_ON_PRIORITY;
                                } else {
                                    priority = NOTE_OFF_PRIORITY;
                                }
                                break;
                            case ShortMessage.NOTE_OFF:
                                priority = NOTE_OFF_PRIORITY;
                                break;
                            default:
                                priority = OTHER_SHORT_PRIORITY;
                        }
                    } else {
                        // ShortMessage System messages
                        priority = SHORT_SYSTEM_PRIORITY;
                    }
                }
                return priority;
            }
        };

        // Collect all the events that occur at tick zero into a new list
        ArrayList<MidiEvent> tickZeroEvents = new ArrayList<>(64);
        for (int i = 0; i < mTrack.size(); ++i) {
            MidiEvent me = mTrack.get(i);
            if (me.getTick() == 0) {
                tickZeroEvents.add(me);
            } else {
                break;
            }
        }

        // Remove all the events at tick zero
        for (MidiEvent me : tickZeroEvents) {
            mTrack.remove(me);
        }

        //Sort the events and put them back in order.
        Collections.sort(tickZeroEvents, comparator);
        for (MidiEvent me : tickZeroEvents) {
            mTrack.add(me);
        }
    }

    void setTrackChannel(int channel) {
        /**
         * BUG - This will not work with the call to updateMessage() in Java 1.4.2
         * because the position of the events in the track may be changed
         * and some events missed.
         * This will be fixed when the Java setMessage() bug is fixed.
         */
        for (int i = 0; i < mTrack.size(); ++i) {
            MidiEvent ev = mTrack.get(i);
            MidiMessage mess = ev.getMessage();
            if (mess instanceof ShortMessage) {
                if (isChannelMessage((ShortMessage)mess)) {
                    ShortMessage sm = (ShortMessage)mess;
                    int command = sm.getCommand();
                    // int channel = sm.getChannel();
                    int d1 = sm.getData1();
                    int d2 = sm.getData2();
                    try {
                        updateMessage(ev, command, channel, d1, d2);
                    } catch (InvalidMidiDataException e) {
                        TraceDialog.addTrace("Error: setTrackChannel. " + // NOI18N
                            e.getMessage());
                    }
                }
            }
        }
        fireTableDataChanged();
    }

    Object[] getMessageArray(MidiMessage mess) {
        // "0 Event", "1 Note", "2 Value", "3 Patch", "4 Text", "5 Channel"
        Object result[] = {null, null, null, null, null, null};

        int st = mess.getStatus();
        switch (st) {
            case MetaMessage.META:
                {
                    // Returns Event, Length, Text
                    Object[] str = MetaEvent.getMetaStrings((MetaMessage)mess);
                    result[0] = str[0];
                    result[4] = str[2];
                    break;
                }
            case SysexMessage.SYSTEM_EXCLUSIVE:
            case SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE:
                {
                    // Returns Event, Length, Text
                    Object[] str = SysexEvent.getSysexStrings((SysexMessage)mess);
                    result[0] = str[0];
                    result[4] = str[2];
                    break;
                }
            default:
                {
                    // Returns Event, Note, Value, Patch, Text, Channel
                    Object[] str =
                        ShortEvent.getShortStrings((ShortMessage)mess, mInFlats);
                    result[0] = str[0];
                    result[1] = str[1];
                    result[2] = str[2];
                    result[3] = str[3];
                    result[4] = str[4];
                    result[5] = str[5];
                    break;
                }
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
}
