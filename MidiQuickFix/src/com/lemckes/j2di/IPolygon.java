
package com.lemckes.j2di;

import java.awt.geom.Path2D;

/**
 * A j2di graphic that draws a polygon.
 * The GeneralPath by which this IPolygon is defined should be created
 * relative to 0, 0 in world coordinates and then have its position set
 * with the {@code setPosition(x, y) } method.
 */
public class IPolygon
    extends IShape {

    /**
     * Create an IPolygon with the given Path2D.
     *
     * @param polygon The polygon to be drawn.
     */
    public IPolygon(Path2D.Double polygon) {
        super(polygon);
    }
}
