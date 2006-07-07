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
import javax.sound.midi.*;

/**
 *
 * @version $Id$
 */
public class PlayController {
    
    static final public int NO_FILE = -1;
    static final public int STOPPED = 0;
    static final public int PAUSED = 1;
    static final public int PLAYING = 2;
    
    private boolean looping = false;
    
    int mPlayState = NO_FILE;
    
    /** The microsecond position at which the sequence was paused. */
    long mPausedPos;
    
    Sequencer mSequencer;
    Sequence mSeq;
    
    /** Creates a new instance of PlayController */
    public PlayController(Sequencer sqr, Sequence seq) {
        mSequencer = sqr;
        mSeq = seq;
    }
    
    /**
     * Set the state of play ;-)
     */
    public void setPlayState(int state) {
        mPlayState = state;
        if (state == STOPPED) {
            mPausedPos = 0;
        }
        setActions();
    }
    
    public int getPlayState() {
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
        /** Creates a new instance of PlayAction */
        public PlayAction() {
//            putValue(SMALL_ICON,
//                new javax.swing.ImageIcon(getClass().getResource(
//                "/com/lemckes/MidiQuickFix/resources/Play16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_SPACE, java.awt.event.InputEvent.ALT_MASK));
        }
        
        /**
         * Performs the functions required for Playing
         * @param e The event which triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // System.out.println("PlayAction.actionPerformed");
            play();
        }
    }
    transient public PlayAction mPlayAction = new PlayAction();
    
    public void play() {
        MidiSeqPlayer player = (MidiSeqPlayer)mPlayAction.getValue("player");
        if (mPlayState == PLAYING) {
            player.rewind();
            mPausedPos = 0;
        }
        player.play();
        mPlayState = PLAYING;
        setActions();
    }
    
    /**
     * An Action to handle the Pause option.
     */
    public class PauseAction extends javax.swing.AbstractAction {
        /** Creates a new instance of PauseAction */
        public PauseAction() {
//            putValue(SMALL_ICON,
//                new javax.swing.ImageIcon(getClass().getResource(
//                "/com/lemckes/MidiQuickFix/resources/Pause16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.ALT_MASK));
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
    transient public PauseAction mPauseAction = new PauseAction();
    
    public void pause() {
        MidiSeqPlayer pauser = (MidiSeqPlayer)mPauseAction.getValue("pauser");
        if (mPlayState == PLAYING) {
            mPausedPos = mSequencer.getMicrosecondPosition();
            pauser.pause();
            mPlayState = PAUSED;
            setActions();
        } else if (mPlayState == PAUSED) {
            pauser.play();
            mPlayState = PLAYING;
            setActions();
        }
    }
    
    /**
     * An Action to handle the Stop option.
     */
    public class StopAction extends javax.swing.AbstractAction {
        /** Creates a new instance of StopAction */
        public StopAction() {
//            putValue(SMALL_ICON,
//                new javax.swing.ImageIcon(getClass().getResource(
//                "/com/lemckes/MidiQuickFix/resources/Stop16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_BACK_SPACE , java.awt.event.InputEvent.ALT_MASK));
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
    transient public StopAction mStopAction = new StopAction();
    
    public void stop() {
        MidiSeqPlayer stopper = (MidiSeqPlayer)mStopAction.getValue("stopper");
        if (mPlayState != STOPPED) {
            stopper.stop();
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
//            putValue(SMALL_ICON,
//                new javax.swing.ImageIcon(getClass().getResource(
//                "/com/lemckes/MidiQuickFix/resources/Rewind16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_LEFT, java.awt.event.InputEvent.ALT_MASK));
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
    transient public RewindAction mRewindAction = new RewindAction();
    
    public void rewind() {
        MidiSeqPlayer rewinder = (MidiSeqPlayer)mRewindAction.getValue("rewinder");
        rewinder.rewind();
        mPausedPos = 0;
        setActions();
    }
    
    /**
     * An Action to handle the Loop option.
     */
    public class LoopAction extends javax.swing.AbstractAction {
        /** Creates a new instance of LoopAction */
        public LoopAction() {
//            putValue(SMALL_ICON,
//                new javax.swing.ImageIcon(getClass().getResource(
//                "/com/lemckes/MidiQuickFix/resources/Loop16.gif")));
            putValue(ACCELERATOR_KEY,
                    javax.swing.KeyStroke.getKeyStroke(
                    java.awt.event.KeyEvent.VK_L , java.awt.event.InputEvent.ALT_MASK));
        }
        
        /**
         * Performs the functions required for Looping
         * @param e The event which triggered the action.
         */
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // System.out.println("LoopAction.actionPerformed");
            loop();
        }
    }
    transient public LoopAction mLoopAction = new LoopAction();
    
    public void loop() {
        MidiSeqPlayer looper = (MidiSeqPlayer)mLoopAction.getValue("looper");
        looping = !looping;
        System.out.println(looping ? "Looping" : "Not Looping");
        looper.loop(looping);
        //setActions();
    }
    
    private void setActions() {
        if (mPlayState == NO_FILE) {
            mPlayAction.setEnabled(false);
            mPauseAction.setEnabled(false);
            mRewindAction.setEnabled(false);
            mStopAction.setEnabled(false);
            mLoopAction.setEnabled(false);
        } else if (mPlayState == PLAYING) {
            mPlayAction.setEnabled(false);
            mPauseAction.setEnabled(true);
            mRewindAction.setEnabled(true);
            mStopAction.setEnabled(true);
            mLoopAction.setEnabled(true);
        } else if (mPlayState == PAUSED) {
            mPlayAction.setEnabled(true);
            mPauseAction.setEnabled(true);
            mRewindAction.setEnabled(true);
            mStopAction.setEnabled(true);
            mLoopAction.setEnabled(true);
        } else if (mPlayState == STOPPED) {
            mPlayAction.setEnabled(true);
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
