
package com.lemckes.j2di;

import java.awt.geom.Arc2D;

/**
 * A j2di graphic that draws an arc.
 * <b>NOTE:</b> There is a known bug in Java 1.3, 1.4 with the calculation of
 * intersections in Arc2D. This may cause an IArc not to be drawn because the
 * test for {@code intersects(Rectangle2D r)} fails when it should succeed.
 * Refer to - <br>
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4724569<br>
 * for details. <br>
 * The bug has been fixed in Java 1.5
 */
public class IArc
    extends IShape {

    /**
     * Create an IArc with the given Arc2D.
     *
     * @param arc The arc to be drawn.
     */
    public IArc(Arc2D arc) {
        super(arc);
    }
}
