/**
 * ************************************************************
 *
 * MidiQuickFix - A Simple Midi file editor and player
 *
 * Copyright (C) 2004-2010 John Lemcke
 * jostle@users.sourceforge.net
 *
 * This program is free software; you can redistribute it
 * and/or modify it under the terms of the Artistic License
 * as published by Larry Wall, either version 2.0,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the Artistic License for more details.
 *
 * You should have received a copy of the Artistic License with this Kit,
 * in the file named "Artistic.clarified".
 * If not, I'll be glad to provide one.
 *
 *************************************************************
 */
package com.lemckes.MidiQuickFix;

//import com.cti.util.chart.ContinuousAxis;
import com.lemckes.MidiQuickFix.components.histogram.QHist;
import com.lemckes.MidiQuickFix.components.histogram.QHistBackground;
import com.lemckes.MidiQuickFix.components.histogram.QHistChart;
import com.lemckes.MidiQuickFix.util.MqfSequence;
import com.lemckes.j2di.ICanvas;
import com.lemckes.j2di.ICanvasScrollPane;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

/**
 * Allow the user to quantise a sequence
 */
public class QuantiseDialog
        extends javax.swing.JDialog
{

    private MqfSequence mSequence;
    private ArrayList<String> mTrackNames;
    private ArrayList<ArrayList<Integer>> mNoteOnEventOffsets;
    private ArrayList<ArrayList<Integer>> mNoteOffEventOffsets;
    private ArrayList<ArrayList<Integer>> mLyricEventOffsets;
    private ArrayList<QHist> mNoteOnHistograms;
    private ArrayList<QHist> mNoteOffHistograms;
    private ArrayList<QHist> mLyricHistograms;
    private ICanvas mChart;
    private QHistBackground mBackground;
    private ArrayList<JCheckBox> mCheckBoxes = new ArrayList<JCheckBox>(8);

    /**
     * Create a new QuantiseDialog for the given Sequence
     *
     * @param seq the sequence in which to create the track
     * @param parent
     * @param modal
     */
    public QuantiseDialog(MqfSequence seq, Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();

        ICanvasScrollPane chartPane = new ICanvasScrollPane();
        mChart = chartPane.getCanvas();

        mSequence = seq;

        mNoteOnHistograms =
                new ArrayList<QHist>(mSequence.getTracks().length);
        mNoteOffHistograms =
                new ArrayList<QHist>(mSequence.getTracks().length);
        mLyricHistograms =
                new ArrayList<QHist>(mSequence.getTracks().length);

        analyseSequence(seq);

        createTrackCheckBoxes();

        mBackground = new QHistBackground(mNoteOnHistograms.get(0));
//        mChart.add(mBackground);

        mainPanel.add(chartPane);

        pack();
        setLocationRelativeTo(parent);
    }

//    private void createTrackRadioButtons() {
//        for (int i = 0; i < mSequence.getTracks().length; ++i) {
//            JRadioButton trackRadio = new JRadioButton();
//            trackRadioGroup.add(trackRadio);
//            trackRadio.setText(Integer.toString(i));
//            trackRadio.setName("trackRadio_" + i); // NOI18N
//            trackRadio.addActionListener(new java.awt.event.ActionListener() {
//                @Override
//                public void actionPerformed(java.awt.event.ActionEvent evt) {
//                    JRadioButton rb = (JRadioButton)evt.getSource();
//                    if (rb.isSelected()) {
//                        drawChart(Integer.parseInt(rb.getText()));
//                    }
//                }
//            });
//            trackRadioPanel.add(trackRadio);
//        }
//    }
    private void createTrackCheckBoxes() {
        for (int i = 0; i < mSequence.getTracks().length; ++i) {
            JCheckBox trackCheck = new JCheckBox();
            trackCheck.setText(Integer.toString(i));
            trackCheck.setName("trackCheck_" + i); // NOI18N
            trackCheck.addActionListener(new java.awt.event.ActionListener()
            {

                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    drawSelectedTrackCharts();
                }
            });
            trackCheck.setToolTipText(mTrackNames.get(i));
            mCheckBoxes.add(trackCheck);
            trackRadioPanel.add(trackCheck);
        }
    }

    private void drawSelectedTrackCharts() {

        // Work out the dimensions for the chart
        double maxVal = 0;
        int numValues = 0;
        for (JCheckBox cb : mCheckBoxes) {
            if (cb.isSelected()) {
                int trackNum = Integer.parseInt(cb.getText());

                // Find the maximum number of values in the data
                numValues = (int)Math.max(
                        numValues, mNoteOnHistograms.get(trackNum).getNumValues());

                boolean noteOnEnabled = !mNoteOnEventOffsets.isEmpty()
                        && noteOnToggle.isSelected();
                boolean noteOffEnabled = !mNoteOffEventOffsets.isEmpty()
                        && noteOffToggle.isSelected();
                boolean lyricEnabled = !mLyricEventOffsets.isEmpty()
                        && lyricToggle.isSelected();

                if (noteOnEnabled) {
                    maxVal = mNoteOnHistograms.get(trackNum).getMaxValue();
                }
                if (noteOffEnabled) {
                    maxVal = Math.max(maxVal,
                            mNoteOffHistograms.get(trackNum).getMaxValue());
                }
                if (lyricEnabled) {
                    maxVal = Math.max(maxVal,
                            mLyricHistograms.get(trackNum).getMaxValue());
                }
            }
        }
//        BucketAxis xaxis = (BucketAxis)mChart.getXAxis();
//        xaxis.setNumberOfBlocksVisible(numValues);
//        xaxis.setTotalNumberOfBlocks(numValues);
//        ContinuousAxis yaxis = (ContinuousAxis)mChart.getYAxis();
//        yaxis.setMaxValue(maxVal);
//        yaxis.setVisibleSize(maxVal);

        for (JCheckBox cb : mCheckBoxes) {
            if (cb.isSelected()) {
                drawChart(Integer.parseInt(cb.getText()));
            }
        }
    }

    private void drawChart(int trackNum) {
        System.out.println("Drawing chart for track " + trackNum);
//        mChart.clearChart();
//        BucketAxis xaxis = (BucketAxis)mChart.getXAxis();
//        xaxis.setNumberOfBlocksVisible(mNoteOnHistograms.get(trackNum).size());
//        xaxis.setTotalNumberOfBlocks(mNoteOnHistograms.get(trackNum).size());
//
        boolean noteOnEnabled = !mNoteOnEventOffsets.isEmpty()
                && noteOnToggle.isSelected();
        boolean noteOffEnabled = !mNoteOffEventOffsets.isEmpty()
                && noteOffToggle.isSelected();
        boolean lyricEnabled = !mLyricEventOffsets.isEmpty()
                && lyricToggle.isSelected();

//        double maxVal = 0;
//        if (noteOnEnabled) {
//            maxVal =mNoteOnHistograms.get(trackNum).getMaxValue();
//        }
//        if (noteOffEnabled) {
//            maxVal = Math.max(maxVal,mNoteOffHistograms.get(trackNum).getMaxValue());
//        }
//        if (lyricEnabled) {
//            maxVal = Math.max(maxVal,mLyricHistograms.get(trackNum).getMaxValue());
//        }
        int width = mChart.getWidth();
        int height = mChart.getHeight();
        if (noteOnEnabled) {
            mChart.add(new QHistChart(mNoteOnHistograms.get(trackNum), width, height));
        }
        if (noteOffEnabled) {
            mChart.add(new QHistChart(mNoteOffHistograms.get(trackNum), width, height));
        }
        if (lyricEnabled) {
            mChart.add(new QHistChart(mLyricHistograms.get(trackNum), width, height));
        }
    }

    private void quantiseSequence(MqfSequence mSequence, int quantiseLevel) {
        for (Track t : mSequence.getTracks()) {
            for (int i = 0; i < t.size(); ++i) {
                MidiEvent ev = t.get(i);
                long tick = ev.getTick();
                int offset = (int)(tick % quantiseLevel);

                int adjustment = offset;
                if (offset > quantiseLevel / 2) {
                    adjustment = offset - quantiseLevel;
                }
                ev.setTick(tick - adjustment);
            }
        }
    }

    private void analyseSequence(MqfSequence mSequence) {
        int ticksPerBeat = mSequence.getResolution();
        int quantiseLevel = Math.round(ticksPerBeat / 1f);
        System.out.println("ticksPerBeat  = " + ticksPerBeat);
        System.out.println("quantiseLevel = " + quantiseLevel);

        int numTracks = mSequence.getTracks().length;

        mTrackNames = new ArrayList<String>(numTracks);
        for (int i = 0; i < numTracks; ++i) {
            mTrackNames.add("Track " + i);
        }

        mNoteOnEventOffsets = new ArrayList<ArrayList<Integer>>(numTracks);
        mNoteOffEventOffsets = new ArrayList<ArrayList<Integer>>(numTracks);
        mLyricEventOffsets = new ArrayList<ArrayList<Integer>>(numTracks);
        int trackNum = 0;
        for (Track t : mSequence.getTracks()) {
            ArrayList<Integer> trackNoteOnEventOffsets =
                    new ArrayList<Integer>(t.size() / 2);
            mNoteOnEventOffsets.add(trackNoteOnEventOffsets);
            ArrayList<Integer> trackNoteOffEventOffsets =
                    new ArrayList<Integer>(t.size() / 2);
            mNoteOffEventOffsets.add(trackNoteOffEventOffsets);
            ArrayList<Integer> trackLyricEventOffsets =
                    new ArrayList<Integer>(t.size() / 2);
            mLyricEventOffsets.add(trackLyricEventOffsets);

            // Collect the offsets for all the NOTE_ON, NOTE_OFF and LYRIC events
            // for this track
            for (int i = 0; i < t.size(); ++i) {
                MidiEvent ev = t.get(i);
                long tick = ev.getTick();
                int offset = (int)(tick % quantiseLevel);

                if (ev.getMessage() instanceof ShortMessage) {
                    ShortMessage sm = (ShortMessage)ev.getMessage();
                    int n = sm.getCommand();
                    switch (n) {
                        case ShortMessage.NOTE_ON:
                            int velocity = Integer.valueOf(sm.getData2());
                            if (velocity != 0) {
                                trackNoteOnEventOffsets.add(offset);
                            } else {
                                trackNoteOffEventOffsets.add(offset);
                            }
                            break;
                        case ShortMessage.NOTE_OFF:
                            trackNoteOffEventOffsets.add(offset);
                            break;
                        default:
                        // Ignore
                    }
                } else if (ev.getMessage() instanceof MetaMessage) {
                    MetaMessage mm = (MetaMessage)ev.getMessage();
                    if (mm.getType() == MetaEvent.LYRIC) {
                        trackLyricEventOffsets.add(offset);
                    }
                    if (mm.getType() == MetaEvent.TRACK_NAME) {
                        mTrackNames.set(trackNum,
                                (String)MetaEvent.getMetaStrings((MetaMessage)mm)[2]);
                    }
                }
            }
            System.out.println("NOTE_ONs  = " + trackNoteOnEventOffsets.size());
            System.out.println("NOTE_OFFs = " + trackNoteOffEventOffsets.size());
            System.out.println("LYRICs    = " + trackLyricEventOffsets.size());

            // Count the number of events for each offset for this track
            QHist trackNoteOnHistograms = new QHist();
            QHist trackNoteOffHistograms = new QHist();
            QHist trackLyricHistograms = new QHist();
            if (mNoteOnEventOffsets.size() > 0) {
                mNoteOnHistograms.add(trackNoteOnHistograms);
                // Initialise the histogram to zeros
                for (int i = 0; i < quantiseLevel; ++i) {
                    trackNoteOnHistograms.put(i, 0);
                }
                // Count the NOTE_ON events for each offset
                for (int i : trackNoteOnEventOffsets) {
                    trackNoteOnHistograms.put(i, trackNoteOnHistograms.get(i) + 1);
                }
            }
            if (mNoteOnEventOffsets.size() > 0) {
                mNoteOffHistograms.add(trackNoteOffHistograms);
                // Initialise the histogram to zeros
                for (int i = 0; i < quantiseLevel; ++i) {
                    trackNoteOffHistograms.put(i, 0);
                }
                // Count the NOTE_OFF events for each offset
                for (int i : trackNoteOffEventOffsets) {
                    trackNoteOffHistograms.put(i, trackNoteOffHistograms.get(i) + 1);
                }

            }
            if (mNoteOnEventOffsets.size() > 0) {
                mLyricHistograms.add(trackLyricHistograms);
                // Initialise the histogram to zeros
                for (int i = 0; i < quantiseLevel; ++i) {
                    trackLyricHistograms.put(i, 0);
                }
                // Count the LYRIC events for each offset
                for (int i : trackLyricEventOffsets) {
                    trackLyricHistograms.put(i, trackLyricHistograms.get(i) + 1);
                }
            }

            System.out.println("Histograms ");
            System.out.println("Offset\tON\tOFF");
            for (int i = 0; i < quantiseLevel; ++i) {
                System.out.println(
                        i + "\t"
                        + trackNoteOnHistograms.get(i) + "\t"
                        + trackNoteOffHistograms.get(i) + "\t"
                        + trackLyricHistograms.get(i));
            }
            System.out.println("END track " + trackNum);
            trackNum++;
        }
    }

    /**
     * Parse an integer from a string, returning zero if the string
     * does not represent a valid integer.
     *
     * @param s the string to parse
     * @return the integer value of the string or zero if the string
     * does not represent a valid integer
     */
    private int safeParseInt(String s) {
        int i = 0;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            // DO NOTHING
        }
        return i;
    }

    private void doClose() {
        setVisible(false);
        dispose();
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        trackRadioGroup = new javax.swing.ButtonGroup();
        quantiseButtonGroup = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        trackRadioPanel = new javax.swing.JPanel();
        trackSelectorLabel = new javax.swing.JLabel();
        quantiseLevelPanel = new javax.swing.JPanel();
        quantiseLevelLabel = new javax.swing.JLabel();
        quarterNoteButton = new javax.swing.JToggleButton();
        eighthNoteButton = new javax.swing.JToggleButton();
        sixteenthNoteButton = new javax.swing.JToggleButton();
        thirtysecondNoteButton = new javax.swing.JToggleButton();
        quarterTripletButton = new javax.swing.JToggleButton();
        eighthTripletButton = new javax.swing.JToggleButton();
        sixteenthTripletButton = new javax.swing.JToggleButton();
        thirtysecondTripletButton = new javax.swing.JToggleButton();
        eventSelectPanel = new javax.swing.JPanel();
        eventSelectLabel = new javax.swing.JLabel();
        noteOnToggle = new javax.swing.JToggleButton();
        noteOffToggle = new javax.swing.JToggleButton();
        lyricToggle = new javax.swing.JToggleButton();
        jPanel2 = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("com/lemckes/MidiQuickFix/Bundle"); // NOI18N
        setTitle(bundle.getString("QuantiseDialog.title")); // NOI18N
        setName("Form"); // NOI18N

        mainPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(bundle.getString("QuantiseDialog.title"))); // NOI18N
        mainPanel.setName("mainPanel"); // NOI18N
        mainPanel.setLayout(new java.awt.BorderLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout(6, 0));

        trackRadioPanel.setName("trackRadioPanel"); // NOI18N
        trackRadioPanel.setLayout(new java.awt.GridLayout(0, 8));
        jPanel1.add(trackRadioPanel, java.awt.BorderLayout.CENTER);

        trackSelectorLabel.setText(bundle.getString("QuantiseDialog.trackSelectorLabel.text")); // NOI18N
        trackSelectorLabel.setName("trackSelectorLabel"); // NOI18N
        jPanel1.add(trackSelectorLabel, java.awt.BorderLayout.LINE_START);

        quantiseLevelPanel.setName("quantiseLevelPanel"); // NOI18N
        quantiseLevelPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING));

        quantiseLevelLabel.setText(bundle.getString("QuantiseDialog.quantiseLevelLabel.text")); // NOI18N
        quantiseLevelLabel.setName("quantiseLevelLabel"); // NOI18N
        quantiseLevelPanel.add(quantiseLevelLabel);

        quantiseButtonGroup.add(quarterNoteButton);
        quarterNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Qnote.png"))); // NOI18N
        quarterNoteButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        quarterNoteButton.setName("quarterNoteButton"); // NOI18N
        quarterNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quarterNoteButtonActionPerformed(evt);
            }
        });
        quantiseLevelPanel.add(quarterNoteButton);

        quantiseButtonGroup.add(eighthNoteButton);
        eighthNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Enote.png"))); // NOI18N
        eighthNoteButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        eighthNoteButton.setName("eighthNoteButton"); // NOI18N
        eighthNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eighthNoteButtonActionPerformed(evt);
            }
        });
        quantiseLevelPanel.add(eighthNoteButton);

        quantiseButtonGroup.add(sixteenthNoteButton);
        sixteenthNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Snote.png"))); // NOI18N
        sixteenthNoteButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        sixteenthNoteButton.setName("sixteenthNoteButton"); // NOI18N
        sixteenthNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sixteenthNoteButtonActionPerformed(evt);
            }
        });
        quantiseLevelPanel.add(sixteenthNoteButton);

        quantiseButtonGroup.add(thirtysecondNoteButton);
        thirtysecondNoteButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Tnote.png"))); // NOI18N
        thirtysecondNoteButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        thirtysecondNoteButton.setName("thirtysecondNoteButton"); // NOI18N
        thirtysecondNoteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thirtysecondNoteButtonActionPerformed(evt);
            }
        });
        quantiseLevelPanel.add(thirtysecondNoteButton);

        quantiseButtonGroup.add(quarterTripletButton);
        quarterTripletButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Qtrip.png"))); // NOI18N
        quarterTripletButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        quarterTripletButton.setName(bundle.getString("QuantiseDialog.quarterTripletButton.name")); // NOI18N
        quarterTripletButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quarterTripletButtonActionPerformed(evt);
            }
        });
        quantiseLevelPanel.add(quarterTripletButton);

        quantiseButtonGroup.add(eighthTripletButton);
        eighthTripletButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Etrip.png"))); // NOI18N
        eighthTripletButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        eighthTripletButton.setName(bundle.getString("QuantiseDialog.eighthTripletButton.name")); // NOI18N
        eighthTripletButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eighthTripletButtonActionPerformed(evt);
            }
        });
        quantiseLevelPanel.add(eighthTripletButton);

        quantiseButtonGroup.add(sixteenthTripletButton);
        sixteenthTripletButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Strip.png"))); // NOI18N
        sixteenthTripletButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        sixteenthTripletButton.setName(bundle.getString("QuantiseDialog.sixteenthTripletButton.name")); // NOI18N
        sixteenthTripletButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sixteenthTripletButtonActionPerformed(evt);
            }
        });
        quantiseLevelPanel.add(sixteenthTripletButton);

        quantiseButtonGroup.add(thirtysecondTripletButton);
        thirtysecondTripletButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/lemckes/MidiQuickFix/resources/Ttrip.png"))); // NOI18N
        thirtysecondTripletButton.setMargin(new java.awt.Insets(1, 1, 1, 1));
        thirtysecondTripletButton.setName(bundle.getString("QuantiseDialog.thirtysecondTripletButton.name")); // NOI18N
        thirtysecondTripletButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                thirtysecondTripletButtonActionPerformed(evt);
            }
        });
        quantiseLevelPanel.add(thirtysecondTripletButton);

        jPanel1.add(quantiseLevelPanel, java.awt.BorderLayout.PAGE_END);

        mainPanel.add(jPanel1, java.awt.BorderLayout.PAGE_START);

        eventSelectPanel.setName("eventSelectPanel"); // NOI18N
        eventSelectPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEADING));

        eventSelectLabel.setText(bundle.getString("QuantiseDialog.eventSelectLabel.text")); // NOI18N
        eventSelectLabel.setName("eventSelectLabel"); // NOI18N
        eventSelectPanel.add(eventSelectLabel);

        noteOnToggle.setSelected(true);
        noteOnToggle.setText(bundle.getString("QuantiseDialog.noteOnToggle.text")); // NOI18N
        noteOnToggle.setName("noteOnToggle"); // NOI18N
        noteOnToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noteOnToggleActionPerformed(evt);
            }
        });
        eventSelectPanel.add(noteOnToggle);

        noteOffToggle.setText(bundle.getString("QuantiseDialog.noteOffToggle.text")); // NOI18N
        noteOffToggle.setName("noteOffToggle"); // NOI18N
        noteOffToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noteOffToggleActionPerformed(evt);
            }
        });
        eventSelectPanel.add(noteOffToggle);

        lyricToggle.setText(bundle.getString("QuantiseDialog.lyricToggle.text")); // NOI18N
        lyricToggle.setName("lyricToggle"); // NOI18N
        lyricToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lyricToggleActionPerformed(evt);
            }
        });
        eventSelectPanel.add(lyricToggle);

        mainPanel.add(eventSelectPanel, java.awt.BorderLayout.PAGE_END);

        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.TRAILING));

        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.GridLayout(1, 0, 5, 0));

        okButton.setText(bundle.getString("QuantiseDialog.okButton.text")); // NOI18N
        okButton.setName("okButton"); // NOI18N
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(okButton);

        cancelButton.setText(bundle.getString("QuantiseDialog.cancelButton.text")); // NOI18N
        cancelButton.setName("cancelButton"); // NOI18N
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        buttonPanel.add(cancelButton);

        jPanel2.add(buttonPanel);

        getContentPane().add(jPanel2, java.awt.BorderLayout.PAGE_END);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        doClose();
    }//GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        doClose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void noteOnToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noteOnToggleActionPerformed
        drawSelectedTrackCharts();
    }//GEN-LAST:event_noteOnToggleActionPerformed

    private void noteOffToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noteOffToggleActionPerformed
        drawSelectedTrackCharts();
    }//GEN-LAST:event_noteOffToggleActionPerformed

    private void lyricToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lyricToggleActionPerformed
        drawSelectedTrackCharts();
    }//GEN-LAST:event_lyricToggleActionPerformed

    private void quarterNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quarterNoteButtonActionPerformed
        if (quarterNoteButton.isSelected()) {
            mBackground.setQuantiseLevel(1);
        }
    }//GEN-LAST:event_quarterNoteButtonActionPerformed

    private void eighthNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eighthNoteButtonActionPerformed
        if (eighthNoteButton.isSelected()) {
            mBackground.setQuantiseLevel(2);
        }
    }//GEN-LAST:event_eighthNoteButtonActionPerformed

    private void sixteenthNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sixteenthNoteButtonActionPerformed
        if (sixteenthNoteButton.isSelected()) {
            mBackground.setQuantiseLevel(4);
        }
    }//GEN-LAST:event_sixteenthNoteButtonActionPerformed

    private void thirtysecondNoteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thirtysecondNoteButtonActionPerformed
        if (thirtysecondNoteButton.isSelected()) {
            mBackground.setQuantiseLevel(8);
        }
    }//GEN-LAST:event_thirtysecondNoteButtonActionPerformed

    private void quarterTripletButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quarterTripletButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_quarterTripletButtonActionPerformed

    private void eighthTripletButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eighthTripletButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_eighthTripletButtonActionPerformed

    private void sixteenthTripletButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sixteenthTripletButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_sixteenthTripletButtonActionPerformed

    private void thirtysecondTripletButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_thirtysecondTripletButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_thirtysecondTripletButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton cancelButton;
    private javax.swing.JToggleButton eighthNoteButton;
    private javax.swing.JToggleButton eighthTripletButton;
    private javax.swing.JLabel eventSelectLabel;
    private javax.swing.JPanel eventSelectPanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JToggleButton lyricToggle;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JToggleButton noteOffToggle;
    private javax.swing.JToggleButton noteOnToggle;
    private javax.swing.JButton okButton;
    private javax.swing.ButtonGroup quantiseButtonGroup;
    private javax.swing.JLabel quantiseLevelLabel;
    private javax.swing.JPanel quantiseLevelPanel;
    private javax.swing.JToggleButton quarterNoteButton;
    private javax.swing.JToggleButton quarterTripletButton;
    private javax.swing.JToggleButton sixteenthNoteButton;
    private javax.swing.JToggleButton sixteenthTripletButton;
    private javax.swing.JToggleButton thirtysecondNoteButton;
    private javax.swing.JToggleButton thirtysecondTripletButton;
    private javax.swing.ButtonGroup trackRadioGroup;
    private javax.swing.JPanel trackRadioPanel;
    private javax.swing.JLabel trackSelectorLabel;
    // End of variables declaration//GEN-END:variables
}
