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
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.*;
import java.net.URL;
import java.util.ArrayList;


/**
 * This class handles the display of the splash screen in the startup dialog.
 * @version $Id$
 */
public class SplashDrawing extends javax.swing.JComponent {
    
    transient BufferedImage mBi;
    transient Image mImage;
    int mImageWidth;
    int mImageHeight;
    
    float mLineHeight;
    
    boolean mCentred = false;
    
    //  For java 1.5 ArrayList<String> mStageMessages = new ArrayList<String>();
    ArrayList<String> mStageMessages = new ArrayList<String>();
    
    Font mFont;
    transient FontRenderContext mFrContext;
    
    /** Creates a new instance of SplashDrawing */
    public SplashDrawing() {
        mFont = new java.awt.Font("Dialog", 1, 14);
        setFont(mFont);
        mFrContext = new FontRenderContext(null, true, true);
        LineMetrics fm = mFont.getLineMetrics("A Typical Message String", mFrContext);
        mLineHeight = fm.getHeight();
        
        java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
        URL url = getClass().getResource("resources/MQFsplash2.png");
        mImage = tk.getImage(url);
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(mImage, 1);
        try {
            mt.waitForAll();
        } catch(InterruptedException e) {
            throw new Error("Could not load image.");
        }
        
        mImageWidth = mImage.getWidth(null);
        mImageHeight = mImage.getHeight(null);
        mBi = new BufferedImage(mImageWidth, mImageHeight,
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g = mBi.createGraphics();
        g.drawImage(mImage, 0, 0, null);
        g.dispose();
    }
    
    /**
     * Draw the splash image and any messages that need to be displayed.
     * @param g The Graphics to draw into.
     */
    public void paint(java.awt.Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawImage(mBi, 0, 0, this);
        
        int startY = (int)((mImageHeight - mLineHeight * mStageMessages.size()) / 2);
        int startX = 20;
        for (int i = 0; i < mStageMessages.size(); ++i) {
            if (!mCentred) {
                startX = 20;
            } else {
                Rectangle2D r =
                    mFont.getStringBounds((String)mStageMessages.get(i), mFrContext);
                double width = r.getWidth();
                startX = (int)((mImageWidth - width) / 2);
            }
            int y = startY + (int)(i * mLineHeight);
            g2.drawString((String)mStageMessages.get(i), startX, y);
        }
    }
    
    void setCentredText(boolean centred) {
        mCentred = centred;
    }
    
    synchronized void setStageMessage(String message) {
        mStageMessages.add(message);
        repaint();
        try {
            // Wait a bit so that the message is seen
            wait(500);
        } catch(InterruptedException e) {
            // Do Nothing
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
    }
}
