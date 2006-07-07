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
import com.lemckes.MidiQuickFix.util.PlayController.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import javax.swing.AbstractAction;

/**
 *
 * @version $Id$
 */
public class TransportPanel extends javax.swing.JPanel {
    
    transient private DrawnIcon pauseIcon;
    transient private DrawnIcon playIcon;
    transient private DrawnIcon rewindIcon;
    transient private DrawnIcon stopIcon;
    transient private DrawnIcon loopIcon;
    transient private DrawnIcon recordIcon;
    
    transient private GeneralPath playPath = new GeneralPath();
    transient private GeneralPath stopPath = new GeneralPath();
    transient private GeneralPath pausePath = new GeneralPath();
    transient private GeneralPath loopPath = new GeneralPath();
    transient private GeneralPath rewindPath = new GeneralPath();
    transient private GeneralPath recordPath = new GeneralPath();
    
    transient private GeneralPath loopOuterPath = new GeneralPath();
    transient private GeneralPath loopInnerPath = new GeneralPath();
    
    /** Creates new form TransportPanel */
    public TransportPanel() {
        initComponents();
        playPath.moveTo(0.2f, 0.2f);
        playPath.lineTo(0.3f, 0.2f);
        playPath.lineTo(0.8f, 0.5f);
        playPath.lineTo(0.3f, 0.8f);
        playPath.lineTo(0.2f, 0.8f);
        playPath.lineTo(0.2f, 0.7f);
        playPath.lineTo(0.2f, 0.5f);
        playPath.lineTo(0.2f, 0.3f);
        playPath.closePath();
        
        stopPath.moveTo(0.2f, 0.2f);
        stopPath.lineTo(0.8f, 0.2f);
        stopPath.lineTo(0.8f, 0.8f);
        stopPath.lineTo(0.2f, 0.8f);
        stopPath.closePath();
        
        pausePath.moveTo(0.2f, 0.2f);
        pausePath.lineTo(0.4f, 0.2f);
        pausePath.lineTo(0.4f, 0.8f);
        pausePath.lineTo(0.2f, 0.8f);
        pausePath.closePath();
        pausePath.moveTo(0.6f, 0.2f);
        pausePath.lineTo(0.8f, 0.2f);
        pausePath.lineTo(0.8f, 0.8f);
        pausePath.lineTo(0.6f, 0.8f);
        pausePath.closePath();
        
        rewindPath.moveTo(0.2f, 0.2f);
        rewindPath.lineTo(0.3f, 0.2f);
        rewindPath.lineTo(0.3f, 0.8f);
        rewindPath.lineTo(0.2f, 0.8f);
        rewindPath.closePath();
        rewindPath.moveTo(0.3f, 0.5f);
        rewindPath.lineTo(0.8f, 0.2f);
        rewindPath.lineTo(0.8f, 0.8f);
        rewindPath.closePath();
        
        Ellipse2D recellipse = new Ellipse2D.Float(0.2f, 0.2f, 0.6f, 0.6f);
        PathIterator pi = recellipse.getPathIterator(null);
        recordPath.append(pi,  false);
        
        loopOuterPath.moveTo(0.55f, 0.30f);
        loopOuterPath.lineTo(0.55f, 0.20f);
        loopOuterPath.lineTo(0.65f, 0.30f);
        loopOuterPath.lineTo(0.70f, 0.30f);
        loopOuterPath.curveTo(0.90f, 0.30f, 0.90f, 0.70f, 0.70f, 0.70f);
        loopOuterPath.lineTo(0.45f, 0.70f);
        loopOuterPath.lineTo(0.45f, 0.80f);
        loopOuterPath.lineTo(0.35f, 0.70f);
        loopOuterPath.lineTo(0.30f, 0.70f);
        loopOuterPath.curveTo(0.10f, 0.70f, 0.10f, 0.30f, 0.30f, 0.30f);
        loopOuterPath.closePath();
        Area loopArea = new Area(loopOuterPath);
        
        loopInnerPath.moveTo(0.55f, 0.40f);
        loopInnerPath.lineTo(0.55f, 0.50f);
        loopInnerPath.lineTo(0.65f, 0.40f);
        loopInnerPath.lineTo(0.70f, 0.40f);
        loopInnerPath.curveTo(0.80f, 0.40f, 0.80f, 0.60f, 0.70f, 0.60f);
        loopInnerPath.lineTo(0.45f, 0.60f);
        loopInnerPath.lineTo(0.45f, 0.50f);
        loopInnerPath.lineTo(0.35f, 0.60f);
        loopInnerPath.lineTo(0.30f, 0.60f);
        loopInnerPath.curveTo(0.20f, 0.60f, 0.20f, 0.40f, 0.30f, 0.40f);
        loopInnerPath.closePath();
        Area loopHoleArea = new Area(loopInnerPath);
        
        loopArea.subtract(loopHoleArea);
        loopPath.append(loopArea.getPathIterator(null), false);
        
        Dimension buttonSize = new Dimension(40, 40);
        
        playIcon = new DrawnIcon(playButton, playPath);
        playIcon.setPath(playPath);
        playIcon.setFillColour(Color.GREEN);
        playButton.setPreferredSize(buttonSize);
        
        rewindIcon = new DrawnIcon(rewindButton, rewindPath);
        rewindIcon.setPath(rewindPath);
        rewindIcon.setFillColour(Color.CYAN);
        rewindButton.setPreferredSize(buttonSize);
        
        stopIcon = new DrawnIcon(stopButton, stopPath);
        stopIcon.setPath(stopPath);
        stopIcon.setFillColour(new Color(1.0f, 0.5f, 0.2f)); // ORANGE
        stopButton.setPreferredSize(buttonSize);
        
        pauseIcon = new DrawnIcon(pauseButton, pausePath);
        pauseIcon.setPath(pausePath);
        pauseIcon.setFillColour(Color.YELLOW);
        pauseButton.setPreferredSize(buttonSize);
        
//        recordIcon.setPath(recordPath);
//        recordButton.setPreferredSize(buttonSize);
//        recordIcon.setFillColour(Color.RED);

        loopIcon = new DrawnIcon(loopButton, loopPath);
        loopIcon.setPath(loopPath);
        loopButton.setPreferredSize(buttonSize);
        loopIcon.setFillColour(Color.LIGHT_GRAY);
        
    }
    
    public void setActions(RewindAction r, PlayAction p, PauseAction u, StopAction s, LoopAction l) {
        r.putValue(AbstractAction.SMALL_ICON, rewindIcon);
        rewindButton.setAction(r);
        p.putValue(AbstractAction.SMALL_ICON, playIcon);
        playButton.setAction(p);
        u.putValue(AbstractAction.SMALL_ICON, pauseIcon);
        pauseButton.setAction(u);
        s.putValue(AbstractAction.SMALL_ICON, stopIcon);
        stopButton.setAction(s);
        l.putValue(AbstractAction.SMALL_ICON, loopIcon);
        loopButton.setAction(l);
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        rewindButton = new javax.swing.JButton();
        playButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        loopButton = new javax.swing.JToggleButton();

        setLayout(new java.awt.GridLayout(1, 0, 1, 1));

        setBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        rewindButton.setToolTipText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("rewind"));
        rewindButton.setBorderPainted(false);
        rewindButton.setDefaultCapable(false);
        rewindButton.setFocusPainted(false);
        rewindButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(rewindButton);

        playButton.setToolTipText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("play"));
        playButton.setBorderPainted(false);
        playButton.setDefaultCapable(false);
        playButton.setFocusPainted(false);
        playButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(playButton);

        pauseButton.setToolTipText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("pause"));
        pauseButton.setBorderPainted(false);
        pauseButton.setDefaultCapable(false);
        pauseButton.setFocusPainted(false);
        pauseButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(pauseButton);

        stopButton.setToolTipText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("stop"));
        stopButton.setBorderPainted(false);
        stopButton.setDefaultCapable(false);
        stopButton.setFocusPainted(false);
        stopButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(stopButton);

        loopButton.setToolTipText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("loop"));
        loopButton.setBorderPainted(false);
        loopButton.setFocusPainted(false);
        loopButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(loopButton);

    }
    // </editor-fold>//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton loopButton;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton playButton;
    private javax.swing.JButton rewindButton;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables
    
}
