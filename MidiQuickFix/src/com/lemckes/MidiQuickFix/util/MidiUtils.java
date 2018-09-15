/*
 */
package com.lemckes.MidiQuickFix.util;

import com.lemckes.MidiQuickFix.MidiQuickFix;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.midi.Instrument;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Patch;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;

/**
 *
 */
public class MidiUtils
{

    Sequence mSeq;
    MidiSystem mSystem;

    public void checkInfos() throws MidiUnavailableException {
        Info[] infos = MidiSystem.getMidiDeviceInfo();
        int i = 0;
        for (Info info : infos) {
            System.out.println("Info " + i++ + " = " + info);
            MidiDevice dev = MidiSystem.getMidiDevice(info);
            for (Receiver rec : dev.getReceivers()) {
                System.out.println("Receiver " + rec.toString());
            }
            for (Transmitter tr : dev.getTransmitters()) {
                System.out.println("Transmitter " + tr.toString());
            }
        }
    }

    public void checkFileTypes() {
        int[] types = MidiSystem.getMidiFileTypes();
        for (int i : types) {
            System.out.println("FileType " + i);
        }
    }

    public void checkReceiver() throws MidiUnavailableException {
        Receiver rec = MidiSystem.getReceiver();
        System.out.println("Receiver : " + rec);
        rec.close();
    }

    public void checkTransmitter() throws MidiUnavailableException {
//        Transmitter tr = MidiSystem.getTransmitter();
//        Receiver rec = new MidiDeviceReceiver() {
//
//            @Override
//            public MidiDevice getMidiDevice() {
//                MidiDevice dev = null;
//                try {
//                     dev= MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[0]);
//                } catch (MidiUnavailableException ex) {
//                    ex.printStackTrace();
//                }
//                return dev;
//            }
//
//            @Override
//            public void send(MidiMessage message, long timeStamp) {
//                System.out.println("send - "+message);
//            }
//
//            @Override
//            public void close() {
//            }
//        };
//        tr.setReceiver(rec);
    }

    public void checkSynthesizer() throws MidiUnavailableException {
        Synthesizer synth = MidiSystem.getSynthesizer();
        System.out.println("Synthesizer : " + synth);
//        Synthesizer synth2 = MidiSystem.getSynthesizer();
//        System.out.println("Synthesizer 2 : " + synth2);
//        Synthesizer synth = MidiQuickFix.getSynth();
        synth.open();
        synth.unloadAllInstruments(synth.getDefaultSoundbank());

//        MidiQuickFix mqf = new MidiQuickFix(null);
//        URL url;
//        url = mqf.getClass().getResource("resources/Soundbanks/Compifont_13082016.sf2"); // NOI18N
//        url = mqf.getClass().getResource("resources/Soundbanks/OmegaGMGS2.sf2"); // NOI18N
//        File file = new File(url.getFile());
        File file;
        file = new File("/home/john/src/playpen/MidiQuickFix/src/com/lemckes/MidiQuickFix/resources/Soundbanks/OmegaGMGS2.sf2");
        try {
            Soundbank soundbank;
            soundbank = MidiSystem.getSoundbank(file);
            System.out.println();
            System.out.println("Soundbank : " + soundbank.getDescription());
            System.out.println("Soundbank : " + soundbank.getName());
            System.out.println("Soundbank : " + soundbank.getVendor());
            System.out.println("Soundbank : " + soundbank.getVersion());

            if (synth.isSoundbankSupported(soundbank)) {
                System.out.println("Soundbank is Supported");
            }

//            synth.loadAllInstruments(soundbank);
            Patch[] patches = new Patch[129];
            for (int prog = 0; prog < 128; prog++) {
                patches[prog] = new Patch(0, prog);
            }
            patches[128] = new Patch(0, 0);
            synth.loadInstruments(soundbank, patches);
            
//            for (Instrument in : soundbank.getInstruments()) {
//
//                synth.loadInstrument(in);
//            }

            Instrument[] instruments2 = synth.getLoadedInstruments();
            int count2 = 0;
            for (Instrument i : instruments2) {
                int bank = i.getPatch().getBank();
                int prog = i.getPatch().getProgram();
                System.out.println("Inst " + bank + "/" + prog + " - " + i.getName() + " - " + i.getSoundbank().getName());
            }
        } catch (InvalidMidiDataException | IOException ex) {
            Logger.getLogger(MidiUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) throws MidiUnavailableException {
        MidiUtils mu = new MidiUtils();

        mu.checkInfos();
        mu.checkFileTypes();
        mu.checkReceiver();
        mu.checkTransmitter();
        mu.checkSynthesizer();
    }
}
