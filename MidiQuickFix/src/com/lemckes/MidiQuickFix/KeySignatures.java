/*
 * KeySignature.java
 *
 * Created on 12 November 2003, 08:18
 */

package com.lemckes.MidiQuickFix;

/** Methods associted with key signatures.
 * @author john
 */
public class KeySignatures {
    
    /** The names of the key signatures. */
    static String[] keyNames = {
        "Cb",
        "Gb",
        "Db",
        "Ab",
        "Eb",
        "Bb",
        "F",
        "C",
        "G",
        "D",
        "A",
        "E",
        "B",
        "F#",
        "C#",
    };
    
    /** Get the name of the key.
     * @return The String representation of the key signature.
     * e.g. "Ab major"
     * @param data data[0] defines the number of sharps/ flats.
     * Negative values represent flats;
     * Positive values represent sharps.
     * i.e. -7=7flats +7=7sharps.
     * data[1] 0=major, 1=minor.
     */
    static String getKeyName(byte data[]) {
        String result = new String("");
        int k = data[0] + 7;
        int m = data[1];
        result += keyNames[k];
        if (m == 1) {
            result += "m";
        }
        return result;
    }
    
    /** Get the key and mode for the given string.
     * @param keyName The String version of the key signature
     * as returned from getKeyName.
     * @return Return data[0] defines the number of sharps/ flats.
     * Negative values represent flats;
     * Positive values represent sharps.
     * i.e. -7=7flats +7=7sharps.
     * Return data[1] 0=major, 1=minor.
     * @see Midi.KeySignatures.getKeyName
     *
     */
    static byte[] getKeyValues(String keyName) {
        byte[] result = { 0, 0 };
        // Check for a minor key
        int mPos = keyName.indexOf("m");
        if (mPos != -1) {
            result[1] = 1;
            // and remove the trailing "m"
            keyName = keyName.substring(0, mPos);
        }
        
        for (byte i = 0; i < keyNames.length; ++i) {
            if (keyName.equals(keyNames[i])) {
                result[0] = (byte)(i - 7);
                break;
            }
        }
        return result;
    }
    
    /** Check if the notes in the key should be displayed as flats or sharps.
     * @param keyName The String version of the key signature
     * as returned by getKeyName.
     * @return True if the notes should be displayed as flats.
     */
    static boolean isInFlats(String keyName) {
        if (keyName == null) {
            return false;
        }
        
        byte[] data = getKeyValues(keyName);
        return isInFlats(data[0]);
    }
    
    /**
     * @param keyNum The key number. Positive values define
     * the number of sharps in the key signature. Negative
     * values define the number of flats.
     * @return True if the notes should be displayed as flats.
     */
    static boolean isInFlats(int keyNum) {
        return keyNum < 0;
    }
}
