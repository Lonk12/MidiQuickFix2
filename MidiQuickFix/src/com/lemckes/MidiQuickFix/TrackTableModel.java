/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2005 John Lemcke
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

import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.UiStrings;
import javax.swing.table.*;

import javax.swing.event.TableModelListener;

import javax.sound.midi.*;

/**
 * The model for the main track table.
 * @version $Id$
 */
class TrackTableModel extends AbstractTableModel implements TableModelListener {
    
    /** The Track which is being displayed. */
    transient Track mTrack;
    
    /** The Beats/Tick resolution of this track. */
    int mResolution;
    
    /** Whether to display notes as flats. */
    boolean mInFlats = true;
    
    /** Whether to show the NOTE_ON/NOTE_OFF events. */
    boolean mShowNotes;
    
    /** The number of NOTE_ON/NOTE_OFF events in this track. */
    int mNumNotes;
    
    /** Maps table row to track index when mShowNotes is false. */
    java.util.Vector<Integer> mNoNotesRowMap;
    
    public TrackTableModel(Track t, int res, boolean showNotes, boolean inFlats) {
        mTrack = t;
        mResolution = res;
        mInFlats = inFlats;
        mShowNotes = showNotes;
        buildNoNotesRowMap();
        addTableModelListener(this);
    }
    
    /**
     * Build the mNoNotesRowMap vector. When mShowNotes is false
     * this provides the mapping from table row to the index of
     * the event in the track.
     */
    private void buildNoNotesRowMap() {
        mNumNotes = 0;
        mNoNotesRowMap = new java.util.Vector<Integer>();
        
        for (int i = 0; i < mTrack.size(); ++i) {
            MidiMessage mess = mTrack.get(i).getMessage();
            if (mess instanceof ShortMessage) {
                int cmd = ((ShortMessage)mess).getCommand();
                if (cmd == ShortMessage.NOTE_OFF
                    || cmd ==  ShortMessage.NOTE_ON) {
                    mNumNotes++;
                } else {
                    mNoNotesRowMap.add(new Integer(i));
                }
            } else {
                mNoNotesRowMap.add(new Integer(i));
            }
        }
    }
    
    public void setShowNotes(boolean show) {
        mShowNotes = show;
        //fireTableDataChanged();
        fireTableStructureChanged();
    }
    
    public int getRowCount() {
        if (mShowNotes) {
            return mTrack.size();
        } else {
            return mTrack.size() - mNumNotes;
        }
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }
    
    public Object getValueAt(int row, int column) {
        Object result = null;
        int eventIndex = row;
        // Adjust the index if notes are not being displayed
        if (!mShowNotes) {
            eventIndex = ((Integer)mNoNotesRowMap.get(row)).intValue();
        }
        MidiMessage mess = mTrack.get(eventIndex).getMessage();
        long tick = mTrack.get(eventIndex).getTick();
        switch (column) {
            case 0:
                result = getTickString(tick, mResolution);
                break;
            case 1:
                result = getMessageArray(mess)[0];
                break;
            case 2:
                result = getMessageArray(mess)[1];
                break;
            case 3:
                result = getMessageArray(mess)[2];
                break;
            case 4:
                result = getMessageArray(mess)[3];
                break;
            case 5:
                result = getMessageArray(mess)[4];
                break;
            case 6:
                result = getMessageArray(mess)[5];
                break;
            default:
                result = "";
        }
        return result;
    }
    
    public boolean isCellEditable(int row, int column) {
        boolean result = false;
        int eventIndex = row;
        // Adjust the index if notes are not being displayed
        if (!mShowNotes) {
            eventIndex = ((Integer)mNoNotesRowMap.get(row)).intValue();
        }
        MidiMessage mess = mTrack.get(eventIndex).getMessage();
        switch (column) {
            case 0:
                // Tick
                result = true;
                break;
            case 1:
                // Event
                result = false;
                break;
            case 2:
                // Note
                if (mess instanceof ShortMessage) {
                    int cmd = ((ShortMessage)mess).getCommand();
                    if (cmd == ShortMessage.NOTE_OFF
                        || cmd ==  ShortMessage.NOTE_ON) {
                        result = true;
                    }
                }
                break;
            case 3:
                // Value
                if (mess instanceof ShortMessage) {
                    int cmd = ((ShortMessage)mess).getCommand();
                    if (cmd != ShortMessage.PROGRAM_CHANGE) {
                        result = true;
                    }
                }
                break;
            case 4:
                // Patch
                if (mess instanceof ShortMessage) {
                    int cmd = ((ShortMessage)mess).getCommand();
                    if (cmd == ShortMessage.PROGRAM_CHANGE) {
                        result = true;
                    }
                }
                break;
            case 5:
                // Text
                if (mess instanceof MetaMessage) {
                    result = MetaEvent.isEditable((MetaMessage)mess);
                }
                break;
            case 6:
                // Channel
                if (mess instanceof ShortMessage) {
                    int st = ((ShortMessage)mess).getStatus();
                    if ((st & 0xf0) <= 0xf0) {
                        result = true;
                    }
                }
                break;
            default:
                result = false;
        }
        return result;
    }
    
    public void setValueAt(Object value, int row, int column) {
        // Don't bother if the value hasn't changed.
        Object oldVal = getValueAt(row, column);
        // Need to compare String versions of the Objects
        if (value.toString().equals(oldVal.toString())) {
            return;
        }
        
        int eventIndex = row;
        // Adjust the index if notes are not being displayed
        if (!mShowNotes) {
            eventIndex = ((Integer)mNoNotesRowMap.get(row)).intValue();
        }
        // System.out.println("setValueAt(" + value.toString() + ", " + eventIndex + ", " + column + ")");
        
        MidiEvent ev = mTrack.get(eventIndex);
        MidiMessage mess = ev.getMessage();
        switch (column) {
            case 0:
                // Tick
                ev.setTick(getTickValue(value.toString(), mResolution));
                fireTableCellUpdated(row, column);
                break;
            case 1:
                // Event
                System.out.println("Error: setValueAt column 1 should not be editable.");
                break;
            case 2:
                // Note
                if (mess instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mess;
                    int command = (int)(sm.getCommand() & 0xff);
                    int channel = (int)(sm.getChannel() & 0xff);
                    // int d1 = (int)(sm.getData1() & 0xff);
                    int d2 = (int)(sm.getData2() & 0xff);
                    try {
                        updateMessage(ev, command, channel, NoteNames.getNoteNumber((String)value), d2);
                        fireTableDataChanged();
                    } catch(InvalidMidiDataException e) {
                        System.out.println("Error: setValueAt column 2. " + e.getMessage());
                    }
                }
                break;
            case 3:
                // Value
                if (mess instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mess;
                    int st = sm.getStatus();
                    int channel = sm.getChannel();
                    int d1 = sm.getData1();
                    int d2 = sm.getData2();
                    int command = (int)(sm.getCommand() & 0xff);
                    
                    if ((st & 0xf0) <= 0xf0) { // This is a channel message
                        switch (command) {
                            case ShortMessage.CHANNEL_PRESSURE:
                                d1 = ((Integer)value).intValue();
                                break;
                            case ShortMessage.CONTROL_CHANGE:
                            case ShortMessage.NOTE_OFF:
                            case ShortMessage.NOTE_ON:
                            case ShortMessage.POLY_PRESSURE:
                                d2 = ((Integer)value).intValue();
                                break;
                            case ShortMessage.PITCH_BEND:
                                int val = ((Integer)value).intValue();
                                d1 = val & 0x7f;
                                d2 = (val - d1) >> 7;
                                break;
                            case ShortMessage.PROGRAM_CHANGE:
                                // Should not get here. PROGRAM_CHANGE is handled in the PATCH column
                                TraceDialog.addTrace("TrackTableModel - ");
                                TraceDialog.addTrace("Got to a PROGRAM_CHANGE event in the value column.");
                                break;
                            default:
                                // Should not get here
                                TraceDialog.addTrace("TrackTableModel - ");
                                TraceDialog.addTrace("Got to a default case in the value column.");
                        }
                    }
                    
                    try {
                        updateMessage(ev, command, channel, d1, d2);
                        fireTableDataChanged();
                    } catch(InvalidMidiDataException e) {
                        System.out.println("Error: setValueAt column 3. " + e.getMessage());
                    }
                }
                break;
            case 4:
                // Patch
                if (mess instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mess;
                    int command = (int)(sm.getCommand() & 0xff);
                    int channel = (int)(sm.getChannel() & 0xff);
                    int d1 = (int)(InstrumentNames.getInstrumentNumber((String)value) & 0xff);
                    int d2 = 0;
                    try {
                        updateMessage(ev, command, channel, d1, 0);
                        fireTableDataChanged();
                    } catch(InvalidMidiDataException e) {
                        System.out.println("Error: setValueAt column 4. " + e.getMessage());
                    }
                }
                break;
            case 5:
                // Text
                if (mess instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage)mess;
                    MetaEvent.setMetaData(mm, value.toString());
                    fireTableCellUpdated(row, column);
                }
                break;
            case 6:
                // Channel
                if (mess instanceof ShortMessage) {
                    int channel = ((Integer)value).intValue();
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
                        } catch(InvalidMidiDataException e) {
                            System.out.println("Error: setValueAt column 6. " + e.getMessage());
                        }
                    }
                    // Must have been CANCEL
                    // Do Nothing
                }
                break;
            default:
                // Do Nothing
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
            throw(e);
        }
    }
    
    public void deleteEvents(int[] rows) {
        // System.out.println("deleteEvents");
        java.util.Vector<MidiEvent> events = new java.util.Vector<MidiEvent>();
        for (int i = 0; i < rows.length; ++i) {
            int eventIndex = rows[i];
            // Adjust the index if notes are not being displayed
            if (!mShowNotes) {
                eventIndex = ((Integer)mNoNotesRowMap.get(rows[i])).intValue();
            }
            // System.out.println("Removing event " + eventIndex + " at row " + rows[i]);
            events.add(mTrack.get(eventIndex));
        }
        
        for (java.util.Enumeration e = events.elements(); e.hasMoreElements(); ) {
            mTrack.remove((MidiEvent)e.nextElement());
        }
        buildNoNotesRowMap();
        fireTableDataChanged();
    }
    
    void setTrackChannel(int channel) {
        /**
         * BUG - This will not work with the call to updateMessage()
         * because the position of the events in the track may be changed
         * and some events missed.
         * This will be fixed when the Java setMessage() bug is fixed.
         */
        for (int i = 0; i < mTrack.size(); ++i) {
            MidiEvent ev = mTrack.get(i);
            MidiMessage mess = ev.getMessage();
            if (mess instanceof ShortMessage) {
                int st = ((ShortMessage)mess).getStatus();
                // Check that this is a channel message
                if ((st & 0xf0) <= 0xf0) {
                    ShortMessage sm = (ShortMessage)mess;
                    int command = sm.getCommand();
                    // int channel = sm.getChannel();
                    int d1 = sm.getData1();
                    int d2 = sm.getData2();
                    try {
                        updateMessage(ev, command, channel, d1, d2);
                    } catch(InvalidMidiDataException e) {
                        System.out.println("Error: setTrackChannel. " + e.getMessage());
                    }
                }
            }
        }
        fireTableDataChanged();
    }
    
    Object[] getMessageArray(MidiMessage mess) {
        // "0 Event", "1 Note", "2 Value", "3 Patch", "4 Text", "5 Channel"
        Object result[] =
        { null, null, null, null, null, null };
        
        int st = mess.getStatus();
        if (st == MetaMessage.META) {
            // Returns Event, Length, Text
            Object[] str = MetaEvent.getMetaStrings((MetaMessage)mess);
            result[0] = str[0];
            result[4] = str[2];
        } else if (st == SysexMessage.SYSTEM_EXCLUSIVE
            || st == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
            // Returns Event, Length, Text
            Object[] str = SysexEvent.getSysexStrings((SysexMessage)mess);
            result[0] = str[0];
            result[4] = str[2];
        } else {
            // Returns Event, Note, Value, Patch, Text, Channel
            Object[] str = ShortEvent.getShortStrings((ShortMessage)mess, mInFlats);
            result[0] = str[0];
            result[1] = str[1];
            result[2] = str[2];
            result[3] = str[3];
            result[4] = str[4];
            result[5] = str[5];
        }
        return result;
    }
    
    static String getTickString(long tick, int res) {
        java.text.DecimalFormat beatF = new java.text.DecimalFormat("00000");
        java.text.DecimalFormat tickF = new java.text.DecimalFormat("000");
        
        String result;
        long beats = tick / res;
        long ticks = tick % res;
        result = beatF.format(beats) + ":" + tickF.format(ticks);
        return result;
    }
    
    static long getTickValue(String tickString, int res) {
        int colonPos = tickString.indexOf(':');
        String beats = tickString.substring(0, colonPos);
        String ticks = tickString.substring(colonPos + 1);
        long b = Long.parseLong(beats);
        long t = Long.parseLong(ticks);
        long r = b * res + t;
        return r;
    }
    
    Class[] types = new Class [] {
        java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Integer.class,
            java.lang.Object.class,
            java.lang.Object.class,
            java.lang.Integer.class
    };
    public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
    }
    
    String[] columnNames = new String[] {
        UiStrings.getString("beat:tick"),
            UiStrings.getString("event"),
            UiStrings.getString("note"),
            UiStrings.getString("value"),
            UiStrings.getString("patch"),
            UiStrings.getString("Text"),
            UiStrings.getString("channel_abbrev")
    };
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    public void tableChanged(javax.swing.event.TableModelEvent e) {
        // System.out.println("TrackTableModel.tableChanged(" + e.toString() + ")");
    }
    
}
