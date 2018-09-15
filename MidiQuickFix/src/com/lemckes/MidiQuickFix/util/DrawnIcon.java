/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2009 John Lemcke
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
package com.lemckes.MidiQuickFix.util;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import javax.swing.AbstractButton;
import javax.swing.Icon;

/**
 * An Icon that draws itself using a GeneralPath.
 * @version $Id: DrawnIcon.java,v 1.6 2010/07/08 04:26:03 jostle Exp $
 */
public class DrawnIcon implements Icon
{

    private AbstractButton mParent;
    private GeneralPath mPath;
    private Color mFillColour = Color.GRAY;
    private Color mBorderColour = Color.BLACK;
    private Color mActiveBorderColour = Color.WHITE;
    private boolean mActive = false;
    private boolean mFilled;
    private boolean mBordered;

    /**
     * Create a new instance of DrawnIcon with the default shape
     * @param parent the AbstractButton on which this icon is drawn
     */
    public DrawnIcon(AbstractButton parent) {
        this(parent,
            new GeneralPath(new Rectangle2D.Double(0.3, 0.3, 0.4, 0.4)));
    }

    /**
     * Create a new instance of DrawnIcon with the given shape
     * @param parent the AbstractButton on which this icon is drawn
     * @param shape the GeneralPath used to draw this icon
     */
    public DrawnIcon(AbstractButton parent, GeneralPath shape) {
        this.mParent = parent;
        this.mPath = shape;
        mFilled = true;
        mBordered = true;
    }

    @Override
    public int getIconHeight() {
        return 12;
    }

    @Override
    public int getIconWidth() {
        return 12;
    }

    @Override
    public void paintIcon(Component component, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform savedAT = g2.getTransform();
        double xScale = component.getWidth();
        double yScale = component.getHeight();

        // Maintain a square aspect ratio for the icon.
        double scale = Math.min(xScale, yScale);
        // Set the scale to give the smallest dimension a size of 1.0
        AffineTransform at =
            AffineTransform.getScaleInstance(scale, scale);

        // Translate either X or Y to centre the icon in the largest dimension
        double xTrans = xScale > yScale ? (xScale - yScale) / (2 * yScale) : 0;
        double yTrans = yScale > xScale ? (yScale - xScale) / (2 * xScale) : 0;

        at.translate(xTrans, yTrans);

        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.transform(at);

        if (mFilled) {
            drawFill(g2);
        }

        if (mBordered) {
            drawBorder(g2);
        }

        g2.setTransform(savedAT);
    }

    private void drawFill(Graphics2D g2) {
        boolean enabled = mParent.isEnabled();

        g2.setColor(enabled ? mFillColour : Color.GRAY);
        g2.fill(mPath);
    }

    private void drawBorder(Graphics2D g2) {
        boolean pressed = mParent.getModel().isArmed();
        boolean selected = mParent.isSelected();

        if (mActive || pressed || selected) {
            g2.setColor(mActiveBorderColour);
        } else {
            g2.setColor(mBorderColour);
        }
        g2.setStroke(new BasicStroke(0.02f));
        g2.draw(mPath);
    }

    public GeneralPath getPath() {
        return mPath;
    }

    public void setPath(GeneralPath path) {
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
        float hue =
            (Color.RGBtoHSB(borderColour.getRed(),
            borderColour.getGreen(), borderColour.getBlue(), null))[0];
        float sat =
            (Color.RGBtoHSB(borderColour.getRed(),
            borderColour.getGreen(), borderColour.getBlue(), null))[1];
        float bri =
            (Color.RGBtoHSB(borderColour.getRed(),
            borderColour.getGreen(), borderColour.getBlue(), null))[2];
        if (sat < 0.2) {
            // This is a grey or close to it so adjust the brightness
            if (bri > 0.4 && bri < 0.6) {
                // Just brighten mid-tones
                bri += 0.4f;
            } else {
                // otherwise invert the brightness
                bri = 1.0f - bri;
            }
            this.mActiveBorderColour =
                Color.getHSBColor(hue, sat, bri);
        } else {
            // This is not grey so adjust the hue.
            this.mActiveBorderColour =
                Color.getHSBColor(hue + 0.5f, sat, bri);
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
