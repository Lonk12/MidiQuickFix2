
package com.lemckes.j2di;

import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 *
 */
public class IStar
    extends IPolygon {

    private double mRadius;
    private double mXoffset;
    private double mYoffset;

    /**
     * Create a star!
     *
     * @param points     number of points
     * @param pointStep  number of points to step for each line segment
     * @param radius     radius
     * @param startAngle the angle for the starting point
     */
    public IStar(int points, int pointStep,
        double radius, double startAngle) {
        super(new Path2D.Double());
        Path2D.Double path = (Path2D.Double)mShape;
        if (points == 6 && pointStep % 2 == 0) {
            Path2D.Double path1 = createPath(3, 1, radius, startAngle);
            Path2D.Double path2 = createPath(3, 1, radius, startAngle + Math.PI);
            path.append(path1.getPathIterator(null), false);
            path.append(path2.getPathIterator(null), false);
        } else {
            path = createPath(points, pointStep, radius, startAngle);
        }

        mRadius = radius;

        Rectangle2D rect = path.getBounds2D();
        mXoffset = mRadius + rect.getX();
        mYoffset = mRadius + rect.getY();

        setShape(path);
    }

    private Path2D.Double createPath(int points, int pointStep,
        double radius, double startAngle) {
        Path2D.Double path = new Path2D.Double();
        double angleStep = (Math.PI * 2 * pointStep) / points;
        boolean first = true;
        for (int point = 0; point < points; ++point) {
            double angle = startAngle + point * angleStep;
            double newX = radius * Math.cos(angle);
            double newY = radius * Math.sin(angle);
            if (first) {
                path.moveTo(newX, newY);
                first = false;
            } else {
                path.lineTo(newX, newY);
            }
        }

        path.closePath();
        return path;
    }

    @Override
    public Rectangle2D getBoundingRect() {
        if (mCachedBounds == null) {
            mCachedBounds = new Rectangle2D.Double(
                mTranslation.getTranslateX(), mTranslation.getTranslateY(),
                mRadius * 2, mRadius * 2);
        }
        return mCachedBounds;

    }

    @Override
    public void setPosition(double x, double y, AnchorPoint anchor) {
        super.setPosition(x + mXoffset, y + mYoffset, anchor);
    }
}
