/*
 * IGroupKeyListener.java
 *
 * Created on January 12, 2005, 4:06 PM
 */
package com.lemckes.j2di;

import java.util.EventListener;

/**
 * The listener interface for receiving keyboard events
 * (keystrokes) on an IGroup.
 * @see IGroupKeyEvent
 */
public interface IGroupKeyListener extends EventListener {

    /**
     * Invoked when a key has been pressed.
     * @param e The event
     */
    public void groupKeyPressed(IGroupKeyEvent e);

    /**
     * Invoked when a key has been released.
     * @param e The event
     */
    public void groupKeyReleased(IGroupKeyEvent e);

    /**
     * Invoked when a key has been typed.
     * @param e The event
     */
    public void groupKeyTyped(IGroupKeyEvent e);
}
