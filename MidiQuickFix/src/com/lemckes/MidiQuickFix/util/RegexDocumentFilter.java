/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2009 John Lemcke
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
package com.lemckes.MidiQuickFix.util;

import java.util.regex.Pattern;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 * A document filter that only allows edits that result in
 * a value that matches a regular expression.
 * @author john
 */
public class RegexDocumentFilter extends DocumentFilter {
    private final Pattern mPattern;

    /**
     * Creates a new instance of RegexDocumentFilter
     * with an 'accept all' pattern.
     */
    public RegexDocumentFilter() {
        super();
        mPattern = Pattern.compile(".*");
    }

    /**
     * Creates a new instance of RegexDocumentFilter
     * with the given pattern.
     * @param pattern the regex pattern to match against
     */
    public RegexDocumentFilter(String pattern) {
        super();
        mPattern = Pattern.compile(pattern);
    }

    @Override
    public void insertString(
        FilterBypass fb, int offset, String string, AttributeSet attr)
        throws BadLocationException {
        replace(fb, offset, 0, string, attr);
    }

    @Override
    public void remove(
        FilterBypass fb, int offset, int length)
        throws BadLocationException {
        replace(fb, offset, length, null, null);
    }

    @Override
    public void replace(
        FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
        throws BadLocationException {
        // Keep the previous value for later
        String prev = fb.getDocument().getText(0, fb.getDocument().getLength());
        // Construct the new string to test for validity
        String s = prev.substring(0, offset) +
            (text == null ? "" : text) +
            prev.substring(offset + length);
        if (mPattern.matcher(s).matches()) {
            // update the value in the bypass
            fb.replace(offset, length, text, attrs);
        }
    }
}
