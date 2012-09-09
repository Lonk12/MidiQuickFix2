package com.lemckes.MidiQuickFix.components.histogram;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 */
public class QHist
{
    private double mMaxValue = 400;
    private double mNumValues = 500;
    private Map<Integer, Integer> mHistogramData =
            new TreeMap<Integer, Integer>();

    public QHist() {
    }

    public Map<Integer, Integer> getHistogramData() {
        return mHistogramData;
    }

    public void setHistogramData(Map<Integer, Integer> histogramData) {
        mHistogramData = histogramData;
        mMaxValue = Collections.max(mHistogramData.values());
        mNumValues = mHistogramData.size();
    }

    public void put(Integer key, Integer value)
    {
        mHistogramData.put(key, value);
    }

    public Integer get(Integer key)
    {
        return mHistogramData.get(key);
    }

    public double getMaxValue() {
        return mMaxValue;
    }

    public double getNumValues() {
        return mNumValues;
    }
}
