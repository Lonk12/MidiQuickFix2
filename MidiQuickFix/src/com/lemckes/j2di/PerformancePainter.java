/*
 * PerformancePainter.java
 */
package com.lemckes.j2di;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

/**
 * Manage the rendering hints for an object.
 * To use this class, you should override paint to
 * look something like:
 *
 * <pre>
 * public void paint(Graphics g)
 * {
 *   Graphics2D graphics = (Graphics2D) g;
 *   mPerformancePainter.paintStart(g);
 *   super.paint(g);
 *   mPerformancePainter.paintFinish();
 * }
 * </pre>
 */
public class PerformancePainter {

    private boolean mLastWasLow;
    private long mTimeOutNanos; //How long do we wait to go back to highquality
    private long mPaintStart;
    private long mMaxHighQualityPaintTime;
    private long mLastPaintLengthNanos;
    private long mLastPaintFinishNanos;
    private JComponent mComponent;
    private Map<RenderingHints.Key, Object> mQualityRenderingHints;
    private Map<RenderingHints.Key, Object> mFastRenderingHints;
    private Map<RenderingHints.Key, Object> mLastRenderingHints;
    private DelayedPaint mDelayPaint = new DelayedPaint();

    /** Creates a new instance of PerformancePainter */
    public PerformancePainter() {
        mQualityRenderingHints = new HashMap<RenderingHints.Key, Object>(4);
        mQualityRenderingHints.put(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        mQualityRenderingHints.put(
            RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_RENDER_QUALITY);
        mQualityRenderingHints.put(
            RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_STROKE_NORMALIZE);

        mFastRenderingHints = new HashMap<RenderingHints.Key, Object>(4);
        mFastRenderingHints.put(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_OFF);
        mFastRenderingHints.put(
            RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_RENDER_SPEED);
        mFastRenderingHints.put(
            RenderingHints.KEY_STROKE_CONTROL,
            RenderingHints.VALUE_STROKE_NORMALIZE);
        reset();
        mTimeOutNanos = 200000000;
        setFramesPerSecond(30);
        mDelayPaint.start();
    }

    /**
     * Call before staring to paint.
     * DO NOT MESS WITH THE CLIP OR TRANSFORM BEFORE CALLING THIS.
     * <p/>
     * @param g
     */
    public void paintStart(Graphics2D g) {
        boolean paintAllVisible = g.getClipBounds().equals(getComponent().
            getVisibleRect());

        Map<RenderingHints.Key, Object> setTo;
        if (!paintAllVisible) {
            //Restore previous paint settings otherwise it will look mighty odd.
            setTo = mLastRenderingHints;
        } else {
            mDelayPaint.cancel();
            //Decide based on previous paint what settings to use.

            //If paint wasn't a long time ago, and the paint took a good long
            //time
            if ((System.nanoTime() - mLastPaintFinishNanos) < mTimeOutNanos
                && (mLastWasLow || mLastPaintLengthNanos
                > mMaxHighQualityPaintTime)) {
                //go for Low quality
                //Time a repaint to happen in a long time...
                setTo = mFastRenderingHints;
                mLastWasLow = true;
            } //Else
            else {
                mLastWasLow = false;
                //go for high quality
                setTo = mQualityRenderingHints;
            }
        }

        mLastRenderingHints = setTo;
        g.setRenderingHints(setTo);
        mPaintStart = System.nanoTime();
    }

    /**
     * Call when you've finsihed painting.
     */
    public void paintFinish() {
        //init a repaint if we get no more paints
        mLastPaintFinishNanos = System.nanoTime();
        mLastPaintLengthNanos = mLastPaintFinishNanos - mPaintStart;
        if (mLastWasLow) {
            mDelayPaint.reset();
        }
    }

    /**
     * Set how many frames per second is the minimum will will allow high
     * quality rendering to work with while paint calls are frequent
     * <p/>
     * @param framesPerSecond
     */
    public void setFramesPerSecond(int framesPerSecond) {
        mMaxHighQualityPaintTime = 1000000000 / framesPerSecond;
    }

    /**
     * Return the high quality hints
     * <p/>
     * @return
     */
    public Map<RenderingHints.Key, Object> getQualityRenderingHints() {
        return mQualityRenderingHints;
    }

    /**
     * Set the high quality hints
     * <p/>
     * @param qualityRenderingHints
     */
    public void setQualityRenderingHints(
        Map<RenderingHints.Key, Object> qualityRenderingHints) {
        this.mQualityRenderingHints = qualityRenderingHints;
    }

    /**
     * Get the fast (lower quality rendering hints.
     * <p/>
     * @return
     */
    public Map<RenderingHints.Key, Object> getFastRenderingHints() {
        return mFastRenderingHints;
    }

    /**
     * Set the fast (lower quality rendering hints.
     * <p/>
     * @param fastRenderingHints
     */
    public void setFastRenderingHints(
        Map<RenderingHints.Key, Object> fastRenderingHints) {
        this.mFastRenderingHints = fastRenderingHints;
    }

    /**
     * Get the componenet this is for. This is probably useless. but here
     * for completeness and for tasty java-beanlikeness.
     * <p/>
     * @return
     */
    public JComponent getComponent() {
        return mComponent;
    }

    /**
     * Set component this is for. this shouldn't be called too often, infact
     * this probably only lives for one component.
     * <p/>
     * @param component
     */
    public void setComponent(JComponent component) {
        this.mComponent = component;
        reset();
    }

    /**
     * Something changed, reset stuff.
     */
    private void reset() {
        mLastPaintFinishNanos = 0;
        mLastPaintLengthNanos = 0;
        mLastRenderingHints = mQualityRenderingHints;
    }

    class DelayedPaint extends Thread {

        public synchronized void cancel() {
            mCanceled = true;
        }

        public synchronized void reset() {
            //let it know we woke it to reset
            mWasWoken = true;
            this.notify();
        }

        /**
         * Handle complicated nasty repaint timing.
         */
        @Override
        public synchronized void run() {
            try {
                //set initial woken state to false
                mWasWoken = false;
                while (true) {
                    // If we weren't just woken, then sleep till we're
                    // ready to start counting
                    if (!mWasWoken) {
                        wait();
                    }
                    //We're about to sleep, reset woken state
                    mWasWoken = false;
                    long time = System.nanoTime();

                    //Apparently there is such a thing as a spurious wake up
                    // where by a wait returns even though the timeout has not
                    //occured, nothing has called (notify(All)?) or interrupted
                    // the thread. Brilliant. Who would do such a thing?!
                    do {
                        wait(mTimeOutNanos / 1000000);
                    } while ((System.nanoTime() - time) < mTimeOutNanos && !mWasWoken);
                    //check if we were woken externally to reset
                    if (!mWasWoken) {
                        mCanceled = false;
                        SwingUtilities.invokeLater(mRepainter);
                        mWasWoken = false;
                    }
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        /** Did we cancel the paint? */
        private boolean mCanceled;
        /** were we woken via a notify? */
        private boolean mWasWoken = true;
        /** Code to repaint in swing thread to check the canceled stuff
         */
        private Runnable mRepainter = new Repainter();

        class Repainter implements Runnable {

            public void run() {
                synchronized (DelayedPaint.this) {
                    if (!mCanceled) {
                        getComponent().repaint();
                    }
                }
            }
        }
    }
}
