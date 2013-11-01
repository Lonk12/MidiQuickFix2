package com.lemckes.j2di;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 *
 */
public class EllipseGroup extends IGroup {

    public EllipseGroup(ILayer l) {
        super(l);
        Ellipse2D ell = new Ellipse2D.Double(320.0, 20.0, 70.0, 30.0);
        IEllipse mEllipse1 = new IEllipse(ell);
        mEllipse1.setPaint(Color.green.darker().darker());

        ell = new Ellipse2D.Double(328.0, 60.0, 70.0, 30.0);
        IEllipse mEllipse2 = new IEllipse(ell);
        mEllipse2.setPaint(Color.green.darker().darker());
        mEllipse2.setFilled(true);

        add(mEllipse1);
        add(mEllipse2);
    }

//    @Override
//    public void paint(Graphics g, Rectangle2D clip) {
//        super.paint(g, clip);
//        if (hasFocus()) {
//            Graphics2D g2 = (Graphics2D)g;
//
//            // Save the current clip
//            Shape oldClip = g2.getClip();
//
//            g2.clip(clip);
//            g2.draw(getBoundingRect());
//
//
//            // Put the clip back the way we found it
//            g2.setClip(oldClip);
//        }
//    }
}
