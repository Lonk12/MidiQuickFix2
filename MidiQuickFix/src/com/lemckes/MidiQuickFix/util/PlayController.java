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
package com.lemckes.MidiQuickFix.util;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.swing.AbstractAction;

/**
 * Maintains the state of play
 * @version $Id$
 */
public class PlayController {
    public enum PlayState {
        NO_FILE,
        STOPPED,
        PAUSED,
        PLAYING;
        static final long serialVersionUID = 0L;
    }
    private boolean looping = false;
    PlayState mPlayState = PlayState.NO_FILE;
    /** The tick position at which the sequence was paused. */
    long mPausedPos;
    Sequencer mSequencer;
    Sequence mSeq;

    /** Creates a new instance of PlayController
     * @param sqr the sequencer to control
     * @param seq the sequence to control
     */
    public PlayController(Sequencer sqr, Sequence seq) {
        mSequencer = sqr;
        mSeq = seq;
    }

    /**
     * Set the state of play ;-)
     * @param state Probably should be one of NO_FILE, STOPPED, PAUSED, PLAYING
     */
    public void setPlayState(PlayState state) {
        mPlayState = state;
        if (state == PlayState.STOPPED) {
            mPausedPos = 0;
        }
        setActions();
    }

    public PlayState getPlayState() {
        return mPlayState;
    }

    public long getPausedPosition() {
        return mPausedPos;
    }

    public void setPausedPosition(long pos) {
        mPausedPos = pos;
    }

    public void setSequence(Sequence seq) {
        mSeq = seq;
    }

    /**
     * An Action to handle the Play option.
     */
    public class PlayAction extends javax.swing.AbstractAction {
        static final long serialVersionUID = 8706407788844636763L;

        /** Creates a new instance of PlayAction */
        public PlayAction() {
            putValue(ACCELERATOR_KEY,
                javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_SPACE,
                java.awt.event.InputEvent.ALT_MASK));
        }

        /**
         * Performs the functions required for Playing
         * @param e The event that triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            play();
        }
    }
    transient public PlayAction mPlayAction = new PlayAction();

    public void play() {
        MidiSeqPlayer player = (MidiSeqPlayer)mPlayAction.getValue("player");
        player.play();
        mPlayState = PlayState.PLAYING;
        setActions();
    }

    /**
     * An Action to handle the Pause option.
     */
    public class PauseAction extends javax.swing.AbstractAction {
        static final long serialVersionUID = -5681941137061861878L;

        /** Creates a new instance of PauseAction */
        public PauseAction() {
            putValue(ACCELERATOR_KEY,
                javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
        }

        /**
         * Performs the functions required for Playing
         * @param e The event that triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            pause();
        }
    }
    transient public PauseAction mPauseAction = new PauseAction();

    public void pause() {
        MidiSeqPlayer pauser = (MidiSeqPlayer)mPauseAction.getValue("pauser");
        if (mPlayState == PlayState.PLAYING) {
            mPausedPos = mSequencer.getTickPosition();
            pauser.pause();
            mPlayState = PlayState.PAUSED;
            setActions();
        } else if (mPlayState == PlayState.PAUSED) {
            pauser.play();
            mPlayState = PlayState.PLAYING;
            setActions();
        }
    }

    /**
     * An Action to handle the Stop option.
     */
    public class StopAction extends javax.swing.AbstractAction {
        static final long serialVersionUID = -1630547059111486423L;

        /** Creates a new instance of StopAction */
        public StopAction() {
            putValue(ACCELERATOR_KEY,
                javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_BACK_SPACE,
                java.awt.event.InputEvent.ALT_MASK));
        }

        /**
         * Performs the functions required for Stopping
         * @param e The event that triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            stop();
        }
    }
    transient public StopAction mStopAction = new StopAction();

    public void stop() {
        MidiSeqPlayer stopper = (MidiSeqPlayer)mStopAction.getValue("stopper");
        if (mPlayState != PlayState.STOPPED) {
            stopper.stop();
            mPausedPos = 0;
            mPlayState = PlayState.STOPPED;
        }
        setActions();
    }

    /**
     * An Action to handle the Rewind option.
     */
    public class RewindAction extends javax.swing.AbstractAction {
        static final long serialVersionUID = 112737302001281486L;

        /** Creates a new instance of RewindAction */
        public RewindAction() {
            putValue(ACCELERATOR_KEY,
                javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_LEFT,
                java.awt.event.InputEvent.ALT_MASK));
        }

        /**
         * Performs the functions required for Rewinding
         * @param e The event that triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            rewind();
        }
    }
    transient public RewindAction mRewindAction = new RewindAction();

    public void rewind() {
        MidiSeqPlayer rewinder =
            (MidiSeqPlayer)mRewindAction.getValue("rewinder");
        rewinder.rewind();
        mPausedPos = 0;
        setActions();
    }

    /**
     * An Action to handle the Loop option.
     */
    public class LoopAction extends javax.swing.AbstractAction {
        static final long serialVersionUID = 8900494421130657603L;

        /** Creates a new instance of LoopAction */
        public LoopAction() {
            putValue(ACCELERATOR_KEY,
                javax.swing.KeyStroke.getKeyStroke(
                java.awt.event.KeyEvent.VK_L,
                java.awt.event.InputEvent.ALT_MASK));
        }

        /**
         * Performs the functions required for Looping
         * @param e The event that triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            loop();
        }
    }
    transient public LoopAction mLoopAction = new LoopAction();

    public void loop() {
        MidiSeqPlayer looper = (MidiSeqPlayer)mLoopAction.getValue("looper");
        looping = !looping;
        looper.loop(looping);
    }

    private void setActions() {
        if (mPlayState == PlayState.NO_FILE) {
            ((DrawnIcon)mPlayAction.getValue(AbstractAction.SMALL_ICON)).setActive(false);
            mPlayAction.setEnabled(false);
            ((DrawnIcon)mPauseAction.getValue(AbstractAction.SMALL_ICON)).setActive(false);
            mPauseAction.setEnabled(false);
            mRewindAction.setEnabled(false);
            mStopAction.setEnabled(false);
            mLoopAction.setEnabled(false);
        } else if (mPlayState == PlayState.PLAYING) {
            ((DrawnIcon)mPlayAction.getValue(AbstractAction.SMALL_ICON)).setActive(true);
            mPlayAction.setEnabled(false);
            ((DrawnIcon)mPauseAction.getValue(AbstractAction.SMALL_ICON)).setActive(false);
            mPauseAction.setEnabled(true);
            mRewindAction.setEnabled(true);
            mStopAction.setEnabled(true);
            mLoopAction.setEnabled(true);
        } else if (mPlayState == PlayState.PAUSED) {
            ((DrawnIcon)mPlayAction.getValue(AbstractAction.SMALL_ICON)).setActive(true);
            mPlayAction.setEnabled(true);
            ((DrawnIcon)mPauseAction.getValue(AbstractAction.SMALL_ICON)).setActive(true);
            mPauseAction.setEnabled(true);
            mRewindAction.setEnabled(true);
            mStopAction.setEnabled(true);
            mLoopAction.setEnabled(true);
        } else if (mPlayState == PlayState.STOPPED) {
            ((DrawnIcon)mPlayAction.getValue(AbstractAction.SMALL_ICON)).setActive(false);
            mPlayAction.setEnabled(true);
            ((DrawnIcon)mPauseAction.getValue(AbstractAction.SMALL_ICON)).setActive(false);
            mPauseAction.setEnabled(false);
            mRewindAction.setEnabled(false);
            mStopAction.setEnabled(false);
            mLoopAction.setEnabled(true);
        }
    }

    public boolean isLooping() {
        return looping;
    }

    public void setLooping(boolean looping) {
        this.looping = looping;
    }
}
