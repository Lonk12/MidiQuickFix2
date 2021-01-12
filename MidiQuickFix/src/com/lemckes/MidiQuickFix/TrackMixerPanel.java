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
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.Sequencer;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

/**
 * Control the volume and muting of each track
 * 
 */
public class TrackMixerPanel
    extends javax.swing.JPanel
{

    private MqfSequence mSequence;
    private Sequencer mSequencer;

    /**
     * The list of registered listeners.
     */
    protected EventListenerList mListenerList;

    /**
     * Creates new form TrackControlPanel
     */
    public TrackMixerPanel() {
        initComponents();
        mListenerList = new EventListenerList();
    }

    public void setSequencer(Sequencer sequencer) {
        mSequencer = sequencer;
    }

    public void setSequence(MqfSequence seq) {
        mSequence = seq;
        EventQueue.invokeLater(() -> {
            rebuild();
        });
    }

    private void rebuild() {

        mainPanel.removeAll();
        revalidate();

        GridBagConstraints gridBagConstraints;

        numLabel = new javax.swing.JLabel();
        numLabel.setFont(numLabel.getFont().deriveFont(numLabel.getFont().getStyle() | java.awt.Font.BOLD));
        numLabel.setText(UiStrings.getString("no.")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(numLabel, gridBagConstraints);

        nameLabel = new javax.swing.JLabel();
        nameLabel.setFont(nameLabel.getFont().deriveFont(nameLabel.getFont().getStyle() | java.awt.Font.BOLD));
        nameLabel.setText(UiStrings.getString("name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 2);
        mainPanel.add(nameLabel, gridBagConstraints);

        muteLabel = new javax.swing.JLabel();
        muteLabel.setFont(muteLabel.getFont().deriveFont(muteLabel.getFont().getStyle() | java.awt.Font.BOLD));
        muteLabel.setText(UiStrings.getString("mute")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(muteLabel, gridBagConstraints);

        volumeLabel = new javax.swing.JLabel();
        volumeLabel.setFont(volumeLabel.getFont().deriveFont(volumeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        volumeLabel.setText(UiStrings.getString("volume")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 8);
        mainPanel.add(volumeLabel, gridBagConstraints);

        int gridY = 0;
        for (int i = 0; i < mSequence.getTracks().length; ++i) {

            // Do not bother if the track has no notes
            if (mSequence.trackHasNotes(i)) {

                gridY += 1;

                JLabel num = new javax.swing.JLabel();
                num.setText(Integer.toString(i));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 0;
                gridBagConstraints.gridy = gridY;
                gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
                mainPanel.add(num, gridBagConstraints);

                JLabel name = new javax.swing.JLabel();
                name.setText(mSequence.getTrackName(i));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 1;
                gridBagConstraints.gridy = gridY;
                gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 2);
                gridBagConstraints.weightx = 0.3;
                mainPanel.add(name, gridBagConstraints);

                JCheckBox mute = new javax.swing.JCheckBox();
                mute.setSelected(mSequence.isTrackMuted(i));
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 2;
                gridBagConstraints.gridy = gridY;
                gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
                mainPanel.add(mute, gridBagConstraints);
                int trackIndex = i;
                mute.addChangeListener((ChangeEvent e) -> {
                    boolean muted = mute.isSelected();
                    muteChanged(trackIndex, muted);
                });

                JSlider volSlider = new javax.swing.JSlider();
                volSlider.setMinimum(0);
                volSlider.setMaximum(100);
                volSlider.setValue((int)((mSequence.getTrackVolume(i) * 100.0) / 127.0));
                //if (gridY == -1) {
                volSlider.setMajorTickSpacing(10);
                volSlider.setPaintLabels(false);
                volSlider.setPaintTicks(true);
                //}
                gridBagConstraints = new java.awt.GridBagConstraints();
                gridBagConstraints.gridx = 3;
                gridBagConstraints.gridy = gridY;
                gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 8);
                gridBagConstraints.weightx = 0.3;
                gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
                mainPanel.add(volSlider, gridBagConstraints);
                int trackChannel = mSequence.getTrackChannel(i);
                volSlider.addChangeListener((ChangeEvent e) -> {
                    int volume = (int)((volSlider.getValue() * 127.0) / 100.0);
                    sliderChanged(trackChannel, volume);
                });
            }
        }

        gridY += 1;
        JLabel master = new javax.swing.JLabel();
        master.setFont(master.getFont().deriveFont(master.getFont().getStyle() | java.awt.Font.BOLD));
        master.setText(UiStrings.getString("master_volume")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = gridY;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(master, gridBagConstraints);

        JSlider masterVolSlider = new javax.swing.JSlider();
        masterVolSlider.setMinimum(0);
        masterVolSlider.setMaximum(100);
        masterVolSlider.setValue(100);
        masterVolSlider.setMajorTickSpacing(10);
        masterVolSlider.setPaintLabels(true);
        masterVolSlider.setPaintTicks(true);
        //}
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = gridY;
        gridBagConstraints.insets = new java.awt.Insets(2, 8, 2, 8);
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(masterVolSlider, gridBagConstraints);
        masterVolSlider.addChangeListener((ChangeEvent e) -> {
            int volume = masterVolSlider.getValue();
            masterVolumeChanged(volume);
        });
    }

    private void sliderChanged(int trackChannel, int volume) {
        final MidiChannel[] channels = MidiQuickFix.getSynth().getChannels();
        channels[trackChannel].controlChange(7, volume);
    }

    private void masterVolumeChanged(int volume) {
        final MidiChannel[] channels = MidiQuickFix.getSynth().getChannels();
        for (int i = 0; i < mSequence.getTracks().length; ++i) {
            // Do not bother if the track has no notes
            if (mSequence.trackHasNotes(i)) {
                int newVolume = (int)(mSequence.getTrackVolume(i) * (volume / 100.0));
                newVolume = Math.max(Math.min(newVolume, 127), 0);
                channels[mSequence.getTrackChannel(i)].controlChange(7, newVolume);
            }
        }
    }

    private void muteChanged(int trackIndex, boolean muted) {
        mSequence.muteTrack(trackIndex, muted);
        mSequencer.setTrackMute(trackIndex, mSequence.isTrackMuted(trackIndex));
        boolean actuallyMuted = mSequencer.getTrackMute(trackIndex);
        if (muted) {

        }
        if (actuallyMuted != mSequence.isTrackMuted(trackIndex)) {
            TraceDialog.addTrace(
                "Sequencer Mute not supported. (Set to " + muted // NOI18N
                + " is actually " + actuallyMuted + ")"); // NOI18N
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        scrollPane = new javax.swing.JScrollPane();
        mainPanel = new javax.swing.JPanel();
        numLabel = new javax.swing.JLabel();
        nameLabel = new javax.swing.JLabel();
        muteLabel = new javax.swing.JLabel();
        volumeLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        mainPanel.setLayout(new java.awt.GridBagLayout());

        numLabel.setFont(numLabel.getFont().deriveFont(numLabel.getFont().getStyle() | java.awt.Font.BOLD));
        numLabel.setText(UiStrings.getString("no.")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(numLabel, gridBagConstraints);

        nameLabel.setFont(nameLabel.getFont().deriveFont(nameLabel.getFont().getStyle() | java.awt.Font.BOLD));
        nameLabel.setText(UiStrings.getString("name")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.weightx = 0.3;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(nameLabel, gridBagConstraints);

        muteLabel.setFont(muteLabel.getFont().deriveFont(muteLabel.getFont().getStyle() | java.awt.Font.BOLD));
        muteLabel.setText(UiStrings.getString("mute")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(muteLabel, gridBagConstraints);

        volumeLabel.setFont(volumeLabel.getFont().deriveFont(volumeLabel.getFont().getStyle() | java.awt.Font.BOLD));
        volumeLabel.setText(UiStrings.getString("volume")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
        mainPanel.add(volumeLabel, gridBagConstraints);

        scrollPane.setViewportView(mainPanel);

        add(scrollPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel muteLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JLabel numLabel;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JLabel volumeLabel;
    // End of variables declaration//GEN-END:variables
}
