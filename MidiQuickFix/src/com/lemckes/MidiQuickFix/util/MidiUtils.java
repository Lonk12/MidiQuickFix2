/*
 */
package com.lemckes.MidiQuickFix.util;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiDeviceReceiver;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Transmitter;

/**
 *
 */
public class MidiUtils
{

    Sequence mSeq;
    MidiSystem mSystem;

    public static void checkInfos() throws MidiUnavailableException {
        Info[] infos = MidiSystem.getMidiDeviceInfo();
        int i = 0;
        for (Info info : infos) {
            System.out.println("Info " + i++ + " = " + info);
            MidiDevice dev = MidiSystem.getMidiDevice(info);
            for(Receiver rec : dev.getReceivers()){
                System.out.println("Receiver " + rec.toString());
            }
            for(Transmitter tr : dev.getTransmitters()){
                System.out.println("Transmitter " + tr.toString());
            }
        }
    }

    public static void checkFileTypes() {
        int[] types = MidiSystem.getMidiFileTypes();
        for (int i : types) {
            System.out.println("FileType " + i);
        }
    }

    public static void checkReceiver() throws MidiUnavailableException
    {
        Receiver rec = MidiSystem.getReceiver();
        rec.close();
    }

    public static void checkTransmitter() throws MidiUnavailableException
    {
        Transmitter tr = MidiSystem.getTransmitter();
        Receiver rec = new MidiDeviceReceiver() {

            @Override
            public MidiDevice getMidiDevice() {
                MidiDevice dev = null;
                try {
                     dev= MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[0]);
                } catch (MidiUnavailableException ex) {
                    ex.printStackTrace();
                }
                return dev;
            }

            @Override
            public void send(MidiMessage message, long timeStamp) {
                System.out.println("send - "+message);
            }

            @Override
            public void close() {
            }
        };
        tr.setReceiver(rec);
    }

    public static void main(String[] args) throws MidiUnavailableException{
        checkInfos();
        checkFileTypes();
    }
}
