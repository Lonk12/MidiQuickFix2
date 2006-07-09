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

import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.UiStrings;
import javax.swing.JLabel;

/**
 *
 * @version $Id$
 */
public class DurationSlider extends javax.swing.JSlider {
    
    /** Creates new form DurationSlider */
    public DurationSlider() {
        initComponents();
    }
    
    public void setDuration(long dur, boolean ticks, int resolution) {
        setMinimum(0);
        setMaximum((int)dur);
        
        int major;
        int minor;
        
        if (ticks) {
            int beats = (int)(dur / resolution);
            major = beats / 10;
            minor = major / 8;
            if (major < 8) {
                major = 8; minor = 1;
            } else if (major < 16) {
                major = 16; minor = 1;
            } else if (major < 32) {
                major = 32; minor = 8;
            } else if (major < 64) {
                major = 64; minor = 16;
            } else if (major < 128) {
                major = 128; minor = 32;
            }
            major *= resolution;
            minor *= resolution;
        } else {
            major = (int)(dur / 10);
            minor = major / 6;
            if (major < 10) {
                major = 10; minor = 1;
            } else if (major < 15) {
                major = 15; minor = 1;
            } else if (major < 20) {
                major = 20; minor = 2;
            } else if (major < 30) {
                major = 30; minor = 5;
            } else if (major < 60) {
                major = 60; minor = 10;
            } else if (major < 120) {
                major = 120; minor = 20;
            }
        }
        setMajorTickSpacing(major);
        setMinorTickSpacing(minor);
        //Create the label table
        java.util.Hashtable labelTable = createStandardLabels(major);
        for (java.util.Enumeration e = labelTable.keys(); e.hasMoreElements();) {
            Integer pos = (Integer)e.nextElement();
            JLabel label = (JLabel)labelTable.get(pos);
            if (ticks) {
                label.setText(Formats.formatBeats(pos.intValue(), resolution));
            } else {
                label.setText(Formats.formatSeconds(pos.intValue()));
            }
        }
        setLabelTable(labelTable);
        setPaintLabels(true);
        setPaintTicks(true);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {

        setFont(new java.awt.Font("Dialog", 0, 10));
        setMajorTickSpacing(10);
        setMinorTickSpacing(1);
        setPaintLabels(true);
        setPaintTicks(true);
        setValue(0);
    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}