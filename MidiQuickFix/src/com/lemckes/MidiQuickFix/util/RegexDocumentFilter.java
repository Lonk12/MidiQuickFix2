/*
 * RegexDocumentFilter.java
 *
 * Created on August 17, 2007, 12:34 PM
 */

package com.lemckes.MidiQuickFix.util;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author john
 */
public class RegexDocumentFilter extends DocumentFilter {
    
    private final String mPattern;
    
    /**
     * Creates a new instance of RegexDocumentFilter
     * with an 'accept all' pattern.
     */
    public RegexDocumentFilter() {
        super();
        mPattern = ".*";
    }
    
    /**
     * Creates a new instance of RegexDocumentFilter
     * with the given pattern.
     */
    public RegexDocumentFilter(String pattern) {
        super();
        mPattern = pattern;
    }
    
    public void insertString(
        FilterBypass fb, int offset, String string, AttributeSet attr)
        throws BadLocationException {
        replace(fb, offset, 0, string, attr);
    }
    
    public void remove(
        FilterBypass fb, int offset, int length)
        throws BadLocationException {
        replace(fb, offset, length, null, null);
    }
    
    public void replace(
        FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
        throws BadLocationException {
        // Keep the previous value for later
        String prev = fb.getDocument().getText(0, fb.getDocument().getLength());
        // Construct the new string to test for validity
        String s = prev.substring(0, offset) + (text == null ? "" : text) + prev.substring(offset + length);
        if (s.matches(mPattern)) {
            // update the value in the bypass
            fb.replace(offset, length, text, attrs);
        }
    }
}
