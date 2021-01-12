/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2021 John Lemcke
 * jostle@users.sourceforge.net
 *
 * This program is free software; you can redistribute it
 * and/or modify it under the terms of the Artistic License
 * as published by Larry Wall, either version 2.0,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Artistic License for more details.
 *
 * You should have received a copy of the Artistic License with this Kit,
 * in the file named "Artistic.clarified".
 * If not, I'll be glad to provide one.
 *
 *************************************************************
 */
package com.lemckes.MidiQuickFix;

import com.lemckes.MidiQuickFix.components.FontSelector;
import com.lemckes.MidiQuickFix.util.FontSelectionEvent;
import com.lemckes.MidiQuickFix.util.FontSelectionListener;
import com.lemckes.MidiQuickFix.util.MqfProperties;
import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.MidiQuickFix.util.StringConverter;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Track;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * The lyrics display.
 */
public class LyricDisplay
    extends JPanel
    implements MetaEventListener,
    FontSelectionListener,
    ItemListener
{

    static final long serialVersionUID = 4418719983394376657L;
    private final TreeMap<Long, String> mWords = new TreeMap<>();
    private final TreeMap<Long, WordPlace> mPlaces = new TreeMap<>();
    private Sequencer mSequencer;
    private MqfSequence mSequence;
    private FontSelector mFontSelector;
    private Highlighter mHighlighter;
    private Object mHighlightTag;
    // An instance of the private subclass of the default highlight painter
    private Highlighter.HighlightPainter myHighlightPainter;

    @Override
    public void itemStateChanged(ItemEvent e) {
        int trackIndex = ((TrackSelectItem)e.getItem()).mTrackIndex;
        if (e.getStateChange() == ItemEvent.SELECTED) {
            mSequence.showTrackLyrics(trackIndex, true);
        } else {
            mSequence.showTrackLyrics(trackIndex, false);
        }
        rebuild();
    }

    // A private subclass of the default highlight painter
    class MyHighlightPainter
        extends DefaultHighlighter.DefaultHighlightPainter
    {

        MyHighlightPainter(Color color) {
            super(color);
        }
    }

    private class WordPlace
    {

        private int startPos;
        private int length;

        WordPlace(int startPos, int length) {
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

    /**
     * Creates new form LyricDisplay
     */
    public LyricDisplay() {
        initComponents();
        trackSelectCombo.setModel(new TrackSelectComboModel());
        mHighlighter = lyricText.getHighlighter();
        myHighlightPainter = new MyHighlightPainter(
            MqfProperties.getColourProperty(
                MqfProperties.LYRIC_HIGHLIGHT_COLOUR, Color.green.darker()));
        try {
            mHighlightTag = mHighlighter.addHighlight(0, 0, myHighlightPainter);
        } catch (BadLocationException e) {
        }
        createStyles();
        updatePreferences();
    }

    /**
     * Implementation of MetaEventListener.meta() that handles
     * lyric and text events.
     *
     * @param metaMessage the META message to handle
     */
    @Override
    public void meta(javax.sound.midi.MetaMessage metaMessage) {
        long tick = mSequencer.getTickPosition();
        int type = metaMessage.getType();
        if (type == MetaEvent.LYRIC && lyricsCheckBox.isSelected()
            || type == MetaEvent.TEXT && textCheckBox.isSelected()) {
            updateHighlight(tick);
        }
    }

    private void updateHighlight(final long tick) {
        WordPlace wp = mPlaces.get(tick);

        // Fudge to find an entry close to the given tick
        int offset = -1;
        while (wp == null && offset > -12) {
            wp = mPlaces.get(tick + offset--);
        }
        if (wp != null) {
            final int start = wp.getStartPos();
            final int len = wp.getLength();
            EventQueue.invokeLater(() -> {
                try {
                    // Get the location of the current text
                    final Rectangle2D r2d = lyricText.modelToView2D(start + len);
                    final Rectangle r = r2d.getBounds();
                    // Make the rectangle 2/3 the height of the scroll pane
                    // so that the current line stays near the top of the window
                    int height1 = lyricScrollPane.getHeight() * 2 / 3;
                    r.height = height1;
                    // Scroll so that the 2/3 height rectangle is visible
                    lyricText.scrollRectToVisible(r);
                    // Move the highlight
                    mHighlighter.changeHighlight(mHighlightTag, start,
                        start + len);
                } catch (BadLocationException ex) {
                    // What a pity.
                }
            });
        } else {
            // Shame really.
        }
    }

    /**
     * Set the Sequencer on which to listen for meta events.
     *
     * @param seq The Sequencer
     */
    public void setSequencer(Sequencer seq) {
        if (mSequencer != null) {
            mSequencer.removeMetaEventListener(this);
        }
        mSequencer = seq;
        mSequencer.addMetaEventListener(this);
    }

    class TrackSelectItem
    {

        private int mTrackIndex;
        private String mTrackName;

        TrackSelectItem(int trackIndex, String trackName) {
            mTrackIndex = trackIndex;
            mTrackName = trackName;
        }

        @Override
        public String toString() {
            String itemName = Integer.toString(mTrackIndex) + " - ";
            if (mTrackName != null) {
                itemName += mTrackName;
            }
            return itemName;
        }
    }

    private class TrackSelectComboModel
        extends DefaultComboBoxModel<TrackSelectItem>
    {
    }

    public void setSequence(MqfSequence seq) {
        mSequence = seq;
        trackSelectCombo.removeItemListener(this);
        trackSelectCombo.removeAllItems();
        Track[] tracks = mSequence.getTracks();
        boolean needLyrics = true;
        for (int i = 0; i < tracks.length; ++i) {
            if (mSequence.getTrackHasLyrics(i)) {
                mSequence.showTrackLyrics(i, needLyrics);
                needLyrics = false;
                var item = new TrackSelectItem(i, mSequence.getTrackName(i));
                trackSelectCombo.addItem(item);
            }
        }
        trackSelectCombo.addItemListener(this);
        rebuild();
    }

    public void rebuild() {
        mWords.clear();
        mPlaces.clear();

        if (mSequence != null) {
            Track[] tracks = mSequence.getTracks();
            for (int i = 0; i < tracks.length; ++i) {
                if (mSequence.getTrackHasLyrics(i)) {

                    boolean show = mSequence.trackLyricsShown(i);
                    if (show) {
                        findLyrics(tracks[i]);
                    }
                }
            }
        }

        int wordstart = 0;
        for (Entry<Long, String> e : mWords.entrySet()) {
            String word = e.getValue();
            String trim = word.trim();
            int start = word.indexOf(trim);
            int trimLen = trim.length();
            mPlaces.put(e.getKey(), new WordPlace(wordstart + start, trimLen));
            wordstart += word.length();
        }
        displayText();
    }

    public void displayText() {
        lyricText.setText(null);
        int patternFlags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        Pattern rubyPattern
            = Pattern.compile("\\[(.*?)\\]", patternFlags);
        StyledDocument doc = lyricText.getStyledDocument();
        Style regularStyle = doc.getStyle("regular");
        Style rubyStyle = doc.getStyle("ruby");
        for (Entry<Long, String> e : mWords.entrySet()) {
            lyricText.setCaretPosition(lyricText.getDocument().getLength());
            String text = e.getValue();
            // Test for possible ruby text
            if (text.contains("[")) {
                Matcher m = rubyPattern.matcher(text);
                int nonRubyStartPos = 0; // Keep track of text outside the ruby delimiters '[]'
                while (m.find()) {
                    int rubyStartPos = m.start();
                    int rubyEndPos = m.end();
                    if (rubyStartPos > nonRubyStartPos) {
                        // There was some text before the start of the ruby text
                        String preRuby = text.substring(nonRubyStartPos, rubyStartPos);
                        appendText(doc, preRuby, regularStyle);
                    }
                    // Add some zero-width spaces to replace the [ and ]
                    String ruby = "\u200B" + m.group(1) + "\u200B";
                    appendText(doc, ruby, rubyStyle);
                    nonRubyStartPos = rubyEndPos;
                }
                String lastBit = text.substring(nonRubyStartPos);
                appendText(doc, lastBit, regularStyle);
            } else {
                // Not ruby, just plain text
                appendText(doc, text, regularStyle);
            }
        }
        lyricText.setCaretPosition(0);
    }

    private void appendText(StyledDocument doc, String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
        } catch (BadLocationException ex) {
            TraceDialog.addTrace("appendText : " + ex.getLocalizedMessage());
        }
    }

    private void findLyrics(Track t) {
        StringConverter.resetDefaultCharset();
        for (int i = 0; i < t.size(); ++i) {
            MidiMessage mess = t.get(i).getMessage();
            long tick = t.get(i).getTick();
            if (mess.getStatus() == MetaMessage.META) {
                MetaMessage metaMessage = (MetaMessage)mess;
                int type = metaMessage.getType();

                if (type == MetaEvent.LYRIC && lyricsCheckBox.isSelected()
                    || type == MetaEvent.TEXT && textCheckBox.isSelected()) {
                    byte[] data = metaMessage.getData();

                    try {
                        String lyricString
                            = StringConverter.convertBytesToString(data);
                        if (lyricString.length() > 0) {
                            checkForCharsetChange(lyricString);
                            checkForSongInfo(lyricString);
                            // Convert MIDI end-of-line/paragraph and Tab characters
                            // for display.
                            lyricString = lyricString.replaceAll("\n", "\n\n");
                            lyricString = lyricString.replaceAll("\r", "\n");
                            lyricString = lyricString.replaceAll("\\\\n", "\n\n");
                            lyricString = lyricString.replaceAll("\\\\r", "\n");
                            lyricString = lyricString.replaceAll("\\\\t", "\t");

                            // Remove any CharsetChange strings
                            lyricString = lyricString.replaceAll("\\{\\@.*?\\}",
                                "");
                            // Remove any SongInfo strings
                            lyricString = lyricString.replaceAll("\\{\\#.*?\\}",
                                "");

                            // Unescape any remaining escaped special characters
                            lyricString = lyricString.replaceAll("\\\\\\[", "[");
                            lyricString = lyricString.replaceAll("\\\\]", "]");
                            lyricString = lyricString.replaceAll("\\\\\\{", "{");
                            lyricString = lyricString.replaceAll("\\\\}", "}");

                            // Convert commonly used (but not recommended)
                            // '/' and '\' end-of-line/paragraph markers for display
                            lyricString = lyricString.replaceAll("\\\\", "\n\n");
                            lyricString = lyricString.replaceAll("/", "\n");

                            if (lyricString.length() > 0) {
                                // if there is already a word at this location
                                // then append the new word to it
                                if (mWords.containsKey(tick)) {
                                    lyricString = mWords.get(tick) + lyricString;
                                }
                                mWords.put(tick, lyricString);
                            }
                        }
                    } catch (UnsupportedEncodingException uee) {
                        TraceDialog.addTrace("findLyrics exception "
                            + uee.getLocalizedMessage());
                    }
                }
            }
        }
    }

    private void checkForCharsetChange(String lyric) {
        int patternFlags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        Pattern charsetPattern
            = Pattern.compile("\\{\\@(.*?)\\}", patternFlags);
        Matcher m = charsetPattern.matcher(lyric);
        if (m.find() && m.groupCount() > 0) {
            String cSet = m.group(1);
            boolean wasSet = StringConverter.setCharsetName(cSet);
            if (!wasSet) {
                TraceDialog.addTrace("Failed to set characterSet " + cSet);
            }
        }
    }

    private void checkForSongInfo(String lyric) {
        int patternFlags = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
        Pattern songInfoPattern
            = Pattern.compile("\\{\\#(.*?)\\}", patternFlags);
        Matcher m = songInfoPattern.matcher(lyric);
        while (m.find()) {
            for (int i = 1; i <= m.groupCount(); ++i) {
                String info = m.group(i);
                String[] keyValue = info.split("=");
                if (keyValue.length > 1) {
                    String key = keyValue[0].trim();
                    String value = keyValue[1].trim();
                    mSequence.putSongInfo(key, value);
                }
            }
        }
    }

    /**
     * Called when a font is selected from the FontSelector.
     *
     * @param e the FontSelectionEvent
     */
    @Override
    public void fontSelected(FontSelectionEvent e) {
        Font font = e.getSelectedFont();
        lyricText.setFont(font);
        MqfProperties.setFontProperty(MqfProperties.LYRIC_FONT, font);

        float scale = MqfProperties.getFloatProperty(
            MqfProperties.LYRIC_RUBY_FONT_SCALE, 1.0f);
        Style s = lyricText.getStyle("ruby");
        float size = font.getSize() * scale;
        StyleConstants.setFontSize(s, Math.round(size));

        rebuild();
    }

    private void createStyles() {
        StyledDocument doc = lyricText.getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().
            getStyle(StyleContext.DEFAULT_STYLE);
        StyleConstants.setLineSpacing(defaultStyle, 2.0f);

        Style regular = doc.addStyle("regular", defaultStyle);
        StyleConstants.setLineSpacing(regular, 2.0f);
        StyleConstants.setForeground(regular, Color.BLACK);

        Style ruby = doc.addStyle("ruby", regular);
        float size = lyricText.getFont().getSize() * 0.6f;
        StyleConstants.setLineSpacing(ruby, 2.0f);
        StyleConstants.setSpaceBelow(ruby, size);
        StyleConstants.setFontSize(ruby, Math.round(size));
        StyleConstants.setForeground(ruby, Color.BLACK);
    }

    /**
     *
     */
    final public void updatePreferences() {
        myHighlightPainter = new MyHighlightPainter(
            MqfProperties.getColourProperty(
                MqfProperties.LYRIC_HIGHLIGHT_COLOUR, Color.green.darker()));
        try {
            mHighlighter = lyricText.getHighlighter();
            mHighlighter.removeAllHighlights();
            mHighlightTag
                = mHighlighter.addHighlight(0, 0, myHighlightPainter);
        } catch (BadLocationException e) {
            // Can not happen with addHighlight(0, 0, myHighlightPainter);
        } catch (Exception ex) {
            TraceDialog.addTrace("an exception " + ex);
        }

        lyricText.setBackground(MqfProperties.getColourProperty(
            MqfProperties.LYRIC_BACKGROUND_COLOUR, Color.WHITE));

        lyricText.setFont(MqfProperties.getFontProperty(
            MqfProperties.LYRIC_FONT, new java.awt.Font("Dialog", 0, 24)));

        StyledDocument doc = lyricText.getStyledDocument();
        Style regular = doc.getStyle("regular");
        StyleConstants.setForeground(regular, MqfProperties.getColourProperty(
            MqfProperties.LYRIC_FOREGROUND_COLOUR, Color.BLACK));

        Style ruby = doc.getStyle("ruby");
        float size
            = lyricText.getFont().getSize() * MqfProperties.getFloatProperty(
            MqfProperties.LYRIC_RUBY_FONT_SCALE, 0.8f);
        StyleConstants.setSpaceBelow(ruby, size);
        StyleConstants.setFontSize(ruby, Math.round(size));
        StyleConstants.setForeground(ruby, MqfProperties.getColourProperty(
            MqfProperties.LYRIC_RUBY_FG_COLOUR, Color.BLACK));
    }

    public void moveCaretToStart() {
        lyricText.setCaretPosition(0);
        final Rectangle r = new Rectangle(1, 1);
        lyricText.scrollRectToVisible(r);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        lyricScrollPane = new javax.swing.JScrollPane();
        lyricText = new javax.swing.JTextPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        showLabel = new javax.swing.JLabel();
        lyricsCheckBox = new javax.swing.JCheckBox();
        textCheckBox = new javax.swing.JCheckBox();
        buttonPanel = new javax.swing.JPanel();
        fontSelectButton = new javax.swing.JButton();
        trackSelectPanel = new javax.swing.JPanel();
        trackSelectCombo = new javax.swing.JComboBox<>();
        trackSelectLabel = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        lyricScrollPane.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        lyricScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        lyricText.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        lyricText.setPreferredSize(new java.awt.Dimension(400, 300));
        lyricScrollPane.setViewportView(lyricText);

        add(lyricScrollPane, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout(8, 0));

        jPanel2.setLayout(new java.awt.GridBagLayout());

        showLabel.setText(UiStrings.getString("show")); // NOI18N
        jPanel2.add(showLabel, new java.awt.GridBagConstraints());

        lyricsCheckBox.setMnemonic('L');
        lyricsCheckBox.setSelected(true);
        lyricsCheckBox.setText(UiStrings.getString("lyrics")); // NOI18N
        lyricsCheckBox.setToolTipText(UiStrings.getString("show_lyric_events")); // NOI18N
        lyricsCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lyricsCheckBoxActionPerformed(evt);
            }
        });
        jPanel2.add(lyricsCheckBox, new java.awt.GridBagConstraints());

        textCheckBox.setMnemonic('T');
        textCheckBox.setSelected(true);
        textCheckBox.setText(UiStrings.getString("text")); // NOI18N
        textCheckBox.setToolTipText(UiStrings.getString("show_text_events")); // NOI18N
        textCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textCheckBoxActionPerformed(evt);
            }
        });
        jPanel2.add(textCheckBox, new java.awt.GridBagConstraints());

        jPanel1.add(jPanel2, java.awt.BorderLayout.WEST);

        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 4, 0));

        fontSelectButton.setText(UiStrings.getString("font")); // NOI18N
        fontSelectButton.setMargin(new java.awt.Insets(2, 2, 2, 2));
        fontSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontSelectButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(fontSelectButton);

        jPanel1.add(buttonPanel, java.awt.BorderLayout.LINE_END);

        trackSelectPanel.setLayout(new java.awt.BorderLayout(4, 0));
        trackSelectPanel.add(trackSelectCombo, java.awt.BorderLayout.CENTER);

        trackSelectLabel.setText(UiStrings.getString("lyrics_track")); // NOI18N
        trackSelectPanel.add(trackSelectLabel, java.awt.BorderLayout.LINE_START);

        jPanel1.add(trackSelectPanel, java.awt.BorderLayout.CENTER);

        add(jPanel1, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents
    private void fontSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontSelectButtonActionPerformed
        if (mFontSelector == null) {
            mFontSelector = new FontSelector(MidiQuickFix.getMainFrame(), false);
            mFontSelector.setLocationRelativeTo(
                lyricScrollPane.getVerticalScrollBar());
            mFontSelector.addFontSelectionListener(this);
        }
        mFontSelector.setVisible(true);
        mFontSelector.setSelectedFont(lyricText.getFont());
    }//GEN-LAST:event_fontSelectButtonActionPerformed

    private void lyricsCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lyricsCheckBoxActionPerformed
        rebuild();
    }//GEN-LAST:event_lyricsCheckBoxActionPerformed

    private void textCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textCheckBoxActionPerformed
        rebuild();
    }//GEN-LAST:event_textCheckBoxActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton fontSelectButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane lyricScrollPane;
    private javax.swing.JTextPane lyricText;
    private javax.swing.JCheckBox lyricsCheckBox;
    private javax.swing.JLabel showLabel;
    private javax.swing.JCheckBox textCheckBox;
    private javax.swing.JComboBox<TrackSelectItem> trackSelectCombo;
    private javax.swing.JLabel trackSelectLabel;
    private javax.swing.JPanel trackSelectPanel;
    // End of variables declaration//GEN-END:variables
}
