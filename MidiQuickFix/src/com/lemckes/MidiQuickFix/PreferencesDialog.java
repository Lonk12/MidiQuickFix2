/* ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2023 John Lemcke
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
 ************************************************************** */
package com.lemckes.MidiQuickFix;

import com.lemckes.MidiQuickFix.components.FontSelector;
import com.lemckes.MidiQuickFix.util.FontSelectionEvent;
import com.lemckes.MidiQuickFix.util.FontSelectionListener;
import com.lemckes.MidiQuickFix.util.MqfProperties;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 * Allow the user to set their preferences
 */
public class PreferencesDialog
    extends javax.swing.JDialog
    implements FontSelectionListener
{

    private static final long serialVersionUID = 9364291903L;

    MidiQuickFix mMQF;
    private FontSelector mFontSelector;
    private final Frame mParentFrame;
    private Font mLyricFont;
    private Color mBackground;
    private Color mForeground;
    private Color mRubyFg;
    private Color mHighlight;
    private float mRubyScale;
    private String mDefaultPath;
    private String mDefaultSoundbankPath;
    private String mLafName;
    private float mUiScale;
    private Boolean mShowTraceDialog;
    private static final String[] sampleText = {
        "Sing a song of sixpence,\n",
        "A pocket ",
        "ruby text",
        " full of rye,\n",
        "Four and ",
        "twenty",
        " blackbirds,\n",
        "Baked in a pie."
    };
    private static final String[] sampleStyles = {
        "regular",
        "regular",
        "ruby",
        "regular",
        "regular",
        "highlight",
        "regular",
        "regular"
    };

    // A private subclass of the default highlight painter
    class MyHighlightPainter
        extends DefaultHighlighter.DefaultHighlightPainter
    {

        MyHighlightPainter(Color color) {
            super(color);
        }
    }

    /**
     * Create a new PreferencesDialog
     *
     * @param parent
     * @param modal
     * @param mqf
     */
    public PreferencesDialog(Frame parent, boolean modal, MidiQuickFix mqf) {
        super(parent, modal);
        initComponents();
        mParentFrame = parent;
        mMQF = mqf;

        mLyricFont = MqfProperties.getFontProperty(
            MqfProperties.LYRIC_FONT, lyricsTextPane.getFont());
        mBackground = MqfProperties.getColourProperty(
            MqfProperties.LYRIC_BACKGROUND_COLOUR, Color.WHITE);
        mForeground = MqfProperties.getColourProperty(
            MqfProperties.LYRIC_FOREGROUND_COLOUR, Color.BLACK);
        mRubyFg = MqfProperties.getColourProperty(
            MqfProperties.LYRIC_RUBY_FG_COLOUR, Color.BLACK);
        mHighlight = MqfProperties.getColourProperty(
            MqfProperties.LYRIC_HIGHLIGHT_COLOUR, Color.GREEN.darker());
        mRubyScale = MqfProperties.getFloatProperty(
            MqfProperties.LYRIC_RUBY_FONT_SCALE, 0.8f);
        mLafName = MqfProperties.getStringProperty(
            MqfProperties.LOOK_AND_FEEL_NAME, "Metal");
        mShowTraceDialog = MqfProperties.getBooleanProperty(
            MqfProperties.SHOW_TRACE, true);

        setStyles();

        displayText();

        mDefaultPath = MqfProperties.getProperty(MqfProperties.LAST_PATH_KEY);
        fileFolderField.setText(mDefaultPath);

        mDefaultSoundbankPath = MqfProperties.getProperty(MqfProperties.LAST_SOUNDBANK_PATH_KEY);
        soundbankFolderField.setText(mDefaultSoundbankPath);

        rubyScaleSpinner.setValue(Math.round(mRubyScale * 100));

        LookAndFeelInfo[] installedLafs = UIManager.getInstalledLookAndFeels();
        String[] lafNames = new String[installedLafs.length];
        for (int i = 0; i < installedLafs.length; ++i) {
            LookAndFeelInfo info = installedLafs[i];
            lafNames[i] = info.getName();
        }
        lookAndFeelCombo.setModel(new DefaultComboBoxModel<>(lafNames));
        String currentLaf = UIManager.getLookAndFeel().getName();
        lookAndFeelCombo.setSelectedItem(currentLaf);

        mUiScale = MqfProperties.getFloatProperty(
            MqfProperties.UI_FONT_SCALE, 1.0f);
        uiScaleField.setValue(mUiScale);

        traceDialogCheckbox.getModel().setSelected(mShowTraceDialog);

        pack();
        setLocationRelativeTo(parent);
    }

    private void savePreferences() {
        MqfProperties.setFontProperty(
            MqfProperties.LYRIC_FONT, mLyricFont);
        MqfProperties.setColourProperty(
            MqfProperties.LYRIC_BACKGROUND_COLOUR, mBackground);
        MqfProperties.setColourProperty(
            MqfProperties.LYRIC_FOREGROUND_COLOUR, mForeground);
        MqfProperties.setColourProperty(
            MqfProperties.LYRIC_RUBY_FG_COLOUR, mRubyFg);
        MqfProperties.setColourProperty(
            MqfProperties.LYRIC_HIGHLIGHT_COLOUR, mHighlight);
        MqfProperties.setFloatProperty(
            MqfProperties.LYRIC_RUBY_FONT_SCALE, mRubyScale);
        MqfProperties.setStringProperty(
            MqfProperties.LAST_PATH_KEY, mDefaultPath);
        MqfProperties.setStringProperty(
            MqfProperties.LAST_SOUNDBANK_PATH_KEY, mDefaultSoundbankPath);
        MqfProperties.setStringProperty(
            MqfProperties.LOOK_AND_FEEL_NAME, mLafName);
        mUiScale = ((Number)uiScaleField.getValue()).floatValue();
        MqfProperties.setFloatProperty(
            MqfProperties.UI_FONT_SCALE, mUiScale);
        MqfProperties.setBooleanProperty(
            MqfProperties.SHOW_TRACE, mShowTraceDialog);

        MqfProperties.writeProperties();
    }

    private void applyChanges() {
        if (mMQF != null) {
            // Set a new LaF if it has changed
            String currentLaf = UIManager.getLookAndFeel().getName();
            if (!currentLaf.equalsIgnoreCase(mLafName)) {
                // Set the LaF on the main app window
                mMQF.setLookAndFeel(mLafName);
                // Different LaF may not handle bg colour so set explicitly
                lyricsTextPane.setBackground(mBackground);
                // now update our components
                SwingUtilities.updateComponentTreeUI(this);
                pack();
                if (mFontSelector != null) {
                    // and update the font selector if needed
                    SwingUtilities.updateComponentTreeUI(mFontSelector);
                    mFontSelector.pack();
                }
            }
            // Set the new colours and font on the main lyric display
            mMQF.updateLyricDisplay();
        }
    }

    @Override
    public void fontSelected(FontSelectionEvent e) {
        mLyricFont = e.getSelectedFont();
        lyricsTextPane.setFont(mLyricFont);

        // calculate the new size for ruby text
        Style s = lyricsTextPane.getStyle("ruby");
        float size = mLyricFont.getSize() * mRubyScale;
        StyleConstants.setFontSize(s, Math.round(size));
    }

    private void displayText() {
        lyricsTextPane.setText(null);
        //Load the text pane with styled text.
        StyledDocument doc = lyricsTextPane.getStyledDocument();
        try {
            for (int i = 0; i < sampleText.length; i++) {
                doc.insertString(doc.getLength(), sampleText[i],
                    doc.getStyle(sampleStyles[i]));
            }
        } catch (BadLocationException ex) {
            Logger.getLogger(PreferencesDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setStyles() {
        lyricsTextPane.setBackground(mBackground);

        StyledDocument doc = lyricsTextPane.getStyledDocument();
        Style defaultStyle = StyleContext.getDefaultStyleContext().
            getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", defaultStyle);
        StyleConstants.setForeground(regular, mForeground);

        Style ruby = doc.addStyle("ruby", regular);
        float size = lyricsTextPane.getFont().getSize() * mRubyScale;
        StyleConstants.setFontSize(ruby, Math.round(size));
        StyleConstants.setForeground(ruby, mRubyFg);

        Style highlight = doc.addStyle("highlight", regular);
        StyleConstants.setBackground(highlight, mHighlight);
    }

    private void doClose() {
        if (mFontSelector != null) {
            mFontSelector.setVisible(false);
        }
        setVisible(false);
        dispose();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException
            | InstantiationException | UnsupportedLookAndFeelException e) {
            // Nimbus is not available, use the default look and feel.
        }
        MqfProperties.readProperties();
        new PreferencesDialog(null, false, null).setVisible(true);
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
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        lyricsDisplayPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        lyricBgLabel = new javax.swing.JLabel();
        lyricBgButton = new javax.swing.JButton();
        lyricFgLabel = new javax.swing.JLabel();
        lyricFgButton = new javax.swing.JButton();
        rubyFgLabel = new javax.swing.JLabel();
        rubyFgButton = new javax.swing.JButton();
        highlightLabel = new javax.swing.JLabel();
        highlightButton = new javax.swing.JButton();
        fontLabel = new javax.swing.JLabel();
        fontButton = new javax.swing.JButton();
        rubyScaleLabel = new javax.swing.JLabel();
        rubyScaleSpinner = new javax.swing.JSpinner();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        lyricsTextPane = new javax.swing.JTextPane();
        defaultFolderPanel = new javax.swing.JPanel();
        fileFolderField = new javax.swing.JTextField();
        browseButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        soundbankFolderField = new javax.swing.JTextField();
        browseSoundbankButton = new javax.swing.JButton();
        lookAndFeelPanel = new javax.swing.JPanel();
        lookAndFeelCombo = new javax.swing.JComboBox<>();
        uiScaleLabel = new javax.swing.JLabel();
        uiScaleField = new javax.swing.JFormattedTextField();
        traceDialogPanel = new javax.swing.JPanel();
        traceDialogCheckbox = new javax.swing.JCheckBox();
        controlPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        applyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle"); // NOI18N
        setTitle(bundle.getString("PreferencesDialog.title")); // NOI18N
        setName("Form"); // NOI18N

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.GridBagLayout());

        lyricsDisplayPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PreferencesDialog.LyricDisplay.border.title"))); // NOI18N
        lyricsDisplayPanel.setName("lyricsDisplayPanel"); // NOI18N
        lyricsDisplayPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.GridBagLayout());

        lyricBgLabel.setText(bundle.getString("PreferencesDialog.lyricBgLabel.text")); // NOI18N
        lyricBgLabel.setName("lyricBgLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(lyricBgLabel, gridBagConstraints);

        lyricBgButton.setText(bundle.getString("PreferencesDialog.lyricBgButton.text")); // NOI18N
        lyricBgButton.setDefaultCapable(false);
        lyricBgButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        lyricBgButton.setMaximumSize(null);
        lyricBgButton.setMinimumSize(null);
        lyricBgButton.setName("lyricBgButton"); // NOI18N
        lyricBgButton.setPreferredSize(null);
        lyricBgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lyricBgButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        jPanel1.add(lyricBgButton, gridBagConstraints);

        lyricFgLabel.setText(bundle.getString("PreferencesDialog.lyricFgLabel.text")); // NOI18N
        lyricFgLabel.setName("lyricFgLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(lyricFgLabel, gridBagConstraints);

        lyricFgButton.setText(bundle.getString("PreferencesDialog.lyricFgButton.text")); // NOI18N
        lyricFgButton.setDefaultCapable(false);
        lyricFgButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        lyricFgButton.setMaximumSize(null);
        lyricFgButton.setMinimumSize(null);
        lyricFgButton.setName("lyricFgButton"); // NOI18N
        lyricFgButton.setPreferredSize(null);
        lyricFgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lyricFgButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        jPanel1.add(lyricFgButton, gridBagConstraints);

        rubyFgLabel.setText(bundle.getString("PreferencesDialog.rubyFgLabel.text")); // NOI18N
        rubyFgLabel.setName("rubyFgLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(rubyFgLabel, gridBagConstraints);

        rubyFgButton.setText(bundle.getString("PreferencesDialog.rubyFgButton.text")); // NOI18N
        rubyFgButton.setDefaultCapable(false);
        rubyFgButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        rubyFgButton.setMaximumSize(null);
        rubyFgButton.setMinimumSize(null);
        rubyFgButton.setName("rubyFgButton"); // NOI18N
        rubyFgButton.setPreferredSize(null);
        rubyFgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rubyFgButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        jPanel1.add(rubyFgButton, gridBagConstraints);

        highlightLabel.setText(bundle.getString("PreferencesDialog.highlightLabel.text")); // NOI18N
        highlightLabel.setName("highlightLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(highlightLabel, gridBagConstraints);

        highlightButton.setText(bundle.getString("PreferencesDialog.highlightButton.text")); // NOI18N
        highlightButton.setDefaultCapable(false);
        highlightButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        highlightButton.setMaximumSize(null);
        highlightButton.setMinimumSize(null);
        highlightButton.setName("highlightButton"); // NOI18N
        highlightButton.setPreferredSize(null);
        highlightButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                highlightButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        jPanel1.add(highlightButton, gridBagConstraints);

        fontLabel.setText(bundle.getString("PreferencesDialog.fontLabel.text")); // NOI18N
        fontLabel.setName("fontLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(fontLabel, gridBagConstraints);

        fontButton.setText(bundle.getString("PreferencesDialog.fontButton.text")); // NOI18N
        fontButton.setDefaultCapable(false);
        fontButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        fontButton.setMaximumSize(null);
        fontButton.setMinimumSize(null);
        fontButton.setName("fontButton"); // NOI18N
        fontButton.setPreferredSize(null);
        fontButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 6);
        jPanel1.add(fontButton, gridBagConstraints);

        rubyScaleLabel.setText(bundle.getString("PreferencesDialog.rubyScaleLabel.text")); // NOI18N
        rubyScaleLabel.setName("rubyScaleLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        jPanel1.add(rubyScaleLabel, gridBagConstraints);

        rubyScaleSpinner.setModel(new javax.swing.SpinnerNumberModel(100, 10, 200, 1));
        rubyScaleSpinner.setName("rubyScaleSpinner"); // NOI18N
        rubyScaleSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rubyScaleSpinnerStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 6, 0);
        jPanel1.add(rubyScaleSpinner, gridBagConstraints);

        jLabel1.setText("%"); // NOI18N
        jLabel1.setName("jLabel1"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        jPanel1.add(jLabel1, gridBagConstraints);

        lyricsDisplayPanel.add(jPanel1, java.awt.BorderLayout.LINE_START);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        lyricsTextPane.setFont(new java.awt.Font("Dialog", 0, 24)); // NOI18N
        lyricsTextPane.setName("lyricsTextPane"); // NOI18N
        jScrollPane1.setViewportView(lyricsTextPane);

        lyricsDisplayPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        mainPanel.add(lyricsDisplayPanel, gridBagConstraints);

        defaultFolderPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PreferencesDialog.DefaultFolder.border.title"))); // NOI18N
        defaultFolderPanel.setName("defaultFolderPanel"); // NOI18N
        defaultFolderPanel.setLayout(new java.awt.GridBagLayout());

        fileFolderField.setColumns(24);
        fileFolderField.setName("fileFolderField"); // NOI18N
        fileFolderField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileFolderFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        defaultFolderPanel.add(fileFolderField, gridBagConstraints);

        browseButton.setText(bundle.getString("PreferencesDialog.browseButton.text")); // NOI18N
        browseButton.setDefaultCapable(false);
        browseButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        browseButton.setName("browseButton"); // NOI18N
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        defaultFolderPanel.add(browseButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(defaultFolderPanel, gridBagConstraints);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PreferencesDialog.soundbankPanel.border.title"))); // NOI18N
        jPanel2.setName("soundbankPanel"); // NOI18N
        jPanel2.setLayout(new java.awt.GridBagLayout());

        soundbankFolderField.setColumns(24);
        soundbankFolderField.setName("defaultSoundbankField"); // NOI18N
        soundbankFolderField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                soundbankFolderFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        jPanel2.add(soundbankFolderField, gridBagConstraints);

        browseSoundbankButton.setText(bundle.getString("PreferencesDialog.browseSoundbankButton.text")); // NOI18N
        browseSoundbankButton.setDefaultCapable(false);
        browseSoundbankButton.setMargin(new java.awt.Insets(1, 3, 1, 3));
        browseSoundbankButton.setName("browseSoundbankButton"); // NOI18N
        browseSoundbankButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseSoundbankButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        jPanel2.add(browseSoundbankButton, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(jPanel2, gridBagConstraints);

        lookAndFeelPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("PreferencesDialog.LookAndFeel.border.title"))); // NOI18N
        lookAndFeelPanel.setName("lookAndFeelPanel"); // NOI18N
        lookAndFeelPanel.setLayout(new java.awt.GridBagLayout());

        lookAndFeelCombo.setName("lookAndFeelCombo"); // NOI18N
        lookAndFeelCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lookAndFeelComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(5, 5, 8, 5);
        lookAndFeelPanel.add(lookAndFeelCombo, gridBagConstraints);

        uiScaleLabel.setText(bundle.getString("PreferencesDialog.uiScaleLabel.text")); // NOI18N
        uiScaleLabel.setName("uiScaleLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(5, 9, 5, 5);
        lookAndFeelPanel.add(uiScaleLabel, gridBagConstraints);

        uiScaleField.setColumns(5);
        uiScaleField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#.###"))));
        uiScaleField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        uiScaleField.setToolTipText(UiStrings.getString("PreferencesDialog.UiScale.ToolTip.text")); // NOI18N
        uiScaleField.setName("uiScaleField"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 9, 0, 9);
        lookAndFeelPanel.add(uiScaleField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(lookAndFeelPanel, gridBagConstraints);

        traceDialogPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(UiStrings.getString("PreferencesDialog.traceDialogPanel.border.title"))); // NOI18N
        traceDialogPanel.setName("traceDialogPanel"); // NOI18N
        traceDialogPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 2, 2));

        traceDialogCheckbox.setText(UiStrings.getString("PreferencesDialog.traceDialogCheckbox.text")); // NOI18N
        traceDialogCheckbox.setName("traceDialogCheckbox"); // NOI18N
        traceDialogCheckbox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traceDialogCheckboxActionPerformed(evt);
            }
        });
        traceDialogPanel.add(traceDialogCheckbox);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        mainPanel.add(traceDialogPanel, gridBagConstraints);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        controlPanel.setName("controlPanel"); // NOI18N
        controlPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING));

        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        okButton.setText(bundle.getString("PreferencesDialog.okButton.text")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        applyButton.setText(bundle.getString("PreferencesDialog.applyButton.text")); // NOI18N
        applyButton.setName("applyButton"); // NOI18N
        applyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                applyButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(applyButton);

        cancelButton.setText(bundle.getString("PreferencesDialog.cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        controlPanel.add(buttonPanel);

        getContentPane().add(controlPanel, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        savePreferences();
        applyChanges();
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void highlightButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_highlightButtonActionPerformed
        Color c = JColorChooser.showDialog(this,
            UiStrings.getString("PreferencesDialog.SelectHighlightColour.title"),
            mHighlight);
        if (c != null) {
            mHighlight = c;
            Style s = lyricsTextPane.getStyle("highlight");
            StyleConstants.setBackground(s, c);
            displayText();
        }
    }//GEN-LAST:event_highlightButtonActionPerformed

    private void lyricBgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lyricBgButtonActionPerformed
        LookAndFeel laf = UIManager.getLookAndFeel();
        if ("Nimbus".equals(laf.getName())) {
            JOptionPane.showMessageDialog(this,
                UiStrings.getString("PreferencesDialog.NoNimbusBg.message"),
                UiStrings.getString("PreferencesDialog.NoNimbusBg.title"),
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        Color c = JColorChooser.showDialog(this,
            UiStrings.getString("PreferencesDialog.SelectBgColour.title"),
            mBackground);
        if (c != null) {
            mBackground = c;
            lyricsTextPane.setBackground(c);
        }
    }//GEN-LAST:event_lyricBgButtonActionPerformed

    private void lyricFgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lyricFgButtonActionPerformed
        Color c = JColorChooser.showDialog(this,
            UiStrings.getString("PreferencesDialog.SelectFgColour.title"),
            mForeground);
        if (c != null) {
            mForeground = c;
            Style s = lyricsTextPane.getStyle("regular");
            StyleConstants.setForeground(s, c);
            displayText();
        }
    }//GEN-LAST:event_lyricFgButtonActionPerformed

    private void rubyFgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rubyFgButtonActionPerformed
        Color c = JColorChooser.showDialog(this,
            UiStrings.getString("PreferencesDialog.SelectRubyColour.title"),
            mRubyFg);
        if (c != null) {
            mRubyFg = c;
            Style s = lyricsTextPane.getStyle("ruby");
            StyleConstants.setForeground(s, c);
            displayText();
        }
    }//GEN-LAST:event_rubyFgButtonActionPerformed

    private void fontButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fontButtonActionPerformed
        if (mFontSelector == null) {
            mFontSelector = new FontSelector(mParentFrame, false);
            mFontSelector.setLocationRelativeTo(lookAndFeelPanel);
            mFontSelector.addFontSelectionListener(this);
        }
        mFontSelector.setVisible(true);
        mFontSelector.setSelectedFont(MqfProperties.getFontProperty(
            MqfProperties.LYRIC_FONT,
            lyricsTextPane.getFont()));
        displayText();
    }//GEN-LAST:event_fontButtonActionPerformed

    private void applyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_applyButtonActionPerformed
        savePreferences();
        applyChanges();
    }//GEN-LAST:event_applyButtonActionPerformed

    private void rubyScaleSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rubyScaleSpinnerStateChanged
        mRubyScale = ((Integer)rubyScaleSpinner.getValue()) / 100f;
        Style s = lyricsTextPane.getStyle("ruby");
        float size = lyricsTextPane.getFont().getSize() * mRubyScale;
        StyleConstants.setFontSize(s, Math.round(size));
        displayText();
    }//GEN-LAST:event_rubyScaleSpinnerStateChanged

    private void lookAndFeelComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lookAndFeelComboActionPerformed
        mLafName = lookAndFeelCombo.getSelectedItem().toString();
    }//GEN-LAST:event_lookAndFeelComboActionPerformed

    private void browseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseButtonActionPerformed

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(fileFolderField.getText()));
        int open = fileChooser.showDialog(
            this, UiStrings.getString("PreferencesDialog.AcceptDefaultPath.text"));
        if (open == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            mDefaultPath = path;
        }

    }//GEN-LAST:event_browseButtonActionPerformed

    private void fileFolderFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileFolderFieldActionPerformed
        mDefaultPath = fileFolderField.getText();
    }//GEN-LAST:event_fileFolderFieldActionPerformed

    private void soundbankFolderFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_soundbankFolderFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_soundbankFolderFieldActionPerformed

    private void browseSoundbankButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseSoundbankButtonActionPerformed

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setCurrentDirectory(new File(soundbankFolderField.getText()));
        int open = fileChooser.showDialog(
            this, UiStrings.getString("PreferencesDialog.AcceptSoundbankPath.text"));
        if (open == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String path = file.getAbsolutePath();
            mDefaultSoundbankPath = path;
        }

    }//GEN-LAST:event_browseSoundbankButtonActionPerformed

    private void traceDialogCheckboxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traceDialogCheckboxActionPerformed

        mShowTraceDialog = traceDialogCheckbox.isSelected();

    }//GEN-LAST:event_traceDialogCheckboxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton applyButton;
    private javax.swing.JButton browseButton;
    private javax.swing.JButton browseSoundbankButton;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JPanel defaultFolderPanel;
    private javax.swing.JTextField fileFolderField;
    private javax.swing.JButton fontButton;
    private javax.swing.JLabel fontLabel;
    private javax.swing.JButton highlightButton;
    private javax.swing.JLabel highlightLabel;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JComboBox<String> lookAndFeelCombo;
    private javax.swing.JPanel lookAndFeelPanel;
    private javax.swing.JButton lyricBgButton;
    private javax.swing.JLabel lyricBgLabel;
    private javax.swing.JButton lyricFgButton;
    private javax.swing.JLabel lyricFgLabel;
    private javax.swing.JPanel lyricsDisplayPanel;
    private javax.swing.JTextPane lyricsTextPane;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JButton rubyFgButton;
    private javax.swing.JLabel rubyFgLabel;
    private javax.swing.JLabel rubyScaleLabel;
    private javax.swing.JSpinner rubyScaleSpinner;
    private javax.swing.JTextField soundbankFolderField;
    private javax.swing.JCheckBox traceDialogCheckbox;
    private javax.swing.JPanel traceDialogPanel;
    private javax.swing.JFormattedTextField uiScaleField;
    private javax.swing.JLabel uiScaleLabel;
    // End of variables declaration//GEN-END:variables
}
