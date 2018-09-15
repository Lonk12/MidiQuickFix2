/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2009 John Lemcke
 * jostle@users.sourceforge.net
 *
 * This program is free software; you can redistribute it
 * and/or modify it under the terms of the Artistic License
 * as published by Larry Wall, either version 2.0,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Artistic License for more details.
 *
 * You should have received a copy of the Artistic License with this Kit,
 * in the file named "Artistic.clarified".
 * If not, I'll be glad to provide one.
 *
 *************************************************************
 */
package com.lemckes.MidiQuickFix;

import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Allow the user to transpose the sequence.
 *
 * @version $Id: TransposeDialog.java,v 1.11 2013/11/01 09:46:28 jostle Exp $
 */
public class TransposeDialog
    extends javax.swing.JDialog
{

    static final long serialVersionUID = -7390651121992118010L;
    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;
    private String mKeyString = UiStrings.getString("key_names_string") + " ";
    private int mTransposeBy = 0;

    /**
     * Creates new form TransposeDialog
     *
     * @param parent the Frame parent for the dialog
     * @param modal the modality of the dialog
     */
    public TransposeDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        jTextField1.setText(mKeyString);
        toKeyField.setText(mKeyString);
        JComponent ed = semitoneSpinner.getEditor();
        if (ed instanceof JSpinner.DefaultEditor) {
            ((JSpinner.DefaultEditor)ed).getTextField().setColumns(2);
            ((JSpinner.DefaultEditor)ed).getTextField().setEditable(false);
        }
        semitoneSpinner.addChangeListener(new ChangeListener()
        {

            public void stateChanged(ChangeEvent e) {
                mTransposeBy = (Integer)semitoneSpinner.getValue();
                updateToString(mTransposeBy);
            }
        });

        KeyHighlighter kh1 = new KeyHighlighter();
        setGlassPane(kh1);
        kh1.setVisible(true);
        kh1.addComponent(jTextField1);
        kh1.addComponent(toKeyField);
        pack();
    }

    /**
     * Rotate the string that represents the transposed keys
     * to show the result of transposing by
     * <code>semitones</code>
     *
     * @param semitones the number of semitones
     */
    private void updateToString(int semitones) {
        int splitPos = (semitones % 12) * 3;
        splitPos = splitPos < 0 ? splitPos + 36 : splitPos;
        String shiftedString = mKeyString.substring(splitPos);
        shiftedString += mKeyString.substring(0, splitPos);
        toKeyField.setText(shiftedString);
    }

    /**
     * Get the setting of the
     * <code>semitones</code> field
     *
     * @return the number of semitones to transpose
     */
    public int getTransposeBy() {
        return mTransposeBy;
    }

    /**
     * Set the number of semitones by which to transpose.
     * Mostly useful for resetting the transpose to zero
     * after each transposition.
     *
     * @param semitones the number of semitones
     */
    public void setTransposeBy(int semitones) {
        mTransposeBy = semitones;
        semitoneSpinner.setValue(mTransposeBy);
    }

    /**
     * Get the setting of the doDrums checkbox
     *
     * @return
     * <code>true</code> if the drum channel should be transposed
     */
    public boolean getDoDrums() {
        return doDrumsCheckBox.isSelected();
    }

    /**
     * Get the status of the dialog when it was closed
     *
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        semitoneSpinner = new javax.swing.JSpinner();
        toKeyField = new javax.swing.JTextField();
        fromLabel = new javax.swing.JLabel();
        toLabel = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        doDrumsCheckBox = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setTitle(UiStrings.getString("transpose_sequence")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        mainPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.setLayout(new java.awt.GridBagLayout());

        jTextField1.setEditable(false);
        jTextField1.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        jTextField1.setText(" C  Db D  Eb E  F  F# G  Ab A  Bb B ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        mainPanel.add(jTextField1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 0);
        mainPanel.add(semitoneSpinner, gridBagConstraints);

        toKeyField.setEditable(false);
        toKeyField.setFont(new java.awt.Font("Monospaced", 0, 12)); // NOI18N
        toKeyField.setText(" C  Db D  Eb E  F  F# G  Ab A  Bb B ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        mainPanel.add(toKeyField, gridBagConstraints);

        fromLabel.setText(UiStrings.getString("from")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mainPanel.add(fromLabel, gridBagConstraints);

        toLabel.setText(UiStrings.getString("to")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        mainPanel.add(toLabel, gridBagConstraints);

        jLabel1.setText(UiStrings.getString("semitones")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        mainPanel.add(jLabel1, gridBagConstraints);

        doDrumsCheckBox.setText(UiStrings.getString("transpose_drum_channel")); // NOI18N
        doDrumsCheckBox.setToolTipText(UiStrings.getString("transpose_drums_tooltip")); // NOI18N
        doDrumsCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        mainPanel.add(doDrumsCheckBox, gridBagConstraints);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        okButton.setText(UiStrings.getString("ok")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        cancelButton.setText(UiStrings.getString("cancel")); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        jPanel3.add(buttonPanel);

        getContentPane().add(jPanel3, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JCheckBox doDrumsCheckBox;
    private javax.swing.JLabel fromLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JSpinner semitoneSpinner;
    private javax.swing.JTextField toKeyField;
    private javax.swing.JLabel toLabel;
    // End of variables declaration//GEN-END:variables
    private int returnStatus = RET_CANCEL;

    /**
     * This class uses the GlassPane to highlight the columns in the
     * text fields.
     * There are probably much better ways to do this but it seemed
     * like a good idea at the time.
     */
    class KeyHighlighter
            extends JComponent
    {

        static final long serialVersionUID = -3094419739144867668L;
        private List<JTextField> comps;

        public KeyHighlighter() {
            comps = new ArrayList<JTextField>();
        }

        public void addComponent(JTextField c) {
            comps.add(c);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D)g;

            Color c1 = new Color(0, 240, 255, 64);

            for (JTextField comp : comps) {
                Rectangle bounds = comp.getBounds();
                Insets insets = comp.getInsets();
                Insets margin = comp.getMargin();
                Rectangle newBounds = new Rectangle();
                newBounds.setBounds(
                    bounds.x + insets.left + margin.left,
                    bounds.y + insets.top + margin.top,
                    bounds.width - insets.left - margin.left - insets.right - margin.right,
                    bounds.height - insets.top - margin.top - insets.bottom - margin.bottom);
                Rectangle r = new Rectangle();
                int width = bounds.width / 12;
                int step = width * 2;
                int x = bounds.x + width + width / 6;
                int y = bounds.y;
                for (int i = 0; i < 6; ++i) {
                    r.setBounds(x + i * step, y, width, bounds.height);
                    g2.setPaint(c1);
                    g2.fill(r);
                }
            }
        }
    }
}
