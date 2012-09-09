
package com.lemckes.j2di;

import java.awt.geom.Ellipse2D;

/**
 * A j2di graphic that draws an ellipse.
 */
public class IEllipse
    extends IShape {

    /**
     * Create an IEllipse with the given Ellipse2D.
     *
     * @param ellipse The ellipse to be drawn.
     */
    public IEllipse(Ellipse2D ellipse) {
        super(ellipse);
    }
}
