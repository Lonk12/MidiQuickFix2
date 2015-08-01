/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2009 John Lemcke
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
package com.lemckes.MidiQuickFix.components;

import com.lemckes.MidiQuickFix.util.FontSelectionEvent;
import com.lemckes.MidiQuickFix.util.FontSelectionListener;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import javax.swing.DefaultComboBoxModel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * A dialog that allows the user to choose a font.
 *
 * @version $Id$
 */
public class FontSelector
    extends javax.swing.JDialog
{

    static final long serialVersionUID = -862613500313024646L;
    /**
     * The font attributes.
     */
    private SimpleAttributeSet mAttributes;
    /**
     * The selected font.
     */
    private Font mFont;
    /**
     * Is this dialog modal
     */
    private boolean mModal;
    /**
     * The status to return
     */
    private int mReturnStatus = RET_CANCEL;
    /**
     * A return status code - returned if Cancel button has been pressed
     */
    public static final int RET_CANCEL = 0;
    /**
     * A return status code - returned if OK button has been pressed
     */
    public static final int RET_OK = 1;
    /**
     * The list of registered listeners.
     */
    protected EventListenerList mListenerList;

    /**
     * Creates a new FontSelector dialog.
     */
    public FontSelector(Frame parent, boolean modal) {
        super(parent, modal);
        mModal = modal;
        initComponents();

        sizeCombo.setModel(new DefaultComboBoxModel<>(
            new String[]{
                "6", "7", "8", "9", "10", "12", "14", "18", "20", "24", "28",
                "32", "36", "40", "44", "48", "56", "64", "72"}));

        if (mModal) {
            okButton.setText(UiStrings.getString("ok"));
            cancelButton.setText(UiStrings.getString("cancel"));
        } else {
            okButton.setText(UiStrings.getString("apply"));
            cancelButton.setText(UiStrings.getString("close"));
        }

        getRootPane().setDefaultButton(okButton);

        mAttributes = new SimpleAttributeSet();

        fontList.setModel(new javax.swing.AbstractListModel<String>()
        {

            static final long serialVersionUID = -3821347059262633012L;
            GraphicsEnvironment ge
                = GraphicsEnvironment.getLocalGraphicsEnvironment();
            String[] fontNames = ge.getAvailableFontFamilyNames();

            @Override
            public int getSize() {
                return fontNames.length;
            }

            @Override
            public String getElementAt(int i) {
                return fontNames[i];
            }
        });

        fontList.addListSelectionListener((ListSelectionEvent e) -> {
            StyleConstants.setFontFamily(mAttributes,
                (String)fontList.getSelectedValue());
            updateFontPreview();
        });

        // Set the default values for the font attributes.
        fontList.setSelectedValue("Dialog", true);
        StyleConstants.setFontFamily(mAttributes,
            (String)fontList.getSelectedValue());
        StyleConstants.setItalic(mAttributes, italicCheckBox.isSelected());
        StyleConstants.setBold(mAttributes, boldCheckBox.isSelected());
        sizeCombo.setSelectedItem("12");
        StyleConstants.setFontSize(mAttributes, 12);

        updateFontPreview();

        mListenerList = new EventListenerList();
    }

    /**
     * Add a listener that will be notified when a font is selected.
     *
     * @param l the listener to add
     */
    public void addFontSelectionListener(FontSelectionListener l) {
        mListenerList.add(FontSelectionListener.class, l);
    }

    private void updateFontPreview() {
        String name = StyleConstants.getFontFamily(mAttributes);
        boolean bold = StyleConstants.isBold(mAttributes);
        boolean ital = StyleConstants.isItalic(mAttributes);
        int size = StyleConstants.getFontSize(mAttributes);

        mFont = new Font(name, (bold ? Font.BOLD : 0)
            + (ital ? Font.ITALIC : 0), size);
        previewText.setFont(mFont);

        this.validate();
    }

    /**
     * Get the selected font.
     *
     * @return The selected font.
     */
    public Font getSelectedFont() {
        return mFont;
    }

    /**
     * Set the selected font based on the font attributes
     *
     * @param name The font family name
     * @param bold true for a bold font
     * @param italic true for an italic font
     * @param size the point size of the font
     */
    public void setSelectedFont(String name, boolean bold, boolean italic,
        int size) {
        fontList.setSelectedValue(name, true);
        StyleConstants.setFontFamily(mAttributes, name);
        sizeCombo.setSelectedItem(Integer.toString(size));
        StyleConstants.setFontSize(mAttributes, size);
        boldCheckBox.setSelected(bold);
        StyleConstants.setBold(mAttributes, bold);
        italicCheckBox.setSelected(italic);
        StyleConstants.setItalic(mAttributes, italic);

        updateFontPreview();
        fontList.ensureIndexIsVisible(fontList.getSelectedIndex());
        
    }

    /**
     * Set the selected font to the given font
     *
     * @param font the font to be selected
     */
    public void setSelectedFont(Font font) {
        String name = font.getFamily();
        boolean bold = font.isBold();
        boolean ital = font.isItalic();
        int size = font.getSize();
        setSelectedFont(name, bold, ital, size);
    }

    /**
     * @return the return status of this dialog - one of RET_OK or RET_CANCEL
     */
    public int getReturnStatus() {
        return mReturnStatus;
    }

    private void setFontSize(int size) {
        StyleConstants.setFontSize(mAttributes, size);
        updateFontPreview();
    }

    private void setBold(boolean isBold) {
        StyleConstants.setBold(mAttributes, isBold);
        updateFontPreview();
    }

    private void setItalic(boolean isItalic) {
        StyleConstants.setItalic(mAttributes, isItalic);
        updateFontPreview();
    }

    private void fireFontSelected() {
        FontSelectionListener[] listeners
            = mListenerList.getListeners(FontSelectionListener.class);
        FontSelectionEvent e = new FontSelectionEvent(mFont);
        for (int i = listeners.length - 1; i >= 0; --i) {
            listeners[i].fontSelected(e);
        }
    }

    private void doClose(int retStatus) {
        mReturnStatus = retStatus;
        setVisible(false);
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        insetPanel = new javax.swing.JPanel();
        mainPanel = new javax.swing.JPanel();
        fontScrollPane = new javax.swing.JScrollPane();
        fontList = new javax.swing.JList<String>();
        attributesPanel = new javax.swing.JPanel();
        sizeLabel = new javax.swing.JLabel();
        boldCheckBox = new javax.swing.JCheckBox();
        italicCheckBox = new javax.swing.JCheckBox();
        sizeCombo = new javax.swing.JComboBox<String>();
        styleLabel = new javax.swing.JLabel();
        previewText = new javax.swing.JTextField();
        familyLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        buttonGrid = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(UiStrings.getString("font_chooser")); // NOI18N
        setName("fontChooserDialog"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });
        getContentPane().setLayout(new java.awt.BorderLayout(12, 12));

        insetPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 11, 11));
        insetPanel.setName("insetPanel"); // NOI18N
        insetPanel.setLayout(new java.awt.BorderLayout(0, 12));

        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.BorderLayout(6, 6));

        fontScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        fontScrollPane.setMinimumSize(new java.awt.Dimension(22, 131));
        fontScrollPane.setName("fontScrollPane"); // NOI18N

        fontList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fontList.setName("fontList"); // NOI18N
        fontScrollPane.setViewportView(fontList);

        mainPanel.add(fontScrollPane, java.awt.BorderLayout.CENTER);

        attributesPanel.setName("attributesPanel"); // NOI18N
        attributesPanel.setLayout(new java.awt.GridBagLayout());

        sizeLabel.setText(UiStrings.getString("size")); // NOI18N
        sizeLabel.setName("sizeLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        attributesPanel.add(sizeLabel, gridBagConstraints);

        boldCheckBox.setFont(boldCheckBox.getFont().deriveFont(boldCheckBox.getFont().getStyle() | java.awt.Font.BOLD));
        boldCheckBox.setText(UiStrings.getString("bold")); // NOI18N
        boldCheckBox.setName("boldCheckBox"); // NOI18N
        boldCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                boldCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 0, 0, 0);
        attributesPanel.add(boldCheckBox, gridBagConstraints);

        italicCheckBox.setFont(italicCheckBox.getFont().deriveFont((italicCheckBox.getFont().getStyle() | java.awt.Font.ITALIC)));
        italicCheckBox.setText(UiStrings.getString("italic")); // NOI18N
        italicCheckBox.setName("italicCheckBox"); // NOI18N
        italicCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                italicCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        attributesPanel.add(italicCheckBox, gridBagConstraints);

        sizeCombo.setEditable(true);
        sizeCombo.setMinimumSize(new java.awt.Dimension(62, 20));
        sizeCombo.setName("sizeCombo"); // NOI18N
        sizeCombo.setPreferredSize(new java.awt.Dimension(62, 20));
        sizeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sizeComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        attributesPanel.add(sizeCombo, gridBagConstraints);

        styleLabel.setText(UiStrings.getString("style")); // NOI18N
        styleLabel.setName("styleLabel"); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 6);
        attributesPanel.add(styleLabel, gridBagConstraints);

        mainPanel.add(attributesPanel, java.awt.BorderLayout.EAST);

        previewText.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        previewText.setText(UiStrings.getString("sample_text_preview")); // NOI18N
        previewText.setToolTipText(UiStrings.getString("sample_text")); // NOI18N
        previewText.setName("previewText"); // NOI18N
        mainPanel.add(previewText, java.awt.BorderLayout.SOUTH);

        familyLabel.setText(UiStrings.getString("family")); // NOI18N
        familyLabel.setName("familyLabel"); // NOI18N
        mainPanel.add(familyLabel, java.awt.BorderLayout.NORTH);

        insetPanel.add(mainPanel, java.awt.BorderLayout.CENTER);

        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 0, 0));

        buttonGrid.setName("buttonGrid"); // NOI18N
        buttonGrid.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        okButton.setText(UiStrings.getString("apply")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonGrid.add(okButton);

        cancelButton.setText(UiStrings.getString("close")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonGrid.add(cancelButton);

        buttonPanel.add(buttonGrid);

        insetPanel.add(buttonPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(insetPanel, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void italicCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_italicCheckBoxActionPerformed
        setItalic(italicCheckBox.isSelected());
    }//GEN-LAST:event_italicCheckBoxActionPerformed

    private void boldCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_boldCheckBoxActionPerformed
        setBold(boldCheckBox.isSelected());
    }//GEN-LAST:event_boldCheckBoxActionPerformed

    private void sizeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sizeComboActionPerformed
        // Default to the previous value if the parseInt fails.
        int size = StyleConstants.getFontSize(mAttributes);
        try {
            size = Integer.parseInt((String)sizeCombo.getSelectedItem());
            setFontSize(size);
        } catch (NumberFormatException nfe) {
            // Ignore it
            sizeCombo.setSelectedItem(Integer.toString(size));
        }
    }//GEN-LAST:event_sizeComboActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        mReturnStatus = RET_OK;
        fireFontSelected();
        if (mModal) {
            doClose(RET_OK);
        }
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }//GEN-LAST:event_closeDialog


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel attributesPanel;
    private javax.swing.JCheckBox boldCheckBox;
    private javax.swing.JPanel buttonGrid;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JLabel familyLabel;
    private javax.swing.JList<String> fontList;
    private javax.swing.JScrollPane fontScrollPane;
    private javax.swing.JPanel insetPanel;
    private javax.swing.JCheckBox italicCheckBox;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JButton okButton;
    private javax.swing.JTextField previewText;
    private javax.swing.JComboBox<String> sizeCombo;
    private javax.swing.JLabel sizeLabel;
    private javax.swing.JLabel styleLabel;
    // End of variables declaration//GEN-END:variables
}
