
package com.lemckes.j2di;

import java.awt.geom.Line2D;

/**
 * A j2di graphic that draws a line.
 */
public class ILine
    extends IShape {

    /**
     * Create an ILine with the given Line2D.
     *
     * @param line The line to be drawn.
     */
    public ILine(Line2D line) {
        super(line);
    }
}