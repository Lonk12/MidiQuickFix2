package com.lemckes.MidiQuickFix.components.histogram;

import com.lemckes.j2di.IDimension;
import com.lemckes.j2di.IGroup;
import com.lemckes.j2di.ILayer;
import com.lemckes.j2di.IShapeGraphic;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

/**
 *
 */
public class QHistBackground
        extends ILayer
{

    private QHist mQHist;
    private double mQuantiseLevel = 1;
    private IGroup mGroup;
    private QuantiseRatioGraphic mGraphic;
    private Rectangle2D.Double mWorld =
            new Rectangle2D.Double(0.0, 0.0, 1000.0, 1000.0);

    public QHistBackground(QHist hist) {
        super(400, 300);
        setWorldBounds(mWorld, true);
        setWorldViewSize(new IDimension(1000.0, 1000.0), true);
        mQHist = hist;
        mGroup = new IGroup(this);
        mGraphic = new QuantiseRatioGraphic();
        mGroup.add(mGraphic);
        add(mGroup);
    }

    public void setQuantiseLevel(double notesPerBeat) {
        mQuantiseLevel = notesPerBeat;
        repaint();
    }

    public class QuantiseRatioGraphic
            extends IShapeGraphic
    {

        @Override
        public void paint(Graphics painter) {
//            System.err.println("------------------ Painting the background ...");
//            new RuntimeException("Background paint").printStackTrace();
            Graphics2D g2 = (Graphics2D)painter;
            Paint savedPaint = g2.getPaint();
            Stroke savedSt = g2.getStroke();
            Stroke stroke = new BasicStroke(0.0f, BasicStroke.CAP_BUTT,
                    BasicStroke.JOIN_BEVEL);
            g2.setStroke(stroke);

//            Rectangle2D rect = painter.getClipBounds();
//            double rectY = rect.getY();
//            double rectH = rect.getHeight();

            double height = getBoundingRect().getHeight();
            double width = getBoundingRect().getWidth();
            double step = width / mQuantiseLevel;

            System.err.println("QuantiseRatioGraphic w=" + width + " h=" + height + " step=" + step);

            double startX = 0;
            Color c1 = Color.getHSBColor(0.5f, 0.05f, 1.0f);
            Color c2 = Color.getHSBColor(0.75f, 0.05f, 1.0f);
            Color c = c1;
            while (startX < width) {
                g2.setPaint(c);
                g2.fill(new Rectangle2D.Double(startX, 0, step, height));
                startX += step;
                c = c == c1 ? c2 : c1;
            }

            g2.setStroke(savedSt);
            g2.setPaint(savedPaint);
        }

        @Override
        public void move(double dx, double dy) {
//            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setPosition(double x, double y) {
//            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Rectangle2D getBoundingRect() {
            return mWorld;
//            Rectangle2D rect = new Rectangle2D.Double(
//                    0, 0, mQHist.getNumValues(), mQHist.getMaxValue());
//            return rect;
        }

        @Override
        public boolean intersects(Rectangle2D r) {
            return true;
        }

        @Override
        public boolean contains(double x, double y) {
            return true;
        }
    }
}
