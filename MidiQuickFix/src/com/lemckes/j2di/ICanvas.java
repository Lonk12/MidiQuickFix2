package com.lemckes.j2di;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Rectangle;
import javax.swing.JLayeredPane;
import javax.swing.Scrollable;
import javax.swing.SwingConstants;

/**
 * A ICanvas is a container for ILayers.
 */
public class ICanvas extends JLayeredPane implements Scrollable {

    private int mHorScrollUnitIncrement = 1;
    private int mVertScrollUnitIncrement = 1;
    private int mHorScrollBlockIncrement = 10;
    private int mVertScrollBlockIncrement = 10;

    /**
     * Create an ICanvas with the given pixel dimensions.
     * <p/>
     * @param width The Width of the canvas
     * @param height The height of the canvas
     */
    public ICanvas(int width, int height) {
        super();
        setLayout(new LayerAttachLayout());
        setSize(width, height);
    }

    /**
     * Set the size of the canvas.
     * The size is calculated from the maximum size of the canvas' children.
     * The component's maximumSize, preferredSize
     * and minimumSize are set to the new size.
     */
    public void setSize() {
        int width = 0;
        int height = 0;

        Component[] components = getComponents();
        for (int i = 0; i < components.length; ++i) {
            Dimension d = components[i].getSize();
            width = Math.max(width, d.width);
            height = Math.max(height, d.height);
        }

        // System.out.println("ICanvas.setSize(" + width + ", " + height + ")");
        super.setSize(width, height);
        setMinimumSize(new Dimension(width, height));
        setPreferredSize(new Dimension(width, height));
        setMaximumSize(new Dimension(width, height));
    }

    /**
     * Set the size of the canvas to the given size.
     * The component's maximumSize, preferredSize
     * and minimumSize are set to the new size.
     * <p/>
     * @param d The new size
     */
    @Override
    public void setSize(Dimension d) {
        // System.out.println("ICanvas.setSize(Dimension d)");
        setSize(d.width, d.height);
    }

    /**
     * Set the amount by which the canvas will be scrolled.
     * These values are in screen units (pixels)
     * <p/>
     * @param horUnit The size of a 'unit' horizontal scroll.
     * A common value is 1/10th of the visible width of the viewport.
     * @param vertUnit The size of a 'unit' vertical scroll.
     * This is usually the logical equivalent of a 'line'.
     * @param horBlock The size of a 'block' horizontal scroll.
     * A common value is the visible width of the viewport.
     * @param vertBlock The size of a 'block' vertical scroll.
     * This is usually the logical equivalent of a 'page'.
     */
    public void setScrollUnits(
        int horUnit, int vertUnit,
        int horBlock, int vertBlock) {
        mHorScrollUnitIncrement = horUnit;
        mVertScrollUnitIncrement = vertUnit;
        mHorScrollBlockIncrement = horBlock;
        mVertScrollBlockIncrement = vertBlock;
    }

    ///////////////////////////////////////////////////////////////
    ////
    /// Scrollable interface implementation
    //
    public int getScrollableBlockIncrement(
        Rectangle rectangle,
        int orientation,
        int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return mHorScrollBlockIncrement;
        } else {
            return mVertScrollBlockIncrement;
        }
    }

    public int getScrollableUnitIncrement(
        Rectangle rectangle,
        int orientation,
        int direction) {
        if (orientation == SwingConstants.HORIZONTAL) {
            return mHorScrollUnitIncrement;
        } else {
            return mVertScrollUnitIncrement;
        }
    }

    public Dimension getPreferredScrollableViewportSize() {
        return getPreferredSize();
    }

    public boolean getScrollableTracksViewportHeight() {
        return false;
    }

    public boolean getScrollableTracksViewportWidth() {
        return false;
    }
}
