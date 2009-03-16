/**************************************************************
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
 **************************************************************/
package com.lemckes.MidiQuickFix;

import com.lemckes.MidiQuickFix.util.TableColumnWidthSetter;
import java.awt.Dimension;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Track;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.EventListenerList;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

/**
 * A table that presents the events in a track for editing.
 * @version $Id$
 */
public class TrackTable extends javax.swing.JTable {
    static final long serialVersionUID = -3055797455895279118L;
    private EventListenerList mListeners = new EventListenerList();

    /** Creates a new TrackTable */
    public TrackTable() {
        initComponents();
        updateModel(new TrackTableModel(null, 96, true, true));
        // Position, Event, Note, Value, Patch, Text, Channel
        Dimension pd = getPreferredSize();
        setPreferredScrollableViewportSize(pd);
    }

    /**
     * Set the track that is displayed in the Track Table
     * @param track the track to display
     * @param resolution the tick resolution of the track
     * @param showNotes if <code>false</code> then NOTE_ON/OFF events are not displayed
     * @param inFlats determines whether notes are displayed as flats or sharps
     */
    public void setTrack(Track track, int resolution,
                         boolean showNotes, boolean inFlats) {
        updateModel(
            new TrackTableModel(track,
            resolution,
            showNotes,
            inFlats));
    }

    /**
     * Set whether notes are displayed or not
     * @param show if <code>true</code> notes are shown
     */
    public void showNotes(boolean show) {
        TableModel model = getModel();
        // Check that the TrackTableModel has been set on the table
        if (model instanceof TrackTableModel) {
            ((TrackTableModel)model).setShowNotes(show);
            updateModel((TrackTableModel)model);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
        if (mListeners != null) {
            TableModelListener[] ls =
                mListeners.getListeners(TableModelListener.class);
            for (TableModelListener l : ls) {
                l.tableChanged(e);
            }
        }
    }

    public void addTrackEditedListener(TableModelListener l) {
        mListeners.add(TableModelListener.class, l);
    }

    public void removeTrackEditedListener(TableModelListener l) {
        mListeners.remove(TableModelListener.class, l);
    }

    private void updateModel(TrackTableModel model) {
        setModel(model);
        setInstrumentEditor();
        Object[] widths = {
            "00000:000", "M:TimeSignature", "G#", "127", // NOI18N
            "Cor___Anglais", "ABCDEFGHIJKL", "16"}; // NOI18N
        TableColumnWidthSetter.setColumnWidths(this, widths, true);
    }

    /**
     * Delete the rows specified in the given array of row indexes
     * @param rows the array of row indexes
     */
    public void deleteRows(int[] rows) {
        ((TrackTableModel)getModel()).deleteEvents(rows);
    }

    /**
     * Insert the given event in the track
     * @param event the event to be inserted
     */
    public void insertEvent(MidiEvent event) {
        ((TrackTableModel)getModel()).insertEvent(event);
    }

    /** Set the table cell editor for the Patch column. */
    void setInstrumentEditor() {
        //trace("setInstrumentEditor");
        TableColumn instrumentColumn = getColumnModel().getColumn(4);

        Object[] s = InstrumentNames.getNameArray();
        JComboBox comboBox =
            new JComboBox(new DefaultComboBoxModel(s));
        instrumentColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        setName("trackTable"); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
