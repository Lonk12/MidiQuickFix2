package com.lemckes.MidiQuickFix;

import java.util.ResourceBundle;


/** Methods associated with MIDI instrument (patch) names. */
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
                System.out.println(j + "   " + instruments[j].getName());
                mNameArray[j] = instruments[j].getName();
            }
        } catch (javax.sound.midi.MidiUnavailableException e) {
            System.out.println("Failed to getSynthesizer() : " + e.getMessage());
            System.out.println("  --  Using resource bundle for instrument names");
            
            ResourceBundle mInstrumentsBundle =
                    ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/GM1Instruments");
            int numInsts =
                    Integer.parseInt(mInstrumentsBundle.getString("count"));
            
            mNameArray = new String[numInsts];
            for (int j = 0; j < numInsts; ++j) {
                mNameArray[j] = mInstrumentsBundle.getString(new Integer(j + 1).toString());
                System.out.println(j + "   " + mNameArray[j]);
            }
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
