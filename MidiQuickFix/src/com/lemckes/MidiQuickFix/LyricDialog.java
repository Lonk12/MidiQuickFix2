/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2005 John Lemcke
 *   jostle@users.sourceforge.net
 *
 *   This program is free software; you can redistribute it
 *   and/or modify it under the terms of the Artistic License
 *   as published by Larry Wall, either version 2.0,
 *   or (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *   See the Artistic License for more details.
 *
 *   You should have received a copy of the Artistic License with this Kit,
 *   in the file named "Artistic.clarified".
 *   If not, I'll be glad to provide one.
 *
 **************************************************************/

package com.lemckes.MidiQuickFix;

import javax.sound.midi.MetaEventListener;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.JDialog;
import javax.swing.text.Document;

/**
 * The lyrics display dialog.
 * @version $Id$
 */
public class LyricDialog extends JDialog implements MetaEventListener {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;
    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;
    
    private Document doc;
    
    /** Creates new form LyricDialog */
    public LyricDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        doc = lyricText.getDocument();
    }
    
    // Implementation of MetaEventListener.meta()
    public void meta(javax.sound.midi.MetaMessage metaMessage) {
        int type = metaMessage.getType();
        
        if (
            ((type == MetaEvent.lyric && lyricsCheckBox.isSelected()) ||
            (type == MetaEvent.text && textCheckBox.isSelected()))
            )
        {
            int len = metaMessage.getLength();
            byte[] data = metaMessage.getData();
            
            StringBuffer sb = new StringBuffer(data.length);
            int charCount = 0;
            for (int k = 0; k < data.length; ++k) {
                byte b = data[k];
                if (b == 10 || (char)b == '\\') {
                    // According to midi.org 'paragraphs' should be delimited
                    // with a line-feed but a back-slash is common.
                    mNewPage = true;
                    //lyricText.setText(null);
                } else if (b == 13 || (char)b == '/') {
                    // According to midi.org 'lines' should be delimited
                    // with a carriage-return but a slash is common.
                    sb.append('\n');
                } else if (b > 31 && b < 128) {
                    // Printable character.
                    sb.append((char)b);
                } else {
                    sb.append('?');
                }
            }
            if (sb.length() > 0) {
                if (mNewPage) {
                    lyricText.setText(sb.toString());
                    mNewPage = false;
                } else {
                    lyricText.setCaretPosition(doc.getLength());
                    lyricText.replaceSelection(sb.toString());
                }
            }
        }
    }
    
    /**
     * Set the file name that is displayed in the dialog.
     * @param name The name of the file
     */
    public void setFileName(String name) {
        titleLabel.setText(name);
    }
    
    /**
     * Set the Sequencer on which to listen for meta events.
     * @param seq The Sequencer
     */
    public void setSequencer(Sequencer seq) {
        if (mSequencer != null){
            mSequencer.removeMetaEventListener(this);
        }
        
        // Get the sequence and pre-load all the 
        // lyric/text events.
        // This will let us display the whole of a 
        // 'paragraph' and highlight the current word.
        // (eventually)
        Sequence mySequence = seq.getSequence();
        
        
        mSequencer = seq;
        mSequencer.addMetaEventListener(this);
    }
    /**
     * Close the lyrics dialog. Also stops listening for events.
     */
    public void close() {
        if (mSequencer != null) {
            mSequencer.removeMetaEventListener(this);
            mSequencer = null;
        }
        setVisible(false);
    }
    /**
     * Clear the contents of the lyrics display.
     */
    public void clear() {
        lyricText.setText(null);
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
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonPanel = new javax.swing.JPanel();
        buttonGrid = new javax.swing.JPanel();
        clearButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        lyricText = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        titleLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        showLabel = new javax.swing.JLabel();
        lyricsCheckBox = new javax.swing.JCheckBox();
        textCheckBox = new javax.swing.JCheckBox();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        buttonGrid.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        clearButton.setText("Clear");
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        buttonGrid.add(clearButton);

        closeButton.setText("Close");
        closeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeButtonActionPerformed(evt);
            }
        });

        buttonGrid.add(closeButton);

        buttonPanel.add(buttonGrid);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        lyricText.setFont(new java.awt.Font("Dialog", 0, 24));
        lyricText.setPreferredSize(new java.awt.Dimension(400, 300));
        jScrollPane1.setViewportView(lyricText);

        getContentPane().add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        titleLabel.setText("filename");
        titleLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 5, 0, 0)));
        jPanel1.add(titleLabel, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 2));

        showLabel.setText("Show:");
        jPanel2.add(showLabel);

        lyricsCheckBox.setMnemonic('L');
        lyricsCheckBox.setSelected(true);
        lyricsCheckBox.setText("Lyrics");
        lyricsCheckBox.setToolTipText("Show Lyric events");
        jPanel2.add(lyricsCheckBox);

        textCheckBox.setMnemonic('T');
        textCheckBox.setSelected(true);
        textCheckBox.setText("Text");
        textCheckBox.setToolTipText("Show Text events");
        jPanel2.add(textCheckBox);

        jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

        getContentPane().add(jPanel1, java.awt.BorderLayout.NORTH);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents
    
    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        // TODO add your handling code here:
        lyricText.setText(null);
    }//GEN-LAST:event_clearButtonActionPerformed
    
    private void closeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeButtonActionPerformed
        doClose(RET_OK);
    }//GEN-LAST:event_closeButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog
    
    private void doClose(int retStatus) {
        returnStatus = retStatus;
        close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LyricDialog(new javax.swing.JFrame(), true).setVisible(true);
            }
        });
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonGrid;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane lyricText;
    private javax.swing.JCheckBox lyricsCheckBox;
    private javax.swing.JLabel showLabel;
    private javax.swing.JCheckBox textCheckBox;
    private javax.swing.JLabel titleLabel;
    // End of variables declaration//GEN-END:variables
    
    private int returnStatus = RET_CANCEL;
    
    private Sequencer mSequencer;
    
    private Sequence mSequence;
    
    private boolean mNewPage = false;
}
