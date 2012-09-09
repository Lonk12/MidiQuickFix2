
package com.lemckes.j2di;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * The base class for an IShapeGraphic that draws a simple {@code java.awt.Shape}.
 */
public class IShape
    extends IShapeGraphic {

    /**
     * The shape to be drawn
     */
    protected Shape mShape;
    /**
     * The translation to apply to position the shape
     */
    protected AffineTransform mTranslation;

    /**
     * Create a new IShape for the given Shape
     *
     * @param shape The shape to be drawn
     */
    public IShape(Shape shape) {
        setShape(shape);
    }

    /**
     * Set the shape to be drawn.
     *
     * @param shape the new Shape
     */
    public final void setShape(Shape shape) {
        double posX = shape.getBounds2D().getX();
        double posY = shape.getBounds2D().getY();
        mTranslation = AffineTransform.getTranslateInstance(posX, posY);
        AffineTransform inverse = AffineTransform.getTranslateInstance(-posX, -posY);
        mShape = inverse.createTransformedShape(shape);
        mCachedBounds = null;
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        Paint savedPaint = g2.getPaint();
        Stroke savedStroke = g2.getStroke();
        AffineTransform savedTransform = g2.getTransform();

        g2.transform(mTranslation);
        g2.setStroke(mStroke);
        g2.setPaint(mPaint);
        if (mFilled) {
            g2.fill(mShape);
        } else {
            if (mUseDeviceStroke) {
                // Copy the transform from the graphics context
                AffineTransform aft = new AffineTransform(savedTransform);
                // add our positioning transform
                aft.concatenate(mTranslation);
                // pre-transform the points of our shape
                Shape screenShape =
                    aft.createTransformedShape(mShape);
                // set the graphics context's transform to IDENTITY_TRANSFORM
                // so that the Stroke will not be transformed
                g2.setTransform(IDENTITY_TRANSFORM);
                g2.draw(screenShape);
            } else {
                g2.draw(mShape);
            }
        }

        g2.setTransform(savedTransform);
        g2.setStroke(savedStroke);
        g2.setPaint(savedPaint);
    }

    @Override
    public void move(double dx, double dy) {
        mTranslation.translate(dx, dy);
        mCachedBounds = null;
    }

    @Override
    public void setPosition(double x, double y) {
        setPosition(x, y, AnchorPoint.TOP_LEFT);
    }

    /**
     *
     * @param x      x
     * @param y      y
     * @param anchor a
     */
    public void setPosition(double x, double y, AnchorPoint anchor) {
        double newX = x;
        double newY = y;
        double width = getBoundingRect().getWidth();
        double height = getBoundingRect().getHeight();
        switch (anchor) {
            case TOP_LEFT:
                break;
            case TOP_CENTRE:
                newX -= width / 2;
                break;
            case TOP_RIGHT:
                newX -= width;
                break;
            case MIDDLE_LEFT:
                newY -= height / 2;
                break;
            case MIDDLE_CENTRE:
                newX -= width / 2;
                newY -= height / 2;
                break;
            case MIDDLE_RIGHT:
                newX -= width;
                newY -= height / 2;
                break;
            case BOTTOM_LEFT:
                newY -= height;
                break;
            case BOTTOM_CENTRE:
                newX -= width / 2;
                newY -= height;
                break;
            case BOTTOM_RIGHT:
                newX -= width;
                newY -= height;
                break;
        }
        //System.out.println(
        //    x + "," + y + " + "
        //    + width + "," + height + " > "
        //    + newX + "," + newY);
        mTranslation.setToTranslation(newX, newY);
        mCachedBounds = null;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        if (mCachedBounds == null) {
            Shape sh = mTranslation.createTransformedShape(mShape);
            mCachedBounds =
                mStroke.createStrokedShape(sh).getBounds2D();
        }
        return mCachedBounds;
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        return mTranslation.createTransformedShape(mShape).intersects(r);
    }

    @Override
    public boolean contains(double x, double y) {
        return mTranslation.createTransformedShape(mShape).contains(x, y);
    }

    /**
     * The reference point in the graphic to use for positioning.
     */
    public enum AnchorPoint {

        TOP_LEFT,
        TOP_CENTRE,
        TOP_RIGHT,
        MIDDLE_LEFT,
        MIDDLE_CENTRE,
        MIDDLE_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_CENTRE,
        BOTTOM_RIGHT
    }
}
