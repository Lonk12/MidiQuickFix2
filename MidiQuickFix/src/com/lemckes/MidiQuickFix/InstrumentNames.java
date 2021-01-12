/** ************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2021 John Lemcke
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
package com.lemckes.MidiQuickFix;

import java.util.SortedMap;
import java.util.TreeMap;
import javax.sound.midi.Instrument;

/**
 * Methods associated with MIDI instrument (patch) names.
 */
class InstrumentNames
{

    /**
     * Map from instrument name to bank and program numbers.
     * The Integer value is calculated as
     * <code>(bank &lt;&lt; 8) + program</code>
     */
    private static SortedMap<String, Integer> mInstrumentNameMap;
    /**
     * Map from bank and program numbers to instrument name.
     * The Integer key is calculated as
     * <code>(bank &lt;&lt; 8) + program</code>
     */
    private static SortedMap<Integer, String> mInstrumentNumberMap;
    /**
     * Map from bank number to a map from instrument name to program number.
     */
    private static SortedMap<Integer, SortedMap<String, Integer>> mBankNameMap;
    /**
     * Map from bank number to a map from program number to instrument name.
     */
    private static SortedMap<Integer, SortedMap<Integer, String>> mBankNumberMap;

    private static final InstrumentNames INSTANCE
        = new InstrumentNames();

    /**
     * Create an InstrumentNames object.
     */
    InstrumentNames() {
        populateNames();
    }

    public final static void populateNames() {
        mInstrumentNameMap = new TreeMap<>();
        mInstrumentNumberMap = new TreeMap<>();
        mBankNameMap = new TreeMap<>();
        mBankNumberMap = new TreeMap<>();
        javax.sound.midi.Synthesizer synth = MidiQuickFix.getSynth();

        javax.sound.midi.Instrument[] instruments;
        instruments = synth.getLoadedInstruments();

        for (Instrument i : instruments) {
            int bank = i.getPatch().getBank();
            int prog = i.getPatch().getProgram();
            int bankProgram = (bank << 8) + prog;
            String name = i.getName();
            //System.out.println(bank + "." + prog + "." + name + " - " + i.getSoundbank().getName());

            mInstrumentNameMap.put(name, bankProgram);
            mInstrumentNumberMap.put(bankProgram, name);

            if (mBankNameMap.get(bank) == null) {
                mBankNameMap.put(bank, new TreeMap<>());
            }
            mBankNameMap.get(bank).put(name, prog);

            if (mBankNumberMap.get(bank) == null) {
                mBankNumberMap.put(bank, new TreeMap<>());
            }
            mBankNumberMap.get(bank).put(prog, name);
        }
    }

    /**
     * Get the single instance of InstrumentNames
     *
     * @return
     */
    public static InstrumentNames getInstance() {
        return INSTANCE;
    }

    /**
     * Get the instrument (patch) name.
     *
     * @param program The program number.
     * @return The instrument (patch) name.
     */
    public String getName(int bank, int program) {
        String name = "NoName";
        int bankProgram = (bank << 8) + program;
        if (mInstrumentNumberMap.containsKey(bankProgram)) {
            name = mInstrumentNumberMap.get(bankProgram);
        }

        return name;
    }

    /**
     * Get the instrument (patch) name assuming it to be in the first bank.
     *
     * @param program The patch number.
     * @return The instrument (patch) name.
     */
    public String getName(int program) {
        return getName(0, program);
    }

    /**
     * Get all the instrument names.
     *
     * @return The array of instrument names.
     */
    public String[] getNameArray() {
        return mInstrumentNumberMap.values().toArray(new String[0]);
    }

    /**
     * Get the instrument names for a given bank.
     *
     * @param bank the bank for which to get the names
     * @return The array of instrument names.
     */
    public String[] getNameArray(int bank) {
        return mBankNumberMap.get(bank).values().toArray(new String[0]);
    }

    /**
     * Get the program number associated with the named instrument.
     *
     * @param name The name of the instrument.
     * @return The patch number for the named instrument.
     */
    public int getInstrumentNumber(String name) {
        // The program number is in the low order byte.
        return mInstrumentNameMap.get(name) & 127;
    }

    /**
     * Get the program number associated with the named instrument.
     *
     * @param name The name of the instrument.
     * @return The patch number for the named instrument.
     */
    public int getInstrumentBank(String name) {
        // The bank number is in the high order byte.
        return mInstrumentNameMap.get(name) >> 8;
    }
}
