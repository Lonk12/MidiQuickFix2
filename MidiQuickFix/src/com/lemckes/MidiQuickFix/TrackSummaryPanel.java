/*
 * TrackSummaryPanel.java
 *
 * Created on 23/06/2010, 6:11:24 PM
 */
package com.lemckes.MidiQuickFix;

import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.TracksChangedEvent;
import com.lemckes.MidiQuickFix.util.TracksChangedEvent.TrackChangeType;
import com.lemckes.MidiQuickFix.util.TracksChangedListener;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.text.MessageFormat;
import java.util.Arrays;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JOptionPane;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 *
 */
public class TrackSummaryPanel
    extends javax.swing.JPanel
    implements TableModelListener
{

    private MqfSequence mSequence;
    private TrackSummaryTable mTrackSummaryTable;
    /** The list of registered listeners. */
    protected EventListenerList mListenerList;

    /** Creates new form TrackSummaryPanel */
    public TrackSummaryPanel() {
        initComponents();
        mListenerList = new EventListenerList();
    }

    public void setSequence(MqfSequence seq) {
        mSequence = seq;
        mTrackSummaryTable.setSequence(seq);
        mTrackSummaryTable.getModel().addTableModelListener(this);

        addTrackButton.setEnabled(mSequence != null);
    }

    public void setSummaryTable(TrackSummaryTable table) {
        mTrackSummaryTable = table;
        mTrackSummaryTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                Object source = e.getSource();
                if (source == mTrackSummaryTable.getSelectionModel()) {
                    int numSelectedRows =
                        mTrackSummaryTable.getSelectedRowCount();
                    copyTrackButton.setEnabled(numSelectedRows == 1);
                    splitTrackButton.setEnabled(numSelectedRows == 1);
                    deleteTrackButton.setEnabled(numSelectedRows > 0);
                }
            }
        });
        mTrackSummaryTable.getModel().addTableModelListener(this);
        jScrollPane1.setViewportView(table);
    }

    private void createTrack() {
        Integer row = mTrackSummaryTable.getSelectedRow();
        if (row < 0) {
            row = mTrackSummaryTable.getRowCount();
        }
        CreateTrackDialog ctd = new CreateTrackDialog(mSequence, row, null, true);
        ctd.setVisible(true);
        if (ctd.wasTrackCreated()) {
            fireTracksChanged(TrackChangeType.TRACK_ADDED);
            ((TrackSummaryTableModel)mTrackSummaryTable.getModel()).updateInfo();
        }
    }

    private void copyTrack() {
        int row = mTrackSummaryTable.getSelectedRow();
        Track newTrack = mSequence.createTrack(row + 1);

        Track[] tracks = mSequence.getTracks();
        Track originalTrack = tracks[row];
        for (int i = 0; i < originalTrack.size(); ++i) {
            MidiEvent oldEvent = originalTrack.get(i);
            MidiMessage oldMess = oldEvent.getMessage();

            MidiMessage newMess = (MidiMessage)oldMess.clone();
            // Change the track name
            if (newMess.getStatus() == MetaMessage.META) {
                Object[] str = MetaEvent.getMetaStrings((MetaMessage)newMess);
                if (str[0].equals("M:TrackName")) {
                    // get the old name
                    String name = "COPY_" + (String)str[2];
                    try {
                        // Create a new event with the new name
                        MidiEvent me =
                            MetaEvent.createMetaEvent("TRACK_NAME", name, 0, 0);
                        // and extract the updated message
                        newMess = me.getMessage();
                    }
                    catch (InvalidMidiDataException ex) {
                    }
                }
            }

            MidiEvent newEvent = new MidiEvent(newMess, oldEvent.getTick());
            newTrack.add(newEvent);
        }
        fireTracksChanged(TrackChangeType.TRACK_ADDED);
        ((TrackSummaryTableModel)mTrackSummaryTable.getModel()).updateInfo();
    }

    private void splitTrack() {
        int row = mTrackSummaryTable.getSelectedRow();
        Track t[] = new Track[17];
        Track originalTrack = mSequence.getTracks()[row];

        // First create the control track to take all the non-channel events
        t[0] = mSequence.createTrack();
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
            t[i] = mSequence.createTrack();
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

        fireTracksChanged(TrackChangeType.TRACK_ADDED);
    }

    private void deleteTracks() {
        int numRows = mTrackSummaryTable.getSelectedRowCount();
        if (numRows > 0) {
            int[] selectedRows = mTrackSummaryTable.getSelectedRows();
            String message = UiStrings.getString("TrackSummaryPanel.deleteTrackMessage");
            String formattedMessage = MessageFormat.format(message, numRows);
            int reply = JOptionPane.showConfirmDialog(
                this, formattedMessage, "Delete Tracks",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (reply == JOptionPane.OK_OPTION) {
                Arrays.sort(selectedRows);
                for (int track = numRows - 1; track >= 0; --track) {
                    mSequence.deleteTrack(mSequence.getTracks()[selectedRows[track]]);
                }
                fireTracksChanged(TrackChangeType.TRACK_DELETED);
                ((TrackSummaryTableModel)mTrackSummaryTable.getModel()).
                    updateInfo();
            }
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        int column = e.getColumn();
        if (column == 9){
            fireTracksChanged(TrackChangeType.TRACK_CHANGED);
        }
    }

    /**
     * Add a listener that will be notified when Tracks are added to
     * or deleted from the Sequence
     * @param l the TracksChangedListener to be added
     */
    public void addTracksChangedListener(TracksChangedListener l) {
        mListenerList.add(TracksChangedListener.class, l);
    }

    /**
     * Remove a TracksChangedListener
     * @param l the TracksChangedListener to be removed
     */
    public void removeEventCreationListener(TracksChangedListener l) {
        mListenerList.remove(TracksChangedListener.class, l);
    }

    void fireTracksChanged(TrackChangeType type) {
        TracksChangedListener[] listeners =
            mListenerList.getListeners(TracksChangedListener.class);
        for (int i = listeners.length - 1; i >= 0; --i) {
            listeners[i].tracksChanged(new TracksChangedEvent(type));
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        controlPanel = new javax.swing.JPanel();
        controlLabel = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        addTrackButton = new javax.swing.JButton();
        copyTrackButton = new javax.swing.JButton();
        deleteTrackButton = new javax.swing.JButton();
        splitTrackButton = new javax.swing.JButton();

        setName("Form"); // NOI18N
        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setName("jScrollPane1"); // NOI18N
        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        controlPanel.setName("controlPanel"); // NOI18N
        controlPanel.setLayout(new java.awt.BorderLayout());

        controlLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle"); // NOI18N
        controlLabel.setText(bundle.getString("TrackSummaryPanel.controlLabel.text")); // NOI18N
        controlLabel.setName("controlLabel"); // NOI18N
        controlPanel.add(controlLabel, java.awt.BorderLayout.PAGE_START);

        jPanel1.setName("jPanel1"); // NOI18N

        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.GridLayout(0, 1, 0, 3));

        addTrackButton.setText(bundle.getString("TrackSummaryPanel.addTrackButton.text")); // NOI18N
        addTrackButton.setEnabled(false);
        addTrackButton.setName("addTrackButton"); // NOI18N
        addTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addTrackButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(addTrackButton);

        copyTrackButton.setText(bundle.getString("TrackSummaryPanel.copyTrackButton.text")); // NOI18N
        copyTrackButton.setEnabled(false);
        copyTrackButton.setName("copyTrackButton"); // NOI18N
        copyTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyTrackButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(copyTrackButton);

        deleteTrackButton.setText(bundle.getString("TrackSummaryPanel.deleteTrackButton.text")); // NOI18N
        deleteTrackButton.setEnabled(false);
        deleteTrackButton.setName("deleteTrackButton"); // NOI18N
        deleteTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteTrackButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(deleteTrackButton);

        splitTrackButton.setText(bundle.getString("TrackSummaryPanel.splitTrackButton.text")); // NOI18N
        splitTrackButton.setEnabled(false);
        splitTrackButton.setName("splitTrackButton"); // NOI18N
        splitTrackButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitTrackButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(splitTrackButton);

        jPanel1.add(buttonPanel);

        controlPanel.add(jPanel1, java.awt.BorderLayout.CENTER);

        add(controlPanel, java.awt.BorderLayout.LINE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void addTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addTrackButtonActionPerformed
        createTrack();
    }//GEN-LAST:event_addTrackButtonActionPerformed

    private void deleteTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteTrackButtonActionPerformed
        deleteTracks();
    }//GEN-LAST:event_deleteTrackButtonActionPerformed

    private void copyTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyTrackButtonActionPerformed
        copyTrack();
    }//GEN-LAST:event_copyTrackButtonActionPerformed

    private void splitTrackButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitTrackButtonActionPerformed
        splitTrack();
    }//GEN-LAST:event_splitTrackButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addTrackButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JLabel controlLabel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JButton copyTrackButton;
    private javax.swing.JButton deleteTrackButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton splitTrackButton;
    // End of variables declaration//GEN-END:variables
}
