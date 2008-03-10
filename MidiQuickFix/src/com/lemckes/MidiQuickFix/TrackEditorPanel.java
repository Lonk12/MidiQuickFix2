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

import com.lemckes.MidiQuickFix.util.EventCreationEvent;
import com.lemckes.MidiQuickFix.util.EventCreationListener;
import com.lemckes.MidiQuickFix.util.UiStrings;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Track;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.TableModelListener;

/**
 * The UI for editing track data
 * @version $Id$
 */
public class TrackEditorPanel extends javax.swing.JPanel implements EventCreationListener {
    static final long serialVersionUID = 116439713789684943L;
    private Sequence mSeq;
    private Track[] mTracks;
    private int mCurrentTrack;
    private String mKeySig;
    private CreateEventDialog mCreateEventDialog;

    /** Creates new form TrackEditorPanel */
    public TrackEditorPanel() {
        initComponents();
    }

    /**
     * Set the sequence that will be edited
     * @param seq the sequence to edit
     */
    public void setSequence(Sequence seq) {
        mSeq = seq;
        mTracks = mSeq.getTracks();
        setTrackComboModel(mTracks);
        selectTrack(0);
    }

    /**
     * Populate the entries in the track selector combo with
     * the track number and track names from the tracks array.
     */
    void setTrackComboModel(Track[] tracks) {
        // Update the track selector combobox model
        String[] trackList = new String[tracks.length];
        for (int i = 0; i < tracks.length; ++i) {
            trackList[i] = Integer.toString(i);
            Track t = tracks[i];
            int count = t.size() - 1;
            for (int j = 0; j < count; ++j) {
                MidiEvent ev = t.get(j);
                MidiMessage mess = ev.getMessage();
                // Don't bother looking past events at time zero.
                if (ev.getTick() > 0) {
                    break;
                }
                if (mess.getStatus() == MetaMessage.META) {
                    int type = ((MetaMessage)mess).getType();
                    Object[] str = MetaEvent.getMetaStrings((MetaMessage)mess);
                    if (type == MetaEvent.TRACK_NAME) {
                        trackList[i] += " - " + (String)str[2];
                    }
                    if (type == MetaEvent.KEY_SIGNATURE) {
                        mKeySig = (String)str[2];
                    }
                }
            }
        }
        trackSelector.setModel(new DefaultComboBoxModel(trackList));
    }

    /** Display the selected track in the editor.
     * @param trackNum The index of the track to be displayed.
     */
    void selectTrack(int trackNum) {
        mCurrentTrack = trackNum;
        if (mSeq != null) {
            trackTable.setTrack(
                mTracks[mCurrentTrack],
                mSeq.getResolution(),
                showNotesCheck.isSelected(),
                KeySignatures.isInFlats(mKeySig));
        }
    }

    /**
     * Add a change listener to the Track Table Model
     * @param l the listener to add
     */
    public void addTableChangeListener(TableModelListener l) {
        trackTable.getModel().addTableModelListener(l);
    }

    private void doCreateEvent() {
        if (mCreateEventDialog == null) {
            mCreateEventDialog = new CreateEventDialog(mSeq.getResolution(),
                null, false);
            mCreateEventDialog.addEventCreationListener(this);
        }
        TrackTableModel ttm = (TrackTableModel)trackTable.getModel();
        int row = trackTable.getSelectedRow();
        long tick = 0;
        int channel = 0;
        if (row > -1) {
            tick = ttm.getTickForRow(row);
            Object o = ttm.getValueAt(row, 6);
            if (o != null) {
                channel = (Integer)ttm.getValueAt(row, 6);
            }
        }
        mCreateEventDialog.setPosition(tick);
        mCreateEventDialog.setChannel(channel);
        mCreateEventDialog.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        tableScrollPane = new javax.swing.JScrollPane();
        trackTable = new com.lemckes.MidiQuickFix.TrackTable();
        controlPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        insertButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        trackPanel = new javax.swing.JPanel();
        showNotesCheck = new javax.swing.JCheckBox();
        trackSelectorPanel = new javax.swing.JPanel();
        trackLabel = new javax.swing.JLabel();
        trackSelector = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout());

        tableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        trackTable.setPreferredScrollableViewportSize(new java.awt.Dimension(320, 100));
        tableScrollPane.setViewportView(trackTable);

        add(tableScrollPane, java.awt.BorderLayout.CENTER);

        controlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 3));

        buttonPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        insertButton.setText(UiStrings.getString("insert")); // NOI18N
        insertButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(insertButton);

        deleteButton.setText(UiStrings.getString("delete")); // NOI18N
        deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(deleteButton);

        controlPanel.add(buttonPanel);

        add(controlPanel, java.awt.BorderLayout.EAST);

        trackPanel.setLayout(new java.awt.BorderLayout());

        trackPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
        showNotesCheck.setSelected(true);
        showNotesCheck.setText(UiStrings.getString("show_notes")); // NOI18N
        showNotesCheck.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNotesCheckActionPerformed(evt);
            }
        });

        trackPanel.add(showNotesCheck, java.awt.BorderLayout.EAST);

        trackSelectorPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        trackLabel.setText(UiStrings.getString("track")); // NOI18N
        trackSelectorPanel.add(trackLabel);

        trackSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0" }));
        trackSelector.setAlignmentX(0.0F);
        trackSelector.setName("trackSelector");
        trackSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackSelectorActionPerformed(evt);
            }
        });

        trackSelectorPanel.add(trackSelector);

        trackPanel.add(trackSelectorPanel, java.awt.BorderLayout.WEST);

        add(trackPanel, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int[] selRows = trackTable.getSelectedRows();
        trackTable.deleteRows(selRows);
    }//GEN-LAST:event_deleteButtonActionPerformed

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        doCreateEvent();
    }//GEN-LAST:event_insertButtonActionPerformed

    private void showNotesCheckActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNotesCheckActionPerformed
        trackTable.showNotes(showNotesCheck.isSelected());
    }//GEN-LAST:event_showNotesCheckActionPerformed

    private void trackSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackSelectorActionPerformed
        selectTrack(trackSelector.getSelectedIndex());
    }//GEN-LAST:event_trackSelectorActionPerformed

    /**
     * Respond to EventCreation events
     * @param e the EventCreation event
     */
    public void eventCreated(EventCreationEvent e) {
        trackTable.insertEvent(e.getEvent());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton insertButton;
    private javax.swing.JCheckBox showNotesCheck;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel trackLabel;
    private javax.swing.JPanel trackPanel;
    private javax.swing.JComboBox trackSelector;
    private javax.swing.JPanel trackSelectorPanel;
    private com.lemckes.MidiQuickFix.TrackTable trackTable;
    // End of variables declaration//GEN-END:variables
}
