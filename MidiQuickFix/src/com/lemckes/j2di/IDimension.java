/*
 * IDimension.java
 *
 * Created on January 20, 2005, 2:01 PM
 */
package com.lemckes.j2di;

import java.awt.geom.Dimension2D;

/**
 * Implements a version of Dimension2D which keeps its values as doubles.
 */
public class IDimension extends Dimension2D {

    /**
     * The width of the dimension.
     */
    public double width;
    /**
     * The height of the dimension.
     */
    public double height;

    /**
     * Creates a new instance of IDimension.
     * <p/>
     * @param w The width of this dimension
     * @param h The height of this dimension
     */
    public IDimension(double w, double h) {
        width = w;
        height = h;
    }

    /**
     * Returns the height of this Dimension in double precision.
     * <p/>
     * @return the height of this Dimension.
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the width of this Dimension in double precision.
     * <p/>
     * @return the width of this Dimension.
     */
    public double getWidth() {
        return width;
    }

    /**
     * Sets the size of this Dimension object to the specified width and height.
     * This method is included for completeness, to parallel the
     * getSize method of Component.
     * <p/>
     * @param w the new width for the Dimension object
     * @param h the new height for the Dimension object
     */
    public void setSize(double w, double h) {
        width = w;
        height = h;
    }
}
