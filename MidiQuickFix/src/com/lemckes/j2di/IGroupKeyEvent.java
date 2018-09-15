/*
 * IGroupKeyEvent.java
 *
 * Created on January 12, 2005, 4:07 PM
 */
package com.lemckes.j2di;

import java.awt.event.KeyEvent;

/**
 * The event that is passed to IGroupKeyListener implementors.
 * @see IGroupKeyListener
 */
public class IGroupKeyEvent extends IGroupEvent<KeyEvent> {

    /**
     * Create an IGroupKeyEvent.
     * @param e The KeyEvent which triggered this constructor.
     * @param g The group for which the event occurred.
     */
    IGroupKeyEvent(KeyEvent e, IGroup g) {
        super(e, g);
    }
}
