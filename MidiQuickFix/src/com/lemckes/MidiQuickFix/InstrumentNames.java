package com.lemckes.MidiQuickFix;

import java.util.*;

/** Methods associated with MIDI instrument (patch) names. */
class InstrumentNames {
    /** The names of the instruments (patches) are retrieved from a resource file. */
    static ResourceBundle mInstrumentsBundle;
    static String[] mNameArray;
    
    /** Create an InstrumentNames object. */
    static {
        mInstrumentsBundle = ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/GM1Instruments");
        int numInsts = Integer.parseInt(mInstrumentsBundle.getString("count"));
        
        javax.sound.midi.Synthesizer synth = null;
        javax.sound.midi.Instrument[] instrument = null;
        try {
            synth = javax.sound.midi.MidiSystem.getSynthesizer(); //
        } catch (javax.sound.midi.MidiUnavailableException e) {
        }
        instrument = synth.getAvailableInstruments(); //
        //             synth.getLoadedInstruments();
        
        // mNameArray = new String[numInsts];
        mNameArray = new String[instrument.length];
        for (int j = 0; j < instrument.length; ++j) {
            // mNameArray[j] = mInstrumentsBundle.getString(new Integer(j + 1).toString());
            System.out.println(j + "   " + instrument[j].getName());
            mNameArray[j] = instrument[j].getName();
        }
    }
    
    /** Get the instrument (patch) name.
     * @return The instrument (patch) name.
     * @param num The patch number.
     */
    static public String getName(int num) {
        return mNameArray[num].toString();
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
