
package com.lemckes.j2di;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 *
 */
public class PolyGroup
    extends IGroup {

    public PolyGroup(ILayer l) {
        super(l);
        add(new IRectangle(new Rectangle2D.Double(349, 319, 3, 3)));
        add(new IRectangle(new Rectangle2D.Double(419, 319, 3, 3)));

        Path2D.Double path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(30, 40);
        path.lineTo(50, 50);
        path.lineTo(70, 10);
        path.closePath();
        IPolygon poly1 = new IPolygon(path);
        GradientPaint gp = new GradientPaint(
            0, 0, Color.black, 70, 50, Color.red.brighter());
        poly1.setPaint(gp);
        poly1.setPosition(350, 320);

        path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(30, 40);
        path.lineTo(50, 50);
        path.lineTo(70, 10);
        path.closePath();
        IPolygon poly2 = new IPolygon(path);
        GradientPaint gp2 = new GradientPaint(
            0, 0, new Color(1.0f, 0f, 0f, 0.3f),
            7, 5, new Color(0.7f, 0f, 0f, 1f), true);
        poly2.setPaint(gp2);
        poly2.setPosition(420, 320);
        poly2.setFilled(true);

        double radius = 30.0;
        int points = 5;
        double angleStep = (Math.PI * 4) / points;
        path = new Path2D.Double();
        boolean first = true;
        for (int point = 0; point < points; ++point) {
            double angle = point * angleStep;
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

        path.transform(AffineTransform.getTranslateInstance(radius, radius));

        IPolygon poly3 = new IPolygon(path);
        GradientPaint gp3 = new GradientPaint(
            0, 0, new Color(1.0f, 0f, 0f, 0.3f),
            7, 5, new Color(0.7f, 0f, 0f, 1f), true);
        poly3.setPaint(gp3);
        poly3.setPosition(220, 320);
        poly3.setFilled(true);

        add(poly1);
        add(poly2);
        add(poly3);
    }

//    @Override
//    public void paint(Graphics g, Rectangle2D clip) {
//        super.paint(g, clip);
//        if (hasFocus()) {
//            Graphics2D g2 = (Graphics2D)g;
//            g2.draw(getBoundingRect());
//        }
//    }

//    //////////////////////////////////
//    // duplicate method for debugging
//    @Override
//    public Rectangle2D getBoundingRect() {
//        if (mCachedBounds == null) {
//            Rectangle2D rect = new Rectangle2D.Double();
//            boolean first = true;
//            for (IGraphic graphic : mGraphics) {
//                if (graphic.isVisible()) {
//                    if (first) {
//                        rect.setRect(graphic.getBoundingRect());
//                        first = false;
//                    } else {
//                        rect.add(graphic.getBoundingRect());
//                    }
//                }
//            }
//            mCachedBounds = rect;
//        }
//        return mCachedBounds;
//    }
}
