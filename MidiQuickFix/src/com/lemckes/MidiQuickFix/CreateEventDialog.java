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

import com.lemckes.MidiQuickFix.util.EventCreationEvent;
import com.lemckes.MidiQuickFix.util.EventCreationListener;
import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.RegexDocumentFilter;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Component;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.EventListenerList;
import javax.swing.text.AbstractDocument;

/**
 * Allow the user to create a Midi event.
 *
 * @version $Id$
 */
public class CreateEventDialog extends javax.swing.JDialog {
    private static final long serialVersionUID = -9202954584726650745L;
    /** A return status code - returned if Close button has been pressed */
    public static final int RET_CLOSE = 0;
    /** A return status code - returned if Insert button has been pressed */
    public static final int RET_INSERT = 1;
    private int returnStatus = RET_CLOSE;
    private final String NOTE_ON = "NOTE_ON"; // NOI18N
    private final String NOTE_OFF = "NOTE_OFF"; // NOI18N
    private final String POLY_PRESSURE = "POLY_PRESSURE"; // NOI18N
    private final String PATCH = "PATCH"; // NOI18N
    private final String CONTROL_CHANGE = "CONTROL_CHANGE"; // NOI18N
    private final String PITCH_BEND = "PITCH_BEND"; // NOI18N
    private final String CHANNEL_PRESSURE = "CHANNEL_PRESSURE"; // NOI18N
    private final String META_EVENT = "META_EVENT"; // NOI18N
    private final String DEFAULT_OCTAVE = "5"; // NOI18N
    private final String DEFAULT_PRESSURE = "127"; // NOI18N
    private final String DEFAULT_PITCH_BEND = "8192"; // NOI18N
    private final String DEFAULT_CONTROL_VALUE = "64"; // NOI18N
    private final String DEFAULT_NOTE_VALUE = "127"; // NOI18N
    private final String DEFAULT_CHANNEL = "0"; // NOI18N
    private final String DEFAULT_POSITION = "00000:000"; // NOI18N
    private int mTicksPerBeat;
    /** The list of registered listeners. */
    protected EventListenerList mListenerList;

    /**
     * Creates a CreateEventDialog that allows the user to create a new MidiEvent
     * @param ticksPerBeat the number of ticks per beat in this sequence.
     * Used to convert the position value that is
     * represented as <code>beats:ticks</code>
     * @param parent the Frame parent of the dialog
     * @param modal the modality of the dialog
     */
    public CreateEventDialog(int ticksPerBeat, Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        mTicksPerBeat = ticksPerBeat;

        setFormats();

        mListenerList = new EventListenerList();

        Object[] s = InstrumentNames.getNameArray();
        patchCombo.setModel(new DefaultComboBoxModel(s));
        s = NoteNames.getNoteNameArray();
        noteCombo.setModel(new DefaultComboBoxModel(s));
        s = Controllers.getNameArray();
        controlChangeCombo.setModel(new DefaultComboBoxModel(s));
        s = MetaEvent.getTypeNames();
        metaEventCombo.setModel(new DefaultComboBoxModel(s));

        // Initialise the enabled state of the controls
        noteOnRadio.doClick();

        pack();
    }

    private void setFormats() {
        AbstractDocument doc = (AbstractDocument)positionField.getDocument();
        doc.setDocumentFilter(new RegexDocumentFilter(Formats.TICK_BEAT_RE));

        RegexDocumentFilter byteFilter =
            new RegexDocumentFilter("\\p{Digit}{0,3}"); // NOI18N
        RegexDocumentFilter wordFilter =
            new RegexDocumentFilter("\\p{Digit}{0,5}"); // NOI18N

        bendField.setText(DEFAULT_PITCH_BEND);
        doc = (AbstractDocument)bendField.getDocument();
        doc.setDocumentFilter(wordFilter);

        channelField.setText(DEFAULT_CHANNEL);
        doc = (AbstractDocument)channelField.getDocument();
        doc.setDocumentFilter(byteFilter);

        channelPressureField.setText(DEFAULT_PRESSURE);
        doc = (AbstractDocument)channelPressureField.getDocument();
        doc.setDocumentFilter(byteFilter);

        controlValueField.setText(DEFAULT_CONTROL_VALUE);
        doc = (AbstractDocument)controlValueField.getDocument();
        doc.setDocumentFilter(byteFilter);

        noteValueField.setText(DEFAULT_NOTE_VALUE);
        doc = (AbstractDocument)noteValueField.getDocument();
        doc.setDocumentFilter(byteFilter);

        octaveField.setText(DEFAULT_OCTAVE);
        doc = (AbstractDocument)octaveField.getDocument();
        doc.setDocumentFilter(byteFilter);
    }

    /**
     * Get the status of the dialog when it was closed
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * Add a listener that will be notified when an event is created.
     * @param l the EventCreationListerner to be added
     */
    public void addEventCreationListener(EventCreationListener l) {
        mListenerList.add(EventCreationListener.class, l);
    }

    /**
     * Remove an EventCreationListener
     * @param l the EventCreationListerner to be removed
     */
    public void removeEventCreationListener(EventCreationListener l) {
        mListenerList.remove(EventCreationListener.class, l);
    }

    private void fireEventCreated(MidiEvent e) {
        EventCreationListener[] listeners =
            mListenerList.getListeners(EventCreationListener.class);
        for (int i = listeners.length - 1; i >= 0; --i) {
            listeners[i].eventCreated(new EventCreationEvent(e));
        }
    }

    private void enableFields(List<Component> components) {
        noteCombo.setEnabled(components.contains(noteCombo));
        octaveField.setEnabled(components.contains(octaveField));
        noteValueField.setEnabled(components.contains(noteValueField));
        patchCombo.setEnabled(components.contains(patchCombo));
        controlChangeCombo.setEnabled(components.contains(controlChangeCombo));
        controlValueField.setEnabled(components.contains(controlValueField));
        bendField.setEnabled(components.contains(bendField));
        channelPressureField.setEnabled(
            components.contains(channelPressureField));
        metaEventCombo.setEnabled(components.contains(metaEventCombo));
        metaDataField.setEnabled(components.contains(metaDataField));
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        eventTypeGroup = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        tickPanel = new javax.swing.JPanel();
        positionLabel = new javax.swing.JLabel();
        positionField = new javax.swing.JFormattedTextField();
        shortEventPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        channelLabel = new javax.swing.JLabel();
        channelField = new javax.swing.JFormattedTextField();
        jPanel1 = new javax.swing.JPanel();
        noteOnRadio = new javax.swing.JRadioButton();
        noteOffRadio = new javax.swing.JRadioButton();
        polyRadio = new javax.swing.JRadioButton();
        noteCombo = new javax.swing.JComboBox();
        noteValueLabel = new javax.swing.JLabel();
        noteValueField = new javax.swing.JFormattedTextField();
        patchRadio = new javax.swing.JRadioButton();
        patchCombo = new javax.swing.JComboBox();
        controlRadio = new javax.swing.JRadioButton();
        controlChangeCombo = new javax.swing.JComboBox();
        controlValueLabel = new javax.swing.JLabel();
        controlValueField = new javax.swing.JFormattedTextField();
        bendRadio = new javax.swing.JRadioButton();
        bendField = new javax.swing.JFormattedTextField();
        channelPressureRadio = new javax.swing.JRadioButton();
        channelPressureField = new javax.swing.JFormattedTextField();
        octaveLabel = new javax.swing.JLabel();
        octaveField = new javax.swing.JFormattedTextField();
        metaEventPanel = new javax.swing.JPanel();
        metaEventRadio = new javax.swing.JRadioButton();
        metaEventCombo = new javax.swing.JComboBox();
        metaDataLabel = new javax.swing.JLabel();
        metaDataField = new javax.swing.JFormattedTextField();
        jPanel3 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        insertButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();

        setTitle(UiStrings.getString("create_event")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new javax.swing.BoxLayout(mainPanel, javax.swing.BoxLayout.PAGE_AXIS));

        tickPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        positionLabel.setText(UiStrings.getString("event_position")); // NOI18N
        tickPanel.add(positionLabel);

        positionField.setColumns(8);
        positionField.setText(DEFAULT_POSITION);
        tickPanel.add(positionField);

        mainPanel.add(tickPanel);

        shortEventPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(UiStrings.getString("short_event"))); // NOI18N
        shortEventPanel.setLayout(new javax.swing.BoxLayout(shortEventPanel, javax.swing.BoxLayout.PAGE_AXIS));

        jPanel2.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 9, 1));
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        channelLabel.setText(UiStrings.getString("channel")); // NOI18N
        jPanel2.add(channelLabel);

        channelField.setColumns(3);
        channelField.setText(DEFAULT_CHANNEL);
        jPanel2.add(channelField);

        shortEventPanel.add(jPanel2);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        eventTypeGroup.add(noteOnRadio);
        noteOnRadio.setSelected(true);
        noteOnRadio.setText(UiStrings.getString("note_on")); // NOI18N
        noteOnRadio.setActionCommand(NOTE_ON);
        noteOnRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noteOnRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(noteOnRadio, gridBagConstraints);

        eventTypeGroup.add(noteOffRadio);
        noteOffRadio.setText(UiStrings.getString("note_off")); // NOI18N
        noteOffRadio.setActionCommand(NOTE_OFF);
        noteOffRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        noteOffRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(noteOffRadio, gridBagConstraints);

        eventTypeGroup.add(polyRadio);
        polyRadio.setText(UiStrings.getString("poly_pressure")); // NOI18N
        polyRadio.setActionCommand(POLY_PRESSURE);
        polyRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        polyRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        jPanel1.add(polyRadio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        jPanel1.add(noteCombo, gridBagConstraints);

        noteValueLabel.setText(UiStrings.getString("value")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(noteValueLabel, gridBagConstraints);

        noteValueField.setColumns(3);
        noteValueField.setText(DEFAULT_NOTE_VALUE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(noteValueField, gridBagConstraints);

        eventTypeGroup.add(patchRadio);
        patchRadio.setText(UiStrings.getString("patch")); // NOI18N
        patchRadio.setActionCommand(PATCH);
        patchRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        patchRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        jPanel1.add(patchRadio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        jPanel1.add(patchCombo, gridBagConstraints);

        eventTypeGroup.add(controlRadio);
        controlRadio.setText(UiStrings.getString("control_change")); // NOI18N
        controlRadio.setActionCommand(CONTROL_CHANGE);
        controlRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        controlRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        jPanel1.add(controlRadio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        jPanel1.add(controlChangeCombo, gridBagConstraints);

        controlValueLabel.setText(UiStrings.getString("value")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(controlValueLabel, gridBagConstraints);

        controlValueField.setColumns(3);
        controlValueField.setText(DEFAULT_CONTROL_VALUE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        jPanel1.add(controlValueField, gridBagConstraints);

        eventTypeGroup.add(bendRadio);
        bendRadio.setText(UiStrings.getString("pitch_bend")); // NOI18N
        bendRadio.setActionCommand(PITCH_BEND);
        bendRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        bendRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        jPanel1.add(bendRadio, gridBagConstraints);

        bendField.setColumns(5);
        bendField.setText(DEFAULT_PITCH_BEND);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        jPanel1.add(bendField, gridBagConstraints);

        eventTypeGroup.add(channelPressureRadio);
        channelPressureRadio.setText(UiStrings.getString("channel_pressure")); // NOI18N
        channelPressureRadio.setActionCommand(CHANNEL_PRESSURE);
        channelPressureRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        channelPressureRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 9, 0);
        jPanel1.add(channelPressureRadio, gridBagConstraints);

        channelPressureField.setColumns(3);
        channelPressureField.setText(DEFAULT_PRESSURE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 9, 0);
        jPanel1.add(channelPressureField, gridBagConstraints);

        octaveLabel.setText(UiStrings.getString("octave")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(octaveLabel, gridBagConstraints);

        octaveField.setColumns(2);
        octaveField.setText(DEFAULT_OCTAVE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        jPanel1.add(octaveField, gridBagConstraints);

        shortEventPanel.add(jPanel1);

        mainPanel.add(shortEventPanel);

        metaEventPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(UiStrings.getString("meta_event"))); // NOI18N
        metaEventPanel.setLayout(new java.awt.GridBagLayout());

        eventTypeGroup.add(metaEventRadio);
        metaEventRadio.setActionCommand(META_EVENT);
        metaEventRadio.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        metaEventRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioSelected(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        metaEventPanel.add(metaEventRadio, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        metaEventPanel.add(metaEventCombo, gridBagConstraints);

        metaDataLabel.setText(UiStrings.getString("data")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        metaEventPanel.add(metaDataLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        metaEventPanel.add(metaDataField, gridBagConstraints);

        mainPanel.add(metaEventPanel);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        insertButton.setText(UiStrings.getString("insert")); // NOI18N
        insertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                insertButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(insertButton);

        closeButton.setText(UiStrings.getString("close")); // NOI18N
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(closeButton);

        jPanel3.add(buttonPanel);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void radioSelected(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_radioSelected
        String cmd = evt.getActionCommand();

        List<Component> enabled = new ArrayList<Component>();
        if (NOTE_ON.equals(cmd) ||
            NOTE_OFF.equals(cmd) ||
            POLY_PRESSURE.equals(cmd)) {
            enabled.add(noteCombo);
            enabled.add(octaveField);
            enabled.add(noteValueField);
            enableFields(enabled);
        } else if (PATCH.equals(cmd)) {
            enabled.add(patchCombo);
            enableFields(enabled);
        } else if (CONTROL_CHANGE.equals(cmd)) {
            enabled.add(controlChangeCombo);
            enabled.add(controlValueField);
            enableFields(enabled);
        } else if (PITCH_BEND.equals(cmd)) {
            enabled.add(bendField);
            enableFields(enabled);
        } else if (CHANNEL_PRESSURE.equals(cmd)) {
            enabled.add(channelPressureField);
            enableFields(enabled);
        } else if (META_EVENT.equals(cmd)) {
            enabled.add(metaEventCombo);
            enabled.add(metaDataField);
            enableFields(enabled);
        }
    }//GEN-LAST:event_radioSelected

    private void insertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_insertButtonActionPerformed
        String selectedEvent = eventTypeGroup.getSelection().getActionCommand();

        MidiEvent event = null;

        if (NOTE_ON.equals(selectedEvent)) {
            event = getNoteOn();
        }
        if (NOTE_OFF.equals(selectedEvent)) {
            event = getNoteOff();
        }
        if (POLY_PRESSURE.equals(selectedEvent)) {
            event = getPolyPressure();
        }
        if (PATCH.equals(selectedEvent)) {
            event = getPatch();
        }
        if (CONTROL_CHANGE.equals(selectedEvent)) {
            event = getControlChange();
        }
        if (PITCH_BEND.equals(selectedEvent)) {
            event = getPitchBend();
        }
        if (CHANNEL_PRESSURE.equals(selectedEvent)) {
            event = getChannelPressure();
        }
        if (META_EVENT.equals(selectedEvent)) {
            event = getMetaEvent();
        }
        fireEventCreated(event);
    }//GEN-LAST:event_insertButtonActionPerformed

    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        doClose(RET_CLOSE);
    }//GEN-LAST:event_closeButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CLOSE);
    }//GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                javax.swing.JFrame f = new javax.swing.JFrame();
                f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
                new CreateEventDialog(192, f, true).setVisible(true);
                System.exit(0);
            }
        });
    }

    private long getTick() {
        long tick = Formats.parseTicks(positionField.getText(), mTicksPerBeat);
        return tick;
    }

    private int getChannel() {
        return safeParseInt(channelField.getText());
    }

    private MidiEvent getNoteEvent(int command) {
        String noteName = noteCombo.getSelectedItem().toString();
        int dividerPos = noteName.indexOf('/');
        if (dividerPos > -1) {
            noteName = noteName.substring(0, dividerPos);
        }
        noteName += octaveField.getText();
        int data1 = NoteNames.getNoteNumber(noteName);
        int data2 = safeParseInt(noteValueField.getText());
        MidiEvent me = null;
        try {
            me =
                ShortEvent.createShortEvent(command, getChannel(), data1, data2,
                getTick());
        } catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    /**
     * Create a NOTE_ON event from the dialog values
     * @return a NOTE_ON event
     */
    public MidiEvent getNoteOn() {
        int command = ShortMessage.NOTE_ON;
        return getNoteEvent(command);
    }

    /**
     * Create a NOTE_OFF event from the dialog values
     * @return a NOTE_OFF event
     */
    public MidiEvent getNoteOff() {
        return getNoteEvent(ShortMessage.NOTE_OFF);
    }

    /**
     * Create a POLY_PRESSURE event from the dialog values
     * @return a POLY_PRESSURE event
     */
    public MidiEvent getPolyPressure() {
        return getNoteEvent(ShortMessage.POLY_PRESSURE);
    }

    /**
     * Create a PROGRAM_CHANGE (patch) event from the dialog values
     * @return a PROGRAM_CHANGE (patch) event
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
                getTick());
        } catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    /**
     * Create a CONTROL_CHANGE event from the dialog values
     * @return a CONTROL_CHANGE event
     */
    public MidiEvent getControlChange() {
        int command = ShortMessage.CONTROL_CHANGE;
        String control = controlChangeCombo.getSelectedItem().toString();
        int data1 = Controllers.getControllerNumber(control);
        int data2 = safeParseInt(controlValueField.getText());
        MidiEvent me = null;
        try {
            me =
                ShortEvent.createShortEvent(command, getChannel(), data1, data2,
                getTick());
        } catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    /**
     * Create a PITCH_BEND event from the dialog values
     * @return a PITCH_BEND event
     */
    public MidiEvent getPitchBend() {
        int command = ShortMessage.PITCH_BEND;
        int bend = safeParseInt(bendField.getText());
        int data1 = bend & 0x7f;
        int data2 = (bend >> 7) & 0x7f;
        MidiEvent me = null;
        try {
            me =
                ShortEvent.createShortEvent(command, getChannel(), data1, data2,
                getTick());
        } catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    /**
     * Create a CHANNEL_PRESSURE event from the dialog values
     * @return a CHANNEL_PRESSURE event
     */
    public MidiEvent getChannelPressure() {
        int command = ShortMessage.CHANNEL_PRESSURE;
        int data1 = safeParseInt(channelPressureField.getText());
        int data2 = 0;
        MidiEvent me = null;
        try {
            me =
                ShortEvent.createShortEvent(command, getChannel(), data1, data2,
                getTick());
        } catch (InvalidMidiDataException ex) {
            TraceDialog.addTrace(ex.getMessage());
        }
        return me;
    }

    /**
     * Create a META event from the dialog values
     * @return a META event
     */
    public MidiEvent getMetaEvent() {
        String type = metaEventCombo.getSelectedItem().toString();
        MidiEvent me = null;
        try {
            me = MetaEvent.createMetaEvent(
                type, metaDataField.getText(), getTick(), mTicksPerBeat);
        } catch (InvalidMidiDataException ex) {
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
        } catch (NumberFormatException nfe) {
            // DO NOTHING
        }
        return i;
    }

    /**
     * Set the value of the position field
     * @param ticks the position in ticks
     */
    public void setPosition(long ticks) {
        positionField.setText(Formats.formatTicks(ticks, mTicksPerBeat, true));
    }

    /**
     * Set the value of the channel field
     * @param channel the channel number
     */
    public void setChannel(int channel) {
        channelField.setText(Integer.toString(channel));
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField bendField;
    private javax.swing.JRadioButton bendRadio;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JFormattedTextField channelField;
    private javax.swing.JLabel channelLabel;
    private javax.swing.JFormattedTextField channelPressureField;
    private javax.swing.JRadioButton channelPressureRadio;
    private javax.swing.JButton closeButton;
    private javax.swing.JComboBox controlChangeCombo;
    private javax.swing.JRadioButton controlRadio;
    private javax.swing.JFormattedTextField controlValueField;
    private javax.swing.JLabel controlValueLabel;
    private javax.swing.ButtonGroup eventTypeGroup;
    private javax.swing.JButton insertButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JFormattedTextField metaDataField;
    private javax.swing.JLabel metaDataLabel;
    private javax.swing.JComboBox metaEventCombo;
    private javax.swing.JPanel metaEventPanel;
    private javax.swing.JRadioButton metaEventRadio;
    private javax.swing.JComboBox noteCombo;
    private javax.swing.JRadioButton noteOffRadio;
    private javax.swing.JRadioButton noteOnRadio;
    private javax.swing.JFormattedTextField noteValueField;
    private javax.swing.JLabel noteValueLabel;
    private javax.swing.JFormattedTextField octaveField;
    private javax.swing.JLabel octaveLabel;
    private javax.swing.JComboBox patchCombo;
    private javax.swing.JRadioButton patchRadio;
    private javax.swing.JRadioButton polyRadio;
    private javax.swing.JFormattedTextField positionField;
    private javax.swing.JLabel positionLabel;
    private javax.swing.JPanel shortEventPanel;
    private javax.swing.JPanel tickPanel;
    // End of variables declaration//GEN-END:variables
}
