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

package com.lemckes.MidiQuickFix;

import java.awt.Graphics2D;
import java.awt.*;
import java.awt.image.*;

import java.util.Vector;

/**
 * This class handles the display of the splash screen in the startup dialog.
 * @version $Id$
 */
public class SplashDrawing extends javax.swing.JComponent {
    
    BufferedImage mBi;
    Image mImage;
    int mImageWidth;
    int mImageHeight;
    
    float mLineHeight;
    
    Vector mStageMessages = new Vector();
    
    /** Creates a new instance of SplashDrawing */
    public SplashDrawing() {
        java.awt.Font font = new java.awt.Font("Dialog", 1, 14);
        setFont(font);
        java.awt.font.FontRenderContext frc = new java.awt.font.FontRenderContext (null, false, false);
        java.awt.font.LineMetrics fm = font.getLineMetrics("A Typical Message String", frc);
        mLineHeight = fm.getHeight();
        
        java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
        mImage = tk.getImage(getClass().getResource("/com/lemckes/MidiQuickFix/resources/MQFsplash2.png"));
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(mImage, 1);
        try {
            mt.waitForAll();
        } catch(InterruptedException e) {
            throw new Error("Could not load image.");
        }
        
        mImageWidth = mImage.getWidth(null);
        mImageHeight = mImage.getHeight(null);
        // System.out.println("Width=" + w + " Height=" + h);
        mBi = new BufferedImage(mImage.getWidth(null), mImage.getHeight(null),
        BufferedImage.TYPE_INT_RGB);
        Graphics2D g = mBi.createGraphics();
        g.drawImage(mImage, 0, 0, null);
        g.dispose();
    }
    
    /**
     * Draw the splash image and any messages which need to be displayed.
     * @param g The Graphics to draw into.
     */
    public void paint(java.awt.Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.drawImage(mBi, 0, 0, this);
        int startPos = (int)((mImageHeight - mLineHeight * mStageMessages.size()) / 2);
        for (int i = 0; i < mStageMessages.size(); ++i) {
            int x = 20;
            int y = startPos + (int)(i * mLineHeight);
            g2.drawString((String)mStageMessages.get(i), x, y);
        }
    }
    
    synchronized void setStageMessage(String message) {
        mStageMessages.add(message);
        repaint(0, 0, mImageWidth, mImageHeight);
        try {
            // Wait a bit so that the message is seen
            wait(500);
        } catch(Exception e) {
        }
    }
    
    /**
     * Return the slash screen preferred size
     * @return The preferred size
     */
    public Dimension getPreferredSize() {
        return getSize();
    }
    
    public Dimension getSize() {
        return new Dimension(mImageWidth, mImageHeight);
        //        return new Dimension(im.getWidth(null), im.getHeight(null));
    }
}
