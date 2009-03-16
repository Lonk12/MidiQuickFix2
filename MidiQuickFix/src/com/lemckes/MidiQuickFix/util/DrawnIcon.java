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
 * @version $Id$
 */
public class DrawnIcon implements Icon {
    
    private AbstractButton parent;
    
    private GeneralPath path;
    
    private Color fillColour = Color.GRAY;
    private Color borderColour = Color.BLACK;
    private Color activeBorderColour = Color.WHITE;
    
    private boolean active = false;
    
    private boolean filled;
    private boolean bordered;
    
    public DrawnIcon(AbstractButton parent) {
        this(parent, 
            new GeneralPath(new Rectangle2D.Double(0.3, 0.3, 0.4, 0.4)));
    }
    
    /** Creates a new instance of DrawnIcon */
    public DrawnIcon(AbstractButton parent, GeneralPath path) {
        this.parent = parent;
        this.path = path;
        filled = true;
        bordered = true;
    }

    public int getIconHeight() {
        return 12;
    }

    public int getIconWidth() {
        return 12;
    }

    public void paintIcon(Component component, Graphics g, int x, int y) {
        Graphics2D g2 = (Graphics2D)g;
        AffineTransform savedAT = g2.getTransform();
        double xScale = component.getWidth();
        double yScale = component.getHeight();
        
        // Maintain a square aspect ratio for the icon.
        double scale = Math.min(xScale, yScale);
        AffineTransform at =
            AffineTransform.getScaleInstance(scale, scale);
        
        double xTrans = xScale > yScale ? (xScale - yScale) / (2 * yScale) : 0;
        double yTrans = yScale > xScale ? (yScale - xScale) / (2 * xScale) : 0;
        
        at.translate(xTrans, yTrans);
        
        g2.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.transform(at);
        
        if (filled) {
            drawFill(g2);
        }
        
        if (bordered) {
            drawBorder(g2);
        }
        
        g2.setTransform(savedAT);
    }
    
    void drawFill(Graphics2D g2) {
        boolean enabled = parent.isEnabled();
        
        g2.setColor(enabled ? fillColour : Color.GRAY);
        g2.fill(path);
    }
    
    void drawBorder(Graphics2D g2) {
        boolean pressed = parent.getModel().isArmed();
        boolean selected = parent.isSelected();
        
        if (active || pressed || selected) {
            g2.setColor(activeBorderColour);
        } else {
            g2.setColor(borderColour);
        }
        g2.setStroke(new BasicStroke(0.02f));
        g2.draw(path);
    }
    
    
    public GeneralPath getPath() {
        return path;
    }
    
    public void setPath(GeneralPath path) {
        this.path = path;
    }
    
    public Color getFillColour() {
        return fillColour;
    }
    
    public void setFillColour(Color colour) {
        this.fillColour = colour;
    }
    
    public Color getBorderColour() {
        return borderColour;
    }
    
    public void setBorderColour(Color borderColour) {
        this.borderColour = borderColour;
        
        // Calculate a contrasting colour for the activeBorderColour
        float hue =
            (borderColour.RGBtoHSB(borderColour.getRed(),
            borderColour.getGreen(), borderColour.getBlue(), null))[0];
        float sat =
            (borderColour.RGBtoHSB(borderColour.getRed(),
            borderColour.getGreen(), borderColour.getBlue(), null))[1];
        float bri =
            (borderColour.RGBtoHSB(borderColour.getRed(),
            borderColour.getGreen(), borderColour.getBlue(), null))[2];
        if (sat < 0.2) {
            // This is a grey or close to it so adjust the brightness
            if (bri > 0.4 && bri < 0.6) {
                // Just brighten mid-tones
                bri = bri + 0.4f;
            } else {
                // otherwise invert the brightness
                bri = 1.0f - bri;
            }
            this.activeBorderColour =
                Color.getHSBColor(hue, sat, bri);
        } else {
            // This is not grey so adjust the hue.
            this.activeBorderColour =
                Color.getHSBColor(hue + 0.5f, sat, bri);
        }
    }
    
    public boolean isFilled() {
        return filled;
    }
    
    public void setFilled(boolean filled) {
        this.filled = filled;
    }
    
    public boolean isBordered() {
        return bordered;
    }
    
    public void setBordered(boolean bordered) {
        this.bordered = bordered;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    public AbstractButton getParent() {
        return parent;
    }

    public void setParent(AbstractButton parent) {
        this.parent = parent;
    }
}
