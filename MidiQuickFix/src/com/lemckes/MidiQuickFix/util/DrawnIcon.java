/** ************************************************************
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
 ************************************************************* */
package com.lemckes.MidiQuickFix.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.AbstractButton;
import javax.swing.Icon;

/**
 * An Icon that draws itself using a Path2D.Float.
 */
public class DrawnIcon
    implements Icon
{

    private AbstractButton mParent;
    private Path2D.Float mPath;
    private Color mFillColour = Color.GRAY;
    private Color mBorderColour = Color.BLACK;
    private Color mActiveBorderColour = Color.WHITE;
    private boolean mActive = false;
    private boolean mFilled;
    private boolean mBordered;

    /**
     * Create a new instance of DrawnIcon with the default shape
     *
     * @param parent the AbstractButton on which this icon is drawn
     */
    public DrawnIcon(AbstractButton parent) {
        this(parent,
            new Path2D.Float(new Rectangle2D.Float(0.3f, 0.3f, 0.4f, 0.4f)));
    }

    /**
     * Create a new instance of DrawnIcon with the given shape
     *
     * @param parent the AbstractButton on which this icon is drawn
     * @param shape the Path2D.Float used to draw this icon
     */
    public DrawnIcon(AbstractButton parent, Path2D.Float shape) {
        this.mParent = parent;
        this.mPath = shape;
        mFilled = true;
        mBordered = true;
    }

    @Override
    public int getIconHeight() {
        return 16;
    }

    @Override
    public int getIconWidth() {
        return 16;
    }

    @Override
    public void paintIcon(Component component, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform savedAT = g2.getTransform();
        float xScale = component.getWidth();
        float yScale = component.getHeight();

        // Maintain a square aspect ratio for the icon.
        float scale = Math.min(xScale, yScale);
        // Set the scale to give the smallest dimension a size of 1.0
        AffineTransform at
            = AffineTransform.getScaleInstance(scale, scale);

        // Translate either X or Y to centre the icon in the largest dimension
        float xTrans = xScale > yScale ? (xScale - yScale) / (2 * yScale) : 0;
        float yTrans = yScale > xScale ? (yScale - xScale) / (2 * xScale) : 0;

        at.translate(xTrans, yTrans);

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.transform(at);

        if (mFilled) {
            drawFill(g2);
        }

        if (mBordered) {
            drawBorder(g2, scale);
        }

        g2.setTransform(savedAT);
    }

    private void drawFill(Graphics2D g2) {
        boolean enabled = mParent.isEnabled();

        g2.setColor(enabled ? mFillColour : Color.GRAY);
        g2.fill(mPath);
    }

    private void drawBorder(Graphics2D g2, float scale) {
        boolean pressed = mParent.getModel().isArmed();
        boolean selected = mParent.isSelected();

        if (mActive || pressed || selected) {
            g2.setColor(mActiveBorderColour);
        } else {
            g2.setColor(mBorderColour);
        }
        float strokeWidth
            = scale < 16 ? 0.5f
                : scale < 64 ? 0.5f + ((scale - 16) / 48) * 0.5f
                    : scale < 256 ? 1.0f + ((scale - 64) / 192) * 1.0f
                        : scale < 1024 ? 2.0f + ((scale - 256) / 768) * 2.0f
                            : 4;
        g2.setStroke(new BasicStroke(strokeWidth / scale));
        g2.draw(mPath);
    }

    public Path2D.Float getPath() {
        return mPath;
    }

    public void setPath(Path2D.Float path) {
        this.mPath = path;
    }

    public Color getFillColour() {
        return mFillColour;
    }

    public void setFillColour(Color colour) {
        this.mFillColour = colour;
    }

    public Color getBorderColour() {
        return mBorderColour;
    }

    public void setBorderColour(Color borderColour) {
        this.mBorderColour = borderColour;

        // Calculate a contrasting colour for the activeBorderColour
        float[] hsbVals = Color.RGBtoHSB(borderColour.getRed(),
            borderColour.getGreen(), borderColour.getBlue(), null);
        float hue = hsbVals[0];
        float sat = hsbVals[1];
        float bri = hsbVals[2];
        if (sat < 0.2) {
            // This is a grey or close to it so adjust the brightness
            if (bri > 0.4 && bri < 0.6) {
                // Just brighten mid-tones
                bri += 0.4f;
            } else {
                // otherwise invert the brightness
                bri = 1.0f - bri;
            }
            this.mActiveBorderColour
                = Color.getHSBColor(hue, sat, bri);
        } else {
            // This is not grey so adjust the hue.
            this.mActiveBorderColour
                = Color.getHSBColor(hue + 0.5f, sat, bri);
        }
    }

    public boolean isFilled() {
        return mFilled;
    }

    public void setFilled(boolean filled) {
        this.mFilled = filled;
    }

    public boolean isBordered() {
        return mBordered;
    }

    public void setBordered(boolean bordered) {
        this.mBordered = bordered;
    }

    public boolean isActive() {
        return mActive;
    }

    public void setActive(boolean active) {
        this.mActive = active;
    }

    public AbstractButton getParent() {
        return mParent;
    }

    public void setParent(AbstractButton parent) {
        this.mParent = parent;
    }
}
