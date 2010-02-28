/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2009 John Lemcke
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
import java.awt.Color;
import java.awt.Rectangle;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

/**
 * The lyrics display.
 * @version $Id$
 */
public class LyricDisplay
    extends JPanel
    implements MetaEventListener,
    FontSelectionListener {
    static final long serialVersionUID = 4418719983394376657L;
    private TreeMap<Long, String> mWords = new TreeMap<Long, String>();
    private TreeMap<Long, WordPlace> mPlaces = new TreeMap<Long, WordPlace>();
    private Sequencer mSequencer;
    private Sequence mSequence;
    private FontSelector mFontSelector;
    private boolean mNewPage = false;
    Highlighter mHighlighter;
    Object mHighlightTag;
    // An instance of the private subclass of the default highlight painter
    Highlighter.HighlightPainter myHighlightPainter = new MyHighlightPainter(
        Color.red);

    // A private subclass of the default highlight painter
    class MyHighlightPainter
        extends DefaultHighlighter.DefaultHighlightPainter {
        public MyHighlightPainter(Color color) {
            super(color);
        }
    }

    private class WordPlace {
        private int startPos;
        private int length;

        public WordPlace(int startPos, int length) {
            this.startPos = startPos;
            this.length = length;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getStartPos() {
            return startPos;
        }

        public void setStartPos(int startPos) {
            this.startPos = startPos;
        }
    }

    /** Creates new form LyricDialog */
    public LyricDisplay() {
        initComponents();
        mHighlighter = lyricText.getHighlighter();
        try {
            mHighlightTag = mHighlighter.addHighlight(0, 0, myHighlightPainter);
        } catch (BadLocationException e) {
        }
    }

    /**
     * Implementation of MetaEventListener.meta() that handles
     * lyric and text events.
     * @param metaMessage the META message to handle
     */
    @Override
    public void meta(javax.sound.midi.MetaMessage metaMessage) {
//        Logger.getLogger("LYRICS").log(Level.INFO, "LyricPanel {0}", "meta");
        long tick = mSequencer.getTickPosition();
        int type = metaMessage.getType();
        if (type == MetaEvent.LYRIC || type == MetaEvent.TEXT) {
            updateHighlight(tick);
        }
    }

    public void updateHighlight(final long tick) {
//        System.err.println("Display recieved event at " + tick);
        WordPlace wp = mPlaces.get(tick);

        // Fudge to find an entry close to the given tick
        int offset = -2;
        while (wp == null && offset < 3) {
            wp = mPlaces.get(tick + offset++);
        }
        if (wp != null) {
            int start = wp.getStartPos();
            int len = wp.getLength();
            try {
                // Get the location of the current text
                Rectangle r = lyricText.modelToView(start + len);
                // Make the rectangle 2/3 the height of the scroll pane
                // so that the current line stays near the top of the window
                int height = jScrollPane1.getHeight() * 2 / 3;
                r.height = height;
                // Scroll so that the 2/3 height rectangle is visible
                lyricText.scrollRectToVisible(r);

                // Move the highlight
                mHighlighter.changeHighlight(mHighlightTag, start, start + len);
            } catch (BadLocationException ex) {
                Logger.getLogger("LYRICS").log(Level.SEVERE, null, ex);
            }
//                    System.out.println(" " + tick + " - " +
//                        wp.getStartPos() + ", " +
//                        wp.getLength() + " - " +
//                        mWords.get(tick));
//                    System.out.flush();
        } else {
            System.err.println("No wordplace at tick " + tick);
        }
    }

    /**
     * Set the Sequencer on which to listen for meta events.
     * @param seq The Sequencer
     */
    public void setSequencer(Sequencer seq) {
        if (mSequencer != null) {
            mSequencer.removeMetaEventListener(this);
        }

        mSequencer = seq;
        Sequence mySequence = seq.getSequence();
        if (mySequence != null) {
            loadSequence(mySequence);
        }

        mSequencer.addMetaEventListener(this);
    }

    public void loadSequence(Sequence seq) {
        Logger.getLogger("LYRICS").log(Level.INFO, "LyricPanel {0}",
            "loadSequence");
        mSequence = seq;
        Track[] tracks = seq.getTracks();
        mWords.clear();
        mPlaces.clear();
        for (Track t : tracks) {
            for (int e = 0; e < t.size(); ++e) {
                MidiEvent ev = t.get(e);
                MidiMessage mess = ev.getMessage();
                long tick = ev.getTick();
                int st = mess.getStatus();
                if (st == MetaMessage.META) {
                    MetaMessage metaMessage = (MetaMessage)mess;
                    int type = metaMessage.getType();

                    if (type == MetaEvent.LYRIC && lyricsCheckBox.isSelected() ||
                        type == MetaEvent.TEXT && textCheckBox.isSelected()) {
                        byte[] data = metaMessage.getData();

                        StringBuffer sb = new StringBuffer(data.length);
                        for (int k = 0; k < data.length; ++k) {
                            byte b = data[k];
                            if (b == 10 || (char)b == '\\') {
                                // According to midi.org 'paragraphs' should be delimited
                                // with a line-feed but a back-slash is common.
                                sb.append("\n\n");
                            } else if (b == 13 || (char)b == '/') {
                                // According to midi.org 'lines' should be delimited
                                // with a carriage-return but a slash is common.
                                sb.append('\n');
                            } else if (b > 31 && b < 128) {
                                // Printable character.
                                sb.append((char)b);
                            } else {
                                if (b > 0) {
                                    sb.append('?');
                                }
                            }
                        }
                        if (sb.length() > 0) {
                            mWords.put(tick, sb.toString());
                        }
                    }
                }
            }
        }
        int wordstart = 0;
        for (Entry<Long, String> e : mWords.entrySet()) {
            int len = e.getValue().length();
            mPlaces.put(e.getKey(), new WordPlace(wordstart, len));
            wordstart += len;
        }
        displayText();
    }

    public void displayText() {
        lyricText.setText(null);
        Logger.getLogger("LYRICS").log(Level.INFO, "LyricPanel {0}",
            "displayText");
        for (Entry<Long, String> e : mWords.entrySet()) {
            lyricText.setCaretPosition(lyricText.getDocument().getLength());
            lyricText.replaceSelection(e.getValue());
        }
        lyricText.setCaretPosition(0);
    }

    /**
     * Called when a font is selected from the FontSelector.
     * @param e the FontSelectionEvent
     */
    @Override
    public void fontSelected(FontSelectionEvent e) {
        lyricText.setFont(e.getSelectedFont());
    }

    /**
     * Clear the contents of the lyrics display.
     */
    public void reset() {
        lyricText.setText(null);
        loadSequence(mSequence);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        lyricText = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        showLabel = new javax.swing.JLabel();
        lyricsCheckBox = new javax.swing.JCheckBox();
        textCheckBox = new javax.swing.JCheckBox();
        resetButton = new javax.swing.JButton();
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

        resetButton.setText(UiStrings.getString("reset")); // NOI18N
        resetButton.setToolTipText(UiStrings.getString("reset_lyrics")); // NOI18N
        resetButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });
        jPanel1.add(resetButton, java.awt.BorderLayout.EAST);

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

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        reset();
}//GEN-LAST:event_resetButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel fontPanel;
    private javax.swing.JButton fontSelectButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane lyricText;
    private javax.swing.JCheckBox lyricsCheckBox;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel showLabel;
    private javax.swing.JCheckBox textCheckBox;
    // End of variables declaration//GEN-END:variables
}
