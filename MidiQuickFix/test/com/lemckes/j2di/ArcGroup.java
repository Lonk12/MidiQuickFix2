package com.lemckes.j2di;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

/**
 *
 */
public class ArcGroup
    extends IGroup
{

    public ArcGroup(ILayer l) {
        super(l);
        Arc2D arc = new Arc2D.Double(20.0, 300.0, 70.0, 30.0, 0.0, 280.0,
            Arc2D.PIE);
        IArc mArc1 = new IArc(arc);
        mArc1.setPaint(new Color(0f, 0f, 0.8f, 0.5f));

        arc = new Arc2D.Double(28.0, 340.0, 70.0, 30.0, 0.0, 280.0, Arc2D.PIE);
        IArc mArc2 = new IArc(arc);
        mArc2.setPaint(new Color(0f, 0f, 0.8f, 0.5f));
        mArc2.setFilled(true);

        add(mArc1);
        add(mArc2);
    }

//    @Override
//    public void paint(Graphics g, Rectangle2D clip) {
//        super.paint(g, clip);
//        if (hasFocus()) {
//            Graphics2D g2 = (Graphics2D)g;
//            g2.draw(getBoundingRect());
//        }
//    }
}
