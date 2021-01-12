/**************************************************************
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
 **************************************************************/

package com.lemckes.MidiQuickFix;

import java.util.ResourceBundle;


/**
 * Methods associated with MIDI Controller events
 */
class Controllers {
    /** The names of the controllers are retrieved from a resource file. */
    private static final ResourceBundle mControllersBundle;
    private static final String[] mNameArray;
    /** A String used to display a 'graphical' representation
     * of the controller's value.
     */
    static String meter = "---------------|---------------"; // NOI18N
    
    /** Create an ControllerNames object. */
    static {
        mControllersBundle =
            ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/Controllers"); // NOI18N
        int count = Integer.parseInt(mControllersBundle.getString("count")); // NOI18N
        mNameArray = new String[count];
        for (int i = 0; i < count; ++i) {
            mNameArray[i] = mControllersBundle.getString(Integer.toString(i));
        }
    }
    
    /** Get the name of a controller, given its numeric value.
     * @param num The MIDI controller number.
     * @return The name of the controller.
     */
    public static String getControlName(int num) {
        return mNameArray[num];
    }
    
    /** Get the array of controller names.
     * @return The names of the controllers.
     */
    public static String[] getNameArray() {
        return mNameArray;
    }
    
    /** Get a String representation of the controller's value.
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
