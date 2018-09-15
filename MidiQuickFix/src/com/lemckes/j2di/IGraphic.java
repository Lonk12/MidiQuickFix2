
package com.lemckes.j2di;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Paint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * This is the abstract base class that all J2Di graphics extend. <br>
 * An IShapeGraphic implementation is responsible for drawing a graphical
 * representation of itself when required. Its position is defined in terms of
 * world coordinates.
 */
public abstract class IGraphic {

    /**
     * The colour in which the graphic is drawn.
     */
    protected Paint mPaint = Color.BLACK;
    /**
     * The bounding rectangle of this graphic that is reset to
     * null when the graphic is changed.
     */
    protected Rectangle2D mCachedBounds;
    /**
     * Determines whether this graphic gets drawn.
     */
    protected boolean mIsVisible = true;
    /**
     * A static instance of AffineTransform set to the identity transform.
     */
    public static AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

    /**
     * Set the colour for this graphic.
     *
     * @param paint The colour in which to draw the graphic.
     */
    public void setPaint(Paint paint) {
        mPaint = paint;
    }

    /**
     * Render this graphic on the given Graphics object.
     *
     * @param g The Graphics on which to render.
     */
    public abstract void paint(Graphics g);

    /**
     * Move this graphic by the given x and y deltas. dx and dy are in world
     * units.
     *
     * @param dx The x delta
     * @param dy The y delta
     */
    public abstract void move(double dx, double dy);

    /**
     * Set the graphic's top-left position in world coordinates.
     *
     * @param x The x position
     * @param y The y position
     */
    public abstract void setPosition(double x, double y);

    /**
     * Get the bounding rectangle of this graphic.
     *
     * @return the bounding rectangle of this graphic.
     */
    public abstract Rectangle2D getBoundingRect();

    /**
     * Test if the given rectangle intersects with this graphic.
     *
     * @param r The rectangle to be tested.
     * @return true if the given rectangle intersects with this graphic.
     */
    public abstract boolean intersects(Rectangle2D r);

    /**
     * Test if this graphic contains the given coordinates.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @return true if this graphic contains the given coordinates.
     */
    public abstract boolean contains(double x, double y);

    /**
     * Return the colour for this graphic.
     *
     * @return The colour in which to draw the graphic.
     */
    public Paint getPaint() {
        return mPaint;
    }

    /**
     * Test if this graphic is visible
     * @return
     */
    public boolean isVisible() {
        return mIsVisible;
    }

    /**
     * Set the visible state of this graphic
     * @param visible
     */
    public void setVisible(boolean visible) {
        mIsVisible = visible;
    }
}
