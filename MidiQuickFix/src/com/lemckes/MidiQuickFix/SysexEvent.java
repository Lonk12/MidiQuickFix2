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

import javax.sound.midi.SysexMessage;

/**
 * Handle Midi System Exclusive events.
 */
class SysexEvent
{

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
        StringBuilder sb = new StringBuilder(data.length * 6);
        for (int k = 0; k < data.length; ++k) {
            int i = data[k] & 0x00ff;
            if (k > 0) {
                sb.append(" "); // NOI18N
            }
            sb.append("0x"); // NOI18N
            sb.append(Integer.toHexString(i)); // NOI18N
        }
        result[2] = sb.toString();
        return result;
    }

    private SysexEvent() {
    }
}
