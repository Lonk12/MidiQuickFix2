package com.lemckes.MidiQuickFix;

import javax.sound.midi.*;

class SysexEvent {
    static String[] getSysexStrings(SysexMessage mess) {
        String[] result = {"", "", ""};
        int st = mess.getStatus();
        if (st == javax.sound.midi.SysexMessage.SYSTEM_EXCLUSIVE) {
            result[0] = "SYSTEM_EXCLUSIVE";
        } else if (st == javax.sound.midi.SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
            result[0] = "SPECIAL_SYSTEM_EXCLUSIVE";
        }
        byte[] data = mess.getData();
        result[1] += data.length;
        for (int i = 0; i < data.length; ++i) {
            if (i > 0) {
                 result[2] += ",";
            }
            result[2] += data[i] & 0x00ff;
        }
        return result;
    }
}
