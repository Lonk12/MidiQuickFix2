package com.lemckes.MidiQuickFix;

import javax.swing.table.*;

import javax.swing.event.TableModelListener;

import javax.sound.midi.*;

class TrackSummaryTableModel extends AbstractTableModel implements TableModelListener {
    
    Synthesizer mSynth;
    
    MidiChannel[] mChannels;
    
    Sequencer mSeq;
    
    /** The Sequence which is loaded. */
    Sequence mSequence;
    
    /** The resolution of the sequence */
    int mRes;
    
    /** The tracks in the sequence */
    Track[] mTracks;
    
    /** The data about a track */
    class TrackInfo {
        String mName;
        long mStart;
        long mEnd;
        int mChannel;
        boolean mSolo;
        boolean mMute;
    };
    
    /** The track info for each track */
    TrackInfo[] mInfo;
    
    /** Creates a new instance of a TrackSummaryTableModel */
    public TrackSummaryTableModel(Sequence s) {
        MidiDevice.Info[] mdi = MidiSystem.getMidiDeviceInfo();
        for (int i = 0; i < mdi.length; ++i) {
            System.out.println(i + " : " + mdi[i].toString());
        }
        
        try {
            mSynth = MidiSystem.getSynthesizer();
            MidiDevice md = MidiSystem.getMidiDevice(mdi[0]);
        } catch(MidiUnavailableException e) {
            System.out.println("No Synthesiser available. (Could make playing tricky.)");
        }
        
        System.out.println("Synth = " + mSynth.toString());
        mChannels = mSynth.getChannels();
        for (int i = 0; i < mChannels.length; ++i) {
            System.out.println("Ch " + i + " : "
            + mChannels[i]);
        }
        try {
            mSeq = MidiSystem.getSequencer();
        } catch(MidiUnavailableException e) {
            System.out.println("No Sequencer available. (Could make playing tricky.)");
        }
        
        mSequence = s;
        mRes = s.getResolution();
        mTracks = mSequence.getTracks();
        int numTracks = mTracks.length;
        
        mInfo = new TrackInfo[numTracks];
        
        for (int i = 0; i < numTracks; ++i) {
            mInfo[i] = new TrackInfo();
            mInfo[i].mSolo = false;
            mInfo[i].mMute = false;
            Track t = mTracks[i];
            mInfo[i].mEnd = t.ticks();
            mInfo[i].mStart = -1;
            mInfo[i].mChannel = -1;
            for (int e = 0; e < t.size(); ++e) {
                MidiEvent me = t.get(e);
                long tick = me.getTick();
                
                MidiMessage mm = me.getMessage();
                if (mm.getStatus() == MetaMessage.META) {
                    Object[] str = MetaEvent.getMetaStrings((MetaMessage)mm);
                    if (str[0].equals("M:TrackName")) {
                        mInfo[i].mName = (String)str[2];
                    }
                    
                }
                
                if (mm instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)mm;
                    int st = sm.getStatus();
                    
                    // Check that this is a channel message
                    if ((st & 0xf0) <= 0xf0) {
                        mInfo[i].mChannel = sm.getChannel();
                        
                        // The first NOTE_ON is the start of
                        // the track.
                        if (sm.getCommand() == ShortMessage.NOTE_ON) {
                            mInfo[i].mStart = tick;
                            break;
                        }
                    }
                }
                
                addTableModelListener(this);
            }
        }
    }
    
    public int getRowCount() {
        return mTracks.length;
    }
    
    public int getColumnCount() {
        return columnNames.length;
    }
    
    public Object getValueAt(int row, int column) {
        Object result = null;
        switch (column) {
            case 0:
                result = new Integer(row);
                break;
            case 1:
                result = mInfo[row].mName;
                break;
            case 2:
                if (mInfo[row].mStart == -1) {
                    // Didn't find a NOTE_ON event
                    result = null;
                } else {
                    result = getTickString(mInfo[row].mStart, mRes);
                }
                break;
            case 3:
                if (mInfo[row].mStart == -1) {
                    // Didn't find a NOTE_ON event
                    result = null;
                } else {
                    result = getTickString(mInfo[row].mEnd, mRes);
                }
                break;
            case 4:
                if (mInfo[row].mChannel == -1) {
                    // Didn't find a channel event
                    result = null;
                } else {
                    result = new Integer(mInfo[row].mChannel);
                }
                break;
            case 5:
                result = new Boolean(mInfo[row].mSolo);
                break;
            case 6:
                result = new Boolean(mInfo[row].mMute);
                break;
            default:
                result = "";
        }
        return result;
    }
    
    public void setValueAt(Object value, int row, int column) {
        // Only boolean toggles here so the test is not needed.
        // Don't bother if the value hasn't changed.
        // Object oldVal = getValueAt(row, column);
        // Need to compare String versions of the Objects
        // if (value.toString().equals(oldVal.toString()))
        // {
        //     return;
        // }
        
        Track t = mTracks[row];
        switch (column) {
            case 5:
                // Solo
                mInfo[row].mSolo = ((Boolean)value).booleanValue();
                mSeq.setTrackSolo(row, mInfo[row].mSolo);
                boolean soloed = mSeq.getTrackSolo(row);
                //                mChannels[mInfo[row].mChannel].setSolo(mInfo[row].mSolo);
                System.out.println("TrackSummaryTableModel.setValueAt Solo = " + value
                + " is actually " + soloed);
                break;
            case 6:
                // Mute
                mInfo[row].mMute = ((Boolean)value).booleanValue();
                mSeq.setTrackMute(row, mInfo[row].mMute);
                boolean muted = mSeq.getTrackMute(row);
                //                mChannels[mInfo[row].mChannel].setMute(mInfo[row].mMute);
                System.out.println("TrackSummaryTableModel.setValueAt Mute = " + value
                + " is actually " + muted);
                break;
            default:
                // Do Nothing
        }
    }
    
    
    String getTickString(long tick, int res) {
        java.text.DecimalFormat beatF = new java.text.DecimalFormat("00000");
        java.text.DecimalFormat tickF = new java.text.DecimalFormat("000");
        
        String result = "";
        long beats = tick / res;
        long ticks = tick % res;
        result = beatF.format(beats) + ":" + tickF.format(ticks);
        return result;
    }
    
    Class[] types = new Class [] {
        java.lang.Integer.class,
        java.lang.String.class,
        java.lang.Object.class,
        java.lang.Object.class,
        java.lang.Integer.class,
        java.lang.Boolean.class,
        java.lang.Boolean.class
    };
    
    public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
    }
    
    String[] columnNames = new String[] {
        java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("no."),
        java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("name"),
        java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("start"),
        java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("end"),
        java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("channel_abbrev"),
        java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("solo"),
        java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/resources/UIStrings").getString("mute")
    };
    public String getColumnName(int col) {
        return columnNames[col];
    }
    
    boolean[] canEdit = new boolean [] {
        false, false, false, false, false, true, true
    };
    
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        boolean ret = canEdit[columnIndex];
        if (columnIndex > 4 && mInfo[rowIndex].mChannel == -1) {
            ret = false;
        }
        return ret;
    }
    
    public void tableChanged(javax.swing.event.TableModelEvent e) {
        System.out.println("TableChanged(" + e.toString() + ")");
    }
    
}
