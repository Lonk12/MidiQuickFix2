/*
 * IGroupMouseEvent.java
 *
 * Created on January 12, 2005, 4:09 PM
 */

package com.lemckes.j2di;

import java.awt.event.MouseEvent;

/**
 * The event that is passed to IGroupMouseListener, IGroupMouseMotionListener
 * and IGroupFocusListener implementors.
 * <p/>
 * @see IGroupMouseListener
 * @see IGroupMouseMotionListener
 * @see IGroupFocusListener
 */
public class IGroupMouseEvent
    extends IGroupEvent<MouseEvent> {

    private double mX;
    private double mY;

    /**
     * Create an IGroupMouseEvent.
     * <p/>
     * @param e The MouseEvent which triggered this constructor.
     * @param g The group for which the event occurred.
     */
    IGroupMouseEvent(MouseEvent e, IGroup g) {
        super(e, g);
        if (e != null) {
            mX = e.getX() / g.getTransform().getScaleX();
            mY = e.getY() / g.getTransform().getScaleY();
        }
    }

    public double getX() {
        return mX;
    }

    public double getY() {
        return mY;
    }
}
