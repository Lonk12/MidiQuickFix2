package com.lemckes.j2di;

import java.awt.geom.Rectangle2D;

/**
 * A j2di graphic that draws a rectangle.
 */
public class IRectangle2
    extends IShape {


    /**
     * Create an IRectangle with the given Rectangle2D.
     * <p/>
     * @param rectangle The rectangle to be drawn.
     */
    public IRectangle2(Rectangle2D rectangle) {
        super(rectangle);
    }
}
