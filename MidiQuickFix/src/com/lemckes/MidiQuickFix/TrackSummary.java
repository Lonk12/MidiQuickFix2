/*
 * TrackSummary.java
 *
 * Created on 12 December 2003, 17:33
 */

package com.lemckes.MidiQuickFix;

/**
 *
 * @author  john
 */
public class TrackSummary extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    
    /** Creates new form TrackSummary */
    public TrackSummary(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }
    
    /** Set the sequence which is to be used */
    public void setSequence(javax.sound.midi.Sequence seq) {
        trackSummaryTable.setModel(new TrackSummaryTableModel(seq));
        Object[] widths = { "99", "Cor Anglais or longer", "00000:000", "00000:000", "16",
        new Boolean(true), new Boolean(true) };
        TableColumnWidthSetter.setColumnWidths(trackSummaryTable, widths);
    }
    
    public void setFileName(String name) {
        seqNameLabel.setText(name);
    }
    
    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        tableScrollPane = new javax.swing.JScrollPane();
        trackSummaryTable = new javax.swing.JTable();
        headerPanel = new javax.swing.JPanel();
        sequenceLabel = new javax.swing.JLabel();
        seqNameLabel = new javax.swing.JLabel();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        okButton.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("ok"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        tableScrollPane.setViewportView(trackSummaryTable);

        getContentPane().add(tableScrollPane, java.awt.BorderLayout.CENTER);

        headerPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        sequenceLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("tracks_for"));
        headerPanel.add(sequenceLabel);

        seqNameLabel.setFont(new java.awt.Font("Dialog", 0, 12));
        seqNameLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("FileName.mid"));
        seqNameLabel.setBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        headerPanel.add(seqNameLabel);

        getContentPane().add(headerPanel, java.awt.BorderLayout.NORTH);

        pack();
    }//GEN-END:initComponents
    
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_okButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog
    
    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new TrackSummary(new javax.swing.JFrame(), true).show();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JPanel headerPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JLabel seqNameLabel;
    private javax.swing.JLabel sequenceLabel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JTable trackSummaryTable;
    // End of variables declaration//GEN-END:variables
    
    private int returnStatus = RET_CANCEL;
}
