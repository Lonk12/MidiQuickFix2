/*
 * IGroupMouseMotionListener.java
 *
 * Created on January 12, 2005, 4:03 PM
 */
package com.lemckes.j2di;

import java.util.EventListener;

/**
 * The listener interface for receiving mouse motion events on an IGroup.
 * <p/>
 * @see IGroupMouseEvent
 */
public interface IGroupMouseMotionListener extends EventListener {

    /**
     * Invoked when a mouse button is pressed on an IGroup and then dragged.
     * <p/>
     * @param e The event
     */
    void groupMouseDragged(IGroupMouseEvent e);

    /**
     * Invoked when the mouse cursor has been moved within an IGroup
     * but no buttons have been pushed.
     * <p/>
     * @param e The event
     */
    void groupMouseMoved(IGroupMouseEvent e);
}
