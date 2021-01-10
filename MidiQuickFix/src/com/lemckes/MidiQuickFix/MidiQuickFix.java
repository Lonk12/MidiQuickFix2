/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2019 John Lemcke
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

import com.lemckes.MidiQuickFix.components.TempoSlider;
import com.lemckes.MidiQuickFix.util.BarBeatTick;
import com.lemckes.MidiQuickFix.util.Formats;
import com.lemckes.MidiQuickFix.util.LoopSliderEvent;
import com.lemckes.MidiQuickFix.util.LoopSliderListener;
import com.lemckes.MidiQuickFix.util.MidiFileFilter;
import com.lemckes.MidiQuickFix.util.MidiSeqPlayer;
import com.lemckes.MidiQuickFix.util.MidiUtils;
import com.lemckes.MidiQuickFix.util.MqfProperties;
import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.MidiQuickFix.util.PlayController;
import com.lemckes.MidiQuickFix.util.RecentFiles;
import com.lemckes.MidiQuickFix.util.SoundbankFileFilter;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import com.lemckes.MidiQuickFix.util.TracksChangedEvent;
import com.lemckes.MidiQuickFix.util.TracksChangedListener;
import com.lemckes.MidiQuickFix.util.Transposer;
import com.lemckes.MidiQuickFix.util.UiStrings;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Track;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * A MIDI file editor that works at the Midi event level.
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
    private static String mJavaVersion;
    /**
     * The system synthesizer.
     */
    private static Synthesizer mSynth;
    /**
     * The synth's channels.
     */
    private MidiChannel[] mChannels;
    /**
     * The default sequencer.
     */
    private Sequencer mSequencer;
    /**
     * The sequence from the current file.
     */
    private MqfSequence mSeq;
    /**
     * The resolution in Ticks/Beat of the file.
     */
    private int mResolution;
    /**
     * The short name of the current file.
     */
    private String mFileName;
    /**
     * The full name and path of the current file.
     */
    private String mFilePath;
    /**
     * The recently used files.
     */
    private RecentFiles mRecentFiles;
    /**
     * The timer that drives the position indicator during play.
     */
    private Timer mTimer;
    /**
     * True if the sequence has been modified.
     */
    private boolean mSequenceModified = false;
    /**
     * The current tempo
     */
    private int mCurrentTempo = 60;
    /**
     * The tempo adjustment being applied to the sequence
     */
    private float mTempoFactor = 1.0f;
    private TrackEditorPanel mTrackEditor;
    private TrackSummaryPanel mTrackSummaryPanel;
    private TrackSummaryTable mTrackSummary;
    private TrackMixerPanel mTrackMixer;
    private LyricDisplay mLyricDisplay;
    private AboutDialog mAboutDialog = null;
    private TransposeDialog mTransposeDialog;
    private PlayController mPlayController;
    private static JFrame mMainFrame;
    private Cursor mUnbusyCursor = Cursor.getDefaultCursor();

    /**
     * Creates a new MidiQuickFix instance
     *
     * @param fileName
     */
    public MidiQuickFix(final String fileName) {
        EventQueue.invokeLater(() -> {
            build(fileName);
        });
    }

    private void build(final String fileName) {
        TraceDialog.getInstance().addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                traceMenuItem.setState(false);
            }

            @Override
            public void windowOpened(java.awt.event.WindowEvent evt) {
                traceMenuItem.setState(true);
            }
        });

        TraceDialog.getInstance().setVisible(
            MqfProperties.getBooleanProperty(MqfProperties.SHOW_TRACE, true));

        Startup startDialog = new Startup(new javax.swing.JFrame(), false);
        // Centre on the screen
        startDialog.setLocationRelativeTo(null);
        startDialog.setVisible(true);

        try {
            startDialog.splash.addStageMessage(
                UiStrings.getString("using_java") + " " + mJavaVersion); // NOI18N

            startDialog.splash.addStageMessage(
                UiStrings.getString("init_synth")); // NOI18N
            initSynth();
            startDialog.splash.addStageMessage(
                UiStrings.getString("init_synth_complete")); // NOI18N

            startDialog.splash.addStageMessage(
                UiStrings.getString("loading_soundbank")); // NOI18N
            loadDefaultSoundbank();
            startDialog.splash.addStageMessage(
                UiStrings.getString("loaded_soundbank")); // NOI18N

            startDialog.splash.addStageMessage(
                UiStrings.getString("getting_sequencer")); // NOI18N
            mSequencer = MidiSystem.getSequencer(false);

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
                if (!mSequencer.isOpen()) {
                    mSequencer.open(); // This call blocks the process...
                    mSequencer.getTransmitter().setReceiver(mSynth.getReceiver());
                }
                startDialog.splash.addStageMessage(
                    UiStrings.getString("sequencer_opened")); // NOI18N
            }

            mPlayController = new PlayController(mSequencer);

            initComponents();

            tempoAdjustField.setValue(1.0f);
            tempoAdjustField.addPropertyChangeListener("value", // NOI18N
                (PropertyChangeEvent evt) -> {
                    float val = 1.0f;
                    try {
                        val = (Float)tempoAdjustField.getValue();
                    } catch (ClassCastException cce) {
                        try {
                            double d = (Double)tempoAdjustField.getValue();
                            val = (float)d;
                        } catch (ClassCastException cce2) {
                            try {
                                val = (Long)tempoAdjustField.getValue();
                            } catch (ClassCastException cce3) {
                                // DO NOTHING
                            }
                        }
                    }
                    tempoAdjustSlider.setValue(TempoSlider.tempoToSlider(val));
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

            String lastPath = MqfProperties.getProperty(
                MqfProperties.LAST_PATH_KEY);
            if (lastPath != null) {
                sequenceChooser.setCurrentDirectory(new File(lastPath));
            } else {
                MqfProperties.setStringProperty(MqfProperties.LAST_PATH_KEY,
                    sequenceChooser.getCurrentDirectory().getAbsolutePath());
            }
            MidiFileFilter midiFilter = new MidiFileFilter();
            sequenceChooser.addChoosableFileFilter(midiFilter);
            sequenceChooser.setFileFilter(midiFilter);

            mRecentFiles = new RecentFiles();
            mRecentFiles.fromPropertyString(
                MqfProperties.getStringProperty(MqfProperties.RECENT_FILES, ""));

            String soundbankPath = MqfProperties.getProperty(
                MqfProperties.LAST_SOUNDBANK_PATH_KEY);
            if (soundbankPath != null) {
                soundbankChooser.setCurrentDirectory(new File(soundbankPath));
            } else {
                MqfProperties.setStringProperty(MqfProperties.LAST_SOUNDBANK_PATH_KEY,
                    soundbankChooser.getCurrentDirectory().getAbsolutePath());
            }
            SoundbankFileFilter soundbankFilter = new SoundbankFileFilter();
            soundbankChooser.addChoosableFileFilter(soundbankFilter);
            soundbankChooser.setFileFilter(soundbankFilter);

            mSequencer.addMetaEventListener(new EventHandler());

            /* Update the position 5 times per second */
            createTimer(200);

            mSequenceModified = false;

            positionSlider.addLoopSliderListener(this);

            mChannels = mSynth.getChannels();

            tempoAdjustSlider.addChangeListener((ChangeEvent e) -> {
                setTempoFactor(
                    TempoSlider.sliderToTempo(tempoAdjustSlider.getValue()));
            });

            mTrackSummaryPanel = new TrackSummaryPanel();
            mTrackSummary = new TrackSummaryTable(mSequencer);
            mTrackSummaryPanel.setSummaryTable(mTrackSummary);
            mTrackSummaryPanel.addTracksChangedListener(this);
            summaryPanel.add(mTrackSummaryPanel);

            mTrackEditor = new TrackEditorPanel();
            editorPanel.add(mTrackEditor);
            mTrackEditor.addTableChangeListener(this);

            mTrackMixer = new TrackMixerPanel();
            trackMixerPanel.add(mTrackMixer);

            mLyricDisplay = new LyricDisplay();
            lyricsPanel.add(mLyricDisplay);

            mLyricDisplay.setSequencer(mSequencer);

            pack();
            setLocationRelativeTo(null);

            EventQueue.invokeLater(() -> {
                updateRecentFilesMenu();
                openFile(fileName);
            });

            startDialog.setVisible(false);

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
        mMainFrame = this;

        mainSplitPane.setDividerLocation(mainSplitPane.getWidth() / 8);

        pack();
    }

    public static JFrame getMainFrame() {
        KeyStroke ks = KeyStroke.getKeyStroke('a');
        ks.getModifiers();
        return mMainFrame;
    }

    private void initSynth() throws MidiUnavailableException {
        mSynth = MidiSystem.getSynthesizer();
        mSynth.open();
    }

    private void openSoundbankFile() {
        File file = null;

        String defPath = MqfProperties.getStringProperty(
            MqfProperties.LAST_SOUNDBANK_PATH_KEY,
            soundbankChooser.getCurrentDirectory().getAbsolutePath());
        soundbankChooser.setCurrentDirectory(new File(defPath));

        int open = soundbankChooser.showOpenDialog(this);
        if (open == JFileChooser.APPROVE_OPTION) {
            file = soundbankChooser.getSelectedFile();
        }

        // file was selected from file chooser
        if (file != null) {
            if (file.canRead()) {
                String path = file.getAbsoluteFile().getParent();
                MqfProperties.setProperty(MqfProperties.LAST_SOUNDBANK_PATH_KEY, path);
                loadSoundbank(file);
            }
        }
    }

    private void loadDefaultSoundbank() {
        String sf2File;
//        sf2File = "FluidR3_GM.sf2";
//        sf2File = "merlin_gmv32.sf2";
//        sf2File = "merlin_silver.sf2";
//        sf2File = "A320U.sf2";
        sf2File = "BankZero.sf2";
//        sf2File = "TimGM6mb.sf2";
        URL url = getClass().getResource("resources/Soundbanks/" + sf2File); // NOI18N
        loadSoundbank(url);
    }

    private void loadSoundbank(File file) {
        try {
            loadSoundbank(file.toURI().toURL());
        } catch (MalformedURLException ex) {
            Logger.getLogger(MidiQuickFix.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void loadSoundbank(URL url) {
        try {
            Soundbank soundbank;
            soundbank = MidiSystem.getSoundbank(url);
            if (mSynth.isSoundbankSupported(soundbank)) {
                mSynth.unloadAllInstruments(mSynth.getDefaultSoundbank());

//                 // Load all the instruments in the soundbank
//                 if (mSynth.loadAllInstruments(soundbank)) {
//                     TraceDialog.addTrace("Loaded Soundbank : " + soundbank.getName());
//                 }
//                 debugSoundbank(soundbank);
//                // Just load the basic Bank 0 patches
//                Patch[] patches = new Patch[128];
//                for (int prog = 0; prog < 128; prog++) {
//                    patches[prog] = new Patch(0, prog);
//                 }
//                // or use Patch[] patches = mSeq.getPatchList();
//                try {
//                    if (mSynth.loadInstruments(soundbank, patches)) {
//                        trace("Loaded Bank 0 from : " + soundbank.getName());
//                    } else {
//                        trace("Failed to Load Bank 0 from : " + soundbank.getName());
//                    }
//                } catch (IllegalArgumentException ex) {
//                    Logger.getLogger(MidiQuickFix.class.getName()).log(Level.SEVERE, null, ex);
//                } finally {
////                     debugSoundbank(soundbank);
//                }
                // Load all the patches from the soundbank, one at a time
                int instrumentCount = 0;
                int failedCount = 0;
                for (Instrument newI : soundbank.getInstruments()) {
                    int bank = newI.getPatch().getBank();
                    int prog = newI.getPatch().getProgram();
                    if (bank == 0 && prog < 128) {
                        ++instrumentCount;
                        if (mSynth.loadInstrument(newI) == false) {
                            ++failedCount;
                            trace("Failed to load instrument. Bank: " + bank
                                + " Prog: " + prog + " from " + soundbank.getName());
                        }
                    } else {
                        break;
                    }
                }
                trace("Loaded " + (instrumentCount - failedCount) + " / "
                    + instrumentCount + " from Bank 0 of : " + soundbank.getName());
            } else {
                trace("Soundbank : " + soundbank.getName() + " not supported!");
            }
            InstrumentNames.populateNames();
        } catch (InvalidMidiDataException | IOException ex) {
            Logger.getLogger(MidiUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void debugSoundbank(Soundbank soundbank) {
        System.out.println("==================================================");
        System.out.println("Soundbank : " + soundbank.getName());
        System.out.println("Soundbank : " + soundbank.getVendor());
        System.out.println("Soundbank : " + soundbank.getVersion());
        // System.out.println("Soundbank : " + soundbank.getDescription());

        for (Instrument loadedInst : mSynth.getLoadedInstruments()) {
            int bank = loadedInst.getPatch().getBank();
            int prog = loadedInst.getPatch().getProgram();

            System.out.println("Loaded Inst "
                + bank + "/" + prog + " - "
                + loadedInst.getName());
        }

        System.out.println("==================================================");
    }

    public static Synthesizer getSynth() {
        return mSynth;
    }

    private void createTimer(int delay) {
        java.awt.event.ActionListener taskPerformer;
        taskPerformer = (java.awt.event.ActionEvent evt) -> {
            final long ticks = mSequencer.getTickPosition();
            positionSlider.setValue((int)ticks, false);
        };
        mTimer = new Timer(delay, taskPerformer);
    }

    private void setTempoFactor(float factor) {
        mTempoFactor = factor;
        mSequencer.setTempoFactor(factor);
        tempoAdjustField.setValue(factor);
        setTempoLabel(Integer.toString(mCurrentTempo));
    }

    /**
     * Change the mouse pointer to the wait or previous cursor
     *
     * @param busy If true show the WAIT_CURSOR otherwise show the previous
     * cursor
     */
    public void setBusy(boolean busy) {
        if (busy) {
            // Save the current cursor
            mUnbusyCursor = getCursor();
            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        } else {
            setCursor(mUnbusyCursor);
        }
    }

    @Override
    public void loopSliderChanged(LoopSliderEvent evt) {
        long pos = evt.getValue();
        mSequencer.setTickPosition(pos);
        if (!mSequencer.isRunning()) {
            mPlayController.setPausedPosition(pos); // in ticks
        }
    }

    @Override
    public void loopPointChanged(LoopSliderEvent evt) {
        long inPoint = evt.getInPoint();
        long outPoint = evt.getOutPoint();
        if (inPoint >= 0) {
            mSequencer.setLoopStartPoint(inPoint);
        }

        if (outPoint >= 0) {
            mSequencer.setLoopEndPoint(outPoint);
        }
    }

    /**
     * Open the given File and create a Sequence object from it.
     * Currently does not validate that the file is a midi file.
     * The file i/o is performed in a worker thread and when it is
     * complete
     * <code>buildNewSequence()</code> is called in the
     * Swing event thread. Any UI updates that are dependent on
     * the completion of the file operation must be done in
     * <code>buildNewSequence()</code>
     *
     * @param file The midi file to open.
     */
    private void newSequence(final java.io.File file) {
        final SwingWorker<MqfSequence, Void> worker
            = new SwingWorker<MqfSequence, Void>()
        {
            // Open the file in the worker thread
            @Override
            public MqfSequence doInBackground() {
                MqfSequence seq = null;
                try {
                    setBusy(true);
                    // Construct an MqfSequence object
                    seq = new MqfSequence(MidiSystem.getSequence(file));

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
                return seq;
            }

            //Build sequence in the event-dispatching thread.
            @Override
            public void done() {
                try {
                    mSeq = get();
                    if (mSeq != null) {
                        buildNewSequence();
                    }
                } catch (InterruptedException ex) {
                    // don't care
                } catch (ExecutionException ex) {
                    String pourQuoi;
                    Throwable cause = ex.getCause();
                    if (cause != null) {
                        pourQuoi = cause.getMessage();
                    } else {
                        pourQuoi = ex.getMessage();
                    }
                    trace("Error creating sequence: " + pourQuoi);
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
            mResolution = mSeq.getResolution();

            tempoAdjustSlider.setValue(TempoSlider.tempoToSlider(1.0f));
            positionSlider.reset();
            positionSlider.setBarBeatTick(new BarBeatTick(mSeq));

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

            if (mTrackMixer != null) {
                mTrackMixer.setSequence(mSeq);
                mTrackMixer.setSequencer(mSequencer);
            }

            mPlayController.setPlayState(PlayController.PlayState.STOPPED);

            mLyricDisplay.setSequence(mSeq);

            mSequenceModified = false;

            setTitle(mFileName);
            setInfoLabels();
        } finally {
            setBusy(false);
            //pack();
        }
    }

    @Override
    public void tracksChanged(TracksChangedEvent changeEvent) {
        // TRACK_CHANGED should only update the LyricDisplay
        if (changeEvent.getChangeType()
            != TracksChangedEvent.TrackChangeType.TRACK_CHANGED) {
            if (mTrackEditor != null) {
                mTrackEditor.setSequence(mSeq);
            }

            if (mTrackSummaryPanel != null) {
                mTrackSummaryPanel.setSequence(mSeq);
            }
            mSequenceModified = true;
        }

        if (mLyricDisplay != null) {
            mLyricDisplay.rebuild();
        }
    }

    private void openFile(String fileToOpen) {
        boolean canContinue = checkForSave(null);
        if (canContinue) {
            String fileName = fileToOpen;
            // No fileName given; open the file chooser
            if (fileName == null) {
                String defPath = MqfProperties.getStringProperty(
                    MqfProperties.LAST_PATH_KEY,
                    sequenceChooser.getCurrentDirectory().getAbsolutePath());
                sequenceChooser.setCurrentDirectory(new File(defPath));
                int open = sequenceChooser.showOpenDialog(this);
                if (open == JFileChooser.APPROVE_OPTION) {
                    File file = sequenceChooser.getSelectedFile();
                    fileName = file.getPath();
                }
            }

            // fileName was given as a parameter or selected from file chooser
            if (fileName != null) {
                File file = new File(fileName).getAbsoluteFile();
                if (file.canRead()) {
                    String absFileName = file.getPath();
                    mRecentFiles.useFile(absFileName);
                    MqfProperties.setProperty(
                        MqfProperties.RECENT_FILES,
                        mRecentFiles.toPropertyString());
                    String parentPathName = file.getParent();
                    MqfProperties.setProperty(
                        MqfProperties.LAST_PATH_KEY, parentPathName);
                    newSequence(file);

                    EventQueue.invokeLater(() -> {
                        updateRecentFilesMenu();
                    });
                }
            }
        }
    }

    private void updateRecentFilesMenu() {

        openRecentMenu.removeAll();

        for (String pathname : mRecentFiles.getFiles()) {
            String filename = "NONE";
            int lastSlash = pathname.lastIndexOf(File.separatorChar);
            if (lastSlash > -1) {
                filename = pathname.substring(lastSlash + 1);
            }
            javax.swing.JMenuItem menuItem = new javax.swing.JMenuItem();

            menuItem.setText(filename);
            menuItem.setToolTipText(pathname);
            menuItem.setActionCommand(pathname);
            openRecentMenu.add(menuItem);
            menuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
                openFile(evt.getActionCommand());
            });
        }

        if (!mRecentFiles.getFiles().isEmpty()) {
            openRecentMenu.addSeparator();
            javax.swing.JMenuItem clearMenuItem = new javax.swing.JMenuItem();
            clearMenuItem.setText(UiStrings.getString("clear_recent"));
            openRecentMenu.add(clearMenuItem);
            clearMenuItem.addActionListener((java.awt.event.ActionEvent evt) -> {
                mRecentFiles.getFiles().clear();
                openRecentMenu.removeAll();
                MqfProperties.setProperty(
                    MqfProperties.RECENT_FILES,
                    mRecentFiles.toPropertyString());
            });
        }

    }

    /**
     * Check if the sequence needs to be saved.
     * If it does give the user a confirmation dialog.
     * When this method is invoked from the user selecting the File/Open
     * or File/Quit menu options the {@code evt} parameter is {@code null}
     * and the dialog contains Yes/No/Cancel options.
     * <br>
     * When this method is invoked from the user clicking the
     * window manager close button the {@code evt} parameter will be
     * an AWTEvent of some sort. In this case only Yes/No options are
     * available as the window manager close action can not be cancelled.
     * <br>
     * If the user elects to save give them a Save As dialog.
     *
     * @param evt Not null if this method is called from the user
     * clicking the window manager close button.
     * @return True if the user chooses to continue with the action by selecting
     * Yes or No,
     * False if the user selects Cancel.
     */
    private boolean checkForSave(java.awt.event.WindowEvent evt) {
        boolean continueAction = true;
        if (mSequenceModified) {
            int answer = JOptionPane.showConfirmDialog(this,
                UiStrings.getString("check_save"),
                UiStrings.getString("save_changes"),
                evt == null ? JOptionPane.YES_NO_CANCEL_OPTION : JOptionPane.YES_NO_OPTION);
            switch (answer) {
                case JOptionPane.YES_OPTION:
                    java.io.File myMidiFile = new java.io.File(mFilePath);
                    saveAs(myMidiFile);
                    break;
                case JOptionPane.NO_OPTION:
                    // Do nothing
                    break;
                case JOptionPane.CANCEL_OPTION:
                    continueAction = false;
                    break;
                default:
                    break;
            }
        }
        return continueAction;
    }

    /**
     * Save the current sequence to the given file.
     *
     * @param file The file to which the sequence is to be saved.
     */
    public void saveFile(java.io.File file) {
        try {
            // Make sure that the Sequencer has the latest version of the Sequence.
            mSequencer.setSequence(mSeq);

            // Save the Sequence object
            int types[] = MidiSystem.getMidiFileTypes(mSeq);

            // Default to type 1
            int type = 1;

            if (types.length > 0) {
                // Use the simplest supported type
                Arrays.sort(types);
                type = types[0];
            }
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
     *
     * @param file If not null the given file will be selected
     * as the default.
     */
    private void saveAs(java.io.File file) {
        if (file != null) {
            sequenceChooser.setSelectedFile(file);
        }

        int save = sequenceChooser.showSaveDialog(this);
        if (save == JFileChooser.APPROVE_OPTION) {
            saveFile(sequenceChooser.getSelectedFile());
        }
    }

    private void debugTiming(String source) {
        long ticks = mSeq.getTickLength();
        long durUs = mSeq.getMicrosecondLength();
        int ticksPerBeat = mSeq.getResolution();

        System.out.println("From: " + source);
        System.out.println("uSecs = " + durUs);
        System.out.println("ticks = " + ticks);
        System.out.println("uS/tick = " + durUs / ticks);
        System.out.println("resol = " + mResolution);
        System.out.println("resol = " + ticksPerBeat);
        System.out.println("beats = " + (float)ticks / mResolution);
        System.out.println("b/min = " + 60.0f * ((float)ticks / mResolution) / (mSeq.getMicrosecondLength() / 1000000f));
    }

    /**
     * Set the text of the info labels.
     */
    private void setInfoLabels() {
        long dur = mSeq.getMicrosecondLength() / 1000000;
        lengthText.setText(Formats.formatSeconds(dur));

        long ticks = mSeq.getTickLength();
        positionSlider.setDuration(ticks, true, mResolution);

//        debugTiming("setInfoLabels()");
        // Get the info from META events at tick zero.
        boolean foundTimeSig = false;
        boolean foundTempo = false;
        boolean foundKeySig = false;
        for (Track track : mSeq.getTracks()) {
            // Stop if we have found all the fields we need.
            if (foundTimeSig && foundTempo && foundKeySig) {
                break;
            }
            int eventCount = track.size() - 1;
            for (int eventIndex = 0; eventIndex < eventCount; ++eventIndex) {
                MidiEvent ev = track.get(eventIndex);
                MidiMessage mess = ev.getMessage();
                long tick = ev.getTick();
                if (tick > 0) {
                    break;
                }
                if (mess.getStatus() == MetaMessage.META) {
                    Object[] str = MetaEvent.getMetaStrings((MetaMessage)mess);
                    if (str[0].equals("M:TimeSignature")) {
                        setTimeSigField(str[2].toString());
                        foundTimeSig = true;
                    } else if (str[0].equals("M:Tempo")) {
                        setTempoLabel(str[2].toString());
                        foundTempo = true;
                    } else if (str[0].equals("M:KeySignature")) {
                        setKeySigField(str[2].toString());
                        foundKeySig = true;
                    }
                }
            }
        }
    }

    private void setTimeSigField(String time) {
        String[] parts = time.split(" ");
        timeSigText.setText(parts[0]);
        timeSigText.setToolTipText(time);
    }

    private void setTempoLabel(String tempo) {
        String labelString = Integer.toString(mCurrentTempo);
        // Get the default bg colour from another JLabel
        Color bg = timeSigText.getBackground();

        try {
            mCurrentTempo = Integer.parseInt(tempo);
            labelString = tempo;
        } catch (NumberFormatException nfe) {
            // just ignore it, leaving the existing value
        }

        if (mTempoFactor != 1.0f) {
            int newTempo = Math.round(mCurrentTempo * mTempoFactor);
            if (mTempoFactor > 1.0f) {
                labelString = newTempo + " +";
                float sat = 0.1f + ((mTempoFactor - 1.0f) * 0.8f);
                bg = Color.getHSBColor(0.4f, sat, 1.0f);
            } else if (mTempoFactor < 1.0f) {
                labelString = newTempo + " -";
                float sat = 0.1f + ((1.0f - mTempoFactor) * 0.8f);
                bg = Color.getHSBColor(0.1f, sat, 1.0f);
            }
        }
        tempoText.setBackground(bg);
        tempoText.setText(labelString);
    }

    private void setKeySigField(String key) {
        keyText.setText(key);
    }

    private void doTranspose() {
        if (mTransposeDialog == null) {
            mTransposeDialog = new TransposeDialog(this, true);
        }
        // Reset the transpose dialog to zero
        mTransposeDialog.setTransposeBy(0);
        mTransposeDialog.pack();
        mTransposeDialog.setLocationRelativeTo(transposeButton);
        mTransposeDialog.setVisible(true);
        if (mTransposeDialog.getReturnStatus() == TransposeDialog.RET_OK) {
            boolean running = mSequencer.isRunning();
            if (running) {
                mSequencer.stop();
            }
            boolean overflowed
                = Transposer.transpose(
                    mSeq,
                    mTransposeDialog.getTransposeBy(),
                    mTransposeDialog.getDoDrums());
            mTrackEditor.setSequence(mSeq);
            mTrackSummaryPanel.setSequence(mSeq);
            setInfoLabels();
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

    /**
     * Called on a change to the track editor table
     *
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
            boolean wasPlaying
                = (mPlayController.getPlayState() == PlayController.PlayState.PLAYING);
            if (wasPlaying) {
                mTimer.stop();
                mPlayController.pause();
            }
            mTrackSummary.setSequence(mSeq);
            try {
                mSequencer.setSequence(mSeq);
                if (e.getColumn() == 5 || e.getColumn() == TableModelEvent.ALL_COLUMNS) {
                    mLyricDisplay.rebuild();
                }
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

    private void showSongInfo() {
        new SongInfoDialog(mSeq, this, false).setVisible(true);
    }

    private void showPreferencesDialog() {
        new PreferencesDialog(this, false, this).setVisible(true);
    }

    private void showQuantiseDialog() {
//        QuantiseDialog.analyseSequence(mSequence);
//        mLyricDisplay.setSequence(mSequence);
//        mTrackEditor.setSequence(mSequence);
//        mSequenceModified=true;
//        mTrackSummary.setSequence(mSequence);
//        new QuantiseDialog(mSeq, this, false).setVisible(true);
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
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | UnsupportedLookAndFeelException e) {
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
        positionSlider.setValue(0, false);
        mLyricDisplay.moveCaretToStart();
    }

    @Override
    public void rewind() {
        // Turn off all notes
        // Does not work!!
//        for (int i = 0; i < mChannels.length; ++i) {
//            mChannels[i].allNotesOff();
//            mChannels[i].allSoundOff();
//        }
        mSequencer.setTickPosition(0);
        positionSlider.setValue(0, false);
        mLyricDisplay.moveCaretToStart();

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
            long inPoint = positionSlider.getLoopInPoint();
            long outPoint = positionSlider.getLoopOutPoint();
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
     *
     * @param s the message to display
     */
    public void trace(final String s) {
        TraceDialog.addTrace(s);
    }

    /**
     * A wrapper to call JOptionPane.showMessageDialog using
     * EventQueue.invokeLater
     *
     * @param message the message to display
     * @param title the title for the dialog window
     * @param messageType one of the JOptionPane.*_MESSAGE constants
     */
    void showDialog(final String message, final String title,
        final int messageType) {
        EventQueue.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                mainPanel, message, title, messageType);
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
         * EndOfTrack - stop the sequencer and update the playing
         * state</li></ul>
         *
         * @param metaMessage the MetaMessage to handle
         */
        @Override
        public void meta(final MetaMessage metaMessage) {
            int type = metaMessage.getType();
            Object[] str = MetaEvent.getMetaStrings(metaMessage);
            // if (!mSequencer.isRunning()) {
            if (type == MetaEvent.END_OF_TRACK) {
                EventQueue.invokeLater(() -> {
                    stop();
                    mPlayController.stop();
                });
            }
            if (str[0].equals("M:TimeSignature")) { // NOI18N
                setTimeSigField(str[2].toString());
                // debugTiming("EventHandler.meta(M:TimeSignature)");
            } else if (str[0].equals("M:KeySignature")) { // NOI18N
                setKeySigField(str[2].toString());
            } else if (str[0].equals("M:Tempo")) { // NOI18N
                setTempoLabel(str[2].toString());
                // debugTiming("EventHandler.meta(M:Tempo)");
            }
        }
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form MidiQuickFix.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sequenceChooser = new javax.swing.JFileChooser();
        soundbankChooser = new javax.swing.JFileChooser();
        mainPanel = new javax.swing.JPanel();
        mainSplitPane = new javax.swing.JSplitPane();
        bodyPanel = new javax.swing.JPanel();
        controlPanel = new javax.swing.JPanel();
        progressPanel = new javax.swing.JPanel();
        positionSlider = new com.lemckes.MidiQuickFix.components.LoopSlider();
        tempoAdjustPanel = new javax.swing.JPanel();
        tempoAdjustLabel = new javax.swing.JLabel();
        tempoAdjustField = new JFormattedTextField(new DecimalFormat("0.00"));
        tempoAdjustSlider = new com.lemckes.MidiQuickFix.components.TempoSlider();
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
        trackMixerPanel = new javax.swing.JPanel();
        lyricsPanel = new javax.swing.JPanel();
        topPanel = new javax.swing.JPanel();
        playControlPanel = new javax.swing.JPanel();
        transportPanel = new com.lemckes.MidiQuickFix.components.TransportPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        openMenuItem = new javax.swing.JMenuItem();
        openRecentMenu = new javax.swing.JMenu();
        reloadMenuitem = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        exitMenuItem = new javax.swing.JMenuItem();
        sequenceMenu = new javax.swing.JMenu();
        songInfoMenuItem = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        transposeMenuItem = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        quantiseMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        soundbankMenuItem = new javax.swing.JMenuItem();
        preferencesMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        traceMenuItem = new javax.swing.JCheckBoxMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        soundbankChooser.setName("soundbankChooser"); // NOI18N

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

        bodyPanel.setBackground(new java.awt.Color(211, 225, 237));
        bodyPanel.setLayout(new java.awt.BorderLayout());

        controlPanel.setBackground(new java.awt.Color(211, 225, 237));
        controlPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        controlPanel.setLayout(new java.awt.GridBagLayout());

        progressPanel.setLayout(new java.awt.GridBagLayout());

        positionSlider.setBackground(new java.awt.Color(197, 210, 221));
        positionSlider.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createEtchedBorder(), javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 210, 221), 2)));
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
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        tempoAdjustPanel.add(tempoAdjustLabel, gridBagConstraints);

        tempoAdjustField.setBackground(new java.awt.Color(233, 247, 255));
        tempoAdjustField.setColumns(4);
        tempoAdjustField.setFont(new java.awt.Font("Monospaced", 0, 10)); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 3;
        gridBagConstraints.ipady = 3;
        tempoAdjustPanel.add(tempoAdjustField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 0, 6);
        tempoAdjustPanel.add(tempoAdjustSlider, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(6, 16, 0, 16);
        controlPanel.add(tempoAdjustPanel, gridBagConstraints);

        seqInfoPanel.setBackground(new java.awt.Color(195, 208, 219));
        seqInfoPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        seqInfoPanel.setLayout(new java.awt.GridBagLayout());

        lengthLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        lengthLabel.setText(UiStrings.getString("duration")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(2, 3, 0, 0);
        seqInfoPanel.add(lengthLabel, gridBagConstraints);

        lengthText.setBackground(new java.awt.Color(233, 247, 255));
        lengthText.setFont(lengthText.getFont().deriveFont(lengthText.getFont().getSize()-1f));
        lengthText.setText("--:--");
        lengthText.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 6));
        lengthText.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(2, 4, 0, 3);
        seqInfoPanel.add(lengthText, gridBagConstraints);

        tempoLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        tempoLabel.setText(UiStrings.getString("tempo")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        seqInfoPanel.add(tempoLabel, gridBagConstraints);

        tempoText.setBackground(new java.awt.Color(233, 247, 255));
        tempoText.setFont(tempoText.getFont().deriveFont(tempoText.getFont().getSize()-1f));
        tempoText.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 3);
        seqInfoPanel.add(tempoText, gridBagConstraints);

        timeLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        timeLabel.setText(UiStrings.getString("timesig")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 0, 0);
        seqInfoPanel.add(timeLabel, gridBagConstraints);

        timeSigText.setBackground(new java.awt.Color(233, 247, 255));
        timeSigText.setFont(timeSigText.getFont().deriveFont(timeSigText.getFont().getSize()-1f));
        timeSigText.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 0, 3);
        seqInfoPanel.add(timeSigText, gridBagConstraints);

        keyLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        keyLabel.setText(UiStrings.getString("key")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        gridBagConstraints.insets = new java.awt.Insets(3, 3, 2, 0);
        seqInfoPanel.add(keyLabel, gridBagConstraints);

        keyText.setBackground(new java.awt.Color(233, 247, 255));
        keyText.setFont(keyText.getFont().deriveFont(keyText.getFont().getSize()-1f));
        keyText.setOpaque(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(3, 4, 2, 3);
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

        bodyPanel.add(controlPanel, java.awt.BorderLayout.PAGE_START);

        detailsTabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        summaryPanel.setLayout(new javax.swing.BoxLayout(summaryPanel, javax.swing.BoxLayout.LINE_AXIS));
        detailsTabbedPane.addTab(UiStrings.getString("track_summary"), summaryPanel); // NOI18N

        editorPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 0, 3, 0));
        editorPanel.setLayout(new javax.swing.BoxLayout(editorPanel, javax.swing.BoxLayout.LINE_AXIS));
        detailsTabbedPane.addTab(UiStrings.getString("track_editor"), editorPanel); // NOI18N

        trackMixerPanel.setLayout(new java.awt.BorderLayout());
        detailsTabbedPane.addTab(UiStrings.getString("track_mixer"), trackMixerPanel); // NOI18N

        lyricsPanel.setLayout(new java.awt.BorderLayout());
        detailsTabbedPane.addTab(UiStrings.getString("lyrics"), lyricsPanel); // NOI18N

        bodyPanel.add(detailsTabbedPane, java.awt.BorderLayout.CENTER);

        mainSplitPane.setRightComponent(bodyPanel);

        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.LINE_AXIS));

        playControlPanel.setLayout(new javax.swing.BoxLayout(playControlPanel, javax.swing.BoxLayout.LINE_AXIS));
        topPanel.add(playControlPanel);

        transportPanel.setAlignmentX(1.0F);
        transportPanel.setPreferredSize(new java.awt.Dimension(200, 40));
        topPanel.add(transportPanel);

        mainSplitPane.setLeftComponent(topPanel);

        mainPanel.add(mainSplitPane, java.awt.BorderLayout.CENTER);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        fileMenu.setMnemonic('F');
        fileMenu.setText(UiStrings.getString("file")); // NOI18N

        openMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        openMenuItem.setMnemonic('O');
        openMenuItem.setText(UiStrings.getString("open")); // NOI18N
        openMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(openMenuItem);

        openRecentMenu.setText("Open Recent");
        fileMenu.add(openRecentMenu);

        reloadMenuitem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings"); // NOI18N
        reloadMenuitem.setText(bundle.getString("reload")); // NOI18N
        reloadMenuitem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadMenuitemActionPerformed(evt);
            }
        });
        fileMenu.add(reloadMenuitem);
        fileMenu.add(jSeparator5);

        saveMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveMenuItem.setMnemonic('S');
        saveMenuItem.setText(UiStrings.getString("save")); // NOI18N
        saveMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveMenuItem);

        saveAsMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_DOWN_MASK | java.awt.event.InputEvent.CTRL_DOWN_MASK));
        saveAsMenuItem.setMnemonic('a');
        saveAsMenuItem.setText(UiStrings.getString("saveas")); // NOI18N
        saveAsMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsMenuItem);
        fileMenu.add(jSeparator1);

        exitMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_DOWN_MASK));
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

        songInfoMenuItem.setMnemonic(UiStrings.getString("SongInfoMenuItem.mnemonic").charAt(0));
        songInfoMenuItem.setText(UiStrings.getString("SongInfoMenuItem.text")); // NOI18N
        songInfoMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                songInfoMenuItemActionPerformed(evt);
            }
        });
        sequenceMenu.add(songInfoMenuItem);
        sequenceMenu.add(jSeparator3);

        transposeMenuItem.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("TransposeMenuItem.mnemonic").charAt(0));
        transposeMenuItem.setText(UiStrings.getString("transpose")); // NOI18N
        transposeMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                transposeMenuItemActionPerformed(evt);
            }
        });
        sequenceMenu.add(transposeMenuItem);
        sequenceMenu.add(jSeparator4);

        quantiseMenuItem.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("QuantiseMenuItem.mnemonic").charAt(0));
        java.util.ResourceBundle bundle1 = java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle"); // NOI18N
        quantiseMenuItem.setText(bundle1.getString("QuantiseMenuItem.text")); // NOI18N
        quantiseMenuItem.setEnabled(false);
        quantiseMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quantiseMenuItemActionPerformed(evt);
            }
        });
        sequenceMenu.add(quantiseMenuItem);

        menuBar.add(sequenceMenu);

        toolsMenu.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("ToolsMenu.mnemonic").charAt(0));
        toolsMenu.setText(bundle1.getString("ToolsMenu.text")); // NOI18N

        soundbankMenuItem.setText("Load Soundbank");
        soundbankMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                soundbankMenuItemActionPerformed(evt);
            }
        });
        toolsMenu.add(soundbankMenuItem);

        preferencesMenuItem.setMnemonic(java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle").getString("ToolsMenu.PreferencesMenuItem.mnemonic").charAt(0));
        preferencesMenuItem.setText(bundle1.getString("ToolsMenu.PreferencesMenuItem.text")); // NOI18N
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
        EventQueue.invokeLater(() -> {
            setBusy(true);
            JFrame frame = mMainFrame;
            if (mAboutDialog == null) {
                mAboutDialog = new AboutDialog(frame, false);
                mAboutDialog.populateMessages();
            }
            mAboutDialog.setVisible(true);
            setBusy(false);
        });

    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void saveMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveMenuItemActionPerformed
        java.io.File myMidiFile = new java.io.File(mFilePath);
        saveFile(myMidiFile);
    }//GEN-LAST:event_saveMenuItemActionPerformed

    private void saveAsMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsMenuItemActionPerformed
        saveAs(null);
    }//GEN-LAST:event_saveAsMenuItemActionPerformed

    private void openMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMenuItemActionPerformed
        openFile(null);
    }//GEN-LAST:event_openMenuItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        exitForm(null);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    /**
     * Exit the Application
     */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        if (checkForSave(evt) == true) {
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

    private void quantiseMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quantiseMenuItemActionPerformed
        //  showQuantiseDialog();
    }//GEN-LAST:event_quantiseMenuItemActionPerformed

    private void reloadMenuitemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadMenuitemActionPerformed
        openFile(mFilePath);
    }//GEN-LAST:event_reloadMenuitemActionPerformed

    private void soundbankMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_soundbankMenuItemActionPerformed
        openSoundbankFile();
    }//GEN-LAST:event_soundbankMenuItemActionPerformed

    /**
     * <B>main</B>
     *
     * @param args The command line arguments
     */
    public static void main(String args[]) {
        MqfProperties.readProperties();
        try {
            String lafName = MqfProperties.getStringProperty(
                MqfProperties.LOOK_AND_FEEL_NAME, "Nimbus");
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (lafName.equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            // lafName is not available, use the default look and feel.
        }

//        float uiScale = MqfProperties.getFloatProperty(MqfProperties.UI_FONT_SCALE, 1.0f);
//        EventQueue.invokeLater(() -> {
//            setDefaultFontSize(uiScale);
//        });
        Properties p = System.getProperties();
        // p.list(System.out);
        mJavaVersion = p.getProperty("java.version", "No java.version found"); // NOI18N
        VERSION_1_4_2_BUG = mJavaVersion.substring(0, 5).equals("1.4.2"); // NOI18N

        // Disable renaming files in the file chooser
        UIManager.put("FileChooser.readOnly", Boolean.TRUE);

        final String fileName;
        if (args.length > 0) {
            fileName = args[0];
        } else {
            fileName = null;
        }
        EventQueue.invokeLater(() -> {
            new MidiQuickFix(fileName).setVisible(true);
        });
    }

    public static void setDefaultFontSize(float scale) {
        Set<Object> keySet = UIManager.getLookAndFeelDefaults().keySet();
        Object[] keys = keySet.toArray(new Object[keySet.size()]);
        for (Object key : keys) {
            if (key != null && key.toString().toLowerCase().contains("font")) {
                java.awt.Font font = UIManager.getDefaults().getFont(key);
                if (font != null) {
                    float defaultSize = font.getSize2D();
                    float size = defaultSize * scale;
                    // String family = font.getFamily();
                    // System.out.println(key + ": \t family = " + family + " \tfrom = " + defaultSize + ", \tto = " + size);
                    font = font.deriveFont(size);
                    UIManager.put(key, font);
                }
            }
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JPanel bodyPanel;
    private javax.swing.JPanel controlPanel;
    private javax.swing.JTabbedPane detailsTabbedPane;
    private javax.swing.JPanel editorPanel;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JPopupMenu.Separator jSeparator5;
    private javax.swing.JLabel keyLabel;
    private javax.swing.JLabel keyText;
    private javax.swing.JLabel lengthLabel;
    private javax.swing.JLabel lengthText;
    private javax.swing.JPanel lyricsPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JSplitPane mainSplitPane;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenu openRecentMenu;
    private javax.swing.JPanel playControlPanel;
    private com.lemckes.MidiQuickFix.components.LoopSlider positionSlider;
    private javax.swing.JMenuItem preferencesMenuItem;
    private javax.swing.JPanel progressPanel;
    private javax.swing.JMenuItem quantiseMenuItem;
    private javax.swing.JMenuItem reloadMenuitem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JPanel seqInfoPanel;
    private javax.swing.JFileChooser sequenceChooser;
    private javax.swing.JMenu sequenceMenu;
    private javax.swing.JMenuItem songInfoMenuItem;
    private javax.swing.JFileChooser soundbankChooser;
    private javax.swing.JMenuItem soundbankMenuItem;
    private javax.swing.JPanel summaryPanel;
    private javax.swing.JFormattedTextField tempoAdjustField;
    private javax.swing.JLabel tempoAdjustLabel;
    private javax.swing.JPanel tempoAdjustPanel;
    private com.lemckes.MidiQuickFix.components.TempoSlider tempoAdjustSlider;
    private javax.swing.JLabel tempoLabel;
    private javax.swing.JLabel tempoText;
    private javax.swing.JLabel timeLabel;
    private javax.swing.JLabel timeSigText;
    private javax.swing.JMenu toolsMenu;
    private javax.swing.JPanel topPanel;
    private javax.swing.JCheckBoxMenuItem traceMenuItem;
    private javax.swing.JPanel trackMixerPanel;
    private com.lemckes.MidiQuickFix.components.TransportPanel transportPanel;
    private javax.swing.JButton transposeButton;
    private javax.swing.JMenuItem transposeMenuItem;
    // End of variables declaration//GEN-END:variables
}
