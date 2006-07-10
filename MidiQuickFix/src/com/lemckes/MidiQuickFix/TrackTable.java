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

import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.FontMetrics;
import javax.sound.midi.Track;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * A table that presents the events in a track for editing.
 * @version $Id$
 */
public class TrackTable extends javax.swing.JTable {
    
    /** Creates new form BeanForm */
    public TrackTable() {
        initComponents();
    }
    
    public void setTrack(Track track, int resolution,
      boolean showNotes, boolean inFlats) {
        updateModel(
          new TrackTableModel(track,
          resolution,
          showNotes,
          inFlats));
    }
    public void showNotes(boolean show) {
        TableModel model = getModel();
        // Check that the TrackTableModel has been set on the table
        if (model instanceof TrackTableModel) {
            ((TrackTableModel)model).setShowNotes(show);
            updateModel((TrackTableModel)model);
        }
    }
    
    private void updateModel(TrackTableModel model) {
        TableModelListener[] ls =
          ((DefaultTableModel)getModel()).getTableModelListeners();
        for (TableModelListener l : ls) {
            model.addTableModelListener(l);
        }
        setModel(model);
        setColumnWidths();
        setInstrumentEditor();
        validate();
    }
    
    public void deleteRows(int[] rows) {
        ((TrackTableModel)getModel()).deleteEvents(rows);
    }
    
    private void setColumnWidths() {
        int margin = 6;
        FontMetrics fm = getFontMetrics(getFont());
        TableColumnModel cm = getColumnModel();
        TableColumn tc = cm.getColumn(0);
        tc.setPreferredWidth(fm.stringWidth("00000:000") + margin);
        tc = cm.getColumn(1);
        tc.setPreferredWidth(fm.stringWidth("A Typical Event") + margin);
        tc = cm.getColumn(2);
        tc.setPreferredWidth(fm.stringWidth(UiStrings.getString("note")) + margin);
        tc = cm.getColumn(3);
        tc.setPreferredWidth(fm.stringWidth(UiStrings.getString("value")) + margin);
        tc = cm.getColumn(4);
        tc.setPreferredWidth(fm.stringWidth("A Typical Instrument") + margin);
        tc = cm.getColumn(5);
        tc.setPreferredWidth(fm.stringWidth("Some Track Name") + margin);
        tc = cm.getColumn(6);
        tc.setPreferredWidth(fm.stringWidth(UiStrings.getString("channel_abbrev")) + margin);
    }
    
    /** Set the table cell editor for the Patch column. */
    void setInstrumentEditor() {
        //trace("setInstrumentEditor");
        TableColumn instrumentColumn = getColumnModel().getColumn(4);
        
        String[] s = InstrumentNames.getNameArray();
        JComboBox comboBox =
          new JComboBox(new DefaultComboBoxModel(s));
        instrumentColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setName("trackTable");
    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}