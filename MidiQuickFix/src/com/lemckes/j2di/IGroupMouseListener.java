/*
 * IGroupMouseListener.java
 *
 * Created on January 12, 2005, 4:01 PM
 */
package com.lemckes.j2di;

import java.util.EventListener;

/**
 * The listener interface for receiving mouse press,
 * release and click events on an IGroup.
 * @see IGroupMouseMotionListener
 * @see IGroupKeyListener
 * @see IGroupFocusListener
 */
public interface IGroupMouseListener extends EventListener {

    /**
     * Invoked when the mouse button has been clicked
     * (pressed and released) on an IGroup.
     * @param e The event
     */
    public void groupMouseClicked(IGroupMouseEvent e);

    /**
     * Invoked when a mouse button has been pressed on an IGroup.
     * @param e The event
     */
    public void groupMousePressed(IGroupMouseEvent e);

    /**
     * Invoked when a mouse button has been released on an IGroup.
     * @param e The event
     */
    public void groupMouseReleased(IGroupMouseEvent e);
}
