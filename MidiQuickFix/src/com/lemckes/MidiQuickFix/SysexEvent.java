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

import javax.sound.midi.SysexMessage;

/**
 * Handle Midi System Exclusive events.
 * @version $Id$
 */
class SysexEvent {
    static String[] getSysexStrings(SysexMessage mess) {
        String[] result = {"", "", ""}; // NOI18N
        int st = mess.getStatus();
        if (st == SysexMessage.SYSTEM_EXCLUSIVE) {
            result[0] = "SYSTEM_EXCLUSIVE"; // NOI18N
        } else if (st == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
            result[0] = "SPECIAL_SYSTEM_EXCLUSIVE"; // NOI18N
        }
        byte[] data = mess.getData();
        result[1] += data.length;
        for (int i = 0; i < data.length; ++i) {
            if (i > 0) {
                 result[2] += ","; // NOI18N
            }
            result[2] += data[i] & 0x00ff;
        }
        return result;
    }
}
