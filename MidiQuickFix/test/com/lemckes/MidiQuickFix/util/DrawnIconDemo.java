package com.lemckes.MidiQuickFix.util;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import java.awt.Color;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author john
 */
public class DrawnIconDemo
    extends javax.swing.JFrame
{

    /**
     * An Icon that draws itself using a Path2D.Float.
     */
//    class DemoDrawnIcon
//        implements Icon
//    {
//
//        private Path2D.Float mPath;
//        private Paint mFillColour = Color.GRAY;
//        private Paint mStrokeColour = Color.BLACK;
//        private boolean mFilled;
//        private boolean mStroked;
//
//        /**
//         * Create a new instance of DrawnIcon with the default shape
//         */
//        public DemoDrawnIcon() {
//            this(new Path2D.Float(new Rectangle2D.Float(0.3f, 0.3f, 0.4f, 0.4f)));
//        }
//
//        /**
//         * Create a new instance of DrawnIcon with the given shape
//         *
//         * @param shape the Path2D used to draw this icon
//         */
//        public DemoDrawnIcon(Path2D.Float shape) {
//            this.mPath = shape;
//            mFilled = true;
//            mStroked = true;
//        }
//
//        @Override
//        public int getIconHeight() {
//            return 1000;
//        }
//
//        @Override
//        public int getIconWidth() {
//            return 1000;
//        }
//
//        @Override
//        public void paintIcon(Component component, Graphics g, int x, int y) {
//            Graphics2D g2 = (Graphics2D)g;
//            AffineTransform savedAT = g2.getTransform();
//            float xScale = component.getWidth();
//            float yScale = component.getHeight();
//
//            // Maintain a square aspect ratio for the icon.
//            float scale = Math.min(xScale, yScale);
//            // Set the scale to give the smallest dimension a logical size of 1.0
//            AffineTransform at
//                = AffineTransform.getScaleInstance(scale, scale);
//
//            // Translate either X or Y to centre the icon in the largest dimension
//            float xTrans = xScale > yScale ? (xScale - yScale) / (2 * yScale) : 0;
//            float yTrans = yScale > xScale ? (yScale - xScale) / (2 * xScale) : 0;
//
//            at.translate(xTrans, yTrans);
//
//            g2.setRenderingHint(
//                RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//            g2.transform(at);
//
//            if (mFilled) {
//                drawFill(g2, component);
//            }
//
//            if (mStroked) {
//                drawStroke(g2, component, scale);
//            }
//
//            g2.setTransform(savedAT);
//        }
//
//        private void drawFill(Graphics2D g2, Component component) {
//            g2.setPaint(component.isEnabled() ? mFillColour : Color.lightGray);
//            g2.fill(mPath);
//        }
//
//        private void drawStroke(Graphics2D g2, Component component, float scale) {
//            g2.setPaint(component.isEnabled() ? mStrokeColour : Color.darkGray);
//            float strokeWidth = getStrokeWidth(scale);
//            g2.setStroke(new BasicStroke(strokeWidth / scale));
//            g2.draw(mPath);
//        }
//
//        public float getStrokeWidth(float scale) {
//            float strokeWidth
//                = scale < 16 ? 0.5f
//                    : scale < 64 ? 0.5f + ((scale - 16) / 48) * 0.5f
//                        : scale < 256 ? 1.0f + ((scale - 64) / 192) * 1.0f
//                            : scale < 1024 ? 2.0f + ((scale - 256) / 768) * 2.0f
//                                : 4;
//            // System.out.println("scale=" + scale + " : width=" + strokeWidth);
//            return strokeWidth;
//        }
//
//        public Path2D.Float getPath() {
//            return mPath;
//        }
//
//        public void setPath(Path2D.Float path) {
//            this.mPath = path;
//        }
//
//        public Paint getFillColour() {
//            return mFillColour;
//        }
//
//        public void setFillColour(Paint colour) {
//            this.mFillColour = colour;
//        }
//
//        public Paint getStrokeColour() {
//            return mStrokeColour;
//        }
//
//        public void setStrokeColour(Paint strokeColour) {
//            this.mStrokeColour = strokeColour;
//        }
//
//        public boolean isFilled() {
//            return mFilled;
//        }
//
//        public void setFilled(boolean filled) {
//            this.mFilled = filled;
//        }
//
//        public boolean isStroked() {
//            return mStroked;
//        }
//
//        public void setStroked(boolean stroked) {
//            this.mStroked = stroked;
//        }
//    }
    private javax.swing.JToggleButton mLoopButton;
    private javax.swing.JButton mPauseButton;
    private javax.swing.JButton mPlayButton;
    private javax.swing.JButton mRecordButton;
    private javax.swing.JButton mRewindButton;
    private javax.swing.JButton mStopButton;

    private transient DrawnIcon mLoopIcon;
    private transient DrawnIcon mPauseIcon;
    private transient DrawnIcon mPlayIcon;
    private transient DrawnIcon mRecordIcon;
    private transient DrawnIcon mRewindIcon;
    private transient DrawnIcon mStopIcon;

    public DrawnIconDemo() {
        initComponents();
        createButtons();
        createIcons();
    }

    final void createIcons() {

        /* Loop Icon */
 /* A complex multi-segment shape */
        Path2D.Float loopOuterPath = new Path2D.Float();
        loopOuterPath.moveTo(0.55f, 0.30f);
        loopOuterPath.lineTo(0.55f, 0.20f);
        loopOuterPath.lineTo(0.65f, 0.30f);
        loopOuterPath.lineTo(0.70f, 0.30f);
        loopOuterPath.curveTo(0.95f, 0.30f, 0.95f, 0.70f, 0.70f, 0.70f);
        loopOuterPath.lineTo(0.45f, 0.70f);
        loopOuterPath.lineTo(0.45f, 0.80f);
        loopOuterPath.lineTo(0.35f, 0.70f);
        loopOuterPath.lineTo(0.30f, 0.70f);
        loopOuterPath.curveTo(0.05f, 0.70f, 0.05f, 0.30f, 0.30f, 0.30f);
        loopOuterPath.closePath();
        Area loopArea = new Area(loopOuterPath);

        Path2D.Float loopInnerPath = new Path2D.Float();
        loopInnerPath.moveTo(0.55f, 0.40f);
        loopInnerPath.lineTo(0.55f, 0.50f);
        loopInnerPath.lineTo(0.65f, 0.40f);
        loopInnerPath.lineTo(0.70f, 0.40f);
        loopInnerPath.curveTo(0.80f, 0.40f, 0.80f, 0.60f, 0.70f, 0.60f);
        loopInnerPath.lineTo(0.45f, 0.60f);
        loopInnerPath.lineTo(0.45f, 0.50f);
        loopInnerPath.lineTo(0.35f, 0.60f);
        loopInnerPath.lineTo(0.30f, 0.60f);
        loopInnerPath.curveTo(0.20f, 0.60f, 0.20f, 0.40f, 0.30f, 0.40f);
        loopInnerPath.closePath();
        Area loopHoleArea = new Area(loopInnerPath);

        loopArea.subtract(loopHoleArea);

        Path2D.Float loopPath = new Path2D.Float();
        loopPath.append(loopArea.getPathIterator(null), false);
        mLoopIcon = new DrawnIcon(mLoopButton, loopPath);
        mLoopIcon.setFillColour(Color.BLUE);
        mLoopButton.setIcon(mLoopIcon);

        /* Pause Icon */
 /* A simple multi-segment shape */
        Path2D.Float pausePath
            = new Path2D.Float(new Rectangle2D.Float(0.2f, 0.2f, 0.2f, 0.6f));
        pausePath.append(new Rectangle2D.Float(0.6f, 0.2f, 0.2f, 0.6f), false);
        mPauseIcon = new DrawnIcon(mPauseButton, pausePath);
        mPauseIcon.setFillColour(Color.YELLOW);
        mPauseButton.setIcon(mPauseIcon);

        /* Play Icon */
 /* A polygonal shape */
        Path2D.Float playPath = new Path2D.Float();
        playPath.moveTo(0.2f, 0.2f);
        playPath.lineTo(0.3f, 0.2f);
        playPath.lineTo(0.8f, 0.5f);
        playPath.lineTo(0.3f, 0.8f);
        playPath.lineTo(0.2f, 0.8f);
        playPath.closePath();
        mPlayIcon = new DrawnIcon(mPlayButton, playPath);
        mPlayIcon.setFillColour(Color.GREEN);
        mPlayButton.setIcon(mPlayIcon);

        /* Record Icon */
 /* A single circle */
        Path2D.Float recordPath
            = new Path2D.Float(new Ellipse2D.Float(0.2f, 0.2f, 0.6f, 0.6f));
        mRecordIcon = new DrawnIcon(mRecordButton, recordPath);
        mRecordIcon.setFillColour(Color.RED);
        mRecordButton.setIcon(mRecordIcon);
        // Maybe later ...
        //GradientPaint paint = new GradientPaint(0, 0, Color.RED, 1, 1, Color.GREEN);
        //mRecordIcon.setFillColour(paint);
        // Maybe later ...
        //BufferedImage txtr = new BufferedImage(2, 2, BufferedImage.TYPE_BYTE_BINARY);
        //txtr.setRGB(0, 0, Color.WHITE.getRGB());
        ////txtr.setRGB(0, 1, Color.WHITE.getRGB());
        ////txtr.setRGB(1, 0, Color.WHITE.getRGB());
        //txtr.setRGB(1, 1, Color.WHITE.getRGB());
        //Rectangle2D.Float anchor = new Rectangle2D.Float(0, 0, 0.02f, 0.02f);
        //TexturePaint tex = new TexturePaint(txtr, anchor);
        // mRecordIcon.setFillColour(tex);


        /* Rewind Icon */
 /* A simple shape plus polygon */
        Path2D.Float rewindPath
            = new Path2D.Float(new Rectangle2D.Float(0.2f, 0.2f, 0.1f, 0.6f));
        rewindPath.moveTo(0.3f, 0.5f);
        rewindPath.lineTo(0.8f, 0.2f);
        rewindPath.lineTo(0.8f, 0.8f);
        rewindPath.closePath();
        mRewindIcon = new DrawnIcon(mRewindButton, rewindPath);
        mRewindIcon.setFillColour(Color.CYAN);
        mRewindIcon.setStrokeColour(Color.YELLOW);
        mRewindButton.setIcon(mRewindIcon);

        /* Stop Icon */
 /* A simple square shape */
        Path2D.Float stopPath
            = new Path2D.Float(new Rectangle2D.Float(0.2f, 0.2f, 0.6f, 0.6f));
        mStopIcon = new DrawnIcon(mStopButton, stopPath);
        mStopIcon.setFillColour(new Color(1.0f, 0.5f, 0.2f)); // ORANGE
        mStopButton.setIcon(mStopIcon);

    }

    final void createButtons() {
        mRewindButton = new javax.swing.JButton();
        mRewindButton.setDefaultCapable(false);
        mRewindButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        buttonPanel.add(mRewindButton);

        mPlayButton = new javax.swing.JButton();
        mPlayButton.setDefaultCapable(false);
        mPlayButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        buttonPanel.add(mPlayButton);

        mPauseButton = new javax.swing.JButton();
        mPauseButton.setDefaultCapable(false);
        mPauseButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        buttonPanel.add(mPauseButton);

        mStopButton = new javax.swing.JButton();
        mStopButton.setDefaultCapable(false);
        mStopButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        buttonPanel.add(mStopButton);

        mRecordButton = new javax.swing.JButton();
        mRecordButton.setDefaultCapable(false);
        mRecordButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        buttonPanel.add(mRecordButton);

        mLoopButton = new javax.swing.JToggleButton();
        mLoopButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        mLoopButton.addChangeListener((javax.swing.event.ChangeEvent evt) -> {
            mRecordButton.setEnabled(!mLoopButton.isSelected());
        });
        buttonPanel.add(mLoopButton);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBounds(new java.awt.Rectangle(0, 0, 400, 120));
        setSize(new java.awt.Dimension(400, 120));

        buttonPanel.setMinimumSize(new java.awt.Dimension(0, 0));
        buttonPanel.setPreferredSize(new java.awt.Dimension(360, 60));
        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 1, 1));
        getContentPane().add(buttonPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('f');
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

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        
        String preferredLaf = "Georgia";
        String[] lafName = {
            preferredLaf,
            "FlatLaf Light",
            "FlatLaf Dark",
            "Nimbus",
            "Metal"};

        FlatLightLaf.installLafInfo();
        FlatDarkLaf.installLafInfo();
        boolean found = false;
        for (int name = 0; (name < lafName.length) && !found; ++name) {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (lafName[name].equals(info.getName())) {
                    try {
                        UIManager.setLookAndFeel(info.getClassName());
                        found = true;
                        break;
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                        // lafName is not available, use the default look and feel.
                    }
                }
            }
        }
//        try {
//            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
//                if ("Nimbus".equals(info.getName())) {
//                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
//                    break;
//                }
//            }
//        } catch (ClassNotFoundException | InstantiationException
//            | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(DrawnIconDemo.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>

        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
            DrawnIconDemo demo = new DrawnIconDemo();
            demo.setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenuBar menuBar;
    // End of variables declaration//GEN-END:variables

}
