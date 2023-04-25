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

import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.MidiQuickFix.util.TableColumnWidthSetter;
import java.awt.Dimension;
import javax.sound.midi.Sequencer;
import javax.swing.event.TableModelEvent;

/**
 * A table that displays summary info about each track in a sequence.
 */
public class TrackSummaryTable
    extends javax.swing.JTable
{

    static final long serialVersionUID = 4128873234789727596L;
    private Sequencer mSequencer;

    /**
     * Create a new TrackSummaryTable
     *
     * @param sequencer
     */
    public TrackSummaryTable(Sequencer sequencer) {
        initComponents();
        mSequencer = sequencer;
        Dimension pd = getPreferredSize();
        setPreferredScrollableViewportSize(pd);
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        super.tableChanged(e);
    }

    /**
     * Set the sequence that is to be displayed
     *
     * @param seq the sequence that is to be displayed
     */
    public void setSequence(MqfSequence seq) {
        setModel(new TrackSummaryTableModel(seq, mSequencer));
        Object[] widths = {
            "99", "The Track Name", // NOI18N
            "00000:000", "00000:000", // NOI18N
            "G#3", "G#3", // NOI18N
            "16", true, true, true}; // NOI18N
        TableColumnWidthSetter.setColumnWidths(this, widths, true);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setAutoCreateRowSorter(true);
        setFont(getFont().deriveFont(getFont().getSize()-1f));
        setName("TrackSummaryTable"); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
