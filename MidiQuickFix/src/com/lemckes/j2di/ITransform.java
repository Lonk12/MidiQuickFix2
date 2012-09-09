package com.lemckes.j2di;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

/**
 *
 */
public class ITransform
    extends AffineTransform {

    @Override
    public Object clone() {
        return super.clone();
    }

    @Override
    public void concatenate(AffineTransform Tx) {
        super.concatenate(Tx);
    }

    @Override
    public AffineTransform createInverse() throws
        NoninvertibleTransformException {
        return super.createInverse();
    }

    @Override
    public Shape createTransformedShape(Shape pSrc) {
        return super.createTransformedShape(pSrc);
    }

    @Override
    public Point2D deltaTransform(Point2D ptSrc, Point2D ptDst) {
        return super.deltaTransform(ptSrc, ptDst);
    }

    @Override
    public void deltaTransform(double[] srcPts, int srcOff, double[] dstPts,
        int dstOff, int numPts) {
        super.deltaTransform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public double getDeterminant() {
        return super.getDeterminant();
    }

    @Override
    public void getMatrix(double[] flatmatrix) {
        super.getMatrix(flatmatrix);
    }

    @Override
    public double getScaleX() {
        return super.getScaleX();
    }

    @Override
    public double getScaleY() {
        return super.getScaleY();
    }

    @Override
    public double getShearX() {
        return super.getShearX();
    }

    @Override
    public double getShearY() {
        return super.getShearY();
    }

    @Override
    public double getTranslateX() {
        return super.getTranslateX();
    }

    @Override
    public double getTranslateY() {
        return super.getTranslateY();
    }

    @Override
    public int getType() {
        return super.getType();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public Point2D inverseTransform(Point2D ptSrc, Point2D ptDst)
        throws NoninvertibleTransformException {
        return super.inverseTransform(ptSrc, ptDst);
    }

    @Override
    public void inverseTransform(double[] srcPts, int srcOff, double[] dstPts,
        int dstOff, int numPts)
        throws NoninvertibleTransformException {
        super.inverseTransform(srcPts, srcOff, dstPts, dstOff, numPts);
    }

    @Override
    public void invert() throws NoninvertibleTransformException {
        super.invert();
    }

    @Override
    public boolean isIdentity() {
        return super.isIdentity();
    }

    @Override
    public void preConcatenate(AffineTransform Tx) {
        super.preConcatenate(Tx);
    }

    @Override
    public void quadrantRotate(int numquadrants) {
        super.quadrantRotate(numquadrants);
    }

    @Override
    public void quadrantRotate(int numquadrants, double anchorx, double anchory) {
        super.quadrantRotate(numquadrants, anchorx, anchory);
    }

    @Override
    public void rotate(double theta) {
        super.rotate(theta);
    }

    @Override
    public void rotate(double theta, double anchorx, double anchory) {
        super.rotate(theta, anchorx, anchory);
    }

    @Override
    public void rotate(double vecx, double vecy) {
        super.rotate(vecx, vecy);
    }

    @Override
    public void rotate(double vecx, double vecy, double anchorx, double anchory) {
        super.rotate(vecx, vecy, anchorx, anchory);
    }

    @Override
    public void scale(double sx, double sy) {
        super.scale(sx, sy);
    }

    @Override
    public void setToIdentity() {
        super.setToIdentity();
    }

    @Override
    public void setToQuadrantRotation(int numquadrants) {
        super.setToQuadrantRotation(numquadrants);
    }

    @Override
    public void setToQuadrantRotation(int numquadrants, double anchorx,
        double anchory) {
        super.setToQuadrantRotation(numquadrants, anchorx, anchory);
    }

    @Override
    public void setToRotation(double theta) {
        super.setToRotation(theta);
    }

    @Override
    public void setToRotation(double theta, double anchorx, double anchory) {
        super.setToRotation(theta, anchorx, anchory);
    }

    @Override
    public void setToRotation(double vecx, double vecy) {
        super.setToRotation(vecx, vecy);
    }

    @Override
    public void setToRotation(double vecx, double vecy, double anchorx,
        double anchory) {
        super.setToRotation(vecx, vecy, anchorx, anchory);
    }

    @Override
    public void setToScale(double sx, double sy) {
        super.setToScale(sx, sy);
    }

    @Override
    public void setToShear(double shx, double shy) {
        super.setToShear(shx, shy);
    }

    @Override
    public void setToTranslation(double tx, double ty) {
        super.setToTranslation(tx, ty);
    }

    @Override
    public void setTransform(AffineTransform Tx) {
        super.setTransform(Tx);
    }

    @Override
    public void setTransform(double m00, double m10, double m01, double m11,
        double m02, double m12) {
        super.setTransform(m00, m10, m01, m11, m02, m12);
    }

    @Override
    public void shear(double shx, double shy) {
        super.shear(shx, shy);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public Point2D transform(Point2D ptSrc, Point2D ptDst) {
        System.out.println("transform point " + ptSrc.getX() + ", " + ptSrc.getY());
        System.out.println("       to point " + ptDst.getX() + ", " + ptDst.getY());
        super.transform(ptSrc, ptDst);
        System.out.println("          super " + ptDst.getX() + ", " + ptDst.getY());
//        if (ptDst == null) {
//            if (ptSrc instanceof Point2D.Double) {
//                ptDst = new Point2D.Double();
//            } else {
//                ptDst = new Point2D.Float();
//            }
//        }
        // Copy source coords into local variables in case src == dst
        double x = ptDst.getX();
        double y = ptDst.getY();
        ptDst.setLocation(x * 0.5, y * 0.5);
        System.out.println("          final " + ptDst.getX() + ", " + ptDst.getY());
        return ptDst;
    }

    @Override
    public void transform(Point2D[] ptSrc, int srcOff, Point2D[] ptDst,
        int dstOff, int numPts) {
        System.out.println("transform(2D, 2D, " + numPts + ")");
//        super.transform(ptSrc, srcOff, ptDst, dstOff, numPts);
        for (int i = 0; i < numPts; ++i) {
            transform(ptSrc[i + srcOff], ptDst[i + dstOff]);
        }
    }

    @Override
    public void transform(float[] srcPts, int srcOff, float[] dstPts, int dstOff,
        int numPts) {
        System.out.println("transform(f, f, " + numPts + ")");
//        super.transform(srcPts, srcOff, dstPts, dstOff, numPts);
        for (int i = 0; i < numPts; i += 2) {
            Point2D.Float ptSrc =
                new Point2D.Float(srcPts[i + srcOff], srcPts[i + srcOff + 1]);
            Point2D.Float ptDst =
                new Point2D.Float(dstPts[i + dstOff], dstPts[i + dstOff + 1]);
            transform(ptSrc, ptDst);
        }
    }

    @Override
    public void transform(double[] srcPts, int srcOff, double[] dstPts,
        int dstOff, int numPts) {
        System.out.println("transform(d, d, " + numPts + ")");
//        super.transform(srcPts, srcOff, dstPts, dstOff, numPts);
        for (int i = 0; i < numPts; i += 2) {
            Point2D.Double ptSrc =
                new Point2D.Double(srcPts[i + srcOff], srcPts[i + srcOff + 1]);
            Point2D.Double ptDst =
                new Point2D.Double(dstPts[i + dstOff], dstPts[i + dstOff + 1]);
            transform(ptSrc, ptDst);
        }
    }

    @Override
    public void transform(float[] srcPts, int srcOff, double[] dstPts,
        int dstOff, int numPts) {
        System.out.println("transform(f, d, " + numPts + ")");
//        super.transform(srcPts, srcOff, dstPts, dstOff, numPts);
        for (int i = 0; i < numPts; i += 2) {
            Point2D.Float ptSrc =
                new Point2D.Float(srcPts[i + srcOff], srcPts[i + srcOff + 1]);
            Point2D.Double ptDst =
                new Point2D.Double(dstPts[i + dstOff], dstPts[i + dstOff + 1]);
            transform(ptSrc, ptDst);
        }
    }

    @Override
    public void transform(double[] srcPts, int srcOff, float[] dstPts,
        int dstOff, int numPts) {
        System.out.println("transform(d, f, " + numPts + ")");
//        super.transform(srcPts, srcOff, dstPts, dstOff, numPts);
        for (int i = 0; i < numPts; i += 2) {
            Point2D.Double ptSrc =
                new Point2D.Double(srcPts[i + srcOff], srcPts[i + srcOff + 1]);
            Point2D.Float ptDst =
                new Point2D.Float(dstPts[i + dstOff], dstPts[i + dstOff + 1]);
            transform(ptSrc, ptDst);
        }
    }

    @Override
    public void translate(double tx, double ty) {
        super.translate(tx, ty);
    }
}
