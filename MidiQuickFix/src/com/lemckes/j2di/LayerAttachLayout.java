/*
 * LayerAttachLayout.java
 *
 * Created on December 17, 2004, 10:04 AM
 */
package com.lemckes.j2di;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.HashMap;
import javax.swing.JLayeredPane;

/**
 * A layout manager for use with JLayeredPane containers (only). <br>
 * Each side of the child layer may be attached to the corresponding
 * side of its container with an optional offset. This allows for
 * the child layers to be resized to maintain a common width and/or
 * height.
 * A free-floating layer can be created by specifying no attachments.
 * @see LayerAttachConstraints
 */
public class LayerAttachLayout implements LayoutManager2 {

    private LayerAttachConstraints mDefaultConstraints;
    private HashMap<Component, LayerAttachConstraints> mComponentConstraints;

    /** Creates a new instance of AttachLayout. */
    public LayerAttachLayout() {
        mDefaultConstraints = new LayerAttachConstraints();
        mComponentConstraints =
            new HashMap<Component, LayerAttachConstraints>(2);
    }

    public void addLayoutComponent(Component component, Object obj) {
        // System.out.println("addLayoutComponent(" + component + ", " + obj + ")");
        if (obj instanceof LayerAttachConstraints) {
            setConstraints(component, (LayerAttachConstraints)obj);
        } else if (obj != null) {
            throw new IllegalArgumentException(
                "cannot add to layout: constraints must be a LayerAttachConstraints");
        }
    }

    /**
     * Sets the constraints for the specified component in this layout.
     * @param comp the component to be modified
     * @param constraints the constraints to be applied
     */
    public void setConstraints(Component comp,
        LayerAttachConstraints constraints) {
        mComponentConstraints.put(
            comp, (LayerAttachConstraints)constraints.clone());
    }

    /**
     * Gets the constraints for the specified component. A copy of
     * the actual
     * <code>LayerAttachConstraints</code> object is returned.
     * @param comp the component to be queried
     * @return the constraint for the specified component in this
     * LayerAttachLayout; a copy of the actual constraint
     * object is returned
     */
    public LayerAttachConstraints getConstraints(Component comp) {
        LayerAttachConstraints constraints = mComponentConstraints.get(comp);

        if (constraints == null) {
            setConstraints(comp, mDefaultConstraints);
            constraints = mComponentConstraints.get(comp);
        }
        return (LayerAttachConstraints)constraints.clone();
    }

    public void addLayoutComponent(String str, Component component) {
    }

    public float getLayoutAlignmentX(Container container) {
        return 0.0f;
    }

    public float getLayoutAlignmentY(Container container) {
        return 0.0f;
    }

    public void invalidateLayout(Container container) {
        // System.out.println("invalidateLayout(Container container)");
    }

    public void layoutContainer(Container container) {
        if (!(container instanceof JLayeredPane)) {
            throw new IllegalArgumentException(
                "cannot layout : Container must be a JLayeredPane");
        }

        Component[] components = container.getComponents();

        Rectangle cntSize = new Rectangle(container.getSize());
        for (int i = components.length - 1; i >= 0; --i) {
            Component component = components[i];
            LayerAttachConstraints lac = mComponentConstraints.get(component);

            // Start with the component's current size
            Dimension d = component.getPreferredSize();
            Point p = component.getLocation();
            Rectangle r = new Rectangle(p.x, p.y, d.width, d.height);

            if (lac != null) {
                // Handle the top/bottom attachments
                if (lac.attachTop && lac.attachBottom) {
                    r.y = lac.topOffset;
                    r.height = cntSize.height - (lac.topOffset + lac.bottomOffset);
                } else if (lac.attachTop) {
                    r.y = lac.topOffset;
                } else if (lac.attachBottom) {
                    r.y = cntSize.height - (r.height + lac.bottomOffset);
                }

                // Handle the left/right attachments
                if (lac.attachLeft && lac.attachRight) {
                    r.x = lac.leftOffset;
                    r.width = cntSize.width - (lac.leftOffset + lac.rightOffset);
                } else if (lac.attachLeft) {
                    r.x = lac.leftOffset;
                } else if (lac.attachRight) {
                    r.x = cntSize.width - (r.width + lac.rightOffset);
                }

                ((JLayeredPane)container).setLayer(component, lac.layerIndex);

                // This is needed because of a bug in JLayeredPane
                // which sets the constraints to null when the layer is set.
                setConstraints(component, lac);
            }
            component.setBounds(r);
        }
    }

    public Dimension maximumLayoutSize(Container container) {
        // System.out.println("maximumLayoutSize(Container container)");
        return container.getMaximumSize();
    }

    public Dimension minimumLayoutSize(Container container) {
        // System.out.println("minimumLayoutSize(Container container)");
        return container.getMinimumSize();
    }

    public Dimension preferredLayoutSize(Container container) {
        // System.out.println("preferredLayoutSize(Container container)");
        Dimension d = new Dimension(0, 0);

        for (Component component : container.getComponents()) {
            Rectangle rect = component.getBounds();
            int xExtent = rect.x + rect.width;
            int yExtent = rect.y + rect.height;

            d.width = Math.max(d.width, xExtent);
            d.height = Math.max(d.height, yExtent);
        }
        // System.out.println("preferredLayoutSize = " + d);
        return d;
    }

    public void removeLayoutComponent(Component component) {
        // System.out.println("removeLayoutComponent(" + component + ")");
        mComponentConstraints.remove(component);
    }
}
