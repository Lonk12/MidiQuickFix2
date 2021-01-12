/** ************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2021 John Lemcke
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
 ************************************************************* */
package com.lemckes.MidiQuickFix;

import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Show the About dialog.
 */
public class AboutDialog
    extends javax.swing.JDialog
{

    static final long serialVersionUID = 934886629248418748L;

    /**
     * The component that displays our splash screen
     */
    SplashDrawing mSplash;

    java.awt.Frame mParent;

    /**
     * Creates an About dialog
     *
     * @param parent the Frame parent of the dialog
     * @param modal the modality of the dialog
     */
    public AboutDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        mParent = parent;
        mSplash = new SplashDrawing();
        mSplash.setCentredText(true);

        initComponents();

        URL url = getClass().getResource("Artistic.clarified"); // NOI18N
        try {
            FileReader fr = new FileReader(new File(url.getFile()));
            BufferedReader br = new BufferedReader(fr);
            String l;
            boolean done = false;
            while (!done) {
                l = br.readLine();
                if (l == null) {
                    done = true;
                } else {
                    licenseText.append(l + "\n");
                }
            }
            licenseText.setCaretPosition(0);
        } catch (FileNotFoundException ex) {
            TraceDialog.addTrace("Artistic.clarified - File Not Found");
        } catch (IOException ex) {
            TraceDialog.addTrace("Artistic.clarified - IOException");
        }

    }

    /**
     * Override to set the position of the dialog
     *
     * @param visible
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            pack();
            setLocationRelativeTo(mParent);
        }
        super.setVisible(visible);
    }

    /**
     * Populate the messages in the About dialog.
     */
    public void populateMessages() {
        aboutPanel.add(mSplash, java.awt.BorderLayout.CENTER);

        mSplash.setMessageDelay(0);

        mSplash.addStageMessage(UiStrings.getString("about_message")); // NOI18N
        mSplash.addStageMessage(UiStrings.getString("version_string")); // NOI18N
        mSplash.addStageMessage("http://midiquickfix.sourceforge.net"); // NOI18N
        mSplash.addStageMessage(""); // NOI18N
        mSplash.addStageMessage(UiStrings.getString("copyright")); // NOI18N
        mSplash.addStageMessage(UiStrings.getString("contact_email")); // NOI18N

        Properties p = System.getProperties();

        String name
            = p.getProperty("java.vm.name", UiStrings.getString("unknown_java")); // NOI18N
        String version
            = p.getProperty("java.vm.version",
                UiStrings.getString("unknown_version")); // NOI18N
        String vendor
            = p.getProperty("java.vm.vendor",
                UiStrings.getString("unknown_vendor")); // NOI18N

        mSplash.addStageMessage("");
        mSplash.addStageMessage(name + " - " + version);
        mSplash.addStageMessage(UiStrings.getString("vendor") + " : " + vendor); // NOI18N

        String osname
            = p.getProperty("os.name", UiStrings.getString("unknown_os")); // NOI18N
        String osversion
            = p.getProperty("os.version",
                UiStrings.getString("unknown_os_version")); // NOI18N
        String arch
            = p.getProperty("os.arch", UiStrings.getString("unknown_os_arch")); // NOI18N

        mSplash.addStageMessage("");
        mSplash.addStageMessage(osname + " - " + arch + " - " + osversion);

        
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        aboutPanel = new javax.swing.JPanel();
        licensePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        licenseText = new javax.swing.JTextArea();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(UiStrings.getString("about_mqf")); // NOI18N
        setFont(new java.awt.Font("Dialog", 1, 14)); // NOI18N
        setName("aboutDialog"); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        buttonPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        okButton.setText(UiStrings.getString("ok")); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);

        aboutPanel.setLayout(new java.awt.BorderLayout());
        jTabbedPane1.addTab(UiStrings.getString("AboutDialog.aboutPanel.title"), aboutPanel); // NOI18N

        licensePanel.setLayout(new java.awt.BorderLayout());

        licenseText.setEditable(false);
        licenseText.setColumns(20);
        licenseText.setLineWrap(true);
        licenseText.setRows(5);
        jScrollPane1.setViewportView(licenseText);

        licensePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab(UiStrings.getString("AboutDialog.licensePanel.title"), licensePanel); // NOI18N

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed

    /**
     * Closes the dialog
     */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        doClose();
    }//GEN-LAST:event_closeDialog
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel aboutPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JPanel licensePanel;
    private javax.swing.JTextArea licenseText;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
