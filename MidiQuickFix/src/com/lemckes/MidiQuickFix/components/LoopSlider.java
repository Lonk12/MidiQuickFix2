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
package com.lemckes.MidiQuickFix.components;

import com.lemckes.MidiQuickFix.util.BarBeatTick;
import com.lemckes.MidiQuickFix.util.DrawnIcon;
import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.LoopSliderEvent;
import com.lemckes.MidiQuickFix.util.LoopSliderListener;
import com.lemckes.MidiQuickFix.util.RegexFormatter;
import com.lemckes.MidiQuickFix.util.SequencePosition;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.GeneralPath;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.Sequence;
import javax.swing.JFormattedTextField;
import javax.swing.event.ChangeListener;
import javax.swing.text.DefaultFormatterFactory;

/**
 * A duration slider that can have loop points
 *
 * @see DurationSlider
 * @version $Id$
 */
public class LoopSlider
    extends javax.swing.JPanel
    implements ChangeListener
{

    static final long serialVersionUID = -2520861084060073513L;
    transient private DrawnIcon mInIcon;
    transient private DrawnIcon mOutIcon;
    transient private GeneralPath mInPath;
    transient private GeneralPath mOutPath;
    private int mResolution;
    private long mLoopInPoint = 0;
    private long mLoopOutPoint = 0;
    private Color mLoopFieldBg;
    private BarBeatTick mBarBeatTick;

    /**
     * Creates a new LoopSlider
     */
    public LoopSlider() {
        initComponents();

        mLoopFieldBg = loopInField.getBackground();
        
        try {
            mBarBeatTick = new BarBeatTick(new Sequence(Sequence.PPQ, 192, 1));
        } catch (InvalidMidiDataException ex) {
            ex.printStackTrace();
        }

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

        configureLoopButtons();

        durationSlider.addChangeListener(this);

    }

    public void setBarBeatTick(BarBeatTick barBeatTick){
        mBarBeatTick = barBeatTick;
    }

    public void addLoopSliderListener(LoopSliderListener l) {
        listenerList.add(LoopSliderListener.class, l);
    }

    public void setValue(long val, boolean updateSequence) {
        if (!updateSequence) {
            durationSlider.removeChangeListener(this);
        }
        durationSlider.setValue((int)val);
        if (!updateSequence) {
            setPositionField(val);
            setBarField(mBarBeatTick.getSequencePosition(val));
            durationSlider.addChangeListener(this);
        }
    }

    public void setPositionField(long tick) {
        currPositionField.setText(Formats.formatTicks(tick, mResolution, false));
    }

    public void setBarField(SequencePosition position) {
        String pos = position.getBar() + ":" + position.getBeat();
        barCountField.setText(pos);
    }

    public void setDuration(long duration, boolean ticks, int resolution) {
        mResolution = resolution;
        mLoopInPoint = 0;
        loopInField.setText(Formats.formatTicks(mLoopInPoint, resolution, false));
        mLoopOutPoint = duration;
        loopOutField.setText(Formats.formatTicks(mLoopOutPoint, resolution, false));
        durationSlider.setDuration(duration, ticks, resolution);
    }

    @Override
    public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
        int val = durationSlider.getValue();
        setPositionField(val);
        setBarField(mBarBeatTick.getSequencePosition(val));
        fireLoopSliderChanged(durationSlider.getValueIsAdjusting());
    }

    public long getLoopInPoint() {
        return mLoopInPoint;
    }

    public void setLoopInPoint(int loopInPoint) {
        int inPoint = loopInPoint;
        inPoint = inPoint < 0 ? 0 : inPoint;
        inPoint = inPoint > durationSlider.getMaximum()
            ? durationSlider.getMaximum() : inPoint;
        if (mLoopInPoint != inPoint) {
            mLoopInPoint = inPoint;
            loopInField.setText(Formats.formatTicks(mLoopInPoint, mResolution, false));
            fireLoopPointChanged();
        }
    }

    public long getLoopOutPoint() {
        return mLoopOutPoint;
    }

    public void setLoopOutPoint(int loopOutPoint) {
        int outPoint = loopOutPoint;
        outPoint = outPoint < 0 ? 0 : outPoint;
        outPoint = outPoint > durationSlider.getMaximum()
            ? durationSlider.getMaximum() : outPoint;
        if (outPoint >= 0 && mLoopOutPoint != outPoint) {
            mLoopOutPoint = outPoint;
            loopOutField.setText(Formats.formatTicks(mLoopOutPoint, mResolution, false));
            fireLoopPointChanged();
        }
    }

    /**
     * Reset the position to zero, the loop points to the start and end
     * of the sequence and turn looping off
     */
    public void reset() {
        durationSlider.setValue(0);
        setIsLooping(false);
        mLoopInPoint = 0;
        loopInField.setText(Formats.formatTicks(mLoopInPoint, mResolution, false));
        mLoopOutPoint = durationSlider.getMaximum();
        loopOutField.setText(Formats.formatTicks(mLoopOutPoint, mResolution, false));
    }

    public void setIsLooping(boolean looping) {
        if (looping) {
            loopInField.setBackground(Color.decode("0xc0ffb0"));
            loopOutField.setBackground(Color.decode("0xc0ffb0"));
        } else {
            loopInField.setBackground(mLoopFieldBg);
            loopOutField.setBackground(mLoopFieldBg);
        }
    }

    private void configureLoopButtons() {
        loopInButton.setPreferredSize(new Dimension(25, 14));
        mInPath = new GeneralPath();
        mInPath.moveTo(0.3f, 0.2f);
        mInPath.lineTo(0.3f, 0.8f);
        mInPath.lineTo(0.3f, 0.4f);
        mInPath.lineTo(0.6f, 0.2f);
        mInPath.closePath();
        mInPath.moveTo(0.1f, 0.8f);
        mInPath.lineTo(0.9f, 0.8f);
        mInPath.moveTo(0.2f, 0.7f);
        mInPath.lineTo(0.2f, 0.8f);
        mInPath.moveTo(0.4f, 0.7f);
        mInPath.lineTo(0.4f, 0.8f);
        mInPath.moveTo(0.6f, 0.7f);
        mInPath.lineTo(0.6f, 0.8f);
        mInPath.moveTo(0.8f, 0.7f);
        mInPath.lineTo(0.8f, 0.8f);
        mInIcon = new DrawnIcon(loopInButton, mInPath);
        mInIcon.setFilled(true);
        mInIcon.setFillColour(Color.BLACK);
        loopInButton.setIcon(mInIcon);

        loopOutButton.setPreferredSize(new Dimension(25, 14));
        mOutPath = new GeneralPath();
        mOutPath.moveTo(0.4f, 0.2f);
        mOutPath.lineTo(0.7f, 0.2f);
        mOutPath.lineTo(0.7f, 0.8f);
        mOutPath.lineTo(0.7f, 0.4f);
        mOutPath.closePath();
        mOutPath.moveTo(0.1f, 0.8f);
        mOutPath.lineTo(0.9f, 0.8f);
        mOutPath.moveTo(0.2f, 0.7f);
        mOutPath.lineTo(0.2f, 0.8f);
        mOutPath.moveTo(0.4f, 0.7f);
        mOutPath.lineTo(0.4f, 0.8f);
        mOutPath.moveTo(0.6f, 0.7f);
        mOutPath.lineTo(0.6f, 0.8f);
        mOutPath.moveTo(0.8f, 0.7f);
        mOutPath.lineTo(0.8f, 0.8f);
        mOutIcon = new DrawnIcon(loopOutButton, mOutPath);
        mOutIcon.setFilled(true);
        mOutIcon.setFillColour(Color.BLACK);
        loopOutButton.setIcon(mOutIcon);
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

        loopInLabel = new javax.swing.JLabel();
        loopInField = new javax.swing.JFormattedTextField();
        loopInButton = new javax.swing.JButton();
        currPositionField = new javax.swing.JFormattedTextField();
        loopOutButton = new javax.swing.JButton();
        loopOutField = new javax.swing.JFormattedTextField();
        loopOutLabel = new javax.swing.JLabel();
        durationSlider = new com.lemckes.MidiQuickFix.components.DurationSlider();
        barCountField = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        loopInLabel.setText(UiStrings.getString("in")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(loopInLabel, gridBagConstraints);

        loopInField.setBackground(new java.awt.Color(233, 247, 255));
        loopInField.setColumns(8);
        loopInField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        loopInField.setFont(new java.awt.Font("DialogInput", 0, 14)); // NOI18N
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
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        add(loopInField, gridBagConstraints);

        loopInButton.setToolTipText(UiStrings.getString("loop-in_point_tooltip")); // NOI18N
        loopInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopInButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        add(loopInButton, gridBagConstraints);

        currPositionField.setBackground(new java.awt.Color(233, 247, 255));
        currPositionField.setColumns(8);
        currPositionField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        currPositionField.setFont(new java.awt.Font("DialogInput", 0, 14)); // NOI18N
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
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 3, 0, 2);
        add(currPositionField, gridBagConstraints);

        loopOutButton.setToolTipText(UiStrings.getString("loop-out_point_tooltip")); // NOI18N
        loopOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loopOutButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        add(loopOutButton, gridBagConstraints);

        loopOutField.setBackground(new java.awt.Color(233, 247, 255));
        loopOutField.setColumns(8);
        loopOutField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        loopOutField.setFont(new java.awt.Font("DialogInput", 0, 14)); // NOI18N
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
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        add(loopOutField, gridBagConstraints);

        loopOutLabel.setText(UiStrings.getString("out")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        add(loopOutLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        add(durationSlider, gridBagConstraints);

        barCountField.setBackground(new java.awt.Color(233, 247, 255));
        barCountField.setColumns(6);
        barCountField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(1, 0, 0, 0);
        add(barCountField, gridBagConstraints);

        jLabel1.setText("Bar:Beat");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 5);
        add(jLabel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    private void loopOutFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopOutFieldActionPerformed
        setLoopOutPoint(Formats.parseTicks(loopOutField.getText(), mResolution));
    }//GEN-LAST:event_loopOutFieldActionPerformed

    private void loopOutFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_loopOutFieldFocusLost
        setLoopOutPoint(Formats.parseTicks(loopOutField.getText(), mResolution));
    }//GEN-LAST:event_loopOutFieldFocusLost

    private void currPositionFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_currPositionFieldActionPerformed
        setValue(Formats.parseTicks(currPositionField.getText(), mResolution), true);
    }//GEN-LAST:event_currPositionFieldActionPerformed

    private void currPositionFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_currPositionFieldFocusLost
        setValue(Formats.parseTicks(currPositionField.getText(), mResolution), true);
    }//GEN-LAST:event_currPositionFieldFocusLost

    private void loopInFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_loopInFieldFocusLost
        setLoopInPoint(Formats.parseTicks(loopInField.getText(), mResolution));
    }//GEN-LAST:event_loopInFieldFocusLost

    private void loopInFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopInFieldActionPerformed
        setLoopInPoint(Formats.parseTicks(loopInField.getText(), mResolution));
    }//GEN-LAST:event_loopInFieldActionPerformed

    private void loopOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopOutButtonActionPerformed
        setLoopOutPoint(durationSlider.getValue());
    }//GEN-LAST:event_loopOutButtonActionPerformed

    private void loopInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loopInButtonActionPerformed
        setLoopInPoint(durationSlider.getValue());
    }//GEN-LAST:event_loopInButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField barCountField;
    private javax.swing.JFormattedTextField currPositionField;
    private com.lemckes.MidiQuickFix.components.DurationSlider durationSlider;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JButton loopInButton;
    private javax.swing.JFormattedTextField loopInField;
    private javax.swing.JLabel loopInLabel;
    private javax.swing.JButton loopOutButton;
    private javax.swing.JFormattedTextField loopOutField;
    private javax.swing.JLabel loopOutLabel;
    // End of variables declaration//GEN-END:variables

    /**
     * Notify listeners that are interested in loop slider events.
     *
     * @param valueIsAdjusting True if the slider is being adjusted.
     */
    protected void fireLoopSliderChanged(boolean valueIsAdjusting) {
        LoopSliderListener[] keyListeners =
            listenerList.getListeners(LoopSliderListener.class);
        for (int i = keyListeners.length - 1; i >= 0; --i) {
            keyListeners[i].loopSliderChanged(
                new LoopSliderEvent(durationSlider.getValue(),
                mLoopInPoint, mLoopOutPoint, valueIsAdjusting));
        }
    }

    /**
     * Notify listeners that are interested in loop slider events.
     */
    protected void fireLoopPointChanged() {
        LoopSliderListener[] keyListeners =
            listenerList.getListeners(LoopSliderListener.class);
        for (int i = keyListeners.length - 1; i >= 0; --i) {
            keyListeners[i].loopPointChanged(
                new LoopSliderEvent(durationSlider.getValue(),
                mLoopInPoint, mLoopOutPoint, false));
        }
    }
}
