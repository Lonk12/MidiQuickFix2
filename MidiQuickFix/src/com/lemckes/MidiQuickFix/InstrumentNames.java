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

import java.util.HashMap;
import com.lemckes.MidiQuickFix.util.TraceDialog;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Methods associated with MIDI instrument (patch) names.
 * @version $Id$
 */
class InstrumentNames {
    
    static SortedMap<String, Integer> mInstrumentNameMap;
    static SortedMap<Integer, String> mInstrumentNumberMap;
    static SortedMap<Integer, SortedMap<String, Integer>> mBankNameMap;
    static SortedMap<Integer, SortedMap<Integer, String>> mBankNumberMap;
    
    /** Create an InstrumentNames object. */
    static {
        try {
            javax.sound.midi.Synthesizer synth = null;
            javax.sound.midi.Instrument[] instruments = null;
            synth = javax.sound.midi.MidiSystem.getSynthesizer();
            instruments = synth.getAvailableInstruments();
            
            mInstrumentNameMap = new TreeMap<String, Integer>();
            mInstrumentNumberMap = new TreeMap<Integer, String>();
            mBankNameMap = new TreeMap<Integer, SortedMap<String, Integer>>();
            mBankNumberMap = new TreeMap<Integer, SortedMap<Integer, String>>();
            
            for (int j = 0; j < instruments.length; ++j) {
                int bank = instruments[j].getPatch().getBank();
                int prog = instruments[j].getPatch().getProgram();
                int bankProgram = bank * 256 + prog;
                String name = instruments[j].getName();
                System.out.println(j + "." + bank + "." + prog + "." + name);
                mInstrumentNameMap.put(name, bankProgram);
                mInstrumentNumberMap.put(bankProgram, name);
                
                if (mBankNameMap.get(bank) == null) {
                    mBankNameMap.put(bank, new TreeMap<String, Integer>());
                }
                mBankNameMap.get(bank).put(name, bankProgram);
                
                if (mBankNumberMap.get(bank) == null) {
                    mBankNumberMap.put(bank, new TreeMap<Integer, String>());
                }
                mBankNumberMap.get(bank).put(bankProgram, name);
            }
        } catch (javax.sound.midi.MidiUnavailableException e) {
            TraceDialog.addTrace("Failed to getSynthesizer()  " + e.getMessage());
            TraceDialog.addTrace("-- Using resource bundle for instrument names");
            
            ResourceBundle mInstrumentsBundle =
                ResourceBundle.getBundle(
                "com/lemckes/MidiQuickFix/resources/GM1Instruments"); // NOI18N
            int numInsts =
                Integer.parseInt(mInstrumentsBundle.getString("count")); // NOI18N
            
            mInstrumentNameMap = new TreeMap<String, Integer>();
            mInstrumentNumberMap = new TreeMap<Integer, String>();
            for (int j = 0; j < numInsts; ++j) {
                mInstrumentNameMap.put(
                  mInstrumentsBundle.getString(Integer.toString(j + 1)), j);
                mInstrumentNumberMap.put(
                  j, mInstrumentsBundle.getString(Integer.toString(j + 1)));
            }
        }
    }
    
    /** Get the instrument (patch) name.
     * @param num The patch number.
     * @return The instrument (patch) name.
     */
    static public String getName(int bank, int program) {
        return mBankNumberMap.get(bank).get(bank * 256 + program);
    }
    
    /** Get the instrument (patch) name assuming it to be in the first bank.
     * @param num The patch number.
     * @return The instrument (patch) name.
     */
    static public String getName(int program) {
        return getName(0, program);
    }
    
    /** Get all the instrument names.
     * @return The array of instrument names.
     */
    static public Object[] getNameArray() {
        return mInstrumentNumberMap.values().toArray();
    }
    
    /**
     * Get the instrument names for a given bank.
     * @param bank the bank for which to get the names
     * @return The array of instrument names.
     */
    static public Object[] getNameArray(int bank) {
        return mBankNumberMap.get(bank).values().toArray();
    }
    
    /** Get the program number associated with the named instrument.
     * @param name The name of the instrument.
     * @return The patch number for the named instrument.
     */
    static public int getInstrumentNumber(String name) {
        // The program number is in the low order byte.
        return mInstrumentNameMap.get(name) & 127;
    }
    
    /** Get the program number associated with the named instrument.
     * @param name The name of the instrument.
     * @return The patch number for the named instrument.
     */
    static public int getInstrumentBank(String name) {
        // The bank number is in the high order byte.
        return mInstrumentNameMap.get(name) >> 8;
    }
    
}
