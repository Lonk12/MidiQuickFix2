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

import java.awt.EventQueue;
import javax.sound.midi.*;

import javax.swing.*;
import javax.swing.event.*;
import java.util.Properties;

import com.lemckes.MidiQuickFix.util.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import com.lemckes.MidiQuickFix.util.TraceDialog;
/**
 * A MIDI file editor which works at the Midi event level.
 * @see javax.sound.midi
 * @version $Id$
 */
public class MidiQuickFix extends JFrame
        implements MidiSeqPlayer {
    
    /**
     * Creates a new MidiQuickFix instance
     */
    public MidiQuickFix() {
        this("");
    }
    
    /**
     * Creates a new MidiQuickFix instance and opens the given midi file.
     * @param fileName The midi file to be opened.
     */
    public MidiQuickFix(String fileName) {
        TraceDialog.getInstance().addComponentListener(new ComponentListener() {
            public void componentShown(ComponentEvent e) {
                traceMenuItem.setState(true);
            }
            
            public void componentHidden(ComponentEvent e) {
                traceMenuItem.setState(false);
            }
            
            public void componentMoved(ComponentEvent e) {
            }
            
            public void componentResized(ComponentEvent e) {
            }
        });
        // mTraceDialog.setVisible(true);
        
        Startup startDialog = new Startup(new javax.swing.JFrame(), false);
        startDialog.setVisible(true);
        
        try {
            startDialog.splash.setStageMessage(
                    "Using Java Version " + mJavaVersion);
            trace("Using Java Version " + mJavaVersion);
            
            startDialog.splash.setStageMessage(
                    UiStrings.getString("getting_sequencer"));
            mSequencer = MidiSystem.getSequencer();
            
            startDialog.splash.setStageMessage(
                    UiStrings.getString("get_sequencer_returned"));
            if (mSequencer == null) {
                // Error -- sequencer device is not supported.
                startDialog.splash.setStageMessage(
                        UiStrings.getString("get_sequencer_failed"));
            } else {
                // Acquire resources and make operational.
                startDialog.splash.setStageMessage(
                        UiStrings.getString("opening_sequencer"));
                mSequencer.open(); // This call blocks the process...
                startDialog.splash.setStageMessage(
                        UiStrings.getString("sequencer_opened"));
            }
            if (fileName != null && fileName.length() > 0) {
                startDialog.splash.setStageMessage(
                        UiStrings.getString("opening_file"));
                newSequence(fileName);
            }
            
            mPlayController = new PlayController(mSequencer, mSeq);
            mPlayController.mRewindAction.putValue("rewinder", this);
            mPlayController.mPlayAction.putValue("player", this);
            mPlayController.mPauseAction.putValue("pauser", this);
            mPlayController.mStopAction.putValue("stopper", this);
            mPlayController.mLoopAction.putValue("looper", this);
            
            initComponents();
            sequenceChooser.addChoosableFileFilter(
                    new MidiFileFilter());
            
            startDialog.setVisible(false);
            
            transportPanel.setActions(
                    mPlayController.mRewindAction,
                    mPlayController.mPlayAction,
                    mPlayController.mPauseAction,
                    mPlayController.mStopAction,
                    mPlayController.mLoopAction);
            
            mPlayController.setPlayState(mPlayController.NO_FILE);
            if (fileName != null && fileName.length() > 0) {
                startDialog.splash.setStageMessage(
                        UiStrings.getString("creating_window"));
                setTrackComboModel();
                setInfoLabels();
                selectTrack(0);
                mPlayController.setPlayState(mPlayController.STOPPED);
            }
            
            mSequencer.addMetaEventListener(new EventHandler());
            
            /* Update the position 10 times per second */
            createTimer(100);
            
            mSequenceModified = false;
        } catch(MidiUnavailableException e) {
            startDialog.splash.setStageMessage(
                    UiStrings.getString("no_midi_message"));
            startDialog.splash.setStageMessage(
                    e.getMessage());
            startDialog.splash.setStageMessage(
                    UiStrings.getString("startup_failed"));
            e.printStackTrace();
        }
        
        positionSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                positionSliderStateChanged(evt);
            }
        });
    }
    
    /** Open the given file and create a Sequence object from it.
     * Currently does not validate that the file is a midi file.
     * @param fileName The full path name of the midi file to open.
     */
    public void newSequence(String fileName) {
        java.io.File myMidiFile = new java.io.File(fileName);
        newSequence(myMidiFile);
    }
    
    /** Open the given File and create a Sequence object from it.
     * Currently does not validate that the file is a midi file.
     * The file i/o is performed in a worker thread and when it is
     * complete <code>buildNewSequence()</code> is called in the
     * Swing event thread. Any UI updates that are dependent on
     * the completion of the file operation must be done in
     * <code>buildNewSequence()</code>
     * @param file The midi file to open.
     */
    public void newSequence(final java.io.File file) {
        final SwingWorker worker = new SwingWorker() {
            // Open the file in the worker thread
            public Object construct() {
                try {
                    // Construct a Sequence object
                    mSeq = MidiFile.openSequenceFile(file);
                    
                    // Remember the file name for later
                    mFileName = file.getName();
                    mFilePath = file.getCanonicalPath();
                    
                    return null;
                } catch (IOException e) {
                    trace("IOException in openFile(String fileName) : " + e);
                    e.printStackTrace();
                    return null;
                } catch (InvalidMidiDataException e) {
                    trace("InvalidMidiDataException in openFile(String fileName) : " + e);
                    e.printStackTrace();
                    return null;
                }
            }
            
            //Build sequence in the event-dispatching thread.
            public void finished() {
                if (mSeq != null) {
                    buildNewSequence();
                }
            }
        };
        worker.start();
    }
    
    /**
     * Update the UI after a new Sequence is loaded.
     */
    private void buildNewSequence() {
        mPlayController.setSequence(mSeq);
        mTracks = mSeq.getTracks();
        mResolution = mSeq.getResolution();
        mCurrentTrack = 0;
        trace("openFile: " + mTracks.length + " tracks");
        
        // Assign the sequence to the sequencer.
        try {
            mSequencer.setSequence(mSeq);
        } catch (Exception e) {
            trace("Exception in openFile(String fileName) : " + e);
            e.printStackTrace();
        }
        
        mSequenceModified = false;
        
        if (mTrackSummary != null) {
            mTrackSummary.setSequence(mSeq);
            mTrackSummary.setFileName(mFileName);
        }
        
        if (mLyricDialog != null) {
            mLyricDialog.setFileName(mFileName);
        }
        mPlayController.setPlayState(mPlayController.STOPPED);
        
        setTitle(mFileName);
        setTrackComboModel();
        setInfoLabels();
        selectTrack(0);
    }
    
    /**
     * Check if the sequence needs to be saved.
     * If it does give the user a Yes/No/Cancel dialog.
     * If the user elects to save give them a Save As dialog.
     * @return True if the user chooses to continue with the action,
     * False if the user selects Cancel.
     */
    private boolean checkForSave() {
        // System.out.println("checkForSave");
        boolean continueAction = true;
        if (mSequenceModified) {
            int answer = JOptionPane.showConfirmDialog(null,
                    UiStrings.getString("check_save"),
                    UiStrings.getString("save_changes"),
                    JOptionPane.YES_NO_CANCEL_OPTION);
            if (answer == JOptionPane.YES_OPTION) {
                java.io.File myMidiFile = new java.io.File(mFilePath);
                saveAs(myMidiFile);
            } else if (answer == JOptionPane.NO_OPTION) {
                // Do nothing
            } else if (answer == JOptionPane.CANCEL_OPTION) {
                continueAction = false;
            }
        }
        return continueAction;
    }
    
    /**
     * Save the current sequence to the given file.
     * @param file The file to which to save the sequence.
     */
    public void saveFile(java.io.File file) {
        try {
            // Make sure that the Sequencer has the latest version of the Sequence.
            mSequencer.setSequence(mSeq);
            
            // Save the Sequence object
            int types[] = MidiSystem.getMidiFileTypes(mSeq);
            
            // System.out.println("saveFile - Midi File Types");
            // for (int i = 0; i < types.length; ++i) {
            //     System.out.println("Type " + i + " = " + types[i]);
            // }
            
            // I guess the last one will be the 'best'
            int type = types[types.length - 1];
            int res = MidiSystem.write(mSeq, type, file);
            // System.out.println("saveFile wrote " + res + " bytes");
            mSequenceModified = false;
        } catch (IOException e) {
            trace("IOException in saveFile(java.io.File file) : " + e);
            e.printStackTrace();
        } catch (InvalidMidiDataException e) {
            trace("InvalidMidiDataException in saveFile(java.io.File file) : " + e);
            e.printStackTrace();
        }
    }
    
    /**
     * Save the current sequence to a user selected file.
     * @param file If not null the given file will be selected
     * as the default.
     */
    void saveAs(java.io.File file) {
        if (file != null) {
            sequenceChooser.setSelectedFile(file);
        }
        
        int save = sequenceChooser.showSaveDialog(this);
        if (save == JFileChooser.APPROVE_OPTION) {
            saveFile(sequenceChooser.getSelectedFile());
        }
    }
    
    /**
     * Populate the entries in the track selector combo with
     * the track number and track names from the mTracks array.
     */
    void setTrackComboModel() {
        trace("setTrackComboModel");
        // Update the track selector combobox model
        String[] trackList = new String[mTracks.length];
        for (int i = 0; i < mTracks.length; ++i) {
            trackList[i] = Integer.toString(i);
            Track t = mTracks[i];
            int count = t.size() - 1;
            for (int j = 0; j < count; ++j) {
                MidiEvent ev = t.get(j);
                MidiMessage mess = ev.getMessage();
                // Don't bother looking past events at time zero.
                if (ev.getTick() > 0) break;
                if (mess.getStatus() == MetaMessage.META) {
                    Object[] str = MetaEvent.getMetaStrings((MetaMessage)mess);
                    if (str[0].equals("M:TrackName")) {
                        trackList[i] += " - " + (String)str[2];
                    }
                }
            }
        }
        trackSelector.setModel(
                new DefaultComboBoxModel(trackList));
    }
    
    /** Set the text of the info labels. */
    void setInfoLabels() {
        trace("setInfoLabels");
        ticksLabel.setText(UiStrings.getString("ticks_per_beat") + mResolution);
        long dur = mSeq.getMicrosecondLength()/1000000;
        lengthLabel.setText(UiStrings.getString("duration") + Formats.formatSeconds(dur));
        String key = UiStrings.getString("key");
        String time = UiStrings.getString("timesig");
        String tempo = UiStrings.getString("tempo");
        
        long ticks =  mSeq.getTickLength();
        positionSlider.setDuration(ticks, true, mResolution);
        
        // First get the info from the control track.
        Track t = mTracks[0];
        int count = t.size() - 1;
        for (int j = 0; j < count; ++j) {
            MidiEvent ev = t.get(j);
            MidiMessage mess = ev.getMessage();
            long tick = ev.getTick();
            if (tick > 0) break;
            int st = mess.getStatus();
            if (st == MetaMessage.META) {
                Object[] str = MetaEvent.getMetaStrings((MetaMessage)mess);
                if (str[0].equals("M:TimeSignature")) {
                    time = UiStrings.getString("timesig") + str[2];
                }
                if (str[0].equals("M:Tempo")) {
                    tempo = UiStrings.getString("tempo") + str[2];
                }
                if (str[0].equals("M:KeySignature")) {
                    mKeySig = (String)str[2];
                    key = UiStrings.getString("key") + mKeySig;
                }
            }
        }
        
        // Now get the current track info.
        t = mTracks[mCurrentTrack];
        count = t.size() - 1;
        for (int j = 0; j < count; ++j) {
            MidiEvent ev = t.get(j);
            MidiMessage mess = ev.getMessage();
            long tick = ev.getTick();
            if (tick > 0) break;
            int st = mess.getStatus();
            // Assume that any meta data in the current track
            // overrides that found in the control track.
            if (st == MetaMessage.META) {
                Object[] str = MetaEvent.getMetaStrings((MetaMessage)mess);
                if (str[0].equals("M:TimeSignature")) {
                    time = UiStrings.getString("timesig") + str[2];
                }
                if (str[0].equals("M:Tempo")) {
                    tempo = UiStrings.getString("tempo") + str[2];
                }
                if (str[0].equals("M:KeySignature")) {
                    mKeySig = (String)str[2];
                    key = UiStrings.getString("key") + mKeySig;
                }
            }
        }
        timeLabel.setText(time);
        tempoLabel.setText(tempo);
        keyLabel.setText(key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form MidiQuickFix.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sequenceChooser = new javax.swing.JFileChooser();
        mainPanel = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        actionsPanel = new javax.swing.JPanel();
        transportPanel = new com.lemckes.MidiQuickFix.components.TransportPanel();
        progressPanel = new javax.swing.JPanel();
        positionPanel = new javax.swing.JPanel();
        posLabel = new javax.swing.JLabel();
        lengthLabel = new javax.swing.JLabel();
        positionSlider = new com.lemckes.MidiQuickFix.components.LoopSlider();
        trackPanel = new javax.swing.JPanel();
        trackLabel = new javax.swing.JLabel();
        trackSelector = new javax.swing.JComboBox();
        infoPanel = new javax.swing.JPanel();
        keyLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        tempoLabel = new javax.swing.JLabel();
        ticksLabel = new javax.swing.JLabel();
        tablePanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        trackTable = new com.lemckes.MidiQuickFix.TrackTable();
        bottomPanel = new javax.swing.JPanel();
        deleteButton = new javax.swing.JButton();
        showNotesCheck = new javax.swing.JCheckBox();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        sequenceMenu = new javax.swing.JMenu();
        playMenuItem = new javax.swing.JMenuItem();
        pauseMenuItem = new javax.swing.JMenuItem();
        stopMenuItem = new javax.swing.JMenuItem();
        rewindMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        splitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        editorMenuItem = new javax.swing.JMenuItem();
        lyricsMenuItem = new javax.swing.JMenuItem();
        summaryMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        traceMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        contentsMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        sequenceChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequenceChooserActionPerformed(evt);
            }
        });

        setTitle(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("mqf"));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        mainPanel.setLayout(new java.awt.BorderLayout());

        mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 11, 11)));
        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.Y_AXIS));

        actionsPanel.setLayout(new java.awt.GridBagLayout());

        actionsPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 6, 0)));
        transportPanel.setAlignmentX(1.0F);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        actionsPanel.add(transportPanel, gridBagConstraints);

        topPanel.add(actionsPanel);

        progressPanel.setLayout(new java.awt.GridBagLayout());

        progressPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 6, 0)));
        positionPanel.setLayout(new javax.swing.BoxLayout(positionPanel, javax.swing.BoxLayout.X_AXIS));

        posLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("position"));
        posLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 6, 0, 0)));
        positionPanel.add(posLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        progressPanel.add(positionPanel, gridBagConstraints);

        lengthLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("duration"));
        lengthLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 6)));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        progressPanel.add(lengthLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        progressPanel.add(positionSlider, gridBagConstraints);

        topPanel.add(progressPanel);

        trackPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));

        trackPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 6, 0)));
        trackLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("track"));
        trackPanel.add(trackLabel);

        trackSelector.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "0" }));
        trackSelector.setAlignmentX(0.0F);
        trackSelector.setName("trackSelector");
        trackSelector.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trackSelectorActionPerformed(evt);
            }
        });

        trackPanel.add(trackSelector);

        topPanel.add(trackPanel);

        infoPanel.setLayout(new java.awt.GridLayout(1, 0, 3, 0));

        keyLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("key"));
        infoPanel.add(keyLabel);

        timeLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("timesig"));
        infoPanel.add(timeLabel);

        tempoLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("tempo"));
        infoPanel.add(tempoLabel);

        ticksLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("ticks_per_beat"));
        infoPanel.add(ticksLabel);

        topPanel.add(infoPanel);

        mainPanel.add(topPanel, java.awt.BorderLayout.NORTH);

        tablePanel.setLayout(new java.awt.BorderLayout());

        tablePanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(3, 0, 3, 0)));
        tableScrollPane.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        tableScrollPane.setPreferredSize(new java.awt.Dimension(500, 403));
        tableScrollPane.setViewportView(trackTable);

        tablePanel.add(tableScrollPane, java.awt.BorderLayout.CENTER);

        mainPanel.add(tablePanel, java.awt.BorderLayout.CENTER);

        bottomPanel.setLayout(new java.awt.BorderLayout());

        deleteButton.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("delete_selected"));
        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        bottomPanel.add(deleteButton, java.awt.BorderLayout.EAST);

        showNotesCheck.setSelected(true);
        showNotesCheck.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("show_notes"));
        showNotesCheck.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                showNotesCheckItemStateChanged(evt);
            }
        });

        bottomPanel.add(showNotesCheck, java.awt.BorderLayout.WEST);

        mainPanel.add(bottomPanel, java.awt.BorderLayout.SOUTH);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("file"));
        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setMnemonic('O');
        openMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("open"));
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("save"));
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("saveas"));
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("exit"));
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("edit"));
        editMenu.setEnabled(false);
        cutMenuItem.setMnemonic('C');
        cutMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("cut"));
        editMenu.add(cutMenuItem);

        copyMenuItem.setMnemonic('o');
        copyMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("copy"));
        editMenu.add(copyMenuItem);

        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("paste"));
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setMnemonic('D');
        deleteMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("delete"));
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        sequenceMenu.setMnemonic('S');
        sequenceMenu.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("sequence"));
        playMenuItem.setAction(mPlayController.mPlayAction);
        sequenceMenu.add(playMenuItem);

        pauseMenuItem.setAction(mPlayController.mPauseAction);
        sequenceMenu.add(pauseMenuItem);

        stopMenuItem.setAction(mPlayController.mStopAction);
        sequenceMenu.add(stopMenuItem);

        rewindMenuItem.setAction(mPlayController.mRewindAction);
        sequenceMenu.add(rewindMenuItem);

        sequenceMenu.add(jSeparator1);

        splitMenuItem.setText("Split");
        splitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitMenuItemActionPerformed(evt);
            }
        });

        sequenceMenu.add(splitMenuItem);

        menuBar.add(sequenceMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText("View");
        editorMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("track_editor"));
        editorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editorMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(editorMenuItem);

        lyricsMenuItem.setMnemonic('L');
        lyricsMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("lyrics"));
        lyricsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lyricsMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(lyricsMenuItem);

        summaryMenuItem.setMnemonic('T');
        summaryMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("track_summary"));
        summaryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(summaryMenuItem);

        viewMenu.add(jSeparator2);

        traceMenuItem.setMnemonic('r');
        traceMenuItem.setText("Trace");
        traceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traceMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(traceMenuItem);

        menuBar.add(viewMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("help"));
        contentsMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("contents"));
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("about"));
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }
    // </editor-fold>//GEN-END:initComponents

    private void editorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editorMenuItemActionPerformed
        if (mTrackEditor == null) {
            mTrackEditor = new TrackEditorDialog(this, false);
        }
        if (mSeq != null) {
            mTrackEditor.setTrack(mTracks[0], mResolution, true, KeySignatures.isInFlats(mKeySig));
            mTrackEditor.setFileName(mFileName);
            mTrackEditor.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    UiStrings.getString("no_sequence_message"),
                    UiStrings.getString("no_sequence_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_editorMenuItemActionPerformed
    
    private void traceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traceMenuItemActionPerformed
        TraceDialog.getInstance().setVisible(traceMenuItem.getState());
    }//GEN-LAST:event_traceMenuItemActionPerformed
    
    private void positionSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_positionSliderStateChanged
        JSlider source = (JSlider)evt.getSource();
        if (source.getValueIsAdjusting()) {
            long pos = (long)source.getValue();
            mSequencer.setTickPosition(pos);
            //mSequencer.setMicrosecondPosition(pos * 1000000);
            setPositionIndicator(true);
            if (!mSequencer.isRunning()) {
                //mPlayController.setPausedPosition(pos * 1000000);
                mPlayController.setPausedPosition(pos); // in ticks
            }
        }
    }//GEN-LAST:event_positionSliderStateChanged
    
    private void splitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_splitMenuItemActionPerformed
        
        JOptionPane.showMessageDialog(this,
                UiStrings.getString("not_implemented_message"),
                UiStrings.getString("not_implemented_title"),
                JOptionPane.ERROR_MESSAGE);
        return;
        
        /*
        Track t[] = new Track[16];
        Track t0 = mTracks[0];
        for (int i = 0; i < 16; ++i) {
            t[i] = mSeq.createTrack();
            MetaMessage mm = new MetaMessage();
            String name = "Channel" + String.valueOf(i);
            char[] cName = name.toCharArray();
            byte[] bName = new byte[cName.length];
            for (int j = 0; j < cName.length; ++j) {
                bName[j] = (byte)cName[j];
            }
            try {
                mm.setMessage(MetaEvent.trackName, bName, bName.length);
            } catch (InvalidMidiDataException imde) {
                imde.printStackTrace();
            }
            t[i].add(new MidiEvent(mm, 0));
        }
         
        for (int i = 0; i < t0.size(); ++i) {
            MidiEvent ev = t0.get(i);
            MidiMessage mess = ev.getMessage();
            if (mess instanceof ShortMessage) {
                int st = ((ShortMessage)mess).getStatus();
                // Check that this is a channel message
                if ((st & 0xf0) <= 0xf0) {
                    ShortMessage sm = (ShortMessage)mess;
                    //int command = sm.getCommand();
                    int channel = sm.getChannel();
                    //int d1 = sm.getData1();
                    //int d2 = sm.getData2();
                    t[channel].add(ev);
                }
            }
        }
         */
    }//GEN-LAST:event_splitMenuItemActionPerformed
    
    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        if (mAboutDialog == null) {
            mAboutDialog = new AboutDialog(this, false);
        }
        mAboutDialog.setVisible(true);
    }//GEN-LAST:event_aboutMenuItemActionPerformed
    
    private void lyricsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lyricsMenuItemActionPerformed
        if (mLyricDialog == null) {
            mLyricDialog = new LyricDialog(this, false);
        }
        if (mSequencer != null) {
            mLyricDialog.setSequencer(mSequencer);
            mLyricDialog.setFileName(mFileName);
            mLyricDialog.setVisible(true);
        }
    }//GEN-LAST:event_lyricsMenuItemActionPerformed
    
    private void summaryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryMenuItemActionPerformed
        if (mTrackSummary == null) {
            mTrackSummary = new TrackSummaryDialog(this, false);
        }
        if (mSeq != null) {
            mTrackSummary.setSequence(mSeq);
            mTrackSummary.setFileName(mFileName);
            mTrackSummary.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                    UiStrings.getString("no_sequence_message"),
                    UiStrings.getString("no_sequence_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_summaryMenuItemActionPerformed
    
    private void showNotesCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showNotesCheckItemStateChanged
        trackTable.showNotes(showNotesCheck.isSelected());
    }//GEN-LAST:event_showNotesCheckItemStateChanged
    
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        try {
            java.io.File myMidiFile = new java.io.File(mFilePath);
            saveFile(myMidiFile);
        } catch (Exception e) {
            trace("Exception in saveMenuItemActionPerformed : " + e);
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed
    
    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        saveAs(null);
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
    
    private void createTimer(int delay) {
        trace("createTimer");
        java.awt.event.ActionListener taskPerformer =
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //...Perform a task...
                EventQueue.invokeLater(new Runnable() {
                    public void run(){
                        setPositionIndicator(false);
                    }
                });
            }
        };
        mTimer = new Timer(delay, taskPerformer);
    }
    
    private void setPositionIndicator(boolean fromSlider) {
        long ticks = mSequencer.getTickPosition();
        long pos = mSequencer.getMicrosecondPosition()/1000000;
        posLabel.setText(UiStrings.getString("position") + Formats.formatSeconds(pos) +
                " (" + Formats.formatTicks(ticks, mResolution) + ")");
        
        if (!fromSlider) {
            positionSlider.setValue((int)ticks);
        }
    }
    
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        int[] selRows = trackTable.getSelectedRows();
        trackTable.deleteRows(selRows);
    }//GEN-LAST:event_deleteButtonActionPerformed
    
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        boolean canContinue = checkForSave();
        // System.out.println("openMenuItemActionPerformed canContinue = " + canContinue);
        if (canContinue != true) {
            return;
        }
        
        int open = sequenceChooser.showOpenDialog(this);
        if (open == JFileChooser.APPROVE_OPTION) {
            newSequence(sequenceChooser.getSelectedFile());
        }
    }//GEN-LAST:event_openMenuItemActionPerformed
    
    private void sequenceChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequenceChooserActionPerformed
        // System.out.println("sequenceChooserActionPerformed");
    }//GEN-LAST:event_sequenceChooserActionPerformed
    
    private void trackSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackSelectorActionPerformed
        selectTrack(trackSelector.getSelectedIndex());
    }//GEN-LAST:event_trackSelectorActionPerformed
    
    /** Nested class to track changes to the track table. */
    class TableChangeListener implements TableModelListener {
        /**
         * Create a TableChangeListener.
         */
        public TableChangeListener() {
        }
        /** Called on a change to the table
         * @param e The event
         */
        public void tableChanged(TableModelEvent e) {
//            System.out.println("TableChangeListener tableChanged\n");
//            System.out.println("  Column    = " + e.getColumn());
//            System.out.println("  First row = " + e.getFirstRow());
//            System.out.println("  Last row  = " + e.getLastRow());
//            System.out.println("  Type      = " + e.getType());
            
            // Hide Notes fires a Table Structure Changed event which has
            // e.getColumn() == e.ALL_COLUMNS && e.getFirstRow() == e.HEADER_ROW
            // We do not want this treated as an edit to the actual sequence.
            if (e.getColumn() != e.ALL_COLUMNS || e.getFirstRow() != e.HEADER_ROW) {
                mSequenceModified = true;
                boolean wasPlaying =
                        (mPlayController.getPlayState() == mPlayController.PLAYING);
                if (wasPlaying) {
                    mTimer.stop();
                    mPlayController.pause();
                }
                try {
                    mSequencer.setSequence(mSeq);
                } catch (javax.sound.midi.InvalidMidiDataException imde) {
                    trace("Exception in tableChangeListener.tableChanged(TableModelEvent e) " + imde);
                    imde.printStackTrace();
                }
                if (wasPlaying) {
                    mTimer.start();
                    mPlayController.play();
                }
            }
        }
    }
    
    /** Display the selected track in the editor.
     * @param trackNum The index of the track to be displayed.
     */
    void selectTrack(int trackNum) {
        trace("selectTrack(" + trackNum + ")");
        mCurrentTrack = trackNum;
        
        // System.out.println(UiStrings.getString("track") + mCurrentTrack + " selected.");
        trackTable.setTrack(
                mTracks[mCurrentTrack],
                mResolution,
                showNotesCheck.isSelected(),
                KeySignatures.isInFlats(mKeySig));
        
        trackTable.getModel().addTableModelListener(new TableChangeListener());
        
        setInfoLabels();
    }
    
    
    /** Get the nominal key signature for the sequence.
     * @return The first key signature found.
     */
    String getKeySig() {
        return mKeySig;
    }
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        if (checkForSave() == true) {
            System.exit(0);
        }
    }//GEN-LAST:event_exitMenuItemActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        if (checkForSave() == true) {
            System.exit(0);
        }
    }//GEN-LAST:event_exitForm
    
    /**
     * <B>main</B>
     * @param args The command line arguments
     */
    public static void main(String args[]) {
        Properties p = System.getProperties();
        // p.list(System.out);
        mJavaVersion = p.getProperty("java.version", "No java.version found");
        if (mJavaVersion.substring(0, 5).equals("1.4.2")) {
            VERSION_1_4_2_BUG = true;
        } else {
            VERSION_1_4_2_BUG = false;
        }
        new MidiQuickFix().setVisible(true);
    }
    
    /**
     * Flag if we need to work around the bug in 1.4.2
     * which does not allow us to update the values of a
     * ShortEvent.
     */
    static boolean VERSION_1_4_2_BUG;
    static String mJavaVersion;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel actionsPanel;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JButton deleteButton;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editorMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JMenuItem lyricsMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JMenuItem pauseMenuItem;
    private javax.swing.JMenuItem playMenuItem;
    private javax.swing.JLabel posLabel;
    private javax.swing.JPanel positionPanel;
    private com.lemckes.MidiQuickFix.components.LoopSlider positionSlider;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JMenuItem rewindMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JFileChooser sequenceChooser;
    private javax.swing.JMenu sequenceMenu;
    private javax.swing.JCheckBox showNotesCheck;
    private javax.swing.JMenuItem splitMenuItem;
    private javax.swing.JMenuItem stopMenuItem;
    private javax.swing.JMenuItem summaryMenuItem;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel tempoLabel;
    private javax.swing.JLabel ticksLabel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JCheckBoxMenuItem traceMenuItem;
    private javax.swing.JLabel trackLabel;
    private javax.swing.JPanel trackPanel;
    private javax.swing.JComboBox trackSelector;
    private com.lemckes.MidiQuickFix.TrackTable trackTable;
    private com.lemckes.MidiQuickFix.components.TransportPanel transportPanel;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
    
    /** The default sequencer. */
    transient Sequencer mSequencer;
    /** The sequence from the current file. */
    transient Sequence mSeq;
    /** The array of tracks from the sequence. */
    transient Track[] mTracks;
    /** The index of the currently display track. */
    int mCurrentTrack;
    /** The resolution in Ticks/Beat of the file. */
    int mResolution;
    /** The key signature string. */
    String mKeySig;
    
    /** The short name of the current file. */
    String mFileName;
    
    /** The full name and path of the current file. */
    String mFilePath;
    
    /** The timer that drives the position indicator during play. */
    Timer mTimer;
    
    /** True if the sequence has been modified. */
    boolean mSequenceModified;
    
    TrackEditorDialog mTrackEditor = null;
    
    TrackSummaryDialog mTrackSummary = null;
    
    LyricDialog mLyricDialog = null;
    
    AboutDialog mAboutDialog = null;
    
    transient PlayController mPlayController;
    
    public void play() {
        try {
            mSequencer.setSequence(mSeq);
        } catch (InvalidMidiDataException imde) {
            trace("Exception play() " + imde);
            imde.printStackTrace();
        }
        
        // Clear the lyrics, but only if we are playing from the start.
        if (mPlayController.getPausedPosition() == 0 && mLyricDialog != null) {
            mLyricDialog.clear();
        }
        
        mSequencer.start();
        mTimer.start();
    }
    
    public void pause() {
        mSequencer.stop();
        mTimer.stop();
    }
    
    public void resume() {
    }
    
    public void stop() {
        mSequencer.stop();
        //mSequencer.setMicrosecondPosition(0);
        mSequencer.setTickPosition(0);
        mTimer.stop();
        setPositionIndicator(false);
    }
    
    public void rewind() {
        //mSequencer.setMicrosecondPosition(0);
        mSequencer.setTickPosition(0);
        setPositionIndicator(false);
    }
    
    public void loop(boolean loop) {
        if (loop) {
            int inPoint = positionSlider.getLoopInPoint();
            int outPoint = positionSlider.getLoopOutPoint();
            System.out.println("MQF.loop in = " + inPoint + " out = " + outPoint);
            mSequencer.setLoopStartPoint(inPoint);
            mSequencer.setLoopEndPoint(outPoint);
            mSequencer.setLoopCount(javax.sound.midi.Sequencer.LOOP_CONTINUOUSLY);
        } else {
            mSequencer.setLoopCount(0);
        }
    }
    
    public void trace(final String s) {
        TraceDialog.addTrace(s);
    }
    
    public class EventHandler implements MetaEventListener {
        public void meta(final MetaMessage metaMessage) {
            if (!mSequencer.isRunning()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run(){
                        System.out.println("Meta event " +
                                MetaEvent.getMetaStrings(metaMessage)[0] + " " +
                                MetaEvent.getMetaStrings(metaMessage)[1] + " " +
                                MetaEvent.getMetaStrings(metaMessage)[2]);
                        System.out.println("Sequencer has stopped.");
                        stop();
                        mPlayController.stop();
                    }
                });
            }
        }
    }
}
