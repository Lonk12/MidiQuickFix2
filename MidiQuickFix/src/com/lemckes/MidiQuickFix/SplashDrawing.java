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

//import com.lemckes.MidiQuickFix.util.TraceDialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.ArrayList;

/**
 * This class handles the display of the splash screen in the startup dialog.
 */
public class SplashDrawing extends javax.swing.JComponent {

    private static final long serialVersionUID = 1436547L;
    transient BufferedImage mBi;
    transient Image mImage;
    int mImageWidth;
    int mImageHeight;
    float mLineHeight;
    boolean mCentred = false;
    ArrayList<String> mStageMessages = new ArrayList<>(16);
    Font mFont;
    transient FontRenderContext mFrContext;
    long mMessageDelay = 250;

    /** Creates a new instance of SplashDrawing */
    public SplashDrawing() {
        mFont = new java.awt.Font("Dialog", 1, 14);
        setFont(mFont);
        mFrContext = new FontRenderContext(null, true, true);
        LineMetrics fm = mFont.getLineMetrics("A Typical Message String",
            mFrContext); // NOI18N
        mLineHeight = fm.getHeight();

        java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
        URL url = getClass().getResource("resources/MQFsplash2.png"); // NOI18N
        mImage = tk.getImage(url);
        MediaTracker mt = new MediaTracker(this);
        mt.addImage(mImage, 1);
        try {
            mt.waitForAll();
        } catch (InterruptedException e) {
            // Probably don't care
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
    @Override
    public void paint(java.awt.Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.drawImage(mBi, 0, 0, this);

        int startY =
            (int)((mImageHeight / 2.0) - (mLineHeight * (mStageMessages.size() - 1) / 2.0));
        int startX;
        int i = 0;
        for (String s : mStageMessages) {
            if (!mCentred) {
                startX = 20;
            } else {
                Rectangle2D r =
                    mFont.getStringBounds(s, mFrContext);
                double width = r.getWidth();
                startX = (int)((mImageWidth - width) / 2);
            }
            int y = startY + (int)(i++ * mLineHeight);
            if (s != null) {
                g2.drawString(s, startX, y);
            }
        }
    }

    /**
     * Set whether the messages are centred
     * @param centred if <code>true</code> the messages are centred
     * otherwise they are left aligned.
     */
    public void setCentredText(boolean centred) {
        mCentred = centred;
    }

    /**
     * Set the delay after calling #addStageMessasge
     * 
     * @param messageDelay the new delay in milliseconds.
     */
    public void setMessageDelay(long messageDelay) {
        mMessageDelay = messageDelay;
    }

    /**
     * Add a message string to the display
     * @param message the new message
     */
    public synchronized void addStageMessage(String message) {
        if (message != null) {
            mStageMessages.add(message);
            paintImmediately(getBounds());

            if (mMessageDelay > 0) {
                try {
                    // Wait a bit so that the message is seen
                    wait(mMessageDelay);
                } catch (InterruptedException e) {
                    // Do Nothing
                }
            }
        }
    }

    /**
     * Return the slash screen preferred size
     * @return The preferred size
     */
    @Override
    public Dimension getPreferredSize() {
        return getSize();
    }

    /**
     * Get the size of the splash image
     * @return the size of the splash image
     */
    @Override
    public Dimension getSize() {
        return new Dimension(mImageWidth, mImageHeight);
    }
}
