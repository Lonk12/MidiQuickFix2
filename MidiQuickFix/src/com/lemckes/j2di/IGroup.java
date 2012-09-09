
package com.lemckes.j2di;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.event.EventListenerList;

/**
 * An IGroup is an IGraphic that can contain other IGraphic objects.
 *
 * IGroup is the class that handles events coming from its ILayer parent.
 * To support nested groups it may, instead, be set to pass events
 * through to its children.
 */
public class IGroup
    extends IGraphic {

    /**
     * The list of registered listeners.
     */
    protected EventListenerList mListenerList;
    /**
     * The collection of IGraphics children.
     */
    protected ArrayList<IGraphic> mGraphics = new ArrayList<IGraphic>(2);
    /**
     * Our parent layer.
     */
    protected ILayer mLayer;
    /**
     * Determines if this group handles events itself.
     */
    protected boolean mHandleEvents = true;
    /**
     * Determines if this group passes events through to its IGroup children.
     */
    protected boolean mPassThruEvents = false;
    /**
     * Indicates if the group has keyboard focus
     */
    private boolean mHasFocus = false;
    /**
     * Determines if this group paints its background
     */
    private boolean mIsOpaque = false;

    /**
     * Create an IGroup in the given ILayer.
     *
     * @param l The layer that will contain this group
     */
    public IGroup(ILayer l) {
        mLayer = l;
        mListenerList = new EventListenerList();
    }

    /**
     * Add a graphic to the end of the list of children.
     * Children are painted in the order in which they appear in the list.
     *
     * @param g The graphic to be added.
     */
    public void add(IGraphic g) {
        mGraphics.add(g);
        mCachedBounds = null;
    }

    /**
     * Inserts the specified graphic at the specified position in the list.
     * Shifts the graphic currently at that position (if any) and any
     * subsequent graphics to the right (adds one to their indices).
     * Children are painted in the order in which they appear in the list.
     *
     * @param index The index at which the graphic is to be inserted.
     * @param g     The graphic to be inserted.
     */
    public void add(int index, IGraphic g) {
        // Make sure the index is within range
        int i = Math.min(Math.max(0, index), mGraphics.size());
        mGraphics.add(i, g);
        mCachedBounds = null;
    }

    /**
     * Remove the child at the given index. <br>
     * If the child is an IGroup then also advise the parent ILayer
     * in case it has some state associated with this group.
     *
     * @param index The index of the child to be removed
     * @return the child that was removed or {@code null} if the index was not
     * valid.
     */
    public IGraphic remove(int index) {
        IGraphic removed = null;
        if (index >= 0 && index < mGraphics.size()) {
            removed = mGraphics.remove(index);
            if (removed instanceof IGroup) {
                mLayer.groupRemoved((IGroup)removed);
            }
        }
        mCachedBounds = null;
        return removed;
    }

    /**
     * Remove the given child.
     * If the child is an IGroup then also advise the parent ILayer
     * in case it has some state associated with this group.
     *
     * @param g The IGraphic to be removed
     * @return {@code true} if the child was found.
     */
    public boolean remove(IGraphic g) {
        if (g instanceof IGroup) {
            mLayer.groupRemoved((IGroup)g);
        }
        mCachedBounds = null;
        return mGraphics.remove(g);
    }

    /**
     * Remove all children.
     */
    public void clear() {
        mGraphics.clear();
        mCachedBounds = null;
    }

    /**
     * Test if this group is opaque (i.e. paints its background)
     *
     * @return {@code true} if this croup is opaque
     */
    public boolean isOpaque() {
        return mIsOpaque;
    }

    /**
     * Set this group's opaque state
     *
     * @param opaque if {@code true} then this group paints its background
     */
    public void setOpaque(boolean opaque) {
        mIsOpaque = opaque;
    }

    /**
     * Test if this group has keyboard focus
     *
     * @return {@code true} if this group has keyboard focus
     */
    public boolean hasFocus() {
        return mHasFocus;
    }

    /**
     * Set whether this group has keyboard focus
     *
     * @param hasFocus {@code true} if this group to have keyboard focus
     */
    public void setHasFocus(boolean hasFocus) {
        mHasFocus = hasFocus;
    }

    /**
     * Paint all the child graphics.
     *
     * @param g The graphics to render into.
     */
    public void paint(Graphics g) {
        paint(g, getBoundingRect());
    }

    /**
     * Paint all the child graphics which intersect the given clip.
     *
     * @param g    The graphics to render into.
     * @param clip The clip rectangle to be painted
     */
    public void paint(Graphics g, Rectangle2D clip) {
        Graphics2D g2 = (Graphics2D)g;

        // Save the current state of the Graphics2D
        Shape savedClip = g2.getClip();
        Paint savedPaint = g2.getPaint();
        Stroke savedStroke = g2.getStroke();
        Font savedFont = g2.getFont();

        g2.setClip(null);

        if (mIsOpaque) {
            g2.setPaint(mPaint);
            g2.fill(getBoundingRect());
        }

        for (IGraphic ig : mGraphics) {
            if (ig.isVisible() && ig.getBoundingRect().intersects(clip)) {
                ig.paint(g2);
            }
        }

        // Restore the Graphics2D state
        g2.setFont(savedFont);
        g2.setStroke(savedStroke);
        g2.setPaint(savedPaint);
        g2.setClip(savedClip);
    }

    /**
     * Transform the clip rectangle to screen coordinates
     * and let the layer component handle the repaint.
     */
    public void repaint() {
        repaint(getBoundingRect());
    }

    /**
     * Let the layer component handle the repaint.
     *
     * @param clip
     */
    public void repaint(Rectangle2D clip) {
        mLayer.repaint(clip);
    }

    /**
     * Get the bounding rectangle that is the union of all children,
     * or 0,0,0,0 if there are no children.
     *
     * @return a rectangle.
     */
    public Rectangle2D getBoundingRect() {
        if (mCachedBounds == null) {
            Rectangle2D rect = new Rectangle2D.Double();
            boolean first = true;
            for (IGraphic graphic : mGraphics) {
                if (graphic.isVisible()) {
                    Rectangle2D r = graphic.getBoundingRect();
                    if (first) {
                        rect.setRect(r);
                        first = false;
                    } else {
                        rect.add(r);
                    }
                }
            }
            mCachedBounds = rect;
        }
        return mCachedBounds;
    }

    public boolean contains(double x, double y) {
        boolean contains = false;
        for (IGraphic ig : mGraphics) {
            if (ig.isVisible()) {
                if (ig.contains(x, y)) {
                    contains = true;
                    break;
                }
            }
        }
        return contains;
    }

    public boolean intersects(Rectangle2D rect) {
        boolean intersects = false;
        for (IGraphic ig : mGraphics) {
            if (ig.isVisible()) {
                if (ig.intersects(rect)) {
                    intersects = true;
                    break;
                }
            }
        }
        return intersects;
    }

    public void move(double dx, double dy) {
        int index = mLayer.indexOf(this);
        boolean isCurrent = mLayer.getCurrentGroup() == this;
        boolean isMouseGrab = mLayer.getMouseGrab() == this;
        boolean isKeyGrab = mLayer.getKeyboardGrab() == this;

        // Clear the current location of this group
        mLayer.remove(this);
        mLayer.repaint(getBoundingRect());

        // Move all our children
        for (IGraphic g : mGraphics) {
            g.move(dx, dy);
        }

        mCachedBounds = null;

        // Put this group back in the layer
        // and restore its state.
        mLayer.insertAt(this, index);
        if (isCurrent) {
            mLayer.setCurrentGroup(this);
        }
        if (isMouseGrab) {
            mLayer.setMouseGrab(this);
        }
        if (isKeyGrab) {
            mLayer.setKeyboardGrab(this);
        }
        mLayer.repaint(getBoundingRect());
    }

    /**
     * Set the position of the group.
     *
     * @param x The new x position
     * @param y The new y position
     */
    public void setPosition(double x, double y) {
        Rectangle2D brect = getBoundingRect();
        double dx = x - brect.getX();
        double dy = y - brect.getY();
        move(dx, dy);
        mCachedBounds = null;
    }

    public AffineTransform getTransform() {
        return mLayer.getTransform();
    }

    ////////////////////////////////////////////////////////////////////////
    //
    // Event handlers
    //
    /**
     * Set whether this group, or any of its children, handles events at all.
     *
     * @param b {@code true} if this group, or any of its children, handles
     * events (default), {@code false} if it ignores events.
     */
    public void setHandleEvents(boolean b) {
        mHandleEvents = b;
    }

    /**
     * Get whether this group, or any of its children, handles events at all.
     *
     * @return {@code true} if this group, or any of its children, handles
     * events (default);
     * {@code false} if it ignores events.
     */
    public boolean getHandleEvents() {
        return mHandleEvents;
    }

    /**
     * Set whether this group handles events itself or passes them through to
     * its child groups.
     *
     * @param b {@code false} if this group handles events (default);
     * {@code true} if it passes them through to its child groups.
     */
    public void setPassThruEvents(boolean b) {
        mPassThruEvents = b;
    }

    /**
     * Get whether this group handles events itself or passes them through to
     * its child groups.
     *
     * @return False if this group handles events (default);
     * True if it passes them through to its child groups.
     */
    public boolean getPassThruEvents() {
        return mPassThruEvents;
    }

    /**
     * Get the group that should handle events at the given cursor position.
     * If {@code passThruEvents} is {@code false} or there is
     * no child IGroup at the given position then return {@code this}.
     * The search descends into nested groups.
     *
     * @param cursor The effective cursor rectangle
     * @return The group which will handle the event.
     */
    public IGroup getEventHandler(Rectangle2D.Double cursor) {
        // Assume nothing
        IGroup found = null;
        if (mHandleEvents && intersects(cursor)) {
            found = this;
        }
        if (mPassThruEvents) {
            for (int i = mGraphics.size() - 1; i >= 0; --i) {
                IGraphic g = mGraphics.get(i);
                if (g instanceof IGroup && g.intersects(cursor)) {
                    found = ((IGroup)g).getEventHandler(cursor);
                    break;
                }
            }
        }
        // If not passing events to our children or no IGroup child
        // contains the cursor rectangle, return this group.
        return found;
    }

    /**
     * Handle a mouseDown event from the parent ILayer.
     *
     * @param e The event
     */
    public void mouseDown(MouseEvent e) {
        fireGroupMousePressed(e);
    }

    /**
     * Handle a mouseUp event from the parent ILayer.
     *
     * @param e The event
     */
    public void mouseUp(MouseEvent e) {
        fireGroupMouseReleased(e);
    }

    /**
     * Handle a mouseClicked event from the parent ILayer.
     *
     * @param e The event
     */
    public void mouseClicked(MouseEvent e) {
        fireGroupMouseClicked(e);
    }

    /**
     * Handle a mouseMove event from the parent ILayer.
     *
     * @param e The event
     */
    public void mouseMove(MouseEvent e) {
        fireGroupMouseMoved(e);
    }

    /**
     * Handle a mouseDrag event from the parent ILayer.
     *
     * @param e The event
     */
    public void mouseDrag(MouseEvent e) {
        fireGroupMouseDragged(e);
    }

    /**
     * Handle a mouseEnter event from the parent ILayer.
     *
     * @param e The event
     */
    public void mouseEnter(MouseEvent e) {
        fireGroupFocusGained(e);
    }

    /**
     * Handle a mouseExit event from the parent ILayer.
     *
     * @param e The event
     */
    public void mouseExit(MouseEvent e) {
        fireGroupFocusLost(e);
    }

    /**
     * Handle a keyDown event from the parent ILayer.
     *
     * @param e The event
     */
    public void keyDown(KeyEvent e) {
        fireGroupKeyPressed(e);
    }

    /**
     * Handle a keyUp event from the parent ILayer.
     *
     * @param e The event
     */
    public void keyUp(KeyEvent e) {
        fireGroupKeyReleased(e);
    }

    /**
     * Handle a keyTyped event from the parent ILayer.
     *
     * @param e The event
     */
    public void keyTyped(KeyEvent e) {
        fireGroupKeyTyped(e);
    }

    ////////////////////////////////////////////////////////////
    //
    // Event listener support
    //
    /**
     * Adds the specified event listener to receive mouse events from this
     * group.
     *
     * @param l The listener to be added.
     */
    public void addGroupMouseListener(IGroupMouseListener l) {
        mListenerList.add(IGroupMouseListener.class, l);
    }

    /**
     * Removes the specified mouse listener from this group.
     *
     * @param l The listener to be removed.
     */
    public void removeGroupMouseListener(IGroupMouseListener l) {
        mListenerList.remove(IGroupMouseListener.class, l);
    }

    /**
     * Adds the specified event listener to receive mouse motion
     * events from this group.
     *
     * @param l The listener to be added.
     */
    public void addGroupMouseMotionListener(IGroupMouseMotionListener l) {
        mListenerList.add(IGroupMouseMotionListener.class, l);
    }

    /**
     * Removes the specified mouse motion listener from this group.
     *
     * @param l The listener to be removed.
     */
    public void removeGroupMouseMotionListener(IGroupMouseMotionListener l) {
        mListenerList.remove(IGroupMouseMotionListener.class, l);
    }

    /**
     * Adds the specified event listener to receive key events from this group.
     *
     * @param l The listener to be added.
     */
    public void addGroupKeyListener(IGroupKeyListener l) {
        mListenerList.add(IGroupKeyListener.class, l);
    }

    /**
     * Removes the specified key listener from this group.
     *
     * @param l The listener to be removed.
     */
    public void removeGroupKeyListener(IGroupKeyListener l) {
        mListenerList.remove(IGroupKeyListener.class, l);
    }

    /**
     * Adds the specified event listener to receive focus
     * events from this group.
     *
     * @param l The listener to be added.
     */
    public void addGroupFocusListener(IGroupFocusListener l) {
        mListenerList.add(IGroupFocusListener.class, l);
    }

    /**
     * Removes the specified focus listener from this group.
     *
     * @param l The listener to be removed.
     */
    public void removeGroupFocusListener(IGroupFocusListener l) {
        mListenerList.remove(IGroupFocusListener.class, l);
    }

    /**
     * Notify listeners that are interested in mouse clicked events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupMouseClicked(MouseEvent e) {
        IGroupMouseEvent gme = new IGroupMouseEvent(e, this);
        for (IGroupMouseListener l : mListenerList.getListeners(
            IGroupMouseListener.class)) {
            l.groupMouseClicked(gme);
        }
    }

    /**
     * Notify listeners that are interested in mouse pressed events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupMousePressed(MouseEvent e) {
        IGroupMouseEvent gme = new IGroupMouseEvent(e, this);
        for (IGroupMouseListener l : mListenerList.getListeners(
            IGroupMouseListener.class)) {
            l.groupMousePressed(gme);
        }
    }

    /**
     * Notify listeners that are interested in mouse released events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupMouseReleased(MouseEvent e) {
        IGroupMouseEvent gme = new IGroupMouseEvent(e, this);
        for (IGroupMouseListener l : mListenerList.getListeners(
            IGroupMouseListener.class)) {
            l.groupMouseReleased(gme);
        }
    }

    /**
     * Notify listeners that are interested in mouse dragged events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupMouseDragged(MouseEvent e) {
        IGroupMouseEvent gme = new IGroupMouseEvent(e, this);
        for (IGroupMouseMotionListener l : mListenerList.getListeners(
            IGroupMouseMotionListener.class)) {
            l.groupMouseDragged(gme);
        }
    }

    /**
     * Notify listeners that are interested in mouse moved events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupMouseMoved(MouseEvent e) {
        IGroupMouseEvent gme = new IGroupMouseEvent(e, this);
        for (IGroupMouseMotionListener l : mListenerList.getListeners(
            IGroupMouseMotionListener.class)) {
            l.groupMouseMoved(gme);
        }
    }

    /**
     * Notify listeners that are interested in focus gained events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupFocusGained(MouseEvent e) {
        IGroupMouseEvent gme = new IGroupMouseEvent(e, this);
        for (IGroupFocusListener l : mListenerList.getListeners(
            IGroupFocusListener.class)) {
            l.groupFocusGained(gme);
        }
    }

    /**
     * Notify listeners that are interested in focus lost events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupFocusLost(MouseEvent e) {
        IGroupMouseEvent gme = new IGroupMouseEvent(e, this);
        for (IGroupFocusListener l : mListenerList.getListeners(
            IGroupFocusListener.class)) {
            l.groupFocusLost(gme);
        }
    }

    /**
     * Notify listeners that are interested in key pressed events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupKeyPressed(KeyEvent e) {
        IGroupKeyEvent gme = new IGroupKeyEvent(e, this);
        for (IGroupKeyListener l : mListenerList.getListeners(
            IGroupKeyListener.class)) {
            l.groupKeyPressed(gme);
        }
    }

    /**
     * Notify listeners that are interested in key released events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupKeyReleased(KeyEvent e) {
        IGroupKeyEvent gme = new IGroupKeyEvent(e, this);
        for (IGroupKeyListener l : mListenerList.getListeners(
            IGroupKeyListener.class)) {
            l.groupKeyReleased(gme);
        }
    }

    /**
     * Notify listeners that are interested in key typed events.
     *
     * @param e The event that triggered the action.
     */
    protected void fireGroupKeyTyped(KeyEvent e) {
        IGroupKeyEvent gme = new IGroupKeyEvent(e, this);
        for (IGroupKeyListener l : mListenerList.getListeners(
            IGroupKeyListener.class)) {
            l.groupKeyTyped(gme);
        }
    }
}
