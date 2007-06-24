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

import com.lemckes.MidiQuickFix.util.TraceDialog;
import java.util.ResourceBundle;


/**
 * Methods associated with MIDI instrument (patch) names.
 * @version $Id$
 */
class InstrumentNames {
    
    static String[] mNameArray;
    
    /** Create an InstrumentNames object. */
    static {
        try {
            javax.sound.midi.Synthesizer synth = null;
            javax.sound.midi.Instrument[] instruments = null;
            synth = javax.sound.midi.MidiSystem.getSynthesizer();
            instruments = synth.getAvailableInstruments();
            
            mNameArray = new String[instruments.length];
            for (int j = 0; j < instruments.length; ++j) {
                // TraceDialog.addTrace(j + "   " + instruments[j].getName());
                mNameArray[j] = instruments[j].getName();
            }
        } catch (javax.sound.midi.MidiUnavailableException e) {
            TraceDialog.addTrace("Failed to getSynthesizer()  " + e.getMessage());
            TraceDialog.addTrace("-- Using resource bundle for instrument names");
            
            ResourceBundle mInstrumentsBundle =
                ResourceBundle.getBundle(
                "com/lemckes/MidiQuickFix/resources/GM1Instruments"); // NOI18N
            int numInsts =
                Integer.parseInt(mInstrumentsBundle.getString("count")); // NOI18N
            
            mNameArray = new String[numInsts];
            for (int j = 0; j < numInsts; ++j) {
                mNameArray[j] =
                    mInstrumentsBundle.getString(Integer.toString(j + 1));
                // TraceDialog.addTrace(j + "   " + mNameArray[j]);
            }
        }
    }
    
    /** Get the instrument (patch) name.
     * @return The instrument (patch) name.
     * @param num The patch number.
     */
    static public String getName(int num) {
        return mNameArray[num];
    }
    
    /** Get all the instrument names from the resource bundle.
     * @return The array of instrument names.
     */
    static public String[] getNameArray() {
        return mNameArray;
    }
    
    /** Get the patch number associated with the named instrument.
     * @param name The name of the instrument.
     * @return The patch number for the named instrument.
     */
    static public int getInstrumentNumber(String name) {
        int res = 0;
        for (int i = 0; i < mNameArray.length; ++i) {
            if (name.equals(mNameArray[i])) {
                res = i;
                break;
            }
        }
        return res;
    }
    
}
