package com.lemckes.j2di;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 *
 */
public class Smiley2
    extends IGroup
{
    private SmileyGraphic mSmiley;

    public Smiley2(ILayer l) {
        super(l);
        mSmiley = new SmileyGraphic();
        add(mSmiley.mFace);
        add(mSmiley.mBorder);
        add(mSmiley.mLeftEye);
        add(mSmiley.mRightEye);
        add(mSmiley.mSmile);
        add(mSmiley.mFrown);
//        add(mSmiley);
    }

    @Override
    public void paint(Graphics g, Rectangle2D clip) {
//        if (!hasFocus()) {
//            remove(mSmiley.mFrown);
//            add(mSmiley.mSmile);
//        } else {
//            remove(mSmiley.mSmile);
//            add(mSmiley.mFrown);
//        }
        mSmiley.mSmile.setVisible(!hasFocus());
        mSmiley.mFrown.setVisible(hasFocus());
        super.paint(g, clip);
    }

    private class SmileyGraphic
//        extends IShapeGraphic
    {
        private double mSize = 60.0;
        IEllipse mFace;
        IEllipse mBorder;
        IEllipse mLeftEye;
        IEllipse mRightEye;
        IArc mSmile;
        IArc mFrown;
        boolean mDoSmile = true;

        SmileyGraphic() {
            mFace = new IEllipse(new Ellipse2D.Double(0.0, 0.0, mSize, mSize));
            mBorder = new IEllipse(new Ellipse2D.Double(0.0, 0.0, mSize, mSize));
            mLeftEye = new IEllipse(new Ellipse2D.Double(mSize / 5, mSize / 5, mSize / 5,
                mSize / 3));
            mRightEye = new IEllipse(new Ellipse2D.Double(mSize * 0.6, mSize / 5, mSize / 5,
                mSize / 3));
            mSmile = new IArc(new Arc2D.Double(
                mSize / 4, mSize * .6, mSize / 2, mSize / 4, 190.0, 160.0,
                Arc2D.OPEN));
            mFrown = new IArc(new Arc2D.Double(
                mSize / 4, mSize * .7, mSize / 2, mSize / 4, 10.0, 160.0,
                Arc2D.OPEN));
            Stroke stroke = new BasicStroke(3.0f);

            mFace.setFilled(true);
            mFace.setPaint(Color.YELLOW);
            mBorder.setStroke(stroke);
            mLeftEye.setFilled(true);
            mRightEye.setFilled(true);
            mSmile.setStroke(stroke);
            mFrown.setStroke(stroke);

        }

//        public void setDoSmile(boolean doSmile) {
//            mDoSmile = doSmile;
//        }
//
//        @Override
//        public void paint(Graphics g) {
//            Graphics2D g2 = (Graphics2D)g;
//            Stroke savedStroke = g2.getStroke();
//            Paint savedPaint = g2.getPaint();
//
//            g2.setPaint(Color.YELLOW);
//            g2.fill(mFace);
//            g2.setPaint(Color.BLACK);
//            g2.setStroke(mStroke);
//            g2.draw(mBorder);
//            g2.fill(mLeftEye);
//            g2.fill(mRightEye);
//            if (mDoSmile) {
//                g2.draw(mSmile);
//            } else {
//                g2.draw(mFrown);
//            }
//
//            g2.setPaint(savedPaint);
//            g2.setStroke(savedStroke);
//        }

//        @Override
//        public void move(double dx, double dy) {
//            mFace.setFrame(
//                mFace.getFrame().getX() + dx, mFace.getFrame().getY() + dy,
//                mFace.getFrame().getWidth(), mFace.getFrame().getHeight());
//            mBorder.setFrame(
//                mBorder.getFrame().getX() + dx, mBorder.getFrame().getY() + dy,
//                mBorder.getFrame().getWidth(), mBorder.getFrame().getHeight());
//            mLeftEye.setFrame(
//                mLeftEye.getFrame().getX() + dx, mLeftEye.getFrame().getY() + dy,
//                mLeftEye.getFrame().getWidth(), mLeftEye.getFrame().getHeight());
//            mRightEye.setFrame(
//                mRightEye.getFrame().getX() + dx,
//                mRightEye.getFrame().getY() + dy,
//                mRightEye.getFrame().getWidth(),
//                mRightEye.getFrame().getHeight());
//            mSmile.setFrame(
//                mSmile.getFrame().getX() + dx, mSmile.getFrame().getY() + dy,
//                mSmile.getFrame().getWidth(), mSmile.getFrame().getHeight());
//            mFrown.setFrame(
//                mFrown.getFrame().getX() + dx, mFrown.getFrame().getY() + dy,
//                mFrown.getFrame().getWidth(), mFrown.getFrame().getHeight());
//            mCachedBounds = null;
//        }

//        @Override
//        public void setPosition(double x, double y) {
//            double dx = x - mFace.getFrame().getX();
//            double dy = y - mFace.getFrame().getY();
//            move(dx, dy);
//            mCachedBounds = null;
//        }
//
//        @Override
//        public Rectangle2D getBoundingRect() {
//            if (mCachedBounds == null) {
//                mCachedBounds = mStroke.createStrokedShape(mBorder).getBounds2D();
//            }
//            return mCachedBounds;
//        }
//
//        @Override
//        public boolean intersects(Rectangle2D r) {
//            return mFace.intersects(r);
//        }
//
//        @Override
//        public boolean contains(double x, double y) {
//            return mFace.contains(x, y);
//        }
    }
}