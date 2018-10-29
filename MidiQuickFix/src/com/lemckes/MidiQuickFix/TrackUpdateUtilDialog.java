/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2018 John Lemcke
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
import java.awt.Component;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.border.TitledBorder;

/**
 * Provide utilities to modify tracks
 */
public class TrackUpdateUtilDialog extends javax.swing.JDialog
{

    TrackEditorPanel mEditor;

    /**
     * Create a new TrackUpdateUtilDialog for the given Sequence
     * @param editor
     * @param parent
     * @param modal
     */
    public TrackUpdateUtilDialog(
        TrackEditorPanel editor, Component parent, boolean modal) {
        super(MidiQuickFix.getMainFrame(), modal);
        initComponents();
        mEditor = editor;
        String title = UiStrings.getString("track") + " : " +mEditor.getCurrentTrackTitle();
        ((TitledBorder)mainPanel.getBorder()).setTitle(title);
        setLocationRelativeTo(parent);
        pack();
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
        convertNoteOnLabel = new javax.swing.JLabel();
        convertNoteOnButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        setNoteOnVelocityLabel = new javax.swing.JLabel();
        setVelocityPanel = new javax.swing.JPanel();
        setVelocitySpinner = new javax.swing.JSpinner();
        setNoteOnVelocityButton = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JSeparator();
        adjustNoteOnVelocityLabel = new javax.swing.JLabel();
        adjustVelocityPanel = new javax.swing.JPanel();
        adjustVelocityFactorField = new javax.swing.JFormattedTextField();
        adjustNoteOnVelocityButton = new javax.swing.JButton();
        jSeparator7 = new javax.swing.JSeparator();
        shiftEventsLabel = new javax.swing.JLabel();
        shiftEventsPanel = new javax.swing.JPanel();
        shiftEventsField = new javax.swing.JFormattedTextField();
        shiftEventsButton = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JSeparator();
        convertTextLabel = new javax.swing.JLabel();
        convertTextButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JSeparator();
        addSpaceLabel = new javax.swing.JLabel();
        addSpaceButton = new javax.swing.JButton();
        jSeparator5 = new javax.swing.JSeparator();
        deleteNotesLabel = new javax.swing.JLabel();
        removeNotesButton = new javax.swing.JButton();
        jSeparator6 = new javax.swing.JSeparator();
        convertTypeZeroLabel = new javax.swing.JLabel();
        convertTypeZeroButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(UiStrings.getString("TrackUpdateUtilDialog.title")); // NOI18N
        setName("Form"); // NOI18N

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, UiStrings.getString("TrackUpdateUtilDialog.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.TOP, new java.awt.Font("Dialog", 1, 12), new java.awt.Color(0, 0, 0))); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.GridBagLayout());

        convertNoteOnLabel.setText(UiStrings.getString("TrackUpdateUtilDialog.convertNoteOnLabel.text")); // NOI18N
        convertNoteOnLabel.setName("convertNoteOnLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        mainPanel.add(convertNoteOnLabel, gridBagConstraints);

        convertNoteOnButton.setText(UiStrings.getString("TrackUpdateUtilDialog.convertNoteOnButton.text")); // NOI18N
        convertNoteOnButton.setName("convertNoteOnButton"); // NOI18N
        convertNoteOnButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertNoteOnButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(convertNoteOnButton, gridBagConstraints);

        jSeparator1.setName("jSeparator1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        mainPanel.add(jSeparator1, gridBagConstraints);

        setNoteOnVelocityLabel.setText(UiStrings.getString("TrackUpdateUtilDialog.setNoteOnVelocityLabel.text")); // NOI18N
        setNoteOnVelocityLabel.setName("setNoteOnVelocityLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        mainPanel.add(setNoteOnVelocityLabel, gridBagConstraints);

        setVelocityPanel.setName("setVelocityPanel"); // NOI18N

        setVelocitySpinner.setModel(new javax.swing.SpinnerNumberModel(127, 1, 127, 1));
        setVelocitySpinner.setName("setVelocitySpinner"); // NOI18N
        setVelocityPanel.add(setVelocitySpinner);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        mainPanel.add(setVelocityPanel, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings"); // NOI18N
        setNoteOnVelocityButton.setText(bundle.getString("TrackUpdateUtilDialog.setNoteOnVelocityButton.text")); // NOI18N
        setNoteOnVelocityButton.setName("setNoteOnVelocityButton"); // NOI18N
        setNoteOnVelocityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setNoteOnVelocityButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(setNoteOnVelocityButton, gridBagConstraints);

        jSeparator2.setName("jSeparator2"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        mainPanel.add(jSeparator2, gridBagConstraints);

        adjustNoteOnVelocityLabel.setText(UiStrings.getString("TrackUpdateUtilDialog.adjustNoteOnVelocityLabel.text")); // NOI18N
        adjustNoteOnVelocityLabel.setName("adjustNoteOnVelocityLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        mainPanel.add(adjustNoteOnVelocityLabel, gridBagConstraints);

        adjustVelocityPanel.setName("adjustVelocityPanel"); // NOI18N

        adjustVelocityFactorField.setColumns(4);
        adjustVelocityFactorField.setText(UiStrings.getString("TrackUpdateUtilDialog.adjustVelocityFactorField.text")); // NOI18N
        adjustVelocityFactorField.setName("adjustVelocityFactorField"); // NOI18N
        adjustVelocityFactorField.setValue(1.0f);
        adjustVelocityPanel.add(adjustVelocityFactorField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        mainPanel.add(adjustVelocityPanel, gridBagConstraints);

        adjustNoteOnVelocityButton.setText(bundle.getString("TrackUpdateUtilDialog.adjustNoteOnVelocityButton.text")); // NOI18N
        adjustNoteOnVelocityButton.setName("adjustNoteOnVelocityButton"); // NOI18N
        adjustNoteOnVelocityButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adjustNoteOnVelocityButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(adjustNoteOnVelocityButton, gridBagConstraints);

        jSeparator7.setName("jSeparator7"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        mainPanel.add(jSeparator7, gridBagConstraints);

        shiftEventsLabel.setText(UiStrings.getString("TrackUpdateUtilDialog.shiftEventsLabel.text")); // NOI18N
        shiftEventsLabel.setName("shiftEventsLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        mainPanel.add(shiftEventsLabel, gridBagConstraints);

        shiftEventsPanel.setName("shiftEventsPanel"); // NOI18N

        shiftEventsField.setColumns(4);
        shiftEventsField.setToolTipText(UiStrings.getString("TrackUpdateUtilDialog.shiftEventsField.toolTipText")); // NOI18N
        shiftEventsField.setName("shiftEventsField"); // NOI18N
        shiftEventsField.setValue(0l);
        shiftEventsPanel.add(shiftEventsField);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        mainPanel.add(shiftEventsPanel, gridBagConstraints);

        shiftEventsButton.setText(UiStrings.getString("TrackUpdateUtilDialog.shiftEventsButton.text")); // NOI18N
        shiftEventsButton.setName("shiftEventsButton"); // NOI18N
        shiftEventsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                shiftEventsButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(shiftEventsButton, gridBagConstraints);

        jSeparator3.setName("jSeparator3"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        mainPanel.add(jSeparator3, gridBagConstraints);

        convertTextLabel.setText(UiStrings.getString("TrackUpdateUtilDialog.convertTextLabel.text")); // NOI18N
        convertTextLabel.setName("convertTextLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        mainPanel.add(convertTextLabel, gridBagConstraints);

        convertTextButton.setText(UiStrings.getString("TrackUpdateUtilDialog.convertTextButton.text")); // NOI18N
        convertTextButton.setName("convertTextButton"); // NOI18N
        convertTextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertTextButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(convertTextButton, gridBagConstraints);

        jSeparator4.setName("jSeparator4"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        mainPanel.add(jSeparator4, gridBagConstraints);

        addSpaceLabel.setText(UiStrings.getString("TrackUpdateUtilDialog.addSpaceLabel.text")); // NOI18N
        addSpaceLabel.setName("addSpaceLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        mainPanel.add(addSpaceLabel, gridBagConstraints);

        addSpaceButton.setText(UiStrings.getString("TrackUpdateUtilDialog.addSpaceButton.text")); // NOI18N
        addSpaceButton.setName("addSpaceButton"); // NOI18N
        addSpaceButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSpaceButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(addSpaceButton, gridBagConstraints);

        jSeparator5.setName("jSeparator5"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        mainPanel.add(jSeparator5, gridBagConstraints);

        deleteNotesLabel.setText(UiStrings.getString("TrackUpdateUtilDialog.deleteNotesLabel.text")); // NOI18N
        deleteNotesLabel.setName("deleteNotesLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        mainPanel.add(deleteNotesLabel, gridBagConstraints);

        removeNotesButton.setText(UiStrings.getString("TrackUpdateUtilDialog.removeNotesButton.text")); // NOI18N
        removeNotesButton.setName("removeNotesButton"); // NOI18N
        removeNotesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeNotesButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(removeNotesButton, gridBagConstraints);

        jSeparator6.setName("jSeparator6"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(7, 0, 7, 0);
        mainPanel.add(jSeparator6, gridBagConstraints);

        convertTypeZeroLabel.setText(UiStrings.getString("TrackUpdateUtilDialog.convertTypeZeroLabel.text")); // NOI18N
        convertTypeZeroLabel.setName("convertTypeZeroLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        mainPanel.add(convertTypeZeroLabel, gridBagConstraints);

        convertTypeZeroButton.setText(UiStrings.getString("TrackUpdateUtilDialog.convertTypeZeroButton.text")); // NOI18N
        convertTypeZeroButton.setName("convertTypeZeroButton"); // NOI18N
        convertTypeZeroButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                convertTypeZeroButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 14;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(convertTypeZeroButton, gridBagConstraints);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING));

        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        cancelButton.setText(UiStrings.getString("close")); // NOI18N
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

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void convertNoteOnButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertNoteOnButtonActionPerformed
        mEditor.convertNoteOn();
}//GEN-LAST:event_convertNoteOnButtonActionPerformed

    private void convertTextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertTextButtonActionPerformed
        mEditor.convertText();
}//GEN-LAST:event_convertTextButtonActionPerformed

    private void removeNotesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeNotesButtonActionPerformed
        mEditor.removeNotes();
}//GEN-LAST:event_removeNotesButtonActionPerformed

    private void convertTypeZeroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_convertTypeZeroButtonActionPerformed
        mEditor.splitTrack();
    }//GEN-LAST:event_convertTypeZeroButtonActionPerformed

    private void addSpaceButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSpaceButtonActionPerformed
        mEditor.addSpaceToLyrics();
    }//GEN-LAST:event_addSpaceButtonActionPerformed

    private void adjustNoteOnVelocityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_adjustNoteOnVelocityButtonActionPerformed
        mEditor.adjustNoteOnVelocity((Float)(adjustVelocityFactorField.getValue()));
    }//GEN-LAST:event_adjustNoteOnVelocityButtonActionPerformed

    private void setNoteOnVelocityButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setNoteOnVelocityButtonActionPerformed
        mEditor.setNoteOnVelocity((Integer)setVelocitySpinner.getValue());
    }//GEN-LAST:event_setNoteOnVelocityButtonActionPerformed

    private void shiftEventsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_shiftEventsButtonActionPerformed
        try {
            shiftEventsField.commitEdit();
            mEditor.shiftEvents((long)shiftEventsField.getValue());
        } catch (ParseException ex) {
            Logger.getLogger(TrackUpdateUtilDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_shiftEventsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addSpaceButton;
    private javax.swing.JLabel addSpaceLabel;
    private javax.swing.JButton adjustNoteOnVelocityButton;
    private javax.swing.JLabel adjustNoteOnVelocityLabel;
    private javax.swing.JFormattedTextField adjustVelocityFactorField;
    private javax.swing.JPanel adjustVelocityPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton convertNoteOnButton;
    private javax.swing.JLabel convertNoteOnLabel;
    private javax.swing.JButton convertTextButton;
    private javax.swing.JLabel convertTextLabel;
    private javax.swing.JButton convertTypeZeroButton;
    private javax.swing.JLabel convertTypeZeroLabel;
    private javax.swing.JLabel deleteNotesLabel;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton removeNotesButton;
    private javax.swing.JButton setNoteOnVelocityButton;
    private javax.swing.JLabel setNoteOnVelocityLabel;
    private javax.swing.JPanel setVelocityPanel;
    private javax.swing.JSpinner setVelocitySpinner;
    private javax.swing.JButton shiftEventsButton;
    private javax.swing.JFormattedTextField shiftEventsField;
    private javax.swing.JLabel shiftEventsLabel;
    private javax.swing.JPanel shiftEventsPanel;
    // End of variables declaration//GEN-END:variables
}
