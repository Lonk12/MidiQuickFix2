/**************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2005 John Lemcke
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

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.swing.text.DefaultFormatter;

/**
 * A regular expression based version of <code>DefaultFormatter</code>.
 * $Id$
 */
public class RegexFormatter extends DefaultFormatter
{
    private Pattern pattern;
    private Matcher matcher;
    
    public RegexFormatter()
    {
        super();
    }
    
    /**
     * Creates a regular expression based <code>AbstractFormatter</code>.
     * <code>pattern</code> specifies the regular expression that will
     * be used to determine if a value is legal.
     */
    public RegexFormatter(String pattern) throws PatternSyntaxException
    {
        this();
        setPattern(Pattern.compile(pattern));
    }
    
    /**
     * Creates a regular expression based <code>AbstractFormatter</code>.
     * <code>pattern</code> specifies the regular expression that will
     * be used to determine if a value is legal.
     */
    public RegexFormatter(Pattern pattern)
    {
        this();
        setPattern(pattern);
    }
    
    /**
     * Sets the pattern that will be used to determine if a value is
     * legal.
     */
    public void setPattern(Pattern pattern)
    {
        this.pattern = pattern;
    }
    
    /**
     * Returns the <code>Pattern</code> used to determine if a value is
     * legal.
     */
    public Pattern getPattern()
    {
        return pattern;
    }
    
    /**
     * Sets the <code>Matcher</code> used in the most recent test
     * if a value is legal.
     */
    protected void setMatcher(Matcher matcher)
    {
        this.matcher = matcher;
    }
    
    /**
     * Returns the <code>Matcher</code> from the most test.
     */
    protected Matcher getMatcher()
    {
        return matcher;
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
    public Object stringToValue(String text) throws ParseException
    {
        Pattern pattern = getPattern();
        
        if (pattern != null)
        {
            Matcher matcher = pattern.matcher(text);
            
            if (matcher.matches())
            {
                setMatcher(matcher);
                return super.stringToValue(text);
            }
            throw new ParseException("Pattern did not match", 0);
        }
        return text;
    }
}
