/** ************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2018 John Lemcke
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
 ************************************************************* */
package com.lemckes.MidiQuickFix.util;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.DefaultFormatter;

/**
 * A regular expression based version of <code>DefaultFormatter</code>.
 * $Id: RegexFormatter.java,v 1.3 2009/03/16 07:44:20 jostle Exp $
 */
public class RegexFormatter
    extends DefaultFormatter
{

    static final long serialVersionUID = -7268001208342020887L;

    private Pattern mPattern;

    public RegexFormatter() {
        super();
    }

    /**
     * Creates a regular expression based
     * <code>AbstractFormatter</code>.<code>pattern</code> specifies the regular
     * expression that will
     * be used to determine if a value is legal.
     *
     * @param pattern
     */
    public RegexFormatter(String pattern) throws PatternSyntaxException {
        this();
        this.mPattern = Pattern.compile(pattern);
    }

    /**
     * Creates a regular expression based
     * <code>AbstractFormatter</code>.<code>pattern</code> specifies the regular
     * expression that will
     * be used to determine if a value is legal.
     *
     * @param pattern
     */
    public RegexFormatter(Pattern pattern) {
        this();
        this.mPattern = pattern;
    }

    /**
     * Parses <code>text</code> returning an arbitrary Object.
     * Some formatters may return null.
     * <p>
     * If a <code>Pattern</code> has been specified and the text
     * completely matches the regular expression this will invoke
     * <code>setMatcher</code>.
     *
     * @throws ParseException if there is an error in the conversion
     * @param text String to convert
     * @return Object representation of text
     */
    @Override
    public Object stringToValue(String text) throws ParseException {
        if (mPattern != null) {
            Matcher matcher = mPattern.matcher(text);

            if (matcher.matches()) {
                return super.stringToValue(text);
            }
            throw new ParseException("Pattern did not match", 0);
        }
        return text;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
