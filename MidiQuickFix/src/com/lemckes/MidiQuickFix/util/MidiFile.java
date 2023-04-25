/** ************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2023 John Lemcke
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
package com.lemckes.MidiQuickFix.util;

import java.io.IOException;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;

/**
 * Get a javax.sound.midi.Sequence from a file
 */
public class MidiFile
{

    static public MqfSequence openSequenceFile(String fileName)
        throws InvalidMidiDataException, IOException {
        java.io.File myMidiFile = new java.io.File(fileName);
        return openSequenceFile(myMidiFile);
    }

    static public MqfSequence openSequenceFile(java.io.File file)
        throws InvalidMidiDataException, IOException {
        // Construct a Sequence object
        return new MqfSequence(MidiSystem.getSequence(file));
    }
}
