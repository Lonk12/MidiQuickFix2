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

package com.lemckes.MidiQuickFix.components;

import com.lemckes.MidiQuickFix.util.DrawnIcon;
import com.lemckes.MidiQuickFix.util.PlayController.LoopAction;
import com.lemckes.MidiQuickFix.util.PlayController.PauseAction;
import com.lemckes.MidiQuickFix.util.PlayController.PlayAction;
import com.lemckes.MidiQuickFix.util.PlayController.RewindAction;
import com.lemckes.MidiQuickFix.util.PlayController.StopAction;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.AbstractAction;

/**
 * The UI for media transport controls such as Play, Pause, Stop etc.
 */
public class TransportPanel extends javax.swing.JPanel {
    static final long serialVersionUID = 6727747406307178988L;

    private transient DrawnIcon pauseIcon;
    private transient DrawnIcon playIcon;
    private transient DrawnIcon rewindIcon;
    private transient DrawnIcon stopIcon;
    private transient DrawnIcon loopIcon;
    //private transient DrawnIcon recordIcon;

    /** Creates new form TransportPanel */
    public TransportPanel() {
        initComponents();
        createIcons();
    }

    final void createIcons() {
        Path2D.Float loopOuterPath = new Path2D.Float();
        loopOuterPath.moveTo(0.55f, 0.30f);
        loopOuterPath.lineTo(0.55f, 0.20f);
        loopOuterPath.lineTo(0.65f, 0.30f);
        loopOuterPath.lineTo(0.70f, 0.30f);
        loopOuterPath.curveTo(0.95f, 0.30f, 0.95f, 0.70f, 0.70f, 0.70f);
        loopOuterPath.lineTo(0.45f, 0.70f);
        loopOuterPath.lineTo(0.45f, 0.80f);
        loopOuterPath.lineTo(0.35f, 0.70f);
        loopOuterPath.lineTo(0.30f, 0.70f);
        loopOuterPath.curveTo(0.05f, 0.70f, 0.05f, 0.30f, 0.30f, 0.30f);
        loopOuterPath.closePath();
        Area loopArea = new Area(loopOuterPath);

        Path2D.Float loopInnerPath = new Path2D.Float();
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

        Path2D.Float loopPath = new Path2D.Float();
        loopPath.append(loopArea.getPathIterator(null), false);
        loopIcon = new DrawnIcon(loopButton, loopPath);
        loopIcon.setFillColour(Color.BLUE);

        Path2D.Float pausePath
            = new Path2D.Float(new Rectangle2D.Float(0.2f, 0.2f, 0.2f, 0.6f));
        pausePath.append(new Rectangle2D.Float(0.6f, 0.2f, 0.2f, 0.6f), false);
        pauseIcon = new DrawnIcon(pauseButton, pausePath);
        pauseIcon.setFillColour(Color.YELLOW);

        Path2D.Float playPath = new Path2D.Float();
        playPath.moveTo(0.2f, 0.2f);
        playPath.lineTo(0.3f, 0.2f);
        playPath.lineTo(0.8f, 0.5f);
        playPath.lineTo(0.3f, 0.8f);
        playPath.lineTo(0.2f, 0.8f);
        playPath.closePath();
        playIcon = new DrawnIcon(playButton, playPath);
        playIcon.setFillColour(Color.GREEN);

//        Path2D.Float recordPath =
//            new Path2D.Float(new Ellipse2D.Float(0.2f, 0.2f, 0.6f, 0.6f));
//        recordIcon = new DrawnIcon(recordButton, recordPath);
//        recordIcon.setFillColour(Color.RED);

        Path2D.Float rewindPath
            = new Path2D.Float(new Rectangle2D.Float(0.2f, 0.2f, 0.1f, 0.6f));
        rewindPath.moveTo(0.3f, 0.5f);
        rewindPath.lineTo(0.8f, 0.2f);
        rewindPath.lineTo(0.8f, 0.8f);
        rewindPath.closePath();
        rewindIcon = new DrawnIcon(rewindButton, rewindPath);
        rewindIcon.setFillColour(Color.CYAN);

        Path2D.Float stopPath
            = new Path2D.Float(new Rectangle2D.Float(0.2f, 0.2f, 0.6f, 0.6f));
        stopIcon = new DrawnIcon(stopButton, stopPath);
        stopIcon.setFillColour(new Color(1.0f, 0.5f, 0.2f)); // ORANGE

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
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        rewindButton = new javax.swing.JButton();
        playButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        loopButton = new javax.swing.JToggleButton();

        setBorder(javax.swing.BorderFactory.createEtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        setLayout(new java.awt.GridLayout(1, 0, 1, 1));

        rewindButton.setToolTipText(UiStrings.getString("rewind")); // NOI18N
        rewindButton.setDefaultCapable(false);
        rewindButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(rewindButton);

        playButton.setToolTipText(UiStrings.getString("play")); // NOI18N
        playButton.setDefaultCapable(false);
        playButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(playButton);

        pauseButton.setToolTipText(UiStrings.getString("pause")); // NOI18N
        pauseButton.setDefaultCapable(false);
        pauseButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(pauseButton);

        stopButton.setToolTipText(UiStrings.getString("stop")); // NOI18N
        stopButton.setDefaultCapable(false);
        stopButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(stopButton);

        loopButton.setToolTipText(UiStrings.getString("loop")); // NOI18N
        loopButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        add(loopButton);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton loopButton;
    private javax.swing.JButton pauseButton;
    private javax.swing.JButton playButton;
    private javax.swing.JButton rewindButton;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables

}
