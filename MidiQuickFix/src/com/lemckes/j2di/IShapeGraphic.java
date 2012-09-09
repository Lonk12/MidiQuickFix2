
package com.lemckes.j2di;

import java.awt.BasicStroke;
import java.awt.Stroke;

/**
 * This is the abstract base class that all J2Di graphics extend. <br/>
 * An IShapeGraphic implementation is responsible for drawing a graphical
 * representation of itself when required. Its position is defined in terms of
 * world coordinates.
 */
public abstract class IShapeGraphic extends IGraphic {

    /**
     * Determines whether the graphic is drawn as an outline or filled.
     */
    protected boolean mFilled = false;
    /**
     * The default stroke for IGraphics.
     */
    protected static Stroke DEFAULT_STROKE = new BasicStroke(1.0f);
    /**
     * The stroke to use for this graphic.
     */
    protected Stroke mStroke = DEFAULT_STROKE;
    /**
     * Determines whether this graphic's stroke is defined in device or world
     * units.
     * By default the stroke is defined in device units, so if you set the
     * stroke to {@code new BasicStroke(3.0f)} the stroke will be 3 pixels
     * wide. If this is set to false then the stroke will be 3 world coordinate
     * units wide. This will mean that, for example, an IRectangle is likely to
     * be drawn with different width lines for the top and bottom than for
     * the left and right sides.
     */
    protected boolean mUseDeviceStroke = true;

    /**
     * Return whether this graphic is to be filled.
     *
     * @return If {@code true} this graphic is filled,
     * otherwise the graphic's outline is drawn.
     */
    public boolean isFilled() {
        return mFilled;
    }

    /**
     * Set whether this graphic is to be filled.
     *
     * @param filled If {@code true} fill this graphic,
     * otherwise draw the graphic's outline.
     */
    public void setFilled(boolean filled) {
        mFilled = filled;
    }

    /**
     * Set the stroke to be used.
     *
     * @param s The Stroke to use. If set to null the default stroke will be
     * used.
     */
    public void setStroke(Stroke s) {
        if (s == null) {
            mStroke = DEFAULT_STROKE;
        } else {
            mStroke = s;
        }
        mCachedBounds = null;
    }

    /**
     * Return the defined stroke.
     *
     * @return The stroke.
     */
    public final Stroke getStroke() {
        return mStroke;
    }

    /**
     * Test whether this graphic's stroke is defined in device or world units.
     * <p/>
     * @return
     */
    public boolean isUseDeviceStroke() {
        return mUseDeviceStroke;
    }

    /**
     * Set whether this graphic's stroke is defined in device or world units.
     * By default the stroke is defined in device units, so if you set the
     * stroke to {@code new BasicStroke(3.0f)} the stroke will be 3 pixels
     * wide. If this is set to false then the stroke will be 3 world coordinate
     * units wide. This will mean that, for example, an IRectangle is likely to
     * be drawn with different width lines for the top and bottom than for
     * the left and right sides.
     *
     * @param useDeviceStroke
     */
    public void setUseDeviceStroke(boolean useDeviceStroke) {
        mUseDeviceStroke = useDeviceStroke;
    }
}
