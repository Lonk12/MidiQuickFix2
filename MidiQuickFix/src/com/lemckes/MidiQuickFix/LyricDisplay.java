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

import com.lemckes.MidiQuickFix.components.FontSelector;
import com.lemckes.MidiQuickFix.util.FontSelectionEvent;
import com.lemckes.MidiQuickFix.util.FontSelectionListener;
import com.lemckes.MidiQuickFix.util.UiStrings;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.JPanel;
import javax.swing.text.Document;

/**
 * The lyrics display.
 * @version $Id$
 */
public class LyricDisplay extends JPanel
  implements MetaEventListener, FontSelectionListener {
    
    transient private Document doc;
    
    /** Creates new form LyricDialog */
    public LyricDisplay() {
        initComponents();
        doc = lyricText.getDocument();
    }
    
    // Implementation of MetaEventListener.meta()
    public void meta(javax.sound.midi.MetaMessage metaMessage) {
        int type = metaMessage.getType();
        
        if (
          ((type == MetaEvent.LYRIC && lyricsCheckBox.isSelected()) ||
          (type == MetaEvent.TEXT && textCheckBox.isSelected()))
          ) {
            //int len = metaMessage.getLength();
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
     * Set the Sequencer on which to listen for meta events.
     * @param seq The Sequencer
     */
    public void setSequencer(Sequencer seq) {
        if (mSequencer != null){
            mSequencer.removeMetaEventListener(this);
        }
        
        //TODO
        // Get the sequence and pre-load all the
        // LYRIC/TEXT events.
        // This will let us display the whole of a
        // 'paragraph' and highlight the current word.
        // (eventually)
        // Sequence mySequence = seq.getSequence();
        
        
        mSequencer = seq;
        mSequencer.addMetaEventListener(this);
    }
    
    /**
     * Called when a font is selected from the FontSelector.
     * @param e the FontSelectionEvent
     */
    public void fontSelected(FontSelectionEvent e) {
        lyricText.setFont(e.getSelectedFont());
    }
    
    /**
     * Close the lyrics dialog. Also stops listening for events.
     */
    public void close() {
        if (mSequencer != null) {
            mSequencer.removeMetaEventListener(this);
            mSequencer = null;
        }
    }
    
    /**
     * Clear the contents of the lyrics display.
     */
    public void clear() {
        lyricText.setText(null);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        lyricText = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        showLabel = new javax.swing.JLabel();
        lyricsCheckBox = new javax.swing.JCheckBox();
        textCheckBox = new javax.swing.JCheckBox();
        clearButton = new javax.swing.JButton();
        fontPanel = new javax.swing.JPanel();
        fontSelectButton = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        jScrollPane1.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        lyricText.setFont(new java.awt.Font("Dialog", 0, 24));
        lyricText.setPreferredSize(new java.awt.Dimension(400, 300));
        jScrollPane1.setViewportView(lyricText);

        add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 5, 0));

        showLabel.setText(UiStrings.getString("show")); // NOI18N
        jPanel2.add(showLabel);

        lyricsCheckBox.setMnemonic('L');
        lyricsCheckBox.setSelected(true);
        lyricsCheckBox.setText(UiStrings.getString("lyrics")); // NOI18N
        lyricsCheckBox.setToolTipText(UiStrings.getString("show_lyric_events")); // NOI18N
        jPanel2.add(lyricsCheckBox);

        textCheckBox.setMnemonic('T');
        textCheckBox.setSelected(true);
        textCheckBox.setText(UiStrings.getString("text")); // NOI18N
        textCheckBox.setToolTipText(UiStrings.getString("show_text_events")); // NOI18N
        jPanel2.add(textCheckBox);

        jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

        clearButton.setText(UiStrings.getString("clear")); // NOI18N
        clearButton.setToolTipText(UiStrings.getString("clear_lyrics")); // NOI18N
        clearButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        clearButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearButtonActionPerformed(evt);
            }
        });

        jPanel1.add(clearButton, java.awt.BorderLayout.EAST);

        fontPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.CENTER, 0, 0));

        fontSelectButton.setText(UiStrings.getString("font")); // NOI18N
        fontSelectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        fontSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSelectButtonActionPerformed(evt);
            }
        });

        fontPanel.add(fontSelectButton);

        jPanel1.add(fontPanel, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);

    }// </editor-fold>//GEN-END:initComponents
    
    private void fontSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSelectButtonActionPerformed
        if (mFontSelector == null) {
            mFontSelector = new FontSelector(null, false);
            mFontSelector.setSelectedFont(lyricText.getFont());
            mFontSelector.addFontSelectionListener(this);
        }
        mFontSelector.setVisible(true);
    }//GEN-LAST:event_fontSelectButtonActionPerformed
    
    private void clearButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearButtonActionPerformed
        clear();
    }//GEN-LAST:event_clearButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton clearButton;
    private javax.swing.JPanel fontPanel;
    private javax.swing.JButton fontSelectButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane lyricText;
    private javax.swing.JCheckBox lyricsCheckBox;
    private javax.swing.JLabel showLabel;
    private javax.swing.JCheckBox textCheckBox;
    // End of variables declaration//GEN-END:variables
    
    
    transient private Sequencer mSequencer;
    
    transient private Sequence mSequence;
    
    private FontSelector mFontSelector;
    
    private boolean mNewPage = false;
}
