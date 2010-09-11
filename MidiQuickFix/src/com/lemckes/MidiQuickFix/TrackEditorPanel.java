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

import com.lemckes.MidiQuickFix.util.TrackUpdateUtils;
import com.lemckes.MidiQuickFix.util.EventCreationEvent;
import com.lemckes.MidiQuickFix.util.EventCreationListener;
import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.TracksChangedEvent.TrackChangeType;
import com.lemckes.MidiQuickFix.util.UiStrings;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JOptionPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;

/**
 * The UI for editing track data
 * @version $Id$
 */
public class TrackEditorPanel extends javax.swing.JPanel
    implements EventCreationListener, ListSelectionListener
{

    private static final long serialVersionUID = -3117013688244779503L;
    private MqfSequence mSeq;
    private int mCurrentTrack;
    private String mKeySig;
    private CreateEventDialog mCreateEventDialog;
    private TrackTable trackTable;

    /** Creates new form TrackEditorPanel */
    public TrackEditorPanel() {
        initComponents();
        trackTable = new TrackTable();
        tableScrollPane.setViewportView(trackTable);
        trackTable.getSelectionModel().addListSelectionListener(this);
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            boolean haveSelection = trackTable.getSelectedRowCount() > 0;
            deleteButton.setEnabled(haveSelection);
            insertButton.setEnabled(haveSelection);
        }
    }

    /**
     * Set the sequence that will be edited
     * @param seq the sequence to edit
     */
    public void setSequence(MqfSequence seq) {
        mSeq = seq;
        int currentTrack = mCurrentTrack;
        boolean haveTracks = false;
        if (mSeq != null) {
            int numTracks = mSeq.getTracks().length;
            if (numTracks > 0) {
                setTrackComboModel(mSeq.getTracks());
                mCurrentTrack = currentTrack < numTracks ? currentTrack : 0;
                trackSelector.setSelectedIndex(mCurrentTrack);
                haveTracks = true;
            }
        }
        trackSelector.setEnabled(haveTracks);
        showNotesCheck.setEnabled(haveTracks);
    }

    public String getCurrentTrackTitle() {
        return trackSelector.getItemAt(mCurrentTrack).toString();
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
                mSeq.getTracks()[mCurrentTrack],
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
        trackTable.addTrackEditedListener(l);
    }

    private void doCreateEvent() {
        if (mCreateEventDialog == null) {
            mCreateEventDialog = new CreateEventDialog(mSeq.getResolution(),
                MidiQuickFix.getMainFrame(), false);
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

    public void convertNoteOn() {
        TrackUpdateUtils.convertNoteOnZeroToNoteOff(mSeq.getTracks()[mCurrentTrack]);
        trackTable.trackModified();
    }

    public void convertText() {
        if (trackTable.getSelectedRowCount() == 0) {
            String message = UiStrings.getString("ConvertAllTextQuestion");
            String title = UiStrings.getString("ConvertAllTextTitle");
            int answer = JOptionPane.showConfirmDialog(controlPanel, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (answer == JOptionPane.YES_OPTION) {
                trackTable.selectAll();
            }
        }
        if (trackTable.getSelectedRowCount() > 0) {
            int[] rows = trackTable.getSelectedRows();
            TrackUpdateUtils.convertTextToLyric(mSeq.getTracks()[mCurrentTrack], rows);
        }
        trackTable.trackModified();
    }

    public void removeNotes() {
        TrackUpdateUtils.removeNotesFromTrack(mSeq.getTracks()[mCurrentTrack]);
        trackTable.trackModified();
    }

    public void splitTrack() {
        Track t[] = new Track[17];
        Track originalTrack = mSeq.getTracks()[mCurrentTrack];

        // First create the control track to take all the non-channel events
        t[0] = mSeq.createTrack();
        String type = "TRACK_NAME";
        try {
            MidiEvent me = MetaEvent.createMetaEvent(
                type, "Control Track", 0, 92);
            t[0].add(me);
        }
        catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }

        // Then create a track to take the events for each channel
        for (int i = 1; i < 17; ++i) {
            t[i] = mSeq.createTrack();
            try {
                MidiEvent me = MetaEvent.createMetaEvent(
                    type, "Channel " + String.valueOf(i), 0, 92);
                t[i].add(me);
            }
            catch (InvalidMidiDataException ex) {
                TraceDialog.addTrace(ex.getMessage());
            }
        }

        // Now copy the events to their new track
        for (int i = 0; i < originalTrack.size(); ++i) {
            MidiEvent ev = originalTrack.get(i);
            MidiMessage mess = ev.getMessage();
            if (mess instanceof ShortMessage) {
                int st = ((ShortMessage)mess).getStatus();
                // Check that this is a channel message
                if ((st & 0xf0) <= 0xf0) {
                    ShortMessage sm = (ShortMessage)mess;
                    int channel = sm.getChannel();
                    t[channel + 1].add(ev);
                } else {
                    t[0].add(ev);
                }
            } else {
                t[0].add(ev);
            }
        }

        trackTable.trackModified();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        tableScrollPane = new javax.swing.JScrollPane();
        controlPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        insertButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        moreButton = new javax.swing.JButton();
        trackPanel = new javax.swing.JPanel();
        showNotesCheck = new javax.swing.JCheckBox();
        trackSelectorPanel = new javax.swing.JPanel();
        trackLabel = new javax.swing.JLabel();
        trackSelector = new javax.swing.JComboBox();

        setLayout(new java.awt.GridBagLayout());

        tableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(tableScrollPane, gridBagConstraints);

        controlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 3, 3));

        buttonPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        insertButton.setMnemonic(UiStrings.getString("TrackEditorPanel.insertButton.mnemonic").charAt(0));
        insertButton.setText(UiStrings.getString("insert")); // NOI18N
        insertButton.setEnabled(false);
        insertButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(insertButton);

        deleteButton.setMnemonic(UiStrings.getString("TrackEditorPanel.deleteButton.mnemonic").charAt(0));
        deleteButton.setText(UiStrings.getString("delete")); // NOI18N
        deleteButton.setEnabled(false);
        deleteButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(deleteButton);

        moreButton.setMnemonic(UiStrings.getString("TrackEditorPanel.moreButton.mnemonic").charAt(0));
        moreButton.setText(UiStrings.getString("more")); // NOI18N
        moreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moreButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(moreButton);

        controlPanel.add(buttonPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        add(controlPanel, gridBagConstraints);

        trackPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
        trackPanel.setLayout(new java.awt.BorderLayout());

        showNotesCheck.setSelected(true);
        showNotesCheck.setText(UiStrings.getString("show_notes")); // NOI18N
        showNotesCheck.setEnabled(false);
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
        trackSelector.setEnabled(false);
        trackSelector.setName("trackSelector"); // NOI18N
        trackSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackSelectorActionPerformed(evt);
            }
        });
        trackSelectorPanel.add(trackSelector);

        trackPanel.add(trackSelectorPanel, java.awt.BorderLayout.WEST);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(trackPanel, gridBagConstraints);
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

    private void moreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moreButtonActionPerformed
        new TrackUpdateUtilDialog(this, moreButton, true).setVisible(true);
    }//GEN-LAST:event_moreButtonActionPerformed

    /**
     * Respond to EventCreation events
     * @param e the EventCreation event
     */
    @Override
    public void eventCreated(EventCreationEvent e) {
        trackTable.insertEvent(e.getEvent());
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton insertButton;
    private javax.swing.JButton moreButton;
    private javax.swing.JCheckBox showNotesCheck;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel trackLabel;
    private javax.swing.JPanel trackPanel;
    private javax.swing.JComboBox trackSelector;
    private javax.swing.JPanel trackSelectorPanel;
    // End of variables declaration//GEN-END:variables
}
