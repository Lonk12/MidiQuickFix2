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
package com.lemckes.MidiQuickFix.util;

/**
 * Defines the interface for classes that can play a midi sequence
 * @version $Id: MidiSeqPlayer.java,v 1.5 2009/03/16 07:44:20 jostle Exp $
 */
public interface MidiSeqPlayer {
    /**
     * Start playing the sequence
     */
    public void play();

    /**
     * Pause the sequence
     */
    public void pause();

    /**
     * Play the sequence from the last pause point
     */
    public void resume();

    /**
     * Stop playing the sequence
     */
    public void stop();

    /**
     * Rewind the sequence to the start
     */
    public void rewind();

    /**
     * Enable or disable looping of the sequence
     * @param loop enable looping if <code>true</code>
     */
    public void loop(boolean loop);
}
