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

import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.LoopSliderEvent;
import com.lemckes.MidiQuickFix.util.LoopSliderListener;
import com.lemckes.MidiQuickFix.util.MidiFile;
import com.lemckes.MidiQuickFix.util.MidiFileFilter;
import com.lemckes.MidiQuickFix.util.MidiSeqPlayer;
import com.lemckes.MidiQuickFix.util.MqfProperties;
import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.MidiQuickFix.util.PlayController;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.TracksChangedEvent;
import com.lemckes.MidiQuickFix.util.TracksChangedListener;
import com.lemckes.MidiQuickFix.util.Transposer;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Properties;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * A MIDI file editor that works at the Midi event level.
 * @version $Id$
 */
public class MidiQuickFix
    extends JFrame
    implements MidiSeqPlayer, LoopSliderListener, TableModelListener,
    TracksChangedListener
{
    static final long serialVersionUID = -3768776503290924603L;
    /**
     * Flag if we need to work around the bug in 1.4.2
     * that does not allow us to update the values of a
     * ShortEvent.
     */
    static boolean VERSION_1_4_2_BUG;
    static String mJavaVersion;
    /** The system synthesizer. */
    transient Synthesizer mSynth;
    /** The synth's channels. */
    transient MidiChannel[] mChannels;
    /** The default sequencer. */
    transient Sequencer mSequencer;
    /** The sequence from the current file. */
    transient MqfSequence mSeq;
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
    boolean mSequenceModified = false;
    TrackEditorPanel mTrackEditor;
    TrackSummaryPanel mTrackSummaryPanel;
    TrackSummaryTable mTrackSummary;
    LyricDisplay mLyricDisplay;
    AboutDialog mAboutDialog = null;
    TransposeDialog mTransposeDialog;
    transient PlayController mPlayController;
    transient Properties mMqfProps;

    /**
     * Creates a new MidiQuickFix instance
     */
    public MidiQuickFix() {
        TraceDialog.getInstance().addComponentListener(new ComponentListener()
        {
            @Override
            public void componentShown(ComponentEvent e) {
                traceMenuItem.setState(true);
            }

            @Override
            public void componentHidden(ComponentEvent e) {
                traceMenuItem.setState(false);
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
            }
        });

        Startup startDialog = new Startup(new javax.swing.JFrame(), false);
        startDialog.setVisible(true);

        try {
            startDialog.splash.addStageMessage(
                UiStrings.getString("using_java") + " " + mJavaVersion); // NOI18N

            startDialog.splash.addStageMessage(
                UiStrings.getString("getting_sequencer")); // NOI18N
            mSequencer = MidiSystem.getSequencer();

            startDialog.splash.addStageMessage(
                UiStrings.getString("get_sequencer_returned")); // NOI18N
            if (mSequencer == null) {
                // Error -- sequencer device is not supported.
                startDialog.splash.addStageMessage(
                    UiStrings.getString("get_sequencer_failed")); // NOI18N
            } else {
                // Acquire resources and make operational.
                startDialog.splash.addStageMessage(
                    UiStrings.getString("opening_sequencer")); // NOI18N
                mSequencer.open(); // This call blocks the process...
                startDialog.splash.addStageMessage(
                    UiStrings.getString("sequencer_opened")); // NOI18N
            }

            mPlayController = new PlayController(mSequencer, mSeq);

            initComponents();

            tempoAdjustField.setValue(1.0f);
            tempoAdjustField.addPropertyChangeListener("value", // NOI18N
                new PropertyChangeListener()
            {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    float val = 1.0f;
                    try {
                        val = (Float)tempoAdjustField.getValue();
                    } catch (ClassCastException cce) {
                        try {
                            double d = (Double)tempoAdjustField.getValue();
                            val = (float)d;
                        } catch (ClassCastException cce2) {
                            try {
                                long l = (Long)tempoAdjustField.getValue();
                                val = (float)l;
                            } catch (ClassCastException cce3) {
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

            mPlayController.mRewindAction.putValue("rewinder", this); // NOI18N
            mPlayController.mPlayAction.putValue("player", this); // NOI18N
            mPlayController.mPauseAction.putValue("pauser", this); // NOI18N
            mPlayController.mStopAction.putValue("stopper", this); // NOI18N
            mPlayController.mLoopAction.putValue("looper", this); // NOI18N
            mPlayController.setPlayState(PlayController.PlayState.NO_FILE);

            MqfProperties.readProperties();
            String lastPath = MqfProperties.getProperty(
                MqfProperties.LAST_PATH_KEY);
            if (lastPath != null) {
                sequenceChooser.setCurrentDirectory(new File(lastPath));
            }
            sequenceChooser.addChoosableFileFilter(new MidiFileFilter());

            startDialog.setVisible(false);

            mSequencer.addMetaEventListener(new EventHandler());

            /* Update the position 10 times per second */
            createTimer(100);

            mSequenceModified = false;

            positionSlider.addLoopSliderListener(this);

            mSynth = MidiSystem.getSynthesizer();
            mChannels = mSynth.getChannels();

            tempoAdjustSlider.addChangeListener(new ChangeListener()
            {
                @Override
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
            mTrackSummaryPanel = new TrackSummaryPanel();
            mTrackSummary = new TrackSummaryTable(mSequencer);
            mTrackSummaryPanel.setSummaryTable(mTrackSummary);
            mTrackSummaryPanel.addTracksChangedListener(this);
            summaryPanel.add(mTrackSummaryPanel);
            mTrackEditor = new TrackEditorPanel();
            editorPanel.add(mTrackEditor);
            mTrackEditor.addTableChangeListener(this);
            TrackSummaryTableModel tstm =
                (TrackSummaryTableModel)mTrackSummary.getModel();
            mLyricDisplay = new LyricDisplay(tstm);
            lyricsPanel.add(mLyricDisplay);

            mLyricDisplay.setSequencer(mSequencer);

            pack();
            setLocationRelativeTo(null);

            EventQueue.invokeLater(new Runnable()
            {
                @Override
                public void run() {
                    openFile();
                }
            });
        } catch (MidiUnavailableException e) {
            startDialog.splash.addStageMessage(
                UiStrings.getString("no_midi_message")); // NOI18N
            startDialog.splash.addStageMessage(
                e.getMessage());
            startDialog.splash.addStageMessage(
                UiStrings.getString("check_other_apps1")); // NOI18N
            startDialog.splash.addStageMessage(
                UiStrings.getString("check_other_apps2")); // NOI18N
            startDialog.splash.addStageMessage(
                UiStrings.getString("startup_failed")); // NOI18N
        }
    }

    private void createTimer(int delay) {
        trace("createTimer"); // NOI18N
        java.awt.event.ActionListener taskPerformer =
            new java.awt.event.ActionListener()
            {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    //...Perform a task...
                    EventQueue.invokeLater(new Runnable()
                    {
                        @Override
                        public void run() {
                            long ticks = mSequencer.getTickPosition();
                            positionSlider.setValue((int)ticks);
                        }
                    });
                }
            };
        mTimer = new Timer(delay, taskPerformer);
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

    @Override
    public void loopSliderChanged(LoopSliderEvent evt) {
        if (evt.getValueIsAdjusting()) {
            long pos = (long)evt.getValue();
            mSequencer.setTickPosition(pos);
            if (!mSequencer.isRunning()) {
                mPlayController.setPausedPosition(pos); // in ticks
            }
        }
    }

    @Override
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
    private void newSequence(String fileName) {
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
    private void newSequence(final java.io.File file) {
        final SwingWorker<Object, Object> worker =
            new SwingWorker<Object, Object>()
            {
                // Open the file in the worker thread
                @Override
                public Object doInBackground() {
                    try {
                        setBusy(true);
                        // Construct a Sequence object
                        mSeq = MidiFile.openSequenceFile(file);

                        // Remember the file name for later
                        mFileName = file.getName();
                        mFilePath = file.getCanonicalPath();
                    } catch (IOException e) {
                        trace("IOException in newSequence() : " + e); // NOI18N
                        showDialog(UiStrings.getString("file_read_error")
                            + UiStrings.getString("file_read_permission"),
                            UiStrings.getString("file_io_error"),
                            JOptionPane.ERROR_MESSAGE);
                    } catch (InvalidMidiDataException e) {
                        trace("InvalidMidiDataException in newSequence() : " + e); // NOI18N
                        showDialog(UiStrings.getString("file_read_error")
                            + UiStrings.getString("file_read_invalid"),
                            UiStrings.getString("file_invalid_data"),
                            JOptionPane.ERROR_MESSAGE);
                    } finally {
                        setBusy(false);
                    }
                    return null;
                }

                //Build sequence in the event-dispatching thread.
                @Override
                public void done() {
                    if (mSeq != null) {
                        buildNewSequence();
                    }
                }
            };
        worker.execute();
    }

    /**
     * Update the UI after a new Sequence is loaded.
     */
    private void buildNewSequence() {
        setBusy(true);
        try {
            mPlayController.setSequence(mSeq);
            mResolution = mSeq.getResolution();
            mCurrentTrack = 0;

            // Assign the sequence to the sequencer.
            try {
                mSequencer.setSequence(mSeq);
            } catch (InvalidMidiDataException e) {
                trace("Exception in buildNewSequence() : " + e);
            }

            if (mTrackEditor != null) {
                mTrackEditor.setSequence(mSeq);
            }

            if (mTrackSummaryPanel != null) {
                mTrackSummaryPanel.setSequence(mSeq);
            }

            mPlayController.setPlayState(PlayController.PlayState.STOPPED);

            TrackSummaryTableModel tstm =
                (TrackSummaryTableModel)mTrackSummary.getModel();
            mLyricDisplay.setTrackSelector(tstm);
            mLyricDisplay.loadSequence(mSeq);

            mSequenceModified = false;

            setTitle(mFileName);
            setInfoLabels();
        } finally {
            setBusy(false);
            pack();
        }
    }

    @Override
    public void tracksChanged(TracksChangedEvent changeEvent) {
        if (mTrackEditor != null) {
            mTrackEditor.setSequence(mSeq);
        }

        if (mTrackSummaryPanel != null) {
            mTrackSummaryPanel.setSequence(mSeq);
        }

        if (mLyricDisplay != null) {
            mLyricDisplay.setTrackSelector(
                (TrackSummaryTableModel)mTrackSummary.getModel());
            mLyricDisplay.loadSequence(mSeq);
        }

        mSequenceModified = true;
    }

    private void openFile() {
        boolean canContinue = checkForSave();
        if (canContinue) {
            int open = sequenceChooser.showOpenDialog(this);
            if (open == JFileChooser.APPROVE_OPTION) {
                File file = sequenceChooser.getSelectedFile();
                String path = file.getParent();
                MqfProperties.setProperty(MqfProperties.LAST_PATH_KEY, path);
                newSequence(file);
            }
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

            // Remember the file name for later
            mFileName = file.getName();
            mFilePath = file.getCanonicalPath();
            setTitle(mFileName);
        } catch (IOException e) {
            trace("IOException in saveFile(java.io.File file) : "
                + e.getLocalizedMessage());
            showDialog(UiStrings.getString("file_save_error")
                + UiStrings.getString("file_save_permission"),
                UiStrings.getString("file_io_error"),
                JOptionPane.ERROR_MESSAGE);
        } catch (InvalidMidiDataException e) {
            trace(
                "InvalidMidiDataException in saveFile(java.io.File file) : "
                + e.getLocalizedMessage());
            showDialog(UiStrings.getString("file_create_error")
                + UiStrings.getString("file_save_invalid"),
                UiStrings.getString("file_invalid_data"),
                JOptionPane.ERROR_MESSAGE);
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
        long dur = mSeq.getMicrosecondLength() / 1000000;
        lengthText.setText(Formats.formatSeconds(dur));
        String key = "";
        String time = "";
        String tempo = "";

        long ticks = mSeq.getTickLength();
        positionSlider.setDuration(ticks, true, mResolution);

        // First get the info from the control track.
        Track t = mSeq.getTracks()[0];
        int count = t.size() - 1;
        for (int j = 0; j < count; ++j) {
            MidiEvent ev = t.get(j);
            MidiMessage mess = ev.getMessage();
            long tick = ev.getTick();
            if (tick > 0) {
                break;
            }
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
        t = mSeq.getTracks()[mCurrentTrack];
        count = t.size() - 1;
        for (int j = 0; j < count; ++j) {
            MidiEvent ev = t.get(j);
            MidiMessage mess = ev.getMessage();
            long tick = ev.getTick();
            if (tick > 0) {
                break;
            }
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
        mTransposeDialog.pack();
        mTransposeDialog.setVisible(true);
        if (mTransposeDialog.getReturnStatus() == TransposeDialog.RET_OK) {
            boolean running = mSequencer.isRunning();
            if (running) {
                mSequencer.stop();
            }
            boolean overflowed =
                Transposer.transpose(
                mSeq,
                mTransposeDialog.getTransposeBy(),
                mTransposeDialog.getDoDrums());
            mTrackEditor.setSequence(mSeq);
            mTrackSummaryPanel.setSequence(mSeq);
            mSequenceModified = true;

            if (overflowed) {
                String message = UiStrings.getString("transpose_out_of_range");
                JOptionPane.showMessageDialog(
                    this, message,
                    UiStrings.getString("notes_out_of_range_title"),
                    JOptionPane.WARNING_MESSAGE);
            }
            if (running) {
                mSequencer.start();
            }
        }
    }

    /** Called on a change to the table
     * @param e The event
     */
    @Override
    public void tableChanged(TableModelEvent e) {
        // Hide Notes fires a Table Structure Changed event that has
        // e.getColumn() == e.ALL_COLUMNS && e.getFirstRow() == e.HEADER_ROW
        // We do not want this treated as an edit to the actual sequence.
        if (e.getColumn() != TableModelEvent.ALL_COLUMNS
            || e.getFirstRow() != TableModelEvent.HEADER_ROW) {
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
                trace("Exception in tableChanged " + imde.getLocalizedMessage()); // NOI18N
                showDialog(UiStrings.getString("edit_sequence_error")
                    + UiStrings.getString("edit_sequence_invalid"),
                    UiStrings.getString("file_invalid_data"),
                    JOptionPane.ERROR_MESSAGE);
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

    private void showSongInfo() {
        new SongInfoDialog(mSeq, this, false).setVisible(true);
    }

    private void showPreferencesDialog() {
        new PreferencesDialog(this, false, this).setVisible(true);
    }

    public void setLookAndFeel(String lafName) {
        String currentLaf = UIManager.getLookAndFeel().getName();
        if (!currentLaf.equalsIgnoreCase(lafName)) {
            try {
                for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (lafName.equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        SwingUtilities.updateComponentTreeUI(this);
                        pack();
                        break;
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    public void updateLyricDisplay() {
        if (mLyricDisplay != null) {
            mLyricDisplay.updatePreferences();
            mLyricDisplay.displayText();
        }
    }

    ////////////////////////////////////////////////
    //
    // MidiSeqPlayer implementation
    //
    @Override
    public void play() {
        try {
            mSequencer.setSequence(mSeq);
        } catch (InvalidMidiDataException imde) {
            trace("Exception in play() " + imde); // NOI18N
            showDialog(UiStrings.getString("play_sequence_error")
                + UiStrings.getString("play_sequence_invalid"),
                UiStrings.getString("file_invalid_data"),
                JOptionPane.ERROR_MESSAGE);
        }

        // Clear the lyrics, but only if we are playing from the start.
        if (mPlayController.getPausedPosition() == 0 && mLyricDisplay != null) {
            mLyricDisplay.reset();
        }

        mSequencer.setTickPosition(mPlayController.getPausedPosition());
        mSequencer.start();
        mTimer.start();
    }

    @Override
    public void pause() {
        mSequencer.stop();
        mTimer.stop();
    }

    @Override
    public void resume() {
    }

    @Override
    public void stop() {
        mSequencer.stop();
        mSequencer.setTickPosition(0);
        mTimer.stop();
        positionSlider.setValue(0);
    }

    @Override
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

    @Override
    public void loop(boolean loop) {
        positionSlider.setIsLooping(loop);
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

    //
    // END -  MidiSeqPlayer implementation
    //
    ////////////////////////////////////////////////
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
    void showDialog(final String message, final String title,
        final int messageType) {
        EventQueue.invokeLater(new Runnable()
        {
            @Override
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
    public class EventHandler
        implements MetaEventListener
    {
        /**
         * Handle meta events -<ul><li>
         * TimeSignature - update the displayed time signature</li><li>
         * KeySignature - update the displayed key signature</li><li>
         * Tempo - update the displayed tempo</li><li>
         * EndOfTrack - stop the sequencer and update the playing state</li></ul>
         * @param metaMessage the MetaMessage to handle
         */
        @Override
        public void meta(final MetaMessage metaMessage) {
            int type = metaMessage.getType();
            Object[] str = MetaEvent.getMetaStrings(metaMessage);
            // if (!mSequencer.isRunning()) {
            if (type == MetaEvent.END_OF_TRACK) {
                EventQueue.invokeLater(new Runnable()
                {
                    @Override
                    public void run() {
                        stop();
                        mPlayController.stop();
                    }
                });
            }
            if (str[0].equals("M:TimeSignature")) { // NOI18N
                timeSigText.setText(str[2].toString());
            }
            if (str[0].equals("M:KeySignature")) { // NOI18N
                keyText.setText(str[2].toString());
            }
            if (str[0].equals("M:Tempo")) { // NOI18N
                tempoText.setText(str[2].toString());
            }
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form MidiQuickFix.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
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
        editorPanel = new javax.swing.JPanel();
        lyricsPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        sequenceMenu = new javax.swing.JMenu();
        songInfoMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        transposeMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        toolsMenu = new javax.swing.JMenu();
        preferencesMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        traceMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();
        
        setTitle(UiStrings.getString("mqf")); // NOI18N
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });
        
        mainPanel.setLayout(new java.awt.BorderLayout());
        
        mainSplitPane.setDividerSize(11);
        mainSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setOneTouchExpandable(true);
        
        topPanel.setLayout(new java.awt.BorderLayout());
        
        playControlPanel.setLayout(new java.awt.GridBagLayout());
        
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
        
        controlPanel.setBackground(new java.awt.Color(211, 225, 237));
        controlPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        controlPanel.setLayout(new java.awt.GridBagLayout());
        
        progressPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        progressPanel.setLayout(new java.awt.GridBagLayout());
        
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
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        controlPanel.add(progressPanel, gridBagConstraints);
        
        tempoAdjustPanel.setOpaque(false);
        tempoAdjustPanel.setLayout(new java.awt.GridBagLayout());
        
        tempoAdjustLabel.setText(UiStrings.getString("tempo_adjust")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        tempoAdjustPanel.add(tempoAdjustLabel, gridBagConstraints);
        
        tempoAdjustSlider.setMaximum(200);
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
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 16);
        controlPanel.add(tempoAdjustPanel, gridBagConstraints);
        
        seqInfoPanel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        seqInfoPanel.setLayout(new java.awt.GridBagLayout());
        
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
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
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
        gridBagConstraints.insets = new java.awt.Insets(6, 3, 0, 0);
        controlPanel.add(transposeButton, gridBagConstraints);
        
        topPanel.add(controlPanel, java.awt.BorderLayout.SOUTH);
        
        mainSplitPane.setLeftComponent(topPanel);
        
        summaryPanel.setLayout(new javax.swing.BoxLayout(summaryPanel, javax.swing.BoxLayout.LINE_AXIS));
        detailsTabbedPane.addTab(UiStrings.getString("track_summary"), summaryPanel); // NOI18N
        
        editorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
        editorPanel.setLayout(new javax.swing.BoxLayout(editorPanel, javax.swing.BoxLayout.LINE_AXIS));
        detailsTabbedPane.addTab(UiStrings.getString("editor"), editorPanel); // NOI18N
        
        lyricsPanel.setLayout(new java.awt.BorderLayout());
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
        
        sequenceMenu.setMnemonic('S');
        sequenceMenu.setText(UiStrings.getString("sequence")); // NOI18N
        
        songInfoMenuItem.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("SongInfoMenuItem.mnemonic").charAt(0));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle"); // NOI18N
        songInfoMenuItem.setText(bundle.getString("SongInfoMenuItem.text")); // NOI18N
        songInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                songInfoMenuItemActionPerformed(evt);
            }
        });
        sequenceMenu.add(songInfoMenuItem);
        sequenceMenu.add(jSeparator3);
        
        transposeMenuItem.setText(UiStrings.getString("transpose")); // NOI18N
        transposeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transposeMenuItemActionPerformed(evt);
            }
        });
        sequenceMenu.add(transposeMenuItem);
        
        menuBar.add(sequenceMenu);
        
        viewMenu.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("ViewMenu.mnemonic").charAt(0));
        viewMenu.setText(UiStrings.getString("view")); // NOI18N
        menuBar.add(viewMenu);
        
        toolsMenu.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("ToolsMenu.mnemonic").charAt(0));
        toolsMenu.setText(bundle.getString("ToolsMenu.text")); // NOI18N
        
        preferencesMenuItem.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("ToolsMenu.PreferencesMenuItem.mnemonic").charAt(0));
        preferencesMenuItem.setText(bundle.getString("ToolsMenu.PreferencesMenuItem.text")); // NOI18N
        preferencesMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                preferencesMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(preferencesMenuItem);
        toolsMenu.add(jSeparator2);
        
        traceMenuItem.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("TraceWindowMenuItem.mnemonic").charAt(0));
        traceMenuItem.setText(UiStrings.getString("trace_window")); // NOI18N
        traceMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traceMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(traceMenuItem);
        
        menuBar.add(toolsMenu);
        
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

    private void traceMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traceMenuItemActionPerformed
        TraceDialog.getInstance().setVisible(traceMenuItem.getState());
    }//GEN-LAST:event_traceMenuItemActionPerformed

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

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        openFile();
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exitForm(null);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        if (checkForSave() == true) {
            MqfProperties.writeProperties();
            System.exit(0);
        }
    }//GEN-LAST:event_exitForm

    private void songInfoMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_songInfoMenuItemActionPerformed
        showSongInfo();
    }//GEN-LAST:event_songInfoMenuItemActionPerformed

    private void preferencesMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_preferencesMenuItemActionPerformed
        showPreferencesDialog();
    }//GEN-LAST:event_preferencesMenuItemActionPerformed

    /**
     * <B>main</B>
     * @param args The command line arguments
     */
    public static void main(String args[]) {
        try {
            String lafName = MqfProperties.getStringProperty(
                MqfProperties.LOOK_AND_FEEL_NAME, "Nimbus");
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (lafName.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // lafName is not available, use the default look and feel.
        }

        Properties p = System.getProperties();
        // p.list(System.out);
        mJavaVersion = p.getProperty("java.version", "No java.version found"); // NOI18N
        if (mJavaVersion.substring(0, 5).equals("1.4.2")) { // NOI18N
            VERSION_1_4_2_BUG = true;
        } else {
            VERSION_1_4_2_BUG = false;
        }

        // Disable renaming files in the file chooser
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);

        EventQueue.invokeLater(new Runnable()
        {
            @Override
            public void run() {
                new MidiQuickFix().setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JTabbedPane detailsTabbedPane;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JLabel keyText;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JLabel lengthText;
    private javax.swing.JPanel lyricsPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JPanel playControlPanel;
    private com.lemckes.MidiQuickFix.components.LoopSlider positionSlider;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JPanel seqInfoPanel;
    private javax.swing.JFileChooser sequenceChooser;
    private javax.swing.JMenu sequenceMenu;
    private javax.swing.JMenuItem songInfoMenuItem;
    private javax.swing.JPanel summaryPanel;
    private javax.swing.JFormattedTextField tempoAdjustField;
    private javax.swing.JLabel tempoAdjustLabel;
    private javax.swing.JPanel tempoAdjustPanel;
    private javax.swing.JSlider tempoAdjustSlider;
    private javax.swing.JLabel tempoLabel;
    private javax.swing.JLabel tempoText;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel timeSigText;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JPanel topPanel;
    private javax.swing.JCheckBoxMenuItem traceMenuItem;
    private com.lemckes.MidiQuickFix.components.TransportPanel transportPanel;
    private javax.swing.JButton transposeButton;
    private javax.swing.JMenuItem transposeMenuItem;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables
}
