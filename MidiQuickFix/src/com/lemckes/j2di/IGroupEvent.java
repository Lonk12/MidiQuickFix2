/*
 * IGroupMouseEvent.java
 *
 * Created on January 12, 2005, 4:09 PM
 */
package com.lemckes.j2di;

import java.awt.event.InputEvent;

/**
 * The event that is passed to IGroupMouseListener, IGroupMouseMotionListener
 * and IGroupFocusListener implementors.
 * @param <T> The type of java.awt.event.InputEvent that this event contains
 * @see IGroupMouseEvent
 * @see IGroupKeyEvent
 */
public abstract class IGroupEvent<T extends InputEvent> {

    protected T mEvent;
    protected IGroup mGroup;

    /**
     * Create an IGroupMouseEvent.
     * @param e The InputEvent which triggered this constructor.
     * @param g The group for which the event occurred.
     */
    IGroupEvent(T e , IGroup g) {
        mEvent = e;
        mGroup = g;
    }
    /**
     * Get the AWT InputEvent.
     * @return The InputEvent that caused the creation of this IGroupEvent.
     */
    public T getEvent() {
        return mEvent;
    }

    /**
     * Get the IGroup to which this event belongs.
     * @return The IGroup to which this event belongs.
     */
    public IGroup getIGroup() {
        return mGroup;
    }
}
