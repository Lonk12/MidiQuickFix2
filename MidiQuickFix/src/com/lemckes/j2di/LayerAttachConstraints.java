/*
 * LayerAttachConstraints.java
 *
 * Created on December 17, 2004, 10:04 AM
 */
package com.lemckes.j2di;

/**
 * The constraints for a LayerAttachLayout.
 * <p/>
 * @see LayerAttachConstraints
 */
public class LayerAttachConstraints implements Cloneable {

    public boolean attachTop;
    public boolean attachBottom;
    public boolean attachLeft;
    public boolean attachRight;
    public int topOffset;
    public int bottomOffset;
    public int leftOffset;
    public int rightOffset;
    public int layerIndex;

    /** Creates a new instance of LayerAttachConstraints. */
    public LayerAttachConstraints() {
        attachTop = true;
        attachBottom = true;
        attachLeft = true;
        attachRight = true;

        topOffset = 0;
        bottomOffset = 0;
        leftOffset = 0;
        rightOffset = 0;

        layerIndex = 0;
    }

    /**
     * Set which sides the layer is attached to.
     * <p/>
     * @param top Attach the layer to the top if
     * <code>true</code>
     * @param bottom Attach the layer to the bottom if
     * <code>true</code>
     * @param left Attach the layer to the left if
     * <code>true</code>
     * @param right Attach the layer to the right if
     * <code>true</code>
     */
    public void setAttachments(
        boolean top, boolean bottom,
        boolean left, boolean right) {
        attachTop = top;
        attachBottom = bottom;
        attachLeft = left;
        attachRight = right;
    }

    /**
     * Set the offsets of the layer from its attached sides.
     * <p/>
     * @param top Top offset
     * @param bottom Bottom offset
     * @param left Left offset
     * @param right Right offset
     */
    public void setOffsets(int top, int bottom, int left, int right) {
        topOffset = top;
        bottomOffset = bottom;
        leftOffset = left;
        rightOffset = right;
    }

    /**
     * Sets the layer index where a high index puts the layer in front of
     * lower indices.
     * <p/>
     * @param layer The layer index.
     */
    public void setLayerIndex(int layer) {
        layerIndex = layer;
    }

    /**
     * Creates a copy of this LayerAttachConstraint.
     * <p/>
     * @return a copy of this LayerAttachConstraint
     */
    @Override
    public Object clone() {
        try {
            LayerAttachConstraints c = (LayerAttachConstraints)super.clone();
            return c;
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError();
        }
    }
}
