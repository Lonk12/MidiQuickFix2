package com.lemckes.j2di;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentListener;
import javax.swing.JScrollPane;

/**
 *
 */
public class ICanvasScrollPane extends JScrollPane implements ComponentListener {

    private ICanvas mCanvas;
    private int mScrollUnitsX = 10;
    private int mScrollUnitsY = 10;

    public ICanvasScrollPane() {
        initView();
    }

    public ICanvasScrollPane(int vsbPolicy, int hsbPolicy) {
        super(vsbPolicy, hsbPolicy);
        initView();
    }

    public ICanvasScrollPane(Component view) {
        super(view);
        initView();
    }

    public ICanvasScrollPane(Component view, int vsbPolicy, int hsbPolicy) {
        super(view, vsbPolicy, hsbPolicy);
        initView();
    }

    private void initView() {
        setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        mCanvas = new ICanvas(1, 1);
        setViewportView(mCanvas);
        viewport = getViewport();
        viewport.addComponentListener(this);
    }

    public ICanvas getCanvas() {
        return mCanvas;
    }

    public int getScrollUnitsX() {
        return mScrollUnitsX;
    }

    /**
     * Set the number of scroll units in a scroll page <br>
     * for example setting this to 10 will scroll the canvas by
     * one tenth of the visible width each time the horizontal scrollbar
     * arrow button is clicked
     * @param scrollUnitsX
     */
    public void setScrollUnitsX(int scrollUnitsX) {
        mScrollUnitsX = scrollUnitsX;
    }

    public int getScrollUnitsY() {
        return mScrollUnitsY;
    }

    /**
     * /**
     * Set the number of scroll units in a scroll page <br>
     * for example setting this to 10 will scroll the canvas by
     * one tenth of the visible height each time the vertical scrollbar
     * arrow button is clicked
     * @param scrollUnitsY
     */
    public void setScrollUnitsY(int scrollUnitsY) {
        mScrollUnitsY = scrollUnitsY;
    }

    public void componentHidden(java.awt.event.ComponentEvent componentEvent) {
    }

    public void componentMoved(java.awt.event.ComponentEvent componentEvent) {
    }

    public void componentResized(java.awt.event.ComponentEvent componentEvent) {
        resizeViewport();
    }

    public void componentShown(java.awt.event.ComponentEvent componentEvent) {
    }

    private void resizeViewport() {
        Dimension viewSize = viewport.getExtentSize();

        updateScrollbarValues();
        for (Component c : mCanvas.getComponents()) {
            if (c instanceof ILayer) {
                ILayer layer = (ILayer)c;
                layer.setScreenViewSize(viewSize, true);
                layer.repaint();
            }
        }
    }

    private void updateScrollbarValues() {
        Dimension viewSize = viewport.getExtentSize();

        mCanvas.setScrollUnits(
            viewSize.width / mScrollUnitsX,
            viewSize.height / mScrollUnitsY,
            viewSize.width,
            viewSize.height);
    }
}
