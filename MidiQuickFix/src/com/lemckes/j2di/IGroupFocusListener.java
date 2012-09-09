/*
 * IGroupFocusListener.java
 *
 * Created on January 12, 2005, 4:05 PM
 */
package com.lemckes.j2di;

import java.util.EventListener;

/**
 * The listener interface for receiving focus events on an IGroup.
 * In j2di a group gains focus when it becomes the 'current' group in the layer.
 * This is usually the front-most group under the mouse pointer.
 * <p/>
 * @see IGroupMouseEvent
 */
public interface IGroupFocusListener extends EventListener {

    /**
     * Invoked when an IGroup gains the keyboard focus.
     * <p/>
     * @param e The event
     */
    public void groupFocusGained(IGroupMouseEvent e);

    /**
     * Invoked when an IGroup loses the keyboard focus.
     * <p/>
     * @param e The event
     */
    public void groupFocusLost(IGroupMouseEvent e);
}
