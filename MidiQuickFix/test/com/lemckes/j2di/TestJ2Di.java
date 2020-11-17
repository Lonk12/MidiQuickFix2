/*
 * TestJ2Di.java
 */
package com.lemckes.j2di;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Date;

/**
 *
 */
public class TestJ2Di
    extends javax.swing.JFrame
    implements IGroupMouseListener, IGroupMouseMotionListener,
    IGroupFocusListener, IGroupKeyListener
{
    private ICanvasScrollPane mScrollPane;
    private ICanvas mCanvas;
    private ILayer mLayer1;
    private ILayer mLayer2;
    private IGroup mOverlay;
    private TextGroup mTextGroup1;
    private TextGroup mTextGroup2;
    private Font mFont;
    private ArcGroup mArcGroup1;
    private EllipseGroup mEllipseGroup1;
    private PolyGroup mPolyGroup1;
    private Smiley2 mSmiley;
    private StarGroup mStarGroup;
    private ILine mLine1;
    private ILine mLine2;
    private IGroup mLineGroup1;
    private IRectangle mRect1;
    private IRectangle mRect2;
    private IGroup mRectGroup1;
    private double dragX;
    private double dragY;

    /**
     * Creates new form TestJ2Di
     */
    public TestJ2Di() {
        initComponents();

        mFont = new Font("Serif", Font.PLAIN, 14);

        mScrollPane = new ICanvasScrollPane();
        getContentPane().add(mScrollPane, BorderLayout.CENTER);
//        mCanvas = new ICanvas(600, 400);
        mCanvas = mScrollPane.getCanvas();
        mCanvas.setBackground(Color.LIGHT_GRAY);

//        mCanvas.setLayout(new LayerAttachLayout());

        mLayer1 = new ILayer(600, 400);
        mLayer1.setName("Layer 1");

        initRect();
        initLine();
        initText();
        initArc();
        initEllipse();
        initPoly();
        initSmiley();
        initStars();


        LayerAttachConstraints lac1 = new LayerAttachConstraints();
        lac1.setAttachments(true, true, true, true); //
        lac1.setOffsets(0, 0, 0, 0);
        lac1.setLayerIndex(0);

        mCanvas.add(mLayer1, lac1);

        mLayer2 = new ILayer(60, 400);
        mLayer2.setName("Layer 2");
        //mLayer2.setBackground(Color.WHITE);
        mLayer2.setBounds(200, 0, 60, 400);
        mLayer2.setOpaque(false);

        LayerAttachConstraints lac2 = new LayerAttachConstraints();
        lac2.setAttachments(true, true, false, false); //
        lac2.setOffsets(20, 20, 0, 0);
        lac2.setLayerIndex(10);
        mOverlay = new IGroup(mLayer2);
        IRectangle rect = new IRectangle(new Rectangle2D.Double(0, 0, 59, 359));
        rect.setPaint(Color.ORANGE);
        rect.setStroke(new BasicStroke(2.0f));
        rect.setFilled(false);
        mOverlay.add(rect);
        Color crossHatchColour = new Color(0.2f, 0.8f, 0.5f);
        for (int y = 0; y < 360; y += 10) {
            ILine line1 = new ILine(new Line2D.Double(0, y, 59, y + 10));
            line1.setPaint(crossHatchColour);
            mOverlay.add(line1);
            ILine line2 = new ILine(new Line2D.Double(59, y+5, 0, y + 15));
            line2.setPaint(crossHatchColour);
            mOverlay.add(line2);
        }
        mLayer2.add(mOverlay);
        mOverlay.addGroupFocusListener(this);
        mOverlay.addGroupMouseListener(this);
        mOverlay.addGroupMouseMotionListener(this);

        mCanvas.add(mLayer2, lac2);

        mCanvas.setSize();

//        jScrollPane1.setViewportView(mCanvas);

//        thePanel.add(mCanvas);
//        thePanel.setPreferredSize(new Dimension(600, 400));
        pack();

    }

    private void initArc() {
        mArcGroup1 = new ArcGroup(mLayer1);
        mLayer1.add(mArcGroup1);

        mArcGroup1.addGroupFocusListener(this);
        mArcGroup1.addGroupMouseListener(this);
        mArcGroup1.addGroupMouseMotionListener(this);
        mArcGroup1.addGroupKeyListener(this);
    }

    private void initEllipse() {
        mEllipseGroup1 = new EllipseGroup(mLayer1);
        mLayer1.add(mEllipseGroup1);

        mEllipseGroup1.addGroupFocusListener(this);
        mEllipseGroup1.addGroupMouseListener(this);
        mEllipseGroup1.addGroupMouseMotionListener(this);
        mEllipseGroup1.addGroupKeyListener(this);
    }

    private void initLine() {
        mLineGroup1 = new IGroup(mLayer1);
        mLine1 = new ILine(new Line2D.Double(0.0, 0.0, 600.0, 400.0));
        mLine1.setPaint(Color.GREEN);
        mLine2 = new ILine(new Line2D.Double(0.0, 400.0, 600.0, 0.0));
        mLine2.setPaint(Color.BLUE);
        mLineGroup1.add(mLine1);
        mLineGroup1.add(mLine2);
        mLayer1.add(mLineGroup1);

        mLineGroup1.addGroupFocusListener(this);
        mLineGroup1.addGroupMouseListener(this);
    }

    private void initPoly() {
        mPolyGroup1 = new PolyGroup(mLayer1);
        mLayer1.add(mPolyGroup1);

        mPolyGroup1.addGroupFocusListener(this);
        mPolyGroup1.addGroupMouseListener(this);
        mPolyGroup1.addGroupMouseMotionListener(this);
        mPolyGroup1.addGroupKeyListener(this);
    }

    private void initRect() {
        mRectGroup1 = new IGroup(mLayer1);
        mRect1 = new IRectangle(new Rectangle2D.Double(10.0, 10.0, 580.0, 380.0));
        mRect1.setPaint(Color.RED);
        mRect2 = new IRectangle(
            new Rectangle2D.Double(20.0, 20.0, 560.0, 360.00));
        mRect2.setPaint(Color.MAGENTA);
        mRect2.setUseDeviceStroke(false);
        mRectGroup1.add(mRect1);
        mRectGroup1.add(mRect2);
        mLayer1.add(mRectGroup1);

        mRectGroup1.addGroupFocusListener(this);
        mRectGroup1.addGroupMouseListener(this);
    }

    private void initText() {
        mTextGroup1 = new TextGroup(mLayer1);
        mTextGroup1.setPosition(100, 100);
        mTextGroup1.setFont(mFont.deriveFont(Font.PLAIN, 18));
        GradientPaint gp = new GradientPaint(
            0, 0, new Color(0.0f, 1.0f, 0f, 1f),
            127, 15, new Color(0.0f, 0f, 1.0f, 1f), true);
        mTextGroup1.setPaint(gp);
        mTextGroup1.setText("Sample text : abFgIjkLMopqSTxyZ.");

        mTextGroup2 = new TextGroup(mLayer1);
        mTextGroup2.setPosition(200, 200);
        mTextGroup2.setFont(mFont);
        mTextGroup2.setFixedPointSize(true);
        mTextGroup2.setText("Some fixed size text.");

        mLayer1.add(mTextGroup1);
        mLayer1.add(mTextGroup2);

        mTextGroup1.addGroupFocusListener(this);
        mTextGroup1.addGroupMouseListener(this);

        mTextGroup2.addGroupFocusListener(this);
        mTextGroup2.addGroupMouseListener(this);
    }

    private void initSmiley() {
        mSmiley = new Smiley2(mLayer1);
        mSmiley.setPosition(60, 60);
        mLayer1.add(mSmiley);

        mSmiley.addGroupFocusListener(this);
        mSmiley.addGroupMouseListener(this);
        mSmiley.addGroupMouseMotionListener(this);
        mSmiley.addGroupKeyListener(this);
    }

    private void initStars() {
        mStarGroup = new StarGroup(mLayer1);
        mLayer1.add(mStarGroup);

        mStarGroup.addGroupFocusListener(this);
        mStarGroup.addGroupMouseListener(this);
        mStarGroup.addGroupMouseMotionListener(this);
        mStarGroup.addGroupKeyListener(this);
    }

    public void groupMouseClicked(IGroupMouseEvent e) {
        // IGroup gg = e.getIGroup();
        // String s = gg.mGraphics.get(0).getClass().getSimpleName();
        // System.out.println("groupMouseClicked - " + s);
    }

    public void groupMousePressed(IGroupMouseEvent e) {
        IGroup gg = e.getIGroup();
        // String s = gg.mGraphics.get(0).getClass().getSimpleName();
        // System.out.println("groupMousePressed - " + s);
        if (gg == mOverlay) {
            mLayer2.setMouseGrab(gg);
            dragX = e.getX();
            dragY = e.getY();
        }
        if (gg == mArcGroup1
            || gg == mPolyGroup1
            || gg == mEllipseGroup1
            || gg == mSmiley) {
            mLayer1.raise(gg);
            mLayer1.setMouseGrab(gg);
            dragX = e.getX();
            dragY = e.getY();
        }
    }

    public void groupMouseReleased(IGroupMouseEvent e) {
        IGroup gg = e.getIGroup();
        String s = gg.mGraphics.get(0).getClass().getSimpleName();
        // System.out.println("groupMouseReleased - " + s);
        if (gg == mOverlay && mLayer2.getMouseGrab() == mOverlay) {
            mLayer2.setMouseGrab(null);
        }
        if (gg == mArcGroup1 && mLayer1.getMouseGrab() == mArcGroup1
            || gg == mPolyGroup1 && mLayer1.getMouseGrab() == mPolyGroup1
            || gg == mEllipseGroup1 && mLayer1.getMouseGrab() == mEllipseGroup1
            || gg == mSmiley && mLayer1.getMouseGrab() == mSmiley) {
            mLayer1.setMouseGrab(null);
        }
    }

    public void groupMouseDragged(IGroupMouseEvent e) {
        IGroup gg = e.getIGroup();
        //String s = gg.mGraphics.get(0).getClass().getSimpleName();
        if (gg == mOverlay && mLayer2.getMouseGrab() == mOverlay) {
            double x = e.getX();
            double y = e.getY();
            int dX = (int)Math.round(x - dragX);
            double dY = y - dragY;
            Rectangle r = mLayer2.getBounds();
            mLayer2.setBounds(r.x + dX, r.y, r.width, r.height);
        }
        if (gg == mArcGroup1 && mLayer1.getMouseGrab() == mArcGroup1
            || gg == mPolyGroup1 && mLayer1.getMouseGrab() == mPolyGroup1
            || gg == mEllipseGroup1 && mLayer1.getMouseGrab() == mEllipseGroup1
            || gg == mSmiley && mLayer1.getMouseGrab() == mSmiley) {
            double x = e.getX();
            double y = e.getY();
            double dX = x - dragX;
            double dY = y - dragY;
            dragX = x;
            dragY = y;
            gg.move(dX, dY);
            gg.repaint();
        }
    }

    public void groupMouseMoved(IGroupMouseEvent e) {
        IGroup gg = e.getIGroup();
        // String s = gg.mGraphics.get(0).getClass().getSimpleName();
        // System.out.println("groupMouseMoved - " + s);
    }

    public void groupFocusGained(IGroupMouseEvent e) {
        IGroup gg = e.getIGroup();
        gg.setHasFocus(true);
        gg.repaint();
        // String s = gg.mGraphics.get(0).getClass().getSimpleName();
        // System.out.println("groupFocusGained - " + s);
    }

    public void groupFocusLost(IGroupMouseEvent e) {
        IGroup gg = e.getIGroup();
        gg.setHasFocus(false);
        gg.repaint();
        // String s = gg.mGraphics.get(0).getClass().getSimpleName();
        // System.out.println("groupFocusLost - " + s);

    }

    public void groupKeyPressed(IGroupKeyEvent e) {
        IGroup gg = e.getIGroup();
        String s = gg.mGraphics.get(0).getClass().getSimpleName();
        KeyEvent kEvent = e.getEvent();
        // String key = "" + kEvent.getKeyChar() + " : " + kEvent.getKeyCode();
        // System.out.println(s + " groupKeyPressed  - " + key);
        if (gg == mArcGroup1
            || gg == mPolyGroup1
            || gg == mEllipseGroup1
            || gg == mSmiley) {
            int code = kEvent.getKeyCode();
            int dX = 0;
            int dY = 0;
            int multiplier = 1;
            if (kEvent.isShiftDown()) {
                multiplier = 10;
            }
            switch (code) {
                case KeyEvent.VK_UP:
                    dY = -1;
                    break;
                case KeyEvent.VK_DOWN:
                    dY = 1;
                    break;
                case KeyEvent.VK_LEFT:
                    dX = -1;
                    break;
                case KeyEvent.VK_RIGHT:
                    dX = 1;
                    break;
            }
            gg.move(dX * multiplier, dY * multiplier);
            gg.repaint();
        }
    }

    public void groupKeyReleased(IGroupKeyEvent e) {
        IGroup gg = e.getIGroup();
        // String s = gg.mGraphics.get(0).getClass().getSimpleName();
        // String key = e.getKeyEvent().paramString();
        // System.out.println(s + " groupKeyReleased - " + key);
    }

    public void groupKeyTyped(IGroupKeyEvent e) {
        IGroup gg = e.getIGroup();
        // String s = gg.mGraphics.get(0).getClass().getSimpleName();
        // String key = e.getKeyEvent().paramString();
        // System.out.println(s + " groupKeyTyped   - " + key);
    }

    private void timedRepaint() {
        // Check if we are running in the Swing event thread
        // boolean swingThread = SwingUtilities.isEventDispatchThread();
        // System.out.println("timedRepaint is "
        // + (swingThread ? "" : "NOT") + " running in the Swing thread.");

        long t1 = new Date().getTime();
        for (int i = 0; i < 1000; ++i) {
            mLayer1.repaint();
        }
        long t2 = new Date().getTime();
        System.out.println("1,000 repaints took "
            + (t2 - t1) + " millisecs");
    }
    private RenderingHints.Key[] hintKeys = {
        RenderingHints.KEY_ALPHA_INTERPOLATION,
        RenderingHints.KEY_ANTIALIASING,
        RenderingHints.KEY_COLOR_RENDERING,
        RenderingHints.KEY_DITHERING,
        RenderingHints.KEY_FRACTIONALMETRICS,
        RenderingHints.KEY_INTERPOLATION,
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.KEY_RENDERING,
        RenderingHints.KEY_STROKE_CONTROL
    };
    private Object[][] hintVals = {
        {
            RenderingHints.VALUE_ALPHA_INTERPOLATION_DEFAULT,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED,
            RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY
        },
        {
            RenderingHints.VALUE_ANTIALIAS_DEFAULT,
            RenderingHints.VALUE_ANTIALIAS_OFF,
            RenderingHints.VALUE_ANTIALIAS_ON
        },
        {
            RenderingHints.VALUE_COLOR_RENDER_DEFAULT,
            RenderingHints.VALUE_COLOR_RENDER_SPEED,
            RenderingHints.VALUE_COLOR_RENDER_QUALITY
        },
        {
            RenderingHints.VALUE_DITHER_DEFAULT,
            RenderingHints.VALUE_DITHER_DISABLE,
            RenderingHints.VALUE_DITHER_ENABLE
        },
        {
            RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT,
            RenderingHints.VALUE_FRACTIONALMETRICS_OFF,
            RenderingHints.VALUE_FRACTIONALMETRICS_ON
        },
        {
            RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR,
            RenderingHints.VALUE_INTERPOLATION_BILINEAR,
            RenderingHints.VALUE_INTERPOLATION_BICUBIC
        },
        {
            RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT,
            RenderingHints.VALUE_TEXT_ANTIALIAS_OFF,
            RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        },
        {
            RenderingHints.VALUE_RENDER_DEFAULT,
            RenderingHints.VALUE_RENDER_SPEED,
            RenderingHints.VALUE_RENDER_QUALITY
        },
        {
            RenderingHints.VALUE_STROKE_DEFAULT,
            RenderingHints.VALUE_STROKE_NORMALIZE,
            RenderingHints.VALUE_STROKE_PURE
        }
    };

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable()
        {
            public void run() {
                new TestJ2Di().setVisible(true);
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup6 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        buttonGroup8 = new javax.swing.ButtonGroup();
        buttonGroup9 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jLabel11 = new javax.swing.JLabel();
        defRadio1 = new javax.swing.JRadioButton();
        defRadio2 = new javax.swing.JRadioButton();
        defRadio3 = new javax.swing.JRadioButton();
        defRadio4 = new javax.swing.JRadioButton();
        defRadio5 = new javax.swing.JRadioButton();
        defRadio6 = new javax.swing.JRadioButton();
        defRadio7 = new javax.swing.JRadioButton();
        defRadio8 = new javax.swing.JRadioButton();
        speedRadio1 = new javax.swing.JRadioButton();
        speedRadio2 = new javax.swing.JRadioButton();
        speedRadio3 = new javax.swing.JRadioButton();
        speedRadio4 = new javax.swing.JRadioButton();
        speedRadio5 = new javax.swing.JRadioButton();
        speedRadio6 = new javax.swing.JRadioButton();
        speedRadio7 = new javax.swing.JRadioButton();
        speedRadio8 = new javax.swing.JRadioButton();
        qualRadio1 = new javax.swing.JRadioButton();
        qualRadio2 = new javax.swing.JRadioButton();
        qualRadio3 = new javax.swing.JRadioButton();
        qualRadio4 = new javax.swing.JRadioButton();
        qualRadio5 = new javax.swing.JRadioButton();
        qualRadio6 = new javax.swing.JRadioButton();
        qualRadio7 = new javax.swing.JRadioButton();
        qualRadio8 = new javax.swing.JRadioButton();
        qualRadio9 = new javax.swing.JRadioButton();
        speedRadio9 = new javax.swing.JRadioButton();
        defRadio9 = new javax.swing.JRadioButton();
        jLabel12 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Test J2Di");

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jLabel1.setText("Alpha");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        jLabel2.setText("AntiAlias");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel2, gridBagConstraints);

        jLabel3.setText("Colour");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel3, gridBagConstraints);

        jLabel4.setText("Dither");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel4, gridBagConstraints);

        jLabel5.setText("FracMetrics");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel5, gridBagConstraints);

        jLabel6.setText("Interp");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel6, gridBagConstraints);

        jLabel7.setText("AAText");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel7, gridBagConstraints);

        jLabel8.setText("Render");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel8, gridBagConstraints);

        jLabel9.setText("Default");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel1.add(jLabel9, gridBagConstraints);

        jLabel10.setText("Speed");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPanel1.add(jLabel10, gridBagConstraints);

        jLabel11.setText("Quality");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jPanel1.add(jLabel11, gridBagConstraints);

        buttonGroup1.add(defRadio1);
        defRadio1.setSelected(true);
        defRadio1.setToolTipText("Default");
        defRadio1.setActionCommand("0:0");
        defRadio1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio1, gridBagConstraints);

        buttonGroup2.add(defRadio2);
        defRadio2.setSelected(true);
        defRadio2.setToolTipText("Default");
        defRadio2.setActionCommand("1:0");
        defRadio2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio2, gridBagConstraints);

        buttonGroup3.add(defRadio3);
        defRadio3.setSelected(true);
        defRadio3.setToolTipText("Default");
        defRadio3.setActionCommand("2:0");
        defRadio3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio3, gridBagConstraints);

        buttonGroup4.add(defRadio4);
        defRadio4.setSelected(true);
        defRadio4.setToolTipText("Default");
        defRadio4.setActionCommand("3:0");
        defRadio4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio4, gridBagConstraints);

        buttonGroup5.add(defRadio5);
        defRadio5.setSelected(true);
        defRadio5.setToolTipText("Default");
        defRadio5.setActionCommand("4:0");
        defRadio5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio5, gridBagConstraints);

        buttonGroup6.add(defRadio6);
        defRadio6.setSelected(true);
        defRadio6.setToolTipText("Nearest Neighbour");
        defRadio6.setActionCommand("5:0");
        defRadio6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio6, gridBagConstraints);

        buttonGroup7.add(defRadio7);
        defRadio7.setSelected(true);
        defRadio7.setToolTipText("Default");
        defRadio7.setActionCommand("6:0");
        defRadio7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 7;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio7, gridBagConstraints);

        buttonGroup8.add(defRadio8);
        defRadio8.setSelected(true);
        defRadio8.setToolTipText("Default");
        defRadio8.setActionCommand("7:0");
        defRadio8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 8;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio8, gridBagConstraints);

        buttonGroup1.add(speedRadio1);
        speedRadio1.setToolTipText("Speed");
        speedRadio1.setActionCommand("0:1");
        speedRadio1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio1, gridBagConstraints);

        buttonGroup2.add(speedRadio2);
        speedRadio2.setToolTipText("Off");
        speedRadio2.setActionCommand("1:1");
        speedRadio2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio2, gridBagConstraints);

        buttonGroup3.add(speedRadio3);
        speedRadio3.setToolTipText("Speed");
        speedRadio3.setActionCommand("2:1");
        speedRadio3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio3, gridBagConstraints);

        buttonGroup4.add(speedRadio4);
        speedRadio4.setToolTipText("Disable");
        speedRadio4.setActionCommand("3:1");
        speedRadio4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio4, gridBagConstraints);

        buttonGroup5.add(speedRadio5);
        speedRadio5.setToolTipText("Off");
        speedRadio5.setActionCommand("4:1");
        speedRadio5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio5, gridBagConstraints);

        buttonGroup6.add(speedRadio6);
        speedRadio6.setToolTipText("Bilinear");
        speedRadio6.setActionCommand("5:1");
        speedRadio6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio6, gridBagConstraints);

        buttonGroup7.add(speedRadio7);
        speedRadio7.setToolTipText("Off");
        speedRadio7.setActionCommand("6:1");
        speedRadio7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio7, gridBagConstraints);

        buttonGroup8.add(speedRadio8);
        speedRadio8.setToolTipText("Speed");
        speedRadio8.setActionCommand("7:1");
        speedRadio8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio8, gridBagConstraints);

        buttonGroup1.add(qualRadio1);
        qualRadio1.setToolTipText("Quality");
        qualRadio1.setActionCommand("0:2");
        qualRadio1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio1, gridBagConstraints);

        buttonGroup2.add(qualRadio2);
        qualRadio2.setToolTipText("On");
        qualRadio2.setActionCommand("1:2");
        qualRadio2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio2, gridBagConstraints);

        buttonGroup3.add(qualRadio3);
        qualRadio3.setToolTipText("Quality");
        qualRadio3.setActionCommand("2:2");
        qualRadio3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio3, gridBagConstraints);

        buttonGroup4.add(qualRadio4);
        qualRadio4.setToolTipText("Enable");
        qualRadio4.setActionCommand("3:2");
        qualRadio4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio4, gridBagConstraints);

        buttonGroup5.add(qualRadio5);
        qualRadio5.setToolTipText("On");
        qualRadio5.setActionCommand("4:2");
        qualRadio5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio5, gridBagConstraints);

        buttonGroup6.add(qualRadio6);
        qualRadio6.setToolTipText("Bicubic");
        qualRadio6.setActionCommand("5:2");
        qualRadio6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio6, gridBagConstraints);

        buttonGroup7.add(qualRadio7);
        qualRadio7.setToolTipText("On");
        qualRadio7.setActionCommand("6:2");
        qualRadio7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio7, gridBagConstraints);

        buttonGroup8.add(qualRadio8);
        qualRadio8.setToolTipText("Quality");
        qualRadio8.setActionCommand("7:2");
        qualRadio8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio8, gridBagConstraints);

        buttonGroup9.add(qualRadio9);
        qualRadio9.setToolTipText("Pure");
        qualRadio9.setActionCommand("8:2");
        qualRadio9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        jPanel1.add(qualRadio9, gridBagConstraints);

        buttonGroup9.add(speedRadio9);
        speedRadio9.setToolTipText("Normalise");
        speedRadio9.setActionCommand("8:1");
        speedRadio9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 2;
        jPanel1.add(speedRadio9, gridBagConstraints);

        buttonGroup9.add(defRadio9);
        defRadio9.setSelected(true);
        defRadio9.setToolTipText("Default");
        defRadio9.setActionCommand("8:0");
        defRadio9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                radioClicked(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 1;
        jPanel1.add(defRadio9, gridBagConstraints);

        jLabel12.setText("Stroke");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 9;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
        jPanel1.add(jLabel12, gridBagConstraints);

        getContentPane().add(jPanel1, java.awt.BorderLayout.PAGE_START);

        jPanel2.setBackground(new java.awt.Color(204, 153, 0));
        jPanel2.setLayout(new java.awt.BorderLayout());
        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText("File");

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        setJMenuBar(menuBar);

        setSize(new java.awt.Dimension(745, 334));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void radioClicked(java.awt.event.ActionEvent evt)//GEN-FIRST:event_radioClicked
    {//GEN-HEADEREND:event_radioClicked
        String cmd = evt.getActionCommand();
        // ActionCommand is in the form of "Column:Row"
        String[] indexes = cmd.split(":");
        int key = Integer.parseInt(indexes[0]);
        int val = Integer.parseInt(indexes[1]);
        mLayer1.putRenderingHint(hintKeys[key], hintVals[key][val]);
        timedRepaint();
    }//GEN-LAST:event_radioClicked

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_exitMenuItemActionPerformed
    {
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup6;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.ButtonGroup buttonGroup8;
    private javax.swing.ButtonGroup buttonGroup9;
    private javax.swing.JRadioButton defRadio1;
    private javax.swing.JRadioButton defRadio2;
    private javax.swing.JRadioButton defRadio3;
    private javax.swing.JRadioButton defRadio4;
    private javax.swing.JRadioButton defRadio5;
    private javax.swing.JRadioButton defRadio6;
    private javax.swing.JRadioButton defRadio7;
    private javax.swing.JRadioButton defRadio8;
    private javax.swing.JRadioButton defRadio9;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButton qualRadio1;
    private javax.swing.JRadioButton qualRadio2;
    private javax.swing.JRadioButton qualRadio3;
    private javax.swing.JRadioButton qualRadio4;
    private javax.swing.JRadioButton qualRadio5;
    private javax.swing.JRadioButton qualRadio6;
    private javax.swing.JRadioButton qualRadio7;
    private javax.swing.JRadioButton qualRadio8;
    private javax.swing.JRadioButton qualRadio9;
    private javax.swing.JRadioButton speedRadio1;
    private javax.swing.JRadioButton speedRadio2;
    private javax.swing.JRadioButton speedRadio3;
    private javax.swing.JRadioButton speedRadio4;
    private javax.swing.JRadioButton speedRadio5;
    private javax.swing.JRadioButton speedRadio6;
    private javax.swing.JRadioButton speedRadio7;
    private javax.swing.JRadioButton speedRadio8;
    private javax.swing.JRadioButton speedRadio9;
    // End of variables declaration//GEN-END:variables
}
