/*
 * MidiQuickFix.java
 *
 * Created on 21 October 2003, 07:58
 */

package com.lemckes.MidiQuickFix;

import javax.sound.midi.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.awt.FontMetrics;


/**
 * A MIDI file editor which works at the Midi event level.
 * @author john
 * @see javax.sound.midi
 * @version $Id$
 */
public class MidiQuickFix extends JFrame {
    
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
        Startup startDialog = new Startup(new javax.swing.JFrame(), false);
        startDialog.show();
        
        try {
            mStringBundle = java.util.ResourceBundle.getBundle(
                "com/lemckes/MidiQuickFix/resources/UIStrings");
            startDialog.splash.setStageMessage(
                mStringBundle.getString("getting_sequencer"));
            mSequencer = MidiSystem.getSequencer();

            startDialog.splash.setStageMessage(
                mStringBundle.getString("get_sequencer_returned"));
            if (mSequencer == null) {
                // Error -- sequencer device is not supported.
                startDialog.splash.setStageMessage(
                    mStringBundle.getString("get_sequencer_failed"));
            } else {
                // Acquire resources and make operational.
                startDialog.splash.setStageMessage(
                    mStringBundle.getString("opening_sequencer"));
                mSequencer.open(); // This call blocks the process...
                startDialog.splash.setStageMessage(
                    mStringBundle.getString("sequencer_opened"));
            }
            if (fileName != null && fileName.length() > 0) {
                startDialog.splash.setStageMessage(
                    mStringBundle.getString("opening_file"));
                openFile(fileName);
            }
            initComponents();
            sequenceChooser.addChoosableFileFilter(
                new MidiQuickFix.MidiFileFilter());
            
            startDialog.setVisible(false);
            
            mPlayState = NO_FILE;
            if (fileName != null && fileName.length() > 0) {
                startDialog.splash.setStageMessage(
                    mStringBundle.getString("creating_window"));
                setTrackComboModel();
                setInfoLabels();
                selectTrack(0);
            mPlayState = STOPPED;
            }
            createTimer(20);
            mSequenceModified = false;
            setActions();
        } catch(Exception e) {
            startDialog.splash.setStageMessage(
                mStringBundle.getString("startup_failed"));
            startDialog.splash.setStageMessage(
                e.getMessage());
            e.printStackTrace();
        }
    }
    
    /** Open the given file and create a Sequence object from it.
     * First checks if the current sequence has been modified.
     * Currently does not validate that the file is a midi file.
     * @param fileName The full path name of the midi file to open.
     */
    public void openFile(String fileName) {
        try {
            java.io.File myMidiFile = new java.io.File(fileName);
            openFile(myMidiFile);
        } catch (Exception e) {
            // Handle error and/or return
            System.out.println(
                    "Exception in openFile(String fileName) : "
                    + e.getMessage()
                    );
            e.printStackTrace();
        }
    }
    
    /** Open the given File and create a Sequence object from it.
     * Currently does not validate that the file is a midi file.
     * @param file The midi file to open.
     */
    public void openFile(java.io.File file) {
        try {
            // Construct a Sequence object
            mSeq = MidiSystem.getSequence(file);
            
            mTracks = mSeq.getTracks();
            mResolution = mSeq.getResolution();
            mCurrentTrack = 0;
            
            // Assign the sequence to the sequencer.
            mSequencer.setSequence(mSeq);
            
            // Remember the file name for later
            mFileName = file.getName();
            mFilePath = file.getCanonicalPath();
            
            mSequenceModified = false;
            mPausedPos = 0;
            
            if (mTrackSummary != null) {
                mTrackSummary.setSequence(mSeq);
            }
        } catch (Exception e) {
            // Handle error and/or return
            e.printStackTrace();
        }
        mPlayState = STOPPED;
        setActions();
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
                    new String(mStringBundle.getString("check_save")),
                    new String(mStringBundle.getString("save_changes")), JOptionPane.YES_NO_CANCEL_OPTION);
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
            
            System.out.println("saveFile - Midi File Types");
            for (int i = 0; i < types.length; ++i) {
                System.out.println("Type " + i + " = " + types[i]);
            }
            
            // I guess the last one will be the 'best'
            int type = types[types.length - 1];
            int res = MidiSystem.write(mSeq, type, file);
            // System.out.println("saveFile wrote " + res + " bytes");
            mSequenceModified = false;
        } catch (Exception e) {
            // Handle error and/or return
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
        // Update the track selector combobox model
        String[] trackList = new String[mTracks.length];
        for (int i = 0; i < mTracks.length; ++i) {
            trackList[i] = new String(new Integer(i).toString());
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
    
    /** Set the table cell editor for the Patch column. */
    void setInstrumentEditor() {
        TableColumn instrumentColumn = trackTable.getColumnModel().getColumn(4);
        
        String[] s = InstrumentNames.getNameArray();
        JComboBox comboBox =
                new JComboBox(new DefaultComboBoxModel(s));
        instrumentColumn.setCellEditor(new DefaultCellEditor(comboBox));
    }
    
    /** Set the text of the info labels. */
    void setInfoLabels() {
        fileNameLabel.setText(mFileName);
        ticksLabel.setText(mStringBundle.getString("ticks_per_beat") + mResolution);
        long dur = mSeq.getMicrosecondLength()/1000000;
        lengthLabel.setText(mStringBundle.getString("duration") + formatSeconds(dur));
        String key = mStringBundle.getString("key");
        String time = mStringBundle.getString("timesig");
        String tempo = mStringBundle.getString("tempo");
        String trackName = "";
        
        positionSlider.setMinimum(0);
        positionSlider.setMaximum((int)dur);
        int major = (int)(dur / 10);
        int minor = major / 6;
        if (major < 10) {
            major = 10; minor = 1; 
        } else if (major < 15) {
            major = 15; minor = 1; 
        } else if (major < 20) {
            major = 20; minor = 2; 
        } else if (major < 30) {
            major = 30; minor = 5;
        } else if (major < 60) {
            major = 60; minor = 10;
        } else if (major < 120) {
            major = 120; minor = 20;
        }
        
        positionSlider.setMajorTickSpacing(major);
        positionSlider.setMinorTickSpacing(minor);
        //Create the label table
        java.util.Hashtable labelTable = positionSlider.createStandardLabels(major);
        for (java.util.Enumeration e = labelTable.keys(); e.hasMoreElements();) {
            Integer pos = (Integer)e.nextElement();
            JLabel label = (JLabel)labelTable.get(pos);
            label.setText(formatSeconds(pos.intValue()));
        }
        positionSlider.setLabelTable(labelTable);
        positionSlider.setPaintLabels(true);
        positionSlider.setPaintTicks(true);
        
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
                    time = mStringBundle.getString("timesig") + str[2];
                }
                if (str[0].equals("M:Tempo")) {
                    tempo = mStringBundle.getString("tempo") + str[2];
                }
                if (str[0].equals("M:TrackName")) {
                    trackName = (String)str[2];
                }
                if (str[0].equals("M:KeySignature")) {
                    mKeySig = (String)str[2];
                    key = mStringBundle.getString("key") + mKeySig;
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
                    time = mStringBundle.getString("timesig") + str[2];
                }
                if (str[0].equals("M:Tempo")) {
                    tempo = mStringBundle.getString("tempo") + str[2];
                }
                if (str[0].equals("M:TrackName")) {
                    trackName = (String)str[2];
                }
                if (str[0].equals("M:KeySignature")) {
                    mKeySig = (String)str[2];
                    key = mStringBundle.getString("key") + mKeySig;
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
    private void initComponents() {//GEN-BEGIN:initComponents
        java.awt.GridBagConstraints gridBagConstraints;

        sequenceChooser = new javax.swing.JFileChooser();
        mainPanel = new javax.swing.JPanel();
        tablePanel = new javax.swing.JPanel();
        tableScrollPane = new javax.swing.JScrollPane();
        trackTable = new javax.swing.JTable();
        topPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        transportPanel = new javax.swing.JPanel();
        rewindButton = new javax.swing.JButton();
        playButton = new javax.swing.JButton();
        pauseButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        fileNameLabel = new javax.swing.JLabel();
        progressPanel = new javax.swing.JPanel();
        positionPanel = new javax.swing.JPanel();
        posLabel = new javax.swing.JLabel();
        positionSlider = new javax.swing.JSlider();
        lengthLabel = new javax.swing.JLabel();
        trackPanel = new javax.swing.JPanel();
        trackLabel = new javax.swing.JLabel();
        trackSelector = new javax.swing.JComboBox();
        infoPanel = new javax.swing.JPanel();
        keyLabel = new javax.swing.JLabel();
        timeLabel = new javax.swing.JLabel();
        tempoLabel = new javax.swing.JLabel();
        ticksLabel = new javax.swing.JLabel();
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
        summaryMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        playMenuItem = new javax.swing.JMenuItem();
        pauseMenuItem = new javax.swing.JMenuItem();
        stopMenuItem = new javax.swing.JMenuItem();
        rewindMenuItem = new javax.swing.JMenuItem();
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
        tablePanel.setLayout(new java.awt.BorderLayout());

        tablePanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(3, 0, 3, 0)));
        tableScrollPane.setVerticalScrollBarPolicy(javax.swing.JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        tableScrollPane.setPreferredSize(new java.awt.Dimension(500, 403));
        trackTable.setName("trackTable");
        tableScrollPane.setViewportView(trackTable);

        tablePanel.add(tableScrollPane, java.awt.BorderLayout.CENTER);

        mainPanel.add(tablePanel, java.awt.BorderLayout.CENTER);

        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.Y_AXIS));

        jPanel3.setLayout(new java.awt.GridBagLayout());

        jPanel3.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 6, 0)));
        transportPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        transportPanel.setBorder(new javax.swing.border.EtchedBorder(javax.swing.border.EtchedBorder.RAISED));
        transportPanel.setAlignmentX(1.0F);
        rewindButton.setAction(mRewindAction);
        rewindButton.setBorderPainted(false);
        rewindButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transportPanel.add(rewindButton);

        playButton.setAction(mPlayAction);
        playButton.setBorderPainted(false);
        playButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transportPanel.add(playButton);

        pauseButton.setAction(mPauseAction);
        pauseButton.setBorderPainted(false);
        pauseButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transportPanel.add(pauseButton);

        stopButton.setAction(mStopAction);
        stopButton.setBorderPainted(false);
        stopButton.setMargin(new java.awt.Insets(0, 0, 0, 0));
        transportPanel.add(stopButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel3.add(transportPanel, gridBagConstraints);

        fileNameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        fileNameLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("filename"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.weightx = 1.0;
        jPanel3.add(fileNameLabel, gridBagConstraints);

        topPanel.add(jPanel3);

        progressPanel.setLayout(new java.awt.GridBagLayout());

        progressPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 6, 0)));
        positionPanel.setLayout(new javax.swing.BoxLayout(positionPanel, javax.swing.BoxLayout.X_AXIS));

        posLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("position"));
        posLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 6, 0, 0)));
        positionPanel.add(posLabel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        progressPanel.add(positionPanel, gridBagConstraints);

        positionSlider.setFont(new java.awt.Font("Dialog", 0, 10));
        positionSlider.setMajorTickSpacing(10);
        positionSlider.setMinorTickSpacing(1);
        positionSlider.setPaintLabels(true);
        positionSlider.setPaintTicks(true);
        positionSlider.setValue(0);
        positionSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                positionSliderStateChanged(evt);
            }
        });

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        progressPanel.add(positionSlider, gridBagConstraints);

        lengthLabel.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("duration"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        progressPanel.add(lengthLabel, gridBagConstraints);

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
        summaryMenuItem.setMnemonic('T');
        summaryMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("track_summary"));
        summaryMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                summaryMenuItemActionPerformed(evt);
            }
        });

        sequenceMenu.add(summaryMenuItem);

        sequenceMenu.add(jSeparator1);

        playMenuItem.setAction(mPlayAction);
        playMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("play"));
        sequenceMenu.add(playMenuItem);

        pauseMenuItem.setAction(mPauseAction);
        pauseMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("pause"));
        sequenceMenu.add(pauseMenuItem);

        stopMenuItem.setAction(mStopAction);
        stopMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("stop"));
        sequenceMenu.add(stopMenuItem);

        rewindMenuItem.setAction(mRewindAction);
        rewindMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("rewind"));
        sequenceMenu.add(rewindMenuItem);

        menuBar.add(sequenceMenu);

        helpMenu.setMnemonic('H');
        helpMenu.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("help"));
        contentsMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("contents"));
        helpMenu.add(contentsMenuItem);

        aboutMenuItem.setText(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("about"));
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }//GEN-END:initComponents
    
    private void summaryMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_summaryMenuItemActionPerformed
        // Add your handling code here:
        if (mTrackSummary == null) {
            mTrackSummary = new TrackSummary(this, false);
        }
        if (mSeq != null) {
            mTrackSummary.setSequence(mSeq);
            mTrackSummary.setFileName(fileNameLabel.getText());
            mTrackSummary.show();
        } else {
            JOptionPane.showMessageDialog(this,
                    java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("no_sequence_message"),
                    java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("no_sequence_title"),
                    JOptionPane.ERROR_MESSAGE);
        }
        
    }//GEN-LAST:event_summaryMenuItemActionPerformed
    
    private void showNotesCheckItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_showNotesCheckItemStateChanged
        TableModel model = trackTable.getModel();
        // Check that the TrackTableModel has been set on the table
        if (model instanceof TrackTableModel) {
            ((TrackTableModel)model).setShowNotes(showNotesCheck.isSelected());
            trackTable.setModel(model);
            
            setColumnWidths();
            
            setInstrumentEditor();
            
            trackTable.validate();
        }
    }//GEN-LAST:event_showNotesCheckItemStateChanged
    
    private void positionSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_positionSliderStateChanged
        // Add your handling code here:
        JSlider source = (JSlider)evt.getSource();
        if (source.getValueIsAdjusting()) {
            long pos = (long)source.getValue();
            mSequencer.setMicrosecondPosition(pos * 1000000);
            setPositionIndicator(true);
            if (!mSequencer.isRunning()) {
                mPausedPos = pos * 1000000;
            }
        }
    }//GEN-LAST:event_positionSliderStateChanged
    
    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        // Add your handling code here:
        try {
            java.io.File myMidiFile = new java.io.File(mFilePath);
            saveFile(myMidiFile);
        } catch (Exception e) {
            // Handle error and/or return
            System.out.println(
                    "Exception in saveMenuItemActionPerformed : "
                    + e.getMessage()
                    );
        }
    }//GEN-LAST:event_saveMenuItemActionPerformed
    
    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        // Add your handling code here:
        saveAs(null);
    }//GEN-LAST:event_saveAsMenuItemActionPerformed
    
    private void createTimer(int delay) {
        java.awt.event.ActionListener taskPerformer =
                new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //...Perform a task...
                setPositionIndicator(false);
                
                if (!mSequencer.isRunning()) {
                    mTimer.stop();
                    mPlayState = STOPPED;
                    setActions();
                }
            }
        };
        mTimer = new Timer(delay, taskPerformer);
    }
    
    private void setPositionIndicator(boolean fromSlider) {
        long ticks = mSequencer.getTickPosition();
        long pos = mSequencer.getMicrosecondPosition()/1000000;
        posLabel.setText(mStringBundle.getString("position") + formatSeconds(pos) +
                " (" + formatTicks(ticks) + ")");
        
        if (!fromSlider) {
            positionSlider.setValue((int)pos);
        }
        
    }
    
    private String formatTicks(long ticks) {
        java.text.DecimalFormat beatF = new java.text.DecimalFormat("0");
        java.text.DecimalFormat tickF = new java.text.DecimalFormat("000");
        long beat = 0;
        long tick = 0;
        if (mResolution > 0) {
            beat = ticks / mResolution;
            tick = ticks % mResolution;
        }
        return beatF.format(beat) + ":" + tickF.format(tick);
    }
    
    private String formatSeconds(long pos) {
        java.text.DecimalFormat minF = new java.text.DecimalFormat("0");
        java.text.DecimalFormat secF = new java.text.DecimalFormat("00");
        long secs = pos % 60;
        long mins = pos / 60;
        return minF.format(mins) + ":" + secF.format(secs);
    }
    
    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        // Add your handling code here:
        int[] selRows = trackTable.getSelectedRows();
        for (int i = 0; i< selRows.length; ++i) {
            System.out.println("Deleting row " + selRows[i]
                    + " " + trackTable.getValueAt(selRows[i], 0));
        }
        ((TrackTableModel)trackTable.getModel()).deleteEvents(selRows);
    }//GEN-LAST:event_deleteButtonActionPerformed
    
    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        // Add your handling code here:
        boolean canContinue = checkForSave();
        // System.out.println("openMenuItemActionPerformed canContinue = " + canContinue);
        if (canContinue != true) {
            return;
        }
        
        int open = sequenceChooser.showOpenDialog(this);
        if (open == JFileChooser.APPROVE_OPTION) {
            openFile(sequenceChooser.getSelectedFile());
            setTrackComboModel();
            setInfoLabels();
            selectTrack(0);
        }
    }//GEN-LAST:event_openMenuItemActionPerformed
    
    private void sequenceChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sequenceChooserActionPerformed
        // Add your handling code here:
        // System.out.println("sequenceChooserActionPerformed");
    }//GEN-LAST:event_sequenceChooserActionPerformed
    
    private void trackSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trackSelectorActionPerformed
        // Add your handling code here:
        selectTrack(trackSelector.getSelectedIndex());
    }//GEN-LAST:event_trackSelectorActionPerformed
    
    /** Nested class to track changes to the track table. */
    class tableChangeListener implements TableModelListener {
        /** Create a tableChangeListener. */
        public tableChangeListener() {
        }
        /** Called on a change to the table
         * @param e The event
         */
        public void tableChanged(TableModelEvent e) {
            // System.out.println("tableChangeListener tableChanged\n");
            // System.out.println("  Column    = " + e.getColumn());
            // System.out.println("  First row = " + e.getFirstRow());
            // System.out.println("  Last row  = " + e.getLastRow());
            // System.out.println("  Type      = " + e.getType());
            
            // Hide Notes fires a Table Structure Changed event which has
            // e.getColumn() == e.ALL_COLUMNS && e.getFirstRow() == e.HEADER_ROW
            // We do not want this treated as an edit to the actual sequence.
            if (e.getColumn() != e.ALL_COLUMNS || e.getFirstRow() != e.HEADER_ROW) {
                mSequenceModified = true;
                try {
                    boolean wasPlaying = (mPlayState == PLAYING);
                    if (wasPlaying) {
                        pause();
                    }
                    mSequencer.setSequence(mSeq);
                    if (wasPlaying) {
                        play();
                    }
                } catch (javax.sound.midi.InvalidMidiDataException imde) {
                    imde.printStackTrace();
                }
            }
        }
    }
    
    /** Display the selected track in the editor.
     * @param trackNum The index of the track to be displayed.
     */
    void selectTrack(int trackNum) {
        mCurrentTrack = trackNum;
        // System.out.println(mStringBundle.getString("track") + mCurrentTrack + " selected.");
        trackTable.setModel(
                new TrackTableModel(mTracks[mCurrentTrack], mResolution, showNotesCheck.isSelected(), KeySignatures.isInFlats(mKeySig))
                );
        
        trackTable.getModel().addTableModelListener(new tableChangeListener());
        
        setColumnWidths();
        
        setInstrumentEditor();
        
        setInfoLabels();
    }
    
    private void setColumnWidths() {
        int margin = 6;
        FontMetrics fm = trackTable.getFontMetrics(trackTable.getFont());
        TableColumnModel cm = trackTable.getColumnModel();
        TableColumn tc = cm.getColumn(0);
        tc.setPreferredWidth(fm.stringWidth("00000:000") + margin);
        tc = cm.getColumn(1);
        tc.setPreferredWidth(fm.stringWidth("A Typical Event") + margin);
        tc = cm.getColumn(2);
        tc.setPreferredWidth(fm.stringWidth(mStringBundle.getString("note")) + margin);
        tc = cm.getColumn(3);
        tc.setPreferredWidth(fm.stringWidth(mStringBundle.getString("value")) + margin);
        tc = cm.getColumn(4);
        tc.setPreferredWidth(fm.stringWidth("A Typical Instrument") + margin);
        tc = cm.getColumn(5);
        tc.setPreferredWidth(fm.stringWidth("Some Track Name") + margin);
        tc = cm.getColumn(6);
        tc.setPreferredWidth(fm.stringWidth(mStringBundle.getString("channel_abbrev")) + margin);
    }
    // End of variables declaration
    
    /** Get the track table.
     * @return The track table.
     */
    JTable getTrackTable() {
        return trackTable;
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
        new MidiQuickFix().show();
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel bottomPanel;
    private javax.swing.JMenuItem contentsMenuItem;
    private javax.swing.JMenuItem copyMenuItem;
    private javax.swing.JMenuItem cutMenuItem;
    private javax.swing.JButton deleteButton;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPanel infoPanel;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem pasteMenuItem;
    private javax.swing.JButton pauseButton;
    private javax.swing.JMenuItem pauseMenuItem;
    private javax.swing.JButton playButton;
    private javax.swing.JMenuItem playMenuItem;
    private javax.swing.JLabel posLabel;
    private javax.swing.JPanel positionPanel;
    private javax.swing.JSlider positionSlider;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JButton rewindButton;
    private javax.swing.JMenuItem rewindMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JFileChooser sequenceChooser;
    private javax.swing.JMenu sequenceMenu;
    private javax.swing.JCheckBox showNotesCheck;
    private javax.swing.JButton stopButton;
    private javax.swing.JMenuItem stopMenuItem;
    private javax.swing.JMenuItem summaryMenuItem;
    private javax.swing.JPanel tablePanel;
    private javax.swing.JScrollPane tableScrollPane;
    private javax.swing.JLabel tempoLabel;
    private javax.swing.JLabel ticksLabel;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JPanel topPanel;
    private javax.swing.JLabel trackLabel;
    private javax.swing.JPanel trackPanel;
    private javax.swing.JComboBox trackSelector;
    private javax.swing.JTable trackTable;
    private javax.swing.JPanel transportPanel;//GEN-END:variables
    
    /** Resource bundle of displayed strings */
    java.util.ResourceBundle mStringBundle;
    
    /** The default sequencer. */
    Sequencer mSequencer;
    /** The sequence from the current file. */
    Sequence mSeq;
    /** The array of tracks from the sequence. */
    Track[] mTracks;
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
    
    /** The microsecond position at which the sequence was paused. */
    long mPausedPos;
    
    /** True if the sequence has been modified. */
    boolean mSequenceModified;
    
    TrackSummary mTrackSummary = null;
    
    static final int NO_FILE = -1;
    static final int STOPPED = 0;
    static final int PAUSED = 1;
    static final int PLAYING = 2;
    
    int mPlayState = NO_FILE;
    
    /**
     * An Action to handle the Play option.
     */
    public class PlayAction extends javax.swing.AbstractAction {
        /** Creates a new instance of PlayAction */
        public PlayAction() {
            putValue(SMALL_ICON,
                    new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Play16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.ALT_MASK));
        }
        
        /**
         * Performs the functions required for Playing
         * @param e The event which triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // System.out.println("PlayAction.actionPerformed");
            if (mPlayState == PLAYING) {
                rewind();
            }
            play();
        }
    }
    PlayAction mPlayAction = new PlayAction();
    
    private void play() {
        mSequencer.start();
        mSequencer.setMicrosecondPosition(mPausedPos);
        mTimer.start();
        mPlayState = PLAYING;
        setActions();
    }
    
    /**
     * An Action to handle the Pause option.
     */
    public class PauseAction extends javax.swing.AbstractAction {
        /** Creates a new instance of PauseAction */
        public PauseAction() {
            putValue(SMALL_ICON,
                    new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Pause16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        }
        
        /**
         * Performs the functions required for Playing
         * @param e The event which triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // System.out.println("PauseAction.actionPerformed");
            pause();
        }
    }
    PauseAction mPauseAction = new PauseAction();
    
    private void pause() {
        if (mPlayState == PLAYING) {
            mPausedPos = mSequencer.getMicrosecondPosition();
            mSequencer.stop();
            mTimer.stop();
            mPlayState = PAUSED;
            setActions();
        } else if (mPlayState == PAUSED) {
            //try {
            //mSequencer.setSequence(mSeq);
            play();
            //} catch (javax.sound.midi.InvalidMidiDataException e) {
            // e.printStackTrace();
            //}
        }
    }
    
    /**
     * An Action to handle the Stop option.
     */
    public class StopAction extends javax.swing.AbstractAction {
        /** Creates a new instance of StopAction */
        public StopAction() {
            putValue(SMALL_ICON,
                    new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Stop16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_BACK_SPACE , java.awt.event.InputEvent.ALT_MASK));
        }
        
        /**
         * Performs the functions required for Stopping
         * @param e The event which triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // System.out.println("StopAction.actionPerformed");
            stop();
        }
    }
    StopAction mStopAction = new StopAction();
    
    private void stop() {
        if (mPlayState != STOPPED) {
            mSequencer.stop();
            mSequencer.setMicrosecondPosition(0);
            mTimer.stop();
            setPositionIndicator(false);
            mPausedPos = 0;
            mPlayState = STOPPED;
        }
        setActions();
    }
    
    /**
     * An Action to handle the Rewind option.
     */
    public class RewindAction extends javax.swing.AbstractAction {
        /** Creates a new instance of RewindAction */
        public RewindAction() {
            putValue(SMALL_ICON,
                    new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Rewind16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, java.awt.event.InputEvent.ALT_MASK));
        }
        
        /**
         * Performs the functions required for Rewinding
         * @param e The event which triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // System.out.println("RewindAction.actionPerformed");
            rewind();
        }
    }
    RewindAction mRewindAction = new RewindAction();
    
    private void rewind() {
        mSequencer.setMicrosecondPosition(0);
        mPausedPos = 0;
        setPositionIndicator(false);
        setActions();
    }
    
    private void setActions() {
        if (mPlayState == NO_FILE) {
            mPlayAction.setEnabled(false);
            mPauseAction.setEnabled(false);
            mRewindAction.setEnabled(false);
            mStopAction.setEnabled(false);
        } else if (mPlayState == PLAYING) {
            mPlayAction.setEnabled(false);
            mPauseAction.setEnabled(true);
            mRewindAction.setEnabled(true);
            mStopAction.setEnabled(true);
        } else if (mPlayState == PAUSED) {
            mPlayAction.setEnabled(true);
            mPauseAction.setEnabled(true);
            mRewindAction.setEnabled(true);
            mStopAction.setEnabled(true);
        } else if (mPlayState == STOPPED) {
            mPlayAction.setEnabled(true);
            mPauseAction.setEnabled(false);
            mRewindAction.setEnabled(false);
            mStopAction.setEnabled(false);
        }
    }
    
    /**
     * A FileFilter to select just midi files (.mid and .kar)
     */
    public class MidiFileFilter extends javax.swing.filechooser.FileFilter {
        /**
         * Check if the given file matches the acceptable files.
         * @param f The file to check
         * @return True if the file is either *.mid or *.kar.
         */
        public boolean accept(java.io.File f) {
            boolean acc = false;
            if (f.isDirectory()) {
                acc = true;
            } else {
                String ext = null;
                String s = f.getName();
                int i = s.lastIndexOf('.');
                if (i > 0 && i < s.length() - 1) {
                    ext = s.substring(i + 1).toLowerCase();
                }
                if (ext != null) {
                    if (ext.equals("mid") || ext.equals("kar")) {
                        acc = true;
                    }
                }
            }
            return acc;
        }
        
        /**
         * Get the description to display in the FileChooser.
         * @return The description of this filter
         */
        public String getDescription() {
            return "Midi Files (*.mid, *.kar)";
        }
    }
}
