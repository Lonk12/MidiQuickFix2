
package com.lemckes.j2di;

import java.awt.geom.Rectangle2D;

/**
 * A j2di graphic that draws a rectangle.
 */
public class IRectangle
    extends IShape {

    /**
     * Create an IRectangle with the given Rectangle2D.
     *
     * @param rectangle The rectangle to be drawn.
     */
    public IRectangle(Rectangle2D rectangle) {
        super(rectangle);
    }
}
