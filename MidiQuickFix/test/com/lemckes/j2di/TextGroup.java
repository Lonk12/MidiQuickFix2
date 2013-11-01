package com.lemckes.j2di;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

/**
 *
 */
public class TextGroup extends IGroup {

    IText mIText;
//    IRectangle mBound1;

    public TextGroup(ILayer l) {
        super(l);
        mIText = new IText("");
        mIText.setPaint(Color.BLACK);
        add(mIText);
//        mBound1 = new IRectangle(mIText.getBoundingRect());
//        mBound1.setPaint(Color.MAGENTA);
//        add(mBound1);
//        add(
//            new IRectangle(new Rectangle2D.Double(100, 100, 100, 20)));
    }

    public void setText(String text) {
        mIText.setText(text);
        mCachedBounds = null;
    }

    public void setFont(Font font) {
        mIText.setFont(font);
        mCachedBounds = null;
    }

    public void setFixedPointSize(boolean fixed) {
        mIText.setFixedPointSize(fixed);
        mCachedBounds = null;
    }

    @Override
    public void setPaint(Paint paint) {
        mIText.setPaint(paint);
    }

//    @Override
//    public void paint(Graphics g, Rectangle2D clip) {
//        super.paint(g, clip);
//        if (hasFocus()) {
//            Graphics2D g2 = (Graphics2D)g;
//            // Save the current clip
//            Shape oldClip = g2.getClip();
//            g2.clip(clip);
//
//            g2.draw(getBoundingRect());
//
//            // Put the clip back the way we found it
//            g2.setClip(oldClip);
//        }
//        remove(mBound1);
//    }
}
