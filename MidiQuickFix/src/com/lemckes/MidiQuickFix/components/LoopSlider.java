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

package com.lemckes.MidiQuickFix.components;

import com.lemckes.MidiQuickFix.util.DrawnIcon;
import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.LoopSliderEvent;
import com.lemckes.MidiQuickFix.util.LoopSliderListener;
import com.lemckes.MidiQuickFix.util.RegexFormatter;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.GeneralPath;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.text.DefaultFormatterFactory;

/**
 * A duration slider that can have loop points
 * @see DurationSlider
 * @version $Id$
 */
public class LoopSlider extends javax.swing.JPanel implements ChangeListener {
    
    transient private DrawnIcon inIcon;
    transient private DrawnIcon outIcon;
    transient private GeneralPath inPath = new GeneralPath();
    transient private GeneralPath outPath = new GeneralPath();
    
    private int resolution;
    
    private int loopInPoint = 0;
    private int loopOutPoint = 0;
    
    /** Creates new form LoopSlider */
    public LoopSlider() {
        initComponents();
        
        RegexFormatter formatter = new RegexFormatter("[0-9]+:[0-9]+");
        formatter.setAllowsInvalid(false);
        formatter.setOverwriteMode(false);
        formatter.setCommitsOnValidEdit(true);
        loopInField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        loopInField.setFormatterFactory(new DefaultFormatterFactory(formatter));
        loopOutField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        loopOutField.setFormatterFactory(new DefaultFormatterFactory(formatter));
        currPositionField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        currPositionField.setFormatterFactory(new DefaultFormatterFactory(formatter));
        
        loopInButton.setPreferredSize(new Dimension(25,14));
        inPath.moveTo(0.3f, 0.2f);
        inPath.lineTo(0.3f, 0.8f);
        inPath.lineTo(0.3f, 0.4f);
        inPath.lineTo(0.6f, 0.2f);
        inPath.closePath();
        inPath.moveTo(0.1f, 0.8f);
        inPath.lineTo(0.9f, 0.8f);
        inPath.moveTo(0.2f, 0.7f);
        inPath.lineTo(0.2f, 0.8f);
        inPath.moveTo(0.4f, 0.7f);
        inPath.lineTo(0.4f, 0.8f);
        inPath.moveTo(0.6f, 0.7f);
        inPath.lineTo(0.6f, 0.8f);
        inPath.moveTo(0.8f, 0.7f);
        inPath.lineTo(0.8f, 0.8f);
        inIcon = new DrawnIcon(loopInButton, inPath);
        inIcon.setFilled(true);
        inIcon.setFillColour(Color.BLACK);
        loopInButton.setIcon(inIcon);
        
        loopOutButton.setPreferredSize(new Dimension(25,14));
        outPath.moveTo(0.4f, 0.2f);
        outPath.lineTo(0.7f, 0.2f);
        outPath.lineTo(0.7f, 0.8f);
        outPath.lineTo(0.7f, 0.4f);
        outPath.closePath();
        outPath.moveTo(0.1f, 0.8f);
        outPath.lineTo(0.9f, 0.8f);
        outPath.moveTo(0.2f, 0.7f);
        outPath.lineTo(0.2f, 0.8f);
        outPath.moveTo(0.4f, 0.7f);
        outPath.lineTo(0.4f, 0.8f);
        outPath.moveTo(0.6f, 0.7f);
        outPath.lineTo(0.6f, 0.8f);
        outPath.moveTo(0.8f, 0.7f);
        outPath.lineTo(0.8f, 0.8f);
        outIcon = new DrawnIcon(loopOutButton, outPath);
        outIcon.setFilled(true);
        outIcon.setFillColour(Color.BLACK);
        loopOutButton.setIcon(outIcon);
        
        durationSlider.addChangeListener(this);
        
        mListenerList = new EventListenerList();
    }
    
    public void addLoopSliderListener(LoopSliderListener l) {
        mListenerList.add(LoopSliderListener.class, l);
    }
    
    public void setValue(int val) {
        durationSlider.setValue(val);
    }
    
    public void setDuration(long duration, boolean ticks, int resolution) {
        this.resolution = resolution;
        loopInPoint = 0;
        loopInField.setText(Formats.formatTicks(loopInPoint, resolution, false));
        loopOutPoint = (int)duration;
        loopOutField.setText(Formats.formatTicks(loopOutPoint, resolution, false));
        durationSlider.setDuration(duration, ticks, resolution);
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        int val = durationSlider.getValue();
        //System.out.println("stateChanged val = " + val);
        currPositionField.setText(Formats.formatTicks(val, resolution, false));
        fireLoopSliderChanged(durationSlider.getValueIsAdjusting());
    }
    
    public int getLoopInPoint() {
        return loopInPoint;
    }
    
    public void setLoopInPoint(int loopInPoint) {
        loopInPoint = loopInPoint < 0 ? 0 : loopInPoint;
        loopInPoint = loopInPoint > durationSlider.getMaximum() ?
            durationSlider.getMaximum() :
            loopInPoint;
        if (this.loopInPoint != loopInPoint) {
            this.loopInPoint = loopInPoint;
            loopInField.setText(Formats.formatTicks(loopInPoint, resolution, false));
            fireLoopPointChanged();
        }
    }
    
    public int getLoopOutPoint() {
        return loopOutPoint;
    }
    
    public void setLoopOutPoint(int loopOutPoint) {
        loopOutPoint = loopOutPoint < 0 ? 0 : loopOutPoint;
        loopOutPoint = loopOutPoint > durationSlider.getMaximum() ?
            durationSlider.getMaximum() :
            loopOutPoint;
        if (loopOutPoint >= 0 && this.loopOutPoint != loopOutPoint) {
            this.loopOutPoint = loopOutPoint;
            loopOutField.setText(Formats.formatTicks(loopOutPoint, resolution, false));
            fireLoopPointChanged();
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        loopInField = new javax.swing.JFormattedTextField();
        loopOutField = new javax.swing.JFormattedTextField();
        durationSlider = new com.lemckes.MidiQuickFix.components.DurationSlider();
        currPositionField = new javax.swing.JFormattedTextField();
        loopInButton = new javax.swing.JButton();
        loopOutButton = new javax.swing.JButton();

        setLayout(new java.awt.GridBagLayout());

        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(3, 3, 3, 3)));
        loopInField.setColumns(8);
        loopInField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        loopInField.setFont(new java.awt.Font("DialogInput", 0, 14));
        loopInField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopInFieldActionPerformed(evt);
            }
        });
        loopInField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                loopInFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        add(loopInField, gridBagConstraints);

        loopOutField.setColumns(8);
        loopOutField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        loopOutField.setFont(new java.awt.Font("DialogInput", 0, 14));
        loopOutField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopOutFieldActionPerformed(evt);
            }
        });
        loopOutField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                loopOutFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        add(loopOutField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(durationSlider, gridBagConstraints);

        currPositionField.setColumns(8);
        currPositionField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        currPositionField.setFont(new java.awt.Font("DialogInput", 0, 14));
        currPositionField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                currPositionFieldActionPerformed(evt);
            }
        });
        currPositionField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                currPositionFieldFocusLost(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 3, 0, 2);
        add(currPositionField, gridBagConstraints);

        loopInButton.setToolTipText("Set Loop-In Point");
        loopInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopInButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(loopInButton, gridBagConstraints);

        loopOutButton.setToolTipText("Set Loop-Out Point");
        loopOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopOutButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        add(loopOutButton, gridBagConstraints);

    }
    // </editor-fold>//GEN-END:initComponents
    
    private void loopOutFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopOutFieldActionPerformed
        System.out.println("loopOutFieldActionPerformed");
        setLoopOutPoint((int)Formats.parseTicks(loopOutField.getText(), resolution));
    }//GEN-LAST:event_loopOutFieldActionPerformed
    
    private void loopOutFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_loopOutFieldFocusLost
        System.out.println("loopOutFieldFocusLost");
        setLoopOutPoint((int)Formats.parseTicks(loopOutField.getText(), resolution));
    }//GEN-LAST:event_loopOutFieldFocusLost
    
    private void currPositionFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currPositionFieldActionPerformed
        System.out.println("currPositionFieldActionPerformed");
        setValue((int)Formats.parseTicks(currPositionField.getText(), resolution));
    }//GEN-LAST:event_currPositionFieldActionPerformed
    
    private void currPositionFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_currPositionFieldFocusLost
        System.out.println("currPositionFieldFocusLost");
        setValue((int)Formats.parseTicks(currPositionField.getText(), resolution));
    }//GEN-LAST:event_currPositionFieldFocusLost
    
    private void loopInFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_loopInFieldFocusLost
        System.out.println("loopInFieldFocusLost");
        setLoopInPoint((int)Formats.parseTicks(loopInField.getText(), resolution));
    }//GEN-LAST:event_loopInFieldFocusLost
    
    private void loopInFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopInFieldActionPerformed
        System.out.println("loopInFieldActionPerformed");
        setLoopInPoint((int)Formats.parseTicks(loopInField.getText(), resolution));
    }//GEN-LAST:event_loopInFieldActionPerformed
    
    private void loopOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopOutButtonActionPerformed
        setLoopOutPoint(durationSlider.getValue());
    }//GEN-LAST:event_loopOutButtonActionPerformed
    
    private void loopInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopInButtonActionPerformed
        setLoopInPoint(durationSlider.getValue());
    }//GEN-LAST:event_loopInButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField currPositionField;
    private com.lemckes.MidiQuickFix.components.DurationSlider durationSlider;
    private javax.swing.JButton loopInButton;
    private javax.swing.JFormattedTextField loopInField;
    private javax.swing.JButton loopOutButton;
    private javax.swing.JFormattedTextField loopOutField;
    // End of variables declaration//GEN-END:variables
    
    /**
     * Notify listeners that are interested in loop slider events.
     * @param valueIsAdjusting True if the slider is being adjusted.
     */
    protected void fireLoopSliderChanged(boolean valueIsAdjusting) {
        LoopSliderListener[] keyListeners =
          (LoopSliderListener[])
          (mListenerList.getListeners(LoopSliderListener.class));
        for (int i = keyListeners.length - 1; i >= 0; --i) {
            keyListeners[i].loopSliderChanged(
              new LoopSliderEvent(durationSlider.getValue(),
              loopInPoint, loopOutPoint, valueIsAdjusting));
        }
    }
    
    /**
     * Notify listeners that are interested in loop slider events.
     * @param valueIsAdjusting True if the slider is being adjusted.
     */
    protected void fireLoopPointChanged() {
        LoopSliderListener[] keyListeners =
          (LoopSliderListener[])
          (mListenerList.getListeners(LoopSliderListener.class));
        for (int i = keyListeners.length - 1; i >= 0; --i) {
            keyListeners[i].loopPointChanged(
              new LoopSliderEvent(durationSlider.getValue(), loopInPoint, loopOutPoint, false));
        }
    }
    
    /** The list of registered listeners. */
    protected EventListenerList mListenerList;
    
}
