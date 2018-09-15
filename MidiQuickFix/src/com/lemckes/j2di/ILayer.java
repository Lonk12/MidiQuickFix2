
package com.lemckes.j2di;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import javax.swing.JComponent;

/**
 * A ILayer is a child of an ICanvas and is the container for IGroups. <br>
 * It is responsible for :
 * <ul>
 * <li>deciding which of its children to render in response to a
 * {@code paint} request.</li>
 * <li>managing the transformation between 'world coordinates'
 * and 'screen coordinates'.</li>
 * <li>handling all the required AWT events and passing them on to the
 * appropriate child IGroup.</li>
 * </ul>
 */
public class ILayer
    extends JComponent
    implements MouseListener, MouseMotionListener, KeyListener, FocusListener {

//    private ICanvas mCanvas;
    private Dimension mPrefSize;
    private IGroup mMouseGrab;
    private IGroup mKeyboardGrab;
    private IGroup mCurrentGroup;
    private boolean mHasFocus = true;
    private ArrayList<IGroup> mGroups;
    /**
     * The boundaries of the 'real world'.
     */
    private Rectangle2D.Double mWorldBounds;
    /**
     * The size of the part of the world which is currently visible.
     */
    private IDimension mWorldViewSize;
    /**
     * The size of the screen area which is displaying the current view.
     */
    private Dimension mScreenViewSize;
    /**
     * The size of the hit area for the cursor
     */
    private int mCursorHitSize = 3;
    /**
     * The relationship between a 'unit' of world space
     * and a pixel on the screen.
     */
    private double mPixPerWorldUnitX;
    private double mPixPerWorldUnitY;
    /**
     * The transform which fills this component with the whole world.
     */
    private AffineTransform mTransform;
    private RenderingHints mRenderingHints;
//    private PerformancePainter mHinter;

    /**
     * Create an ILayer with the given size, as a child of the given ICanvas.
     * The transform is initialised to the identity transform.
     * @param width  The width of the layer
     * @param height The height of the layer
     */
    public ILayer(int width, int height) {
        super();

//        mCanvas = canvas;

        setSize(width, height);

        // Start with the assumption the we can see the whole world.
        mWorldBounds = new Rectangle2D.Double(0, 0, width, height);
        mWorldViewSize = new IDimension(width, height);

        mPixPerWorldUnitX = 1.0;
        mPixPerWorldUnitY = 1.0;

        mTransform = new AffineTransform();
        mTransform.setToScale(1.0, 1.0);

        mGroups = new ArrayList<IGroup>(64);

        initRenderingHints();

        enableEventHandling();
    }

    /**
     * Enable all of this ILayer's built in awt event listeners.
     * Subsequent events will be passed on to the appropriate IGroup child.
     * @see #disableEventHandling()
     */
    public void enableEventHandling() {
        final ILayer l = this;
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                addMouseListener(l);
                addMouseMotionListener(l);
                addKeyListener(l);
                addFocusListener(l);
            }
        });
    }

    /**
     * Disable all of this ILayer's built in awt event listeners.
     * The IGroup children will not receive any further event notifications
     * until enableEventHandling is called.
     * This interface has been included specifically to allow the use of
     * popup menus. It should also be useful in implementing 'busy state'
     * behaviour.
     * @see #enableEventHandling()
     */
    public void disableEventHandling() {
        final ILayer l = this;
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                removeMouseListener(l);
                removeMouseMotionListener(l);
                removeKeyListener(l);
                removeFocusListener(l);
            }
        });
    }

    /**
     * Set the bounds of the world coordinates.
     * @param bounds The rectangle which represents the limits of the world.
     * @param update If {@code true} this layer will be repainted.
     */
    public void setWorldBounds(Rectangle2D.Double bounds, boolean update) {
        // System.out.println(getName()
        //    + " ILayer.setWorldBounds(" + bounds + ", " + update);
        mWorldBounds = bounds;
        if (update) {
            calculateTransform();
        }
    }

    /**
     * Get the bounds of this layer in world coordinates.
     * @return The rectangle that represents the bounds of this layer.
     */
    public Rectangle2D.Double getWorldBounds() {
        return mWorldBounds;
    }

    /**
     * Set the size of the visible part of the world.
     * @param viewSize The dimensions of the visible part of the world.
     * @param update   If {@code true} this layer will be repainted.
     */
    public void setWorldViewSize(IDimension viewSize, boolean update) {
        // System.out.println("ILayer.setWorldViewSize("
        //    + viewSize + ", " + update);
        mWorldViewSize = viewSize;
        if (update) {
            calculateTransform();
        }
    }

    /**
     * Set the size of the visible part of the screen. This would commonly
     * be the size of a JScrollPane's JViewport.
     * @param viewSize The dimensions of the visible part of the screen.
     * @param update   If {@code true} this layer will be repainted.
     */
    public void setScreenViewSize(Dimension viewSize, boolean update) {
        // System.out.println(getName()
        //     + " ILayer.setScreenViewSize(" + viewSize + ", " + update);
        mScreenViewSize = viewSize;
        if (update) {
            calculateTransform();
        }
    }

    /**
     * Calculate the affine transform that converts world coordinates
     * to screen coordinates and resize this layer to fit the new size.
     * The resize will cause the layer to be repainted.
     */
    private void calculateTransform() {
        ICanvas canvas = (ICanvas)getParent();
        if (canvas != null) {
            LayerAttachConstraints lac =
                ((LayerAttachLayout)canvas.getLayout()).getConstraints(this);
            if (lac.attachLeft && lac.attachRight) {
                int width = mScreenViewSize.width - (lac.leftOffset + lac.rightOffset);
                mPixPerWorldUnitX = width / mWorldViewSize.width;
            }
            if (lac.attachTop && lac.attachBottom) {
                int height = mScreenViewSize.height - (lac.topOffset + lac.bottomOffset);
                mPixPerWorldUnitY = height / mWorldViewSize.height;
            }

            mTransform.setToTranslation(-mWorldBounds.x, -mWorldBounds.y);
            mTransform.scale(mPixPerWorldUnitX, mPixPerWorldUnitY);

            // Resize our component to fit the new world size.
            setSize((int)(Math.ceil(mPixPerWorldUnitX * mWorldBounds.width)),
                (int)(Math.ceil(mPixPerWorldUnitY * mWorldBounds.height)));

            // System.out.println("ILayer.setTransform ----------------------------");
            // System.out.println("  scaleX = " + pixPerWorldUnitX);
            // System.out.println("  scaleY = " + pixPerWorldUnitY);
            // System.out.println("  width  = " + (int)(Math.ceil(pixPerWorldUnitX * mWorldBounds.width)));
            // System.out.println("  height = " + (int)(Math.ceil(pixPerWorldUnitY * mWorldBounds.height)));
            // System.out.println("transform= " + mTransform);
        }
    }

    /**
     * Get the affine transform which converts world coordinates to screen
     * coordinates.
     * @return the world to screen transform.
     */
    public AffineTransform getTransform() {
        return mTransform;
    }

    /**
     * Add a group to the end of the list of children.
     * @param g The group to be added
     */
    public void add(IGroup g) {
        insertAt(g, mGroups.size());
    }

    /**
     * Insert a group at the given index in the list of children.
     * @param g     The group to be inserted.
     * @param index The index at which the group is to be inserted
     */
    public void insertAt(IGroup g, int index) {
        int i = Math.min(Math.max(0, index), mGroups.size());
        mGroups.add(i, g);
        repaint(g.getBoundingRect().getBounds());
    }

    /**
     * Replace which ever group previously occupied
     * <code>index</code> with
     * <code>g</code>
     * @param g     The new group that should occupy
     * <code>index</code>
     * @param index The index of the group to be replaced
     * @see #indexOf(IGroup)
     */
    public void replaceAt(IGroup g, int index) {
        Rectangle2D rect = mGroups.get(index).getBoundingRect();
        mGroups.set(index, g);
        rect.add(g.getBoundingRect());
        repaint(rect.getBounds());
    }

    /**
     * Raise a group so that it becomes the frontmost child.
     * @param g The group to be raised
     */
    public void raise(IGroup g) {
        remove(g);
        add(g);
    }

    /**
     * Lower a group so that it becomes the backmost child.
     * @param g The group to be lowered
     */
    public void lower(IGroup g) {
        remove(g);
        insertAt(g, 0);
    }

    /**
     * Set the index of a group in the list of children.
     * @param g     The group to be reordered
     * @param index the new position of the group
     */
    public void setIndex(IGroup g, int index) {
        int safeIndex = Math.min(Math.max(0, index), mGroups.size());
        remove(g);
        insertAt(g, safeIndex);
    }

    /**
     * Find the index in the list of children of the given group.
     * @param g The group to find.
     * @return the index of the group or -1 if the group is not found.
     */
    public int indexOf(IGroup g) {
        return mGroups.indexOf(g);
    }

    /**
     * Get the child IGroup at the given index.
     * This assumes that the caller knows how to map indices
     * to groups.
     * @param i The index of the child group.
     * @return The child group at the given index or
     * <code>null</code>
     * if there is no such group.
     */
    public IGroup getGroup(int i) {
        if (i < 0 || i >= mGroups.size()) {
            return null;
        }
        return mGroups.get(i);
    }

    /**
     * Remove all the child groups.
     */
    public void removeAllGroups() {
        mGroups.removeAll(mGroups);

        mCurrentGroup = null;
        mMouseGrab = null;
        mKeyboardGrab = null;
        repaint();
    }

    /**
     * Remove the given child group.
     * @param g The group to be removed.
     */
    public void remove(IGroup g) {
        groupRemoved(g);
        mGroups.remove(g);
    }

    /**
     * Advise the layer that a group has been removed so that it can
     * clear any state associated with the group. <br>
     * The group may be one that is nested inside the groups which this
     * layer directly manages.
     * @param g The group that has been removed.
     */
    public void groupRemoved(IGroup g) {
        if (mCurrentGroup != null) {
            if (mCurrentGroup == g) {
                mCurrentGroup.mouseExit(null);
                mCurrentGroup = null;
            }
        }
        if (mMouseGrab == g) {
            mMouseGrab = null;
        }
        if (mKeyboardGrab == g) {
            mKeyboardGrab = null;
        }
    }

    /**
     * Set the group which will receive all mouse events.
     * This may be used to implement interactive dragging of graphics.
     * @param mouse The group which will receive all mouse events.
     */
    public void setMouseGrab(IGroup mouse) {
        mMouseGrab = mouse;
    }

    /**
     * Get the group which has grabbed mouse events.
     * @return the group which has grabbed mouse events
     */
    public IGroup getMouseGrab() {
        return mMouseGrab;
    }

    /**
     * Set the group which will receive all keyboard events.
     * @param keyboard The group which will receive all keyboard events.
     */
    public void setKeyboardGrab(IGroup keyboard) {
        // System.out.println("ILayer.setKeyboardGrab(IGroup keyboard)");

        mKeyboardGrab = keyboard;
    }

    /**
     * Get the group which has grabbed keyboard events.
     * @return the group which has grabbed keyboard events
     */
    public IGroup getKeyboardGrab() {
        return mKeyboardGrab;
    }

    /**
     * Get the current group. This is generally the group which contains the
     * mouse pointer.
     * @return the current group
     */
    public IGroup getCurrentGroup() {
        return mCurrentGroup;
    }

    /**
     * Set which group is current.
     * @param g The group to be made current
     */
    public void setCurrentGroup(IGroup g) {
        mCurrentGroup = g;
    }

    /**
     * Get the layer's minimum size. In fact an ILayer has only one size
     * which is returned as its minimum, preferred and maximum.
     * @return the size of the layer
     */
    @Override
    public Dimension getMinimumSize() {
        return mPrefSize;
    }

    /**
     * Get the layer's preferred size. In fact an ILayer has only one size
     * which is returned as its minimum, preferred and maximum.
     * @return the layer's preferred size
     */
    @Override
    public Dimension getPreferredSize() {
        return mPrefSize;
    }

    /**
     * Get the layer's maximum size. In fact an ILayer has only one size
     * which is returned as its minimum, preferred and maximum.
     * @return the layer's maximum size
     */
    @Override
    public Dimension getMaximumSize() {
        return mPrefSize;
    }

    @Override
    public void setSize(int width, int height) {
        // System.out.println(getName()
        //     + " ILayer.setSize(" + width + ", " + height + ")");
        mPrefSize = new Dimension(width, height);
        super.setSize(width, height);

        // Try resizing the canvas as well ....
        ICanvas canvas = (ICanvas)getParent();
        if (canvas != null) {
            canvas.setSize();
        }
    }

    @Override
    public void setSize(Dimension d) {
        // System.out.println("ILayer.setSize(Dimension d)");
        setSize(d.width, d.height);
    }

    @Override
    public void setBounds(int x, int y, int width, int height) {
        // System.out.println("ILayer.setBounds was " + getBounds());
        // System.out.println("ILayer.setBounds(" + x + ", " + y + ", "
        //     + width + ", " + height + ")");
        mPrefSize = new Dimension(width, height);
        super.setBounds(x, y, width, height);
    }

    /**
     * Set up the default rendering hints. The defaults are
     * <ul>
     * <li>TEXT_ANTIALIASING - OFF</li>
     * <li>FRACTIONALMETRICS - OFF</li>
     * <li>RENDERING - RENDER_SPEED</li>
     * </ul>
     */
    private void initRenderingHints() {
        mRenderingHints = new RenderingHints(null);

        mRenderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING,
            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        mRenderingHints.put(RenderingHints.KEY_FRACTIONALMETRICS,
            RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        mRenderingHints.put(RenderingHints.KEY_RENDERING,
            RenderingHints.VALUE_RENDER_SPEED);
    }

    /**
     * Put the given key/value pair into the rendering hints map.
     * @param key   The rendering hint key
     * @param value The rendering hint value
     * @see java.awt.RenderingHints
     */
    public void putRenderingHint(Object key, Object value) {
        mRenderingHints.put(key, value);
    }

    /**
     * Clear all the rendering hints.
     */
    public void clearRenderingHints() {
        mRenderingHints.clear();
    }

    /**
     * Calls
     * <code>paint</code> for all the child groups that intersect the
     * currently visible part of the world.
     * Invoked by Swing to draw components. Applications should not invoke
     * paint directly, but should instead use the repaint method to schedule
     * the component for redrawing.
     * @param g the Graphics context in which to paint
     */
    @Override
    public void paint(Graphics g) {
        // System.out.println("ILayer.paint(Graphics g) - "
        //     + this.getClass().getName());

        assert javax.swing.SwingUtilities.isEventDispatchThread();

        Graphics2D g2 = (Graphics2D)g;
        g2.setRenderingHints(mRenderingHints);

        // Save the graphic's current transform
        AffineTransform savedAT = g2.getTransform();

        Rectangle clip;
        clip = g2.getClipBounds(new Rectangle(0, 0, 0, 0));

        // Transform the clip to the world coordinates.
        AffineTransform at;
        try {
            at = mTransform.createInverse();
        } catch (java.awt.geom.NoninvertibleTransformException nte) {
            // System.out.println("ILayer can not invert the world transform!");
            // nte.printStackTrace();
            return;
        }
        Rectangle2D worldClip = at.createTransformedShape(clip).getBounds2D();

        g2.transform(mTransform);
        //g2.setRenderingHints(mRenderingHints);


        // System.out.println(hashCode() + "------------ paint() Really rendering the image.");
        // System.out.println("                 bounds = " + getBounds());
        // System.out.println("        saved transform = " + savedAT);
        // System.out.println("             paint clip = " + clip);
        // System.out.println("              transform = " + mTransform);
        // System.out.println("      inverse transform = " + at);
        // System.out.print(  "             world clip = ");
        // java.awt.geom.PathIterator pi = worldClip.getPathIterator(null);
        // while (!pi.isDone())
        // {
        //     double coords[] = new double[6];
        //     int type = pi.currentSegment(coords);
        //     System.out.print(type + "," + coords[0] + "," + coords[1] + " ");
        //     pi.next();
        // }
        // System.out.println("");

        // Date d1 = new Date();

        if (isOpaque()) {
            g2.setColor(getBackground());
            g2.fill(worldClip);
        }

        for (IGroup grp : mGroups) {
            if (grp.getBoundingRect().intersects(worldClip)) {
                grp.paint(g2, worldClip);
            }
        }

        // Restore the previous transform
        g2.setTransform(savedAT);
    }

    /**
     * Transform the clip rectangle to screen co-ords
     * and let the layer component handle the repaint.
     * @param clip
     */
    public void repaint(Rectangle2D clip) {
        Rectangle r =
            getTransform().
            createTransformedShape(clip).
            getBounds();
        super.repaint(r);
    }

    /**
     * Set the size of the cursor hit area in pixels. This defines the square
     * which is tested for intersection with groups when determining the
     * 'current' group.
     * @param size The hit area size. Use an odd number to ensure a
     * symmetrical area around the mouse pointer.
     */
    public void setCursorHitSize(int size) {
        mCursorHitSize = size;
    }

    /**
     * Find the child group that will respond to events at
     * the given screen location. <br>
     * The location is taken as the centre of the {@code mCursorHitSize}
     * square of pixels which is tested for intersection with this layer's
     * children.<br>
     * The search is performed from the highest indexed child
     * to the lowest which results in the 'frontmost' of overlapping
     * children being returned. <br>
     * <em>Note</em> that, in the case of nested groups, the returned group
     * may not be a direct child of this layer.
     *
     * @param x The x screen coordinate
     * @param y The y screen coordinate
     * @return The front-most group at the location or {@code null} if none was
     * found.
     * @see com.lemckes.j2di.IGroup#getEventHandler
     */
    public IGroup findGroup(int x, int y) {
        // System.out.println("ILayer.findGroup(int x, int y)");

        // Set the cursor hit area to a mCursorHitSize pixel square
        // converted to world coordinates.
        double tX = (x - mCursorHitSize / 2) / mTransform.getScaleX();
        double tY = (y - mCursorHitSize / 2) / mTransform.getScaleY();
        double tW = mCursorHitSize / mTransform.getScaleX();
        double tH = mCursorHitSize / mTransform.getScaleY();

        Rectangle2D.Double pixelCursor = new Rectangle2D.Double(tX, tY, tW, tH);

        IGroup found = null;
        // A group with a higher index appears in front of other groups
        // so start the search at the highest group.
        for (int i = mGroups.size() - 1; i >= 0; --i) {
            IGroup g = mGroups.get(i);
            found = g.getEventHandler(pixelCursor);
            if (found != null) {
                break;
            }
        }
        return found;
    }

    /**
     * Find the group that will respond to the given mouse event.
     * @param evt The mouse event.
     * @return the group that will respond to the given event.
     */
    private IGroup findMouseGroup(MouseEvent evt) {
        // If there is a currently "grabbed" group just return it.
        if (mMouseGrab != null) {
            return mMouseGrab;
        }

        IGroup g = findGroup(evt.getX(), evt.getY());
        if (g != mCurrentGroup) {
            if (mCurrentGroup != null) {
                mCurrentGroup.mouseExit(evt);
            }
            mCurrentGroup = g;
            if (g != null) {
                g.mouseEnter(evt);
            }
        }
        return g;
    }

    /**
     * Find the group which will respond to key events.
     * @return the group which will respond to key events.
     */
    private IGroup findKeyboardGroup() {
        // If there is a currently "grabbed" group just return it.
        if (mKeyboardGrab != null) {
            return mKeyboardGrab;
        } else {
            return mCurrentGroup;
        }
    }

    ////////////////////////////////////////////////////////////////
    //
    // Listener interface implementation
    //
    public void mouseEntered(MouseEvent e) {
        requestFocusInWindow(); // So that we get key events.

        // IGroup g = findMouseGroup(e);
    }

    public void mouseExited(MouseEvent e) {
        if (mCurrentGroup != null && mMouseGrab == null) {
            mCurrentGroup.mouseExit(e);
            mCurrentGroup = null;
        }
    }

    public void mouseClicked(MouseEvent e) {
        if (!mHasFocus) {
            requestFocusInWindow();
        }

        IGroup g = findMouseGroup(e);
        if (g != null) {
            g.mouseClicked(e);
        }
    }

    public void mousePressed(MouseEvent e) {
        if (!mHasFocus) {
            requestFocusInWindow();
        }

        IGroup g = findMouseGroup(e);
        if (g != null) {
            g.mouseDown(e);
        }
    }

    public void mouseReleased(MouseEvent e) {
        if (mHasFocus || mMouseGrab != null) {
            IGroup g = findMouseGroup(e);
            if (g != null) {
                g.mouseUp(e);
            }
        }
    }

    public void mouseDragged(MouseEvent e) {
        if (mHasFocus || mMouseGrab != null) {
            IGroup g = findMouseGroup(e);
            if (g != null) {
                g.mouseDrag(e);
            }
        }
    }

    public void mouseMoved(MouseEvent e) {
        IGroup g = findMouseGroup(e);
        if (g != null) {
            g.mouseMove(e);
        }
    }

    public void keyPressed(KeyEvent e) {
        if (mHasFocus) {
            IGroup g = findKeyboardGroup();
            if (g != null) {
                g.keyDown(e);
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        if (mHasFocus) {
            IGroup g = findKeyboardGroup();
            if (g != null) {
                g.keyUp(e);
            }
        }
    }

    public void keyTyped(KeyEvent e) {
        if (mHasFocus) {
            IGroup g = findKeyboardGroup();
            if (g != null) {
                g.keyTyped(e);
            }
        }
    }

    public void focusGained(FocusEvent e) {
        mHasFocus = true;
    }

    public void focusLost(FocusEvent e) {
        mHasFocus = false;
    }
}
