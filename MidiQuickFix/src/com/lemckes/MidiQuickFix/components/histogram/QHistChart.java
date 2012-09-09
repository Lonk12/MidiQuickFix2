package com.lemckes.MidiQuickFix.components.histogram;

import com.lemckes.j2di.IDimension;
import com.lemckes.j2di.IGroup;
import com.lemckes.j2di.ILayer;
import com.lemckes.j2di.IShape;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 */
public class QHistChart
    extends ILayer
{
    private QHist mHist;
    private IGroup mGroup;
//    private List<QHistBar> mBars;

    public QHistChart(QHist hist, int width, int height) {
        super(width, height);
        mHist = hist;
        updateChart();
    }

    private void updateChart() {
        removeAllGroups();
        setWorldBounds(new Rectangle2D.Double(
            0, 0, mHist.getNumValues(), mHist.getMaxValue()), true);
        setWorldViewSize(new IDimension(
            mHist.getNumValues(), mHist.getMaxValue()), true);
        mGroup = new IGroup(this);
        Map<Integer, Integer> data = mHist.getHistogramData();
//        mBars = new ArrayList<QHistBar>(data.size());
        for (Entry<Integer, Integer> e : data.entrySet()) {
            Rectangle2D r = new Rectangle2D.Double(0, 0, 1, e.getValue());
            QHistBar bar = new QHistBar(r);
            bar.setPosition(e.getKey(), mHist.getMaxValue() + 1,
                IShape.AnchorPoint.BOTTOM_LEFT);
            mGroup.add(bar);
        }
        add(mGroup);
    }

    @Override
    public void paint(Graphics g) {
//        System.err.println("==================== Painting the chart ...");
//        new RuntimeException("Chart paint").printStackTrace();
        super.paint(g);
    }
}
