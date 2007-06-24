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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.sound.midi.*;

import javax.swing.*;
import javax.swing.event.*;
import java.util.Properties;

import com.lemckes.MidiQuickFix.util.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import java.awt.Cursor;
import java.text.DecimalFormat;

/**
 * A MIDI file editor that works at the Midi event level.
 * @see javax.sound.midi
 * @version $Id$
 */
public class MidiQuickFix extends JFrame
    implements MidiSeqPlayer, LoopSliderListener, TableModelListener {
    
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
            
            mPlayController = new PlayController(mSequencer, mSeq);
            
            initComponents();
            
            tempoAdjustField.setValue(1.0f);
            tempoAdjustField.addPropertyChangeListener("value",
                new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    float val = 1.0f;
                    try {
                        val = (Float)tempoAdjustField.getValue();
                    } catch (ClassCastException cce){
                        try {
                            double d = (Double)tempoAdjustField.getValue();
                            val = (float)d;
                        } catch (ClassCastException cce2){
                            try {
                                long l = (Long)tempoAdjustField.getValue();
                                val = (float)l;
                            } catch (ClassCastException cce3){
                                // DO NOTHING
                            }
                        }
                    }
                    setTempoFactor(val);
                }
            });
            
            transportPanel.setActions(
                mPlayController.mRewindAction,
                mPlayController.mPlayAction,
                mPlayController.mPauseAction,
                mPlayController.mStopAction,
                mPlayController.mLoopAction);
            
            mPlayController.mRewindAction.putValue("rewinder", this);
            mPlayController.mPlayAction.putValue("player", this);
            mPlayController.mPauseAction.putValue("pauser", this);
            mPlayController.mStopAction.putValue("stopper", this);
            mPlayController.mLoopAction.putValue("looper", this);
            mPlayController.setPlayState(PlayController.PlayState.NO_FILE);
            
            if (fileName != null && fileName.length() > 0) {
                startDialog.splash.setStageMessage(
                    UiStrings.getString("opening_file"));
                newSequence(fileName);
            }
            
            sequenceChooser.addChoosableFileFilter(new MidiFileFilter());
            
            startDialog.setVisible(false);
            
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
        }
        
        positionSlider.addLoopSliderListener(this);
        
        try {
            mSynth = MidiSystem.getSynthesizer();
        } catch(MidiUnavailableException e) {
            TraceDialog.addTrace("No Synthesiser available." +
                " (Could make playing tricky.)");
        }
        mChannels = mSynth.getChannels();
        
        tempoAdjustSlider.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // the slider range is [0, 200]
                // convert the value to the range [-1.0, 1.0]
                int val = tempoAdjustSlider.getValue();
                float factor = (val - 100) / 100.0f;
                
                // convert from [-1.0, 1.0] to [0.5, 2.0]
                // i.e.  2^-1.0 to 2^1.0
                factor = (float)Math.pow(2.0, factor);
                setTempoFactor(factor);
            }
        });
    }
    
    private void setTempoFactor(float factor) {
        mSequencer.setTempoFactor(factor);
        tempoAdjustField.setValue(factor);
    }
    
    /**
     * Change the mouse pointer to the wait or default cursor
     * @param busy If true show the WAIT_CURSOR otherwise show the default cursor
     */
    public void setBusy(boolean busy) {
        if (busy) {
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(Cursor.getDefaultCursor());
        }
    }
    
    public void loopSliderChanged(LoopSliderEvent evt) {
        if (evt.getValueIsAdjusting()) {
            long pos = (long)evt.getValue();
            mSequencer.setTickPosition(pos);
            if (!mSequencer.isRunning()) {
                mPlayController.setPausedPosition(pos); // in ticks
            }
        }
    }
    
    public void loopPointChanged(LoopSliderEvent evt) {
        int inPoint = evt.getInPoint();
        int outPoint = evt.getOutPoint();
        if (inPoint >= 0) {
            mSequencer.setLoopStartPoint(inPoint);
        }
        
        if (outPoint >= 0) {
            mSequencer.setLoopEndPoint(outPoint);
        }
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
                    setBusy(true);
                    // Construct a Sequence object
                    mSeq = MidiFile.openSequenceFile(file);
                    
                    // Remember the file name for later
                    mFileName = file.getName();
                    mFilePath = file.getCanonicalPath();
                } catch (IOException e) {
                    trace("IOException in newSequence(final java.io.File file) : " + e);
                    showDialog("<html>There was an error reading the selected file<br>" +
                        "It may be corrupt or you might not have permission to open it.</html>",
                        "IO Error", JOptionPane.ERROR_MESSAGE);
                } catch (InvalidMidiDataException e) {
                    trace("InvalidMidiDataException in newSequence(final java.io.File file) : " + e);
                    showDialog("<html>There was an error reading the selected file<br>" +
                        "It may not be a MIDI file.</html>",
                        "Invalid Data", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                } finally {
                    setBusy(false);
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
        setBusy(true);
        try {
            mPlayController.setSequence(mSeq);
            mTracks = mSeq.getTracks();
            mResolution = mSeq.getResolution();
            mCurrentTrack = 0;
            
            // Assign the sequence to the sequencer.
            try {
                mSequencer.setSequence(mSeq);
            } catch (InvalidMidiDataException e) {
                trace("Exception in buildNewSequence() : " + e);
            }
            
            mSequenceModified = false;
            
            if (mTrackEditor != null) {
                mTrackEditor.setSequence(mSeq);
            }
            
            if (mTrackSummary != null) {
                mTrackSummary.setSequence(mSeq);
            }
            
            mPlayController.setPlayState(PlayController.PlayState.STOPPED);
            
            setTitle(mFileName);
            setInfoLabels();
        } finally {
            setBusy(false);
        }
    }
    
    /**
     * Check if the sequence needs to be saved.
     * If it does give the user a Yes/No/Cancel dialog.
     * If the user elects to save give them a Save As dialog.
     * @return True if the user chooses to continue with the action,
     * False if the user selects Cancel.
     */
    private boolean checkForSave() {
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
     * @param file The file to which the sequence is to be saved.
     */
    public void saveFile(java.io.File file) {
        try {
            // Make sure that the Sequencer has the latest version of the Sequence.
            mSequencer.setSequence(mSeq);
            
            // Save the Sequence object
            int types[] = MidiSystem.getMidiFileTypes(mSeq);
            
            // I guess the last one will be the 'best'
            int type = types[types.length - 1];
            int res = MidiSystem.write(mSeq, type, file);
            mSequenceModified = false;
        } catch (IOException e) {
            trace("IOException in saveFile(java.io.File file) : " + e);
            showDialog("<html>There was an error saving the file<br>" +
                "The disk might be full or you might not<br>" +
                "have permission to write to it.</html>",
                "IO Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        } catch (InvalidMidiDataException e) {
            trace("InvalidMidiDataException in saveFile(java.io.File file) : " + e);
            showDialog("<html>There was an error creating the file to save<br>" +
                "There are some invalid values in the sequence data.</html>",
                "Invalid Data", JOptionPane.ERROR_MESSAGE);
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
    
    /** Set the text of the info labels. */
    void setInfoLabels() {
        trace("setInfoLabels");
        long dur = mSeq.getMicrosecondLength()/1000000;
        lengthText.setText(Formats.formatSeconds(dur));
        String key = "";
        String time = "";
        String tempo = "";
        
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
                    time = str[2].toString();
                }
                if (str[0].equals("M:Tempo")) {
                    tempo = str[2].toString();
                }
                if (str[0].equals("M:KeySignature")) {
                    mKeySig = (String)str[2];
                    key = mKeySig;
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
                    time = str[2].toString();
                }
                if (str[0].equals("M:Tempo")) {
                    tempo = str[2].toString();
                }
                if (str[0].equals("M:KeySignature")) {
                    mKeySig = (String)str[2];
                    key = mKeySig;
                }
            }
        }
        timeSigText.setText(time);
        tempoText.setText(tempo);
        keyText.setText(key);
    }
    
    private void doTranspose() {
        if (mTransposeDialog == null) {
            mTransposeDialog = new TransposeDialog(this, true);
        }
        // Reset the transpose dialog to zero
        mTransposeDialog.setTransposeBy(0);
        mTransposeDialog.setVisible(true);
        if (mTransposeDialog.getReturnStatus() == TransposeDialog.RET_OK) {
            boolean overflowed =
                Transposer.transpose(
                mSeq,
                mTransposeDialog.getTransposeBy(),
                mTransposeDialog.getDoDrums());
            
            if (overflowed) {
                String message = UiStrings.getString("transpose_out_of_range");
                JOptionPane.showMessageDialog(
                    this, message, UiStrings.getString("notes_out_of_range_title"),
                    JOptionPane.WARNING_MESSAGE);
            }
        }
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
        mainSplitPane = new javax.swing.JSplitPane();
        topPanel = new javax.swing.JPanel();
        playControlPanel = new javax.swing.JPanel();
        transportPanel = new com.lemckes.MidiQuickFix.components.TransportPanel();
        controlPanel = new javax.swing.JPanel();
        progressPanel = new javax.swing.JPanel();
        positionSlider = new com.lemckes.MidiQuickFix.components.LoopSlider();
        tempoAdjustPanel = new javax.swing.JPanel();
        tempoAdjustLabel = new javax.swing.JLabel();
        tempoAdjustSlider = new javax.swing.JSlider();
        tempoAdjustField = new JFormattedTextField(new DecimalFormat("0.00"));
        seqInfoPanel = new javax.swing.JPanel();
        lengthLabel = new javax.swing.JLabel();
        lengthText = new javax.swing.JLabel();
        tempoLabel = new javax.swing.JLabel();
        tempoText = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        timeSigText = new javax.swing.JLabel();
        keyLabel = new javax.swing.JLabel();
        keyText = new javax.swing.JLabel();
        transposeButton = new javax.swing.JButton();
        detailsTabbedPane = new javax.swing.JTabbedPane();
        summaryPanel = new javax.swing.JPanel();
        summaryScrollPane = new javax.swing.JScrollPane();
        editorPanel = new javax.swing.JPanel();
        lyricsPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        cutMenuItem = new javax.swing.JMenuItem();
        copyMenuItem = new javax.swing.JMenuItem();
        pasteMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        sequenceMenu = new javax.swing.JMenu();
        splitMenuItem = new javax.swing.JMenuItem();
        transposeMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        traceMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        sequenceChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sequenceChooserActionPerformed(evt);
            }
        });

        setTitle(UiStrings.getString("mqf")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        mainPanel.setLayout(new java.awt.BorderLayout());

        mainSplitPane.setDividerSize(9);
        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setOneTouchExpandable(true);
        topPanel.setLayout(new java.awt.BorderLayout());

        playControlPanel.setLayout(new java.awt.GridBagLayout());

        playControlPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 6, 0));
        transportPanel.setAlignmentX(1.0F);
        transportPanel.setPreferredSize(new java.awt.Dimension(200, 40));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        playControlPanel.add(transportPanel, gridBagConstraints);

        topPanel.add(playControlPanel, java.awt.BorderLayout.CENTER);

        controlPanel.setLayout(new java.awt.GridBagLayout());

        progressPanel.setLayout(new java.awt.GridBagLayout());

        progressPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2), javax.swing.BorderFactory.createEtchedBorder()));
        positionSlider.setBorder(null);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        progressPanel.add(positionSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        controlPanel.add(progressPanel, gridBagConstraints);

        tempoAdjustPanel.setLayout(new java.awt.GridBagLayout());

        tempoAdjustLabel.setText(UiStrings.getString("tempo_adjust")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        tempoAdjustPanel.add(tempoAdjustLabel, gridBagConstraints);

        tempoAdjustSlider.setMajorTickSpacing(10);
        tempoAdjustSlider.setMaximum(200);
        tempoAdjustSlider.setMinorTickSpacing(1);
        tempoAdjustSlider.setSnapToTicks(true);
        tempoAdjustSlider.setValue(100);
        tempoAdjustSlider.setPreferredSize(new java.awt.Dimension(100, 16));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        tempoAdjustPanel.add(tempoAdjustSlider, gridBagConstraints);

        tempoAdjustField.setColumns(4);
        tempoAdjustField.setFont(new java.awt.Font("Monospaced", 0, 10));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.ipady = 3;
        tempoAdjustPanel.add(tempoAdjustField, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 9);
        controlPanel.add(tempoAdjustPanel, gridBagConstraints);

        seqInfoPanel.setLayout(new java.awt.GridBagLayout());

        seqInfoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2), javax.swing.BorderFactory.createEtchedBorder()));
        lengthLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lengthLabel.setText(UiStrings.getString("duration")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 3, 0, 0);
        seqInfoPanel.add(lengthLabel, gridBagConstraints);

        lengthText.setBackground(new java.awt.Color(255, 255, 255));
        lengthText.setFont(new java.awt.Font("DialogInput", 0, 12));
        lengthText.setText("--:--");
        lengthText.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 6));
        lengthText.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 3);
        seqInfoPanel.add(lengthText, gridBagConstraints);

        tempoLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        tempoLabel.setText(UiStrings.getString("tempo")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        seqInfoPanel.add(tempoLabel, gridBagConstraints);

        tempoText.setBackground(new java.awt.Color(255, 255, 255));
        tempoText.setFont(new java.awt.Font("DialogInput", 0, 12));
        tempoText.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 3);
        seqInfoPanel.add(tempoText, gridBagConstraints);

        timeLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        timeLabel.setText(UiStrings.getString("timesig")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        seqInfoPanel.add(timeLabel, gridBagConstraints);

        timeSigText.setBackground(new java.awt.Color(255, 255, 255));
        timeSigText.setFont(new java.awt.Font("DialogInput", 0, 12));
        timeSigText.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 3);
        seqInfoPanel.add(timeSigText, gridBagConstraints);

        keyLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        keyLabel.setText(UiStrings.getString("key")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        seqInfoPanel.add(keyLabel, gridBagConstraints);

        keyText.setBackground(new java.awt.Color(255, 255, 255));
        keyText.setFont(new java.awt.Font("DialogInput", 0, 12));
        keyText.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 3);
        seqInfoPanel.add(keyText, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        controlPanel.add(seqInfoPanel, gridBagConstraints);

        transposeButton.setText(UiStrings.getString("transpose")); // NOI18N
        transposeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transposeButtonActionPerformed(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        controlPanel.add(transposeButton, gridBagConstraints);

        topPanel.add(controlPanel, java.awt.BorderLayout.SOUTH);

        mainSplitPane.setLeftComponent(topPanel);

        detailsTabbedPane.setPreferredSize(new java.awt.Dimension(480, 320));
        summaryPanel.setLayout(new javax.swing.BoxLayout(summaryPanel, javax.swing.BoxLayout.LINE_AXIS));

        summaryPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                summaryPanelComponentShown(evt);
            }
        });

        summaryPanel.add(summaryScrollPane);

        detailsTabbedPane.addTab(UiStrings.getString("track_summary"), summaryPanel); // NOI18N

        editorPanel.setLayout(new java.awt.BorderLayout());

        editorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
        editorPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                editorPanelComponentShown(evt);
            }
        });

        detailsTabbedPane.addTab(UiStrings.getString("editor"), editorPanel); // NOI18N

        lyricsPanel.setLayout(new java.awt.BorderLayout());

        lyricsPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                lyricsPanelComponentShown(evt);
            }
        });

        detailsTabbedPane.addTab(UiStrings.getString("lyrics"), lyricsPanel); // NOI18N

        mainSplitPane.setBottomComponent(detailsTabbedPane);

        mainPanel.add(mainSplitPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText(UiStrings.getString("file")); // NOI18N
        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openMenuItem.setMnemonic('O');
        openMenuItem.setText(UiStrings.getString("open")); // NOI18N
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(openMenuItem);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText(UiStrings.getString("save")); // NOI18N
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText(UiStrings.getString("saveas")); // NOI18N
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(saveAsMenuItem);

        fileMenu.add(jSeparator1);

        exitMenuItem.setMnemonic('x');
        exitMenuItem.setText(UiStrings.getString("exit")); // NOI18N
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });

        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        editMenu.setMnemonic('E');
        editMenu.setText(UiStrings.getString("edit")); // NOI18N
        editMenu.setEnabled(false);
        cutMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        cutMenuItem.setMnemonic('C');
        cutMenuItem.setText(UiStrings.getString("cut")); // NOI18N
        editMenu.add(cutMenuItem);

        copyMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.CTRL_MASK));
        copyMenuItem.setMnemonic('o');
        copyMenuItem.setText(UiStrings.getString("copy")); // NOI18N
        editMenu.add(copyMenuItem);

        pasteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        pasteMenuItem.setMnemonic('P');
        pasteMenuItem.setText(UiStrings.getString("paste")); // NOI18N
        editMenu.add(pasteMenuItem);

        deleteMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0));
        deleteMenuItem.setMnemonic('D');
        deleteMenuItem.setText(UiStrings.getString("delete")); // NOI18N
        editMenu.add(deleteMenuItem);

        menuBar.add(editMenu);

        sequenceMenu.setMnemonic('S');
        sequenceMenu.setText(UiStrings.getString("sequence")); // NOI18N
        splitMenuItem.setText(UiStrings.getString("split")); // NOI18N
        splitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                splitMenuItemActionPerformed(evt);
            }
        });

        sequenceMenu.add(splitMenuItem);

        transposeMenuItem.setText(UiStrings.getString("transpose")); // NOI18N
        transposeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transposeMenuItemActionPerformed(evt);
            }
        });

        sequenceMenu.add(transposeMenuItem);

        menuBar.add(sequenceMenu);

        viewMenu.setMnemonic('V');
        viewMenu.setText(UiStrings.getString("view")); // NOI18N
        traceMenuItem.setMnemonic('r');
        traceMenuItem.setText(UiStrings.getString("trace_window")); // NOI18N
        traceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traceMenuItemActionPerformed(evt);
            }
        });

        viewMenu.add(traceMenuItem);

        menuBar.add(viewMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText(UiStrings.getString("help")); // NOI18N
        aboutMenuItem.setText(UiStrings.getString("about")); // NOI18N
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });

        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void transposeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transposeButtonActionPerformed
        doTranspose();
    }//GEN-LAST:event_transposeButtonActionPerformed
    
    private void transposeMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_transposeMenuItemActionPerformed
        doTranspose();
    }//GEN-LAST:event_transposeMenuItemActionPerformed
    
    private void editorPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_editorPanelComponentShown
        if (mTrackEditor == null) {
            mTrackEditor = new TrackEditorPanel();
            editorPanel.add(mTrackEditor);
            mTrackEditor.setVisible(true);
            editorPanel.validate();
            if (mSeq != null) {
                mTrackEditor.setSequence(mSeq);
            }
            mTrackEditor.addTableChangeListener(this);
        }
    }//GEN-LAST:event_editorPanelComponentShown
    
    private void summaryPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_summaryPanelComponentShown
        if (mTrackSummary == null) {
            mTrackSummary = new TrackSummaryTable();
            summaryScrollPane.setViewportView(mTrackSummary);
            mTrackSummary.setVisible(true);
            summaryScrollPane.validate();
        }
        if (mSeq != null) {
            mTrackSummary.setSequence(mSeq);
        }
    }//GEN-LAST:event_summaryPanelComponentShown
    
    private void lyricsPanelComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_lyricsPanelComponentShown
        if (mLyricDisplay == null) {
            mLyricDisplay = new LyricDisplay();
            lyricsPanel.add(mLyricDisplay);
            mLyricDisplay.setVisible(true);
            lyricsPanel.validate();
        }
        if (mSequencer != null) {
            mLyricDisplay.setSequencer(mSequencer);
        }
    }//GEN-LAST:event_lyricsPanelComponentShown
    
    private void traceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traceMenuItemActionPerformed
        TraceDialog.getInstance().setVisible(traceMenuItem.getState());
    }//GEN-LAST:event_traceMenuItemActionPerformed
    
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
    
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        java.io.File myMidiFile = new java.io.File(mFilePath);
        saveFile(myMidiFile);
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
                        long ticks = mSequencer.getTickPosition();
                        positionSlider.setValue((int)ticks);
                    }
                });
            }
        };
        mTimer = new Timer(delay, taskPerformer);
    }
    
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        boolean canContinue = checkForSave();
        if (canContinue != true) {
            return;
        }
        
        int open = sequenceChooser.showOpenDialog(this);
        if (open == JFileChooser.APPROVE_OPTION) {
            newSequence(sequenceChooser.getSelectedFile());
        }
    }//GEN-LAST:event_openMenuItemActionPerformed
    
    private void sequenceChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequenceChooserActionPerformed
            }//GEN-LAST:event_sequenceChooserActionPerformed
    
    /** Called on a change to the table
     * @param e The event
     */
    public void tableChanged(TableModelEvent e) {
        // Hide Notes fires a Table Structure Changed event that has
        // e.getColumn() == e.ALL_COLUMNS && e.getFirstRow() == e.HEADER_ROW
        // We do not want this treated as an edit to the actual sequence.
        if (e.getColumn() != e.ALL_COLUMNS || e.getFirstRow() != e.HEADER_ROW) {
            mSequenceModified = true;
            boolean wasPlaying =
                (mPlayController.getPlayState() == PlayController.PlayState.PLAYING);
            if (wasPlaying) {
                mTimer.stop();
                mPlayController.pause();
            }
            try {
                mSequencer.setSequence(mSeq);
            } catch (javax.sound.midi.InvalidMidiDataException imde) {
                trace("Exception in tableChanged(TableModelEvent e) " + imde);
                showDialog("<html>There was an error updating the sequence.<br>" +
                    "There are some invalid values in the sequence data.</html>",
                    "Invalid Data", JOptionPane.ERROR_MESSAGE);
                imde.printStackTrace();
            }
            if (wasPlaying) {
                mTimer.start();
                mPlayController.play();
            }
        }
    }
    
    /** Get the nominal key signature for the sequence.
     * @return The first key signature found.
     */
    String getKeySig() {
        return mKeySig;
    }
    
    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exitForm(null);
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
        
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MidiQuickFix().setVisible(true);
            }
        });
    }
    
    /**
     * Flag if we need to work around the bug in 1.4.2
     * that does not allow us to update the values of a
     * ShortEvent.
     */
    static boolean VERSION_1_4_2_BUG;
    static String mJavaVersion;
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JTabbedPane detailsTabbedPane;
    private javax.swing.JMenu editMenu;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JLabel keyText;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JLabel lengthText;
    private javax.swing.JPanel lyricsPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JPanel playControlPanel;
    private com.lemckes.MidiQuickFix.components.LoopSlider positionSlider;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JPanel seqInfoPanel;
    private javax.swing.JFileChooser sequenceChooser;
    private javax.swing.JMenu sequenceMenu;
    private javax.swing.JMenuItem splitMenuItem;
    private javax.swing.JPanel summaryPanel;
    private javax.swing.JScrollPane summaryScrollPane;
    private javax.swing.JFormattedTextField tempoAdjustField;
    private javax.swing.JLabel tempoAdjustLabel;
    private javax.swing.JPanel tempoAdjustPanel;
    private javax.swing.JSlider tempoAdjustSlider;
    private javax.swing.JLabel tempoLabel;
    private javax.swing.JLabel tempoText;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel timeSigText;
    private javax.swing.JPanel topPanel;
    private javax.swing.JCheckBoxMenuItem traceMenuItem;
    private com.lemckes.MidiQuickFix.components.TransportPanel transportPanel;
    private javax.swing.JButton transposeButton;
    private javax.swing.JMenuItem transposeMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
    
    /** The system synthesizer. */
    transient Synthesizer mSynth;
    /** The syth's channels. */
    transient MidiChannel[] mChannels;
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
    
    TrackEditorPanel mTrackEditor = null;
    
    TrackSummaryTable mTrackSummary = null;
    
    LyricDisplay mLyricDisplay = null;
    
    AboutDialog mAboutDialog = null;
    
    TransposeDialog mTransposeDialog;
    
    transient PlayController mPlayController;
    
    public void play() {
        try {
            mSequencer.setSequence(mSeq);
        } catch (InvalidMidiDataException imde) {
            trace("Exception in play() " + imde);
            showDialog("<html>There was an error playing the sequence.<br>" +
                "There are some invalid values in the sequence data.</html>",
                "Invalid Data", JOptionPane.ERROR_MESSAGE);
        }
        
        // Clear the lyrics, but only if we are playing from the start.
        if (mPlayController.getPausedPosition() == 0 && mLyricDisplay != null) {
            mLyricDisplay.clear();
        }
        
        mSequencer.setTickPosition(mPlayController.getPausedPosition());
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
        mSequencer.setTickPosition(0);
        mTimer.stop();
        positionSlider.setValue(0);
    }
    
    public void rewind() {
        // Turn off all notes
        // Does not work!!
        for (int i = 0; i < mChannels.length; ++i) {
            mChannels[i].allNotesOff();
            mChannels[i].allSoundOff();
        }
        mSequencer.setTickPosition(0);
        positionSlider.setValue(0);
        
        // Workaround to turn off the sound ...
        if (mPlayController.getPlayState() == PlayController.PlayState.PLAYING) {
            mSequencer.stop();
            mSequencer.start();
        }
    }
    
    public void loop(boolean loop) {
        if (loop) {
            int inPoint = positionSlider.getLoopInPoint();
            int outPoint = positionSlider.getLoopOutPoint();
            mSequencer.setLoopStartPoint(inPoint);
            mSequencer.setLoopEndPoint(outPoint);
            mSequencer.setLoopCount(javax.sound.midi.Sequencer.LOOP_CONTINUOUSLY);
        } else {
            mSequencer.setLoopCount(0);
        }
    }
    
    /**
     * Send a trace message to the trace window
     * @param s the message to display
     */
    public void trace(final String s) {
        TraceDialog.addTrace(s);
    }
    
    /**
     * A wrapper to call JOptionPane.showMessageDialog using EventQueue.invokeLater
     * @param message the message to display
     * @param title the title for the dialog window
     * @param messageType one of the JOptionPane.*_MESSAGE constants
     */
    void showDialog(final String message, final String title, final int messageType) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                JOptionPane.showMessageDialog(
                    mainPanel, message, title, messageType);
            }
        });
        
    }
    
    /**
     * A class to handle meta events -<ul><li>
     * TimeSignature - update the displayed time signature</li><li>
     * KeySignature - update the displayed key signature</li><li>
     * Tempo - update the displayed tempo</li><li>
     * EndOfTrack - stop the sequencer and update the playing state</li></ul>
     */
    public class EventHandler implements MetaEventListener {
        public void meta(final MetaMessage metaMessage) {
            int type = metaMessage.getType();
            Object[] str = MetaEvent.getMetaStrings(metaMessage);
            // if (!mSequencer.isRunning()) {
            if (type == MetaEvent.END_OF_TRACK) {
                EventQueue.invokeLater(new Runnable() {
                    public void run(){
                        stop();
                        mPlayController.stop();
                    }
                });
            }
            if (str[0].equals("M:TimeSignature")) {
                timeSigText.setText(str[2].toString());
            }
            if (str[0].equals("M:KeySignature")) {
                keyText.setText(str[2].toString());
            }
            if (str[0].equals("M:Tempo")) {
                tempoText.setText(str[2].toString());
            }
        }
    }
}
