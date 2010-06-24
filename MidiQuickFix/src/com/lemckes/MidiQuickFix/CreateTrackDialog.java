/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2010 John Lemcke
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

import com.lemckes.MidiQuickFix.util.RegexDocumentFilter;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.TracksChangedEvent;
import com.lemckes.MidiQuickFix.util.TracksChangedEvent.TrackChangeType;
import com.lemckes.MidiQuickFix.util.TracksChangedListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.Sequence;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.text.AbstractDocument;

/**
 * Allow the user to create a new track
 */
public class CreateTrackDialog extends javax.swing.JDialog
{

    private final String DEFAULT_CONTROL_VALUE = "64"; // NOI18N
    private final String DEFAULT_CHANNEL = "0"; // NOI18N
    private Sequence mSequence;
    private boolean mTrackCreated = false;

    /**
     * Create a new CreateTrackDialog for the given Sequence
     * @param seq
     * @param parent
     * @param modal
     */
    public CreateTrackDialog(Sequence seq, java.awt.Frame parent, boolean modal) {
        super(parent, modal);

        mSequence = seq;
        mTrackCreated = false;

        initComponents();

        setFormats();

        Object[] s = InstrumentNames.getNameArray();
        patchCombo.setModel(new DefaultComboBoxModel(s));
    }

    private void createTrack() {
        Track track = mSequence.createTrack();
        track.add(getTrackName());
        track.add(getPatch());
        track.add(getVolume());
        track.add(getPan());
        mTrackCreated = true;
    }

    public boolean wasTrackCreated(){
        return mTrackCreated;
    }

    private void setFormats() {
        RegexDocumentFilter byteFilter =
            new RegexDocumentFilter("\\p{Digit}{0,3}"); // NOI18N
        RegexDocumentFilter wordFilter =
            new RegexDocumentFilter("\\p{Digit}{0,5}"); // NOI18N

        channelField.setText(DEFAULT_CHANNEL);
        AbstractDocument doc = (AbstractDocument)channelField.getDocument();
        doc.setDocumentFilter(byteFilter);

        volumeField.setText(DEFAULT_CONTROL_VALUE);
        doc = (AbstractDocument)volumeField.getDocument();
        doc.setDocumentFilter(byteFilter);

        panField.setText(DEFAULT_CONTROL_VALUE);
        doc = (AbstractDocument)panField.getDocument();
        doc.setDocumentFilter(byteFilter);
    }

    /**
     * Create a META event from the dialog values
     * @return a META event
     */
    public MidiEvent getTrackName() {
        String type = "TRACK_NAME";
        MidiEvent me = null;
        try {
            me = MetaEvent.createMetaEvent(
                type, trackNameField.getText(), 0, 92);
        }
        catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    private int getChannel() {
        return safeParseInt(channelField.getText());
    }

    /**
     * Create a PROGRAM_CHAGE (patch) event from the dialog values
     * @return a PROGRAM_CHAGE (patch) event
     */
    public MidiEvent getPatch() {
        int command = ShortMessage.PROGRAM_CHANGE;
        String instrument = patchCombo.getSelectedItem().toString();
        int data1 = InstrumentNames.getInstrumentNumber(instrument);
        int data2 = InstrumentNames.getInstrumentBank(instrument);
        MidiEvent me = null;
        try {
            me =
                ShortEvent.createShortEvent(command, getChannel(), data1, data2,
                0);
        }
        catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    /**
     * Create a CONTROL_CHANGE (Volume) event from the dialog values
     * @return a CONTROL_CHANGE event
     */
    public MidiEvent getVolume() {
        int command = ShortMessage.CONTROL_CHANGE;
        String control = "Volume";
        int data1 = Controllers.getControllerNumber(control);
        int data2 = safeParseInt(volumeField.getText());
        MidiEvent me = null;
        try {
            me = ShortEvent.createShortEvent(
                command, getChannel(), data1, data2, 0);
        }
        catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    /**
     * Create a CONTROL_CHANGE (Pan) event from the dialog values
     * @return a CONTROL_CHANGE event
     */
    public MidiEvent getPan() {
        int command = ShortMessage.CONTROL_CHANGE;
        String control = "Pan";
        int data1 = Controllers.getControllerNumber(control);
        int data2 = safeParseInt(panField.getText());
        MidiEvent me = null;
        try {
            me = ShortEvent.createShortEvent(
                command, getChannel(), data1, data2, 0);
        }
        catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    /**
     * Parse an integer from a string, returning zero if the string
     * does not represent a valid integer.
     * @param s the string to parse
     * @return the integer value of the string or zero if the string
     * does not represent a valid integer
     */
    private int safeParseInt(String s) {
        int i = 0;
        try {
            i = Integer.parseInt(s);
        }
        catch (NumberFormatException nfe) {
            // DO NOTHING
        }
        return i;
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        trackNameLabel = new javax.swing.JLabel();
        trackNameField = new javax.swing.JTextField();
        channelLabel = new javax.swing.JLabel();
        channelField = new javax.swing.JFormattedTextField();
        patchLabel = new javax.swing.JLabel();
        patchCombo = new javax.swing.JComboBox();
        volumeLabel = new javax.swing.JLabel();
        volumeField = new javax.swing.JFormattedTextField();
        panLabel = new javax.swing.JLabel();
        panField = new javax.swing.JFormattedTextField();
        jPanel2 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle"); // NOI18N
        setTitle(bundle.getString("CreateTrackDialog.title")); // NOI18N
        setName("Form"); // NOI18N

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("CreateTrackDialog.title"))); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.GridBagLayout());

        trackNameLabel.setText(bundle.getString("CreateTrackDialog.trackNameLabel.text")); // NOI18N
        trackNameLabel.setName("trackNameLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        mainPanel.add(trackNameLabel, gridBagConstraints);

        trackNameField.setColumns(20);
        trackNameField.setName("trackNameField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        mainPanel.add(trackNameField, gridBagConstraints);

        channelLabel.setText(bundle.getString("CreateTrackDialog.channelLabel.text")); // NOI18N
        channelLabel.setName("channelLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        mainPanel.add(channelLabel, gridBagConstraints);

        channelField.setColumns(3);
        channelField.setText(DEFAULT_CHANNEL);
        channelField.setName("channelField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        mainPanel.add(channelField, gridBagConstraints);

        patchLabel.setText(bundle.getString("CreateTrackDialog.patchLabel.text")); // NOI18N
        patchLabel.setName("patchLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        mainPanel.add(patchLabel, gridBagConstraints);

        patchCombo.setName("patchCombo"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        mainPanel.add(patchCombo, gridBagConstraints);

        volumeLabel.setText(bundle.getString("CreateTrackDialog.volumeLabel.text")); // NOI18N
        volumeLabel.setName("volumeLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        mainPanel.add(volumeLabel, gridBagConstraints);

        volumeField.setColumns(3);
        volumeField.setText(DEFAULT_CONTROL_VALUE);
        volumeField.setName("volumeField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        mainPanel.add(volumeField, gridBagConstraints);

        panLabel.setText(bundle.getString("CreateTrackDialog.panLabel.text")); // NOI18N
        panLabel.setName("panLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        mainPanel.add(panLabel, gridBagConstraints);

        panField.setColumns(3);
        panField.setText(DEFAULT_CONTROL_VALUE);
        panField.setName("panField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        mainPanel.add(panField, gridBagConstraints);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING));

        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        okButton.setText(bundle.getString("CreateTrackDialog.okButton.text")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        cancelButton.setText(bundle.getString("CreateTrackDialog.cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        jPanel2.add(buttonPanel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        createTrack();
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable()
        {

            public void run() {
                try {
                    CreateTrackDialog dialog = new CreateTrackDialog(
                        new Sequence(Sequence.PPQ, 92), new javax.swing.JFrame(), true);
                    dialog.addWindowListener(new java.awt.event.WindowAdapter()
                    {

                        public void windowClosing(java.awt.event.WindowEvent e) {
                            System.exit(0);
                        }
                    });
                    dialog.setVisible(true);
                }
                catch (InvalidMidiDataException ex) {
                    Logger.getLogger(CreateTrackDialog.class.getName()).
                        log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JFormattedTextField channelField;
    private javax.swing.JLabel channelLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JFormattedTextField panField;
    private javax.swing.JLabel panLabel;
    private javax.swing.JComboBox patchCombo;
    private javax.swing.JLabel patchLabel;
    private javax.swing.JTextField trackNameField;
    private javax.swing.JLabel trackNameLabel;
    private javax.swing.JFormattedTextField volumeField;
    private javax.swing.JLabel volumeLabel;
    // End of variables declaration//GEN-END:variables
}
