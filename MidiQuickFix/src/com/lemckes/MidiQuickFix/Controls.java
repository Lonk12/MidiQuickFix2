package com.lemckes.MidiQuickFix;

import java.util.*;

/** Methods associated with MIDI Controller events */
class Controls {
    /** The names of the controllers are retrieved from a resource file. */
    static ResourceBundle mControllersBundle;
    static String[] mNameArray;
    
    /** Create an ControllerNames object. */
    static {
        mControllersBundle = ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/Controllers");
        int count = Integer.parseInt(mControllersBundle.getString("count"));
        mNameArray = new String[count];
        for (int i = 0; i < count; ++i) {
            mNameArray[i] = mControllersBundle.getString(new Integer(i).toString());
        }
    }
    
    /** Get the name of a controller, given its numeric value.
     * @param num The MIDI controller number.
     * @return The name of the controller.
     */
    public static String getControlName(int num) {
        return mNameArray[num];
    }
    
    /** A String used to display a 'graphical' representation
     * of the controller's value.
     */
    static String meter = "---------------|---------------";
    /** Get a String representatioin of the controller's value.
     * @param val The controller's value
     * @param graphic If true the returned string is a graphical
     * representation of the value. e.g. value 64 gives
     * "--------|--------"
     * @return The value of the controller.
     */
    public static String getControlValue(int val, boolean graphic) {
        String result = "";
        if (graphic) {
            int start = (127 - val)/8;
            result += meter.substring(start, start + 16);
        } else {
            result += val;
        }
        return result;
    }
    
    /** Get the controller number associated with the name.
     * @param name The name of the controller.
     * @return The controller number for the named controller.
     */
    static public int getControllerNumber(String name) {
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
