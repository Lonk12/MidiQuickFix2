
package com.lemckes.j2di;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

/**
 * A j2di graphic that draws a string.
 */
public class IText
    extends IGraphic {

    protected String mText;
    protected double mX;
    protected double mY;
    protected Font mFont;
    protected FontRenderContext mFrc;
    protected boolean mFixedPointSize;
    protected AffineTransform mTransform = IDENTITY_TRANSFORM;
    private ILayer mParent;

    /**
     * Create an IText with the given String at position 0, 0 and with the
     * default font;
     * {@code Font("SansSerif", Font.PLAIN, 12)}
     *
     * @param text The string to be drawn.
     */
    public IText(String text) {
        this(text, 0, 0);
    }

    /**
     * Create an IText with the given String at position 0, 0 and with the given
     * font.
     *
     * @param text The string to be drawn.
     * @param font The font to use
     */
    public IText(String text, Font font) {
        this(text, font, 0, 0);
    }

    /**
     * Create an IText with the given String at the given position and with the
     * default font;
     * {@code Font("SansSerif", Font.PLAIN, 12)}
     *
     * @param text The string to be drawn.
     * @param x    The x position of the string
     * @param y    The y position of the string
     */
    public IText(String text, double x, double y) {
        this(text, new Font("SansSerif", Font.PLAIN, 12), x, y);
    }

    /**
     * Create an IText with the given String at the given position and with the
     * given font
     *
     * @param text The string to be drawn.
     * @param x    The x position of the string
     * @param y    The y position of the string
     * @param font The font to use
     */
    public IText(String text, Font font, double x, double y) {
        mText = text;
        mFont = font;
        mX = x;
        mY = y;
        mFrc = new FontRenderContext(null, true, true);
    }

    /**
     * Set the text for this IText.
     *
     * @param text The text to use.
     */
    public void setText(String text) {
        mText = text;
        mCachedBounds = null;
    }

    /**
     * Set a new font for this IText.
     *
     * @param font The font to use.
     */
    public void setFont(Font font) {
        mFont = font;
        mCachedBounds = null;
    }

    /**
     * Test if this IText has a fixed point size instead of scaling with its
     * parent
     *
     * @return {@code true} if this IText keeps the same point size
     * regardless of its parent's scaling
     */
    public boolean isFixedPointSize() {
        return mFixedPointSize;
    }

    /**
     * Set whether this IText maintains a fixed size or is scaled with its
     * parent
     *
     * @param fixed if {@code true} this IText is rendered at the same size
     * regardless of its parent's scaling
     */
    public void setFixedPointSize(boolean fixed) {
        mFixedPointSize = fixed;
        mCachedBounds = null;
    }

    public Rectangle2D getBoundingRect() {
        if (mCachedBounds == null) {
            Rectangle2D r = mFont.getStringBounds(mText, mFrc);
            System.out.println("r = "+r);
            mCachedBounds = new Rectangle2D.Double(
                r.getX() + mX, r.getY() + mY,
                r.getWidth(), r.getHeight());
            if (mFixedPointSize) {
                double xScale = mTransform.getScaleX();
                double yScale = mTransform.getScaleY();
                mCachedBounds.setRect(r.getX() + mX, r.getY() / yScale + mY,
                    r.getWidth() / xScale, r.getHeight() / yScale);
                System.out.println("scale = " + xScale + ", " + yScale);
                System.out.println("rect  = " + mCachedBounds);
            }
        }
        return mCachedBounds;
    }

    public boolean intersects(Rectangle2D r) {
        return getBoundingRect().intersects(r);
    }

    public boolean contains(double x, double y) {
        return getBoundingRect().contains(x, y);
    }

    public void move(double dx, double dy) {
        mX += dx;
        mY += dy;
        mCachedBounds = null;
    }

    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        Paint savedPaint = g2.getPaint();
        Font savedFont = g2.getFont();

        if (!mTransform.equals(g2.getTransform())) {
            mTransform = g2.getTransform();
            mCachedBounds = null;
        }

        // If the text is fixed size it needs to be inverse transformed
        if (mFixedPointSize) {
            double xScale = 1 / mTransform.getScaleX();
            double yScale = 1 / mTransform.getScaleY();
            g2.setFont(mFont.deriveFont(AffineTransform.getScaleInstance(xScale, yScale)));
        } else {
            g2.setFont(mFont);
        }

        mFrc = g2.getFontRenderContext();

        g2.setPaint(mPaint);
        g2.drawString(mText, (float)mX, (float)mY);

        g2.setTransform(mTransform);
        g2.setFont(savedFont);
        g2.setPaint(savedPaint);
    }

    @Override
    public void setPosition(double x, double y) {
        mX = x;
        mY = y;
        mCachedBounds = null;
    }
}
