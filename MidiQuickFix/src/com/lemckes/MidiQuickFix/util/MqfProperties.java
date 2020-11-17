/** ************************************************************
 *
 *   MidiQuickFix - A Simple Midi file editor and player
 *
 *   Copyright (C) 2004-2019 John Lemcke
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

import java.awt.Color;
import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.Properties;

/**
 * A class to persist any user preference properties.
 */
public class MqfProperties
{

    private static final String mPropertiesFileName
        = System.getProperty("user.home") //NOI18N
        + System.getProperty("file.separator") //NOI18N
        + "MQF.properties"; //NOI18N
    private static final Properties mProps = new Properties();

    public static final String UI_FONT_SCALE = "ui_font_scale"; //NOI18N
    public static final String LAST_PATH_KEY = "lastpath"; //NOI18N
    public static final String LAST_SOUNDBANK_PATH_KEY = "last_soundbank_path"; //NOI18N
    public static final String LAST_SOUNDBANK_FILE_KEY = "last_soundbank_file"; //NOI18N
    public static final String LOOK_AND_FEEL_NAME = "laf_name"; //NOI18N
    public static final String LYRIC_FONT = "lyric_font"; //NOI18N
    public static final String LYRIC_RUBY_FONT_SCALE = "lyric_ruby_font_scale"; //NOI18N
    public static final String LYRIC_BACKGROUND_COLOUR = "lyric_bg"; //NOI18N
    public static final String LYRIC_FOREGROUND_COLOUR = "lyric_fg"; //NOI18N
    public static final String LYRIC_RUBY_FG_COLOUR = "lyric_ruby_fg"; //NOI18N
    public static final String LYRIC_HIGHLIGHT_COLOUR = "lyric_highlight"; //NOI18N
    public static final String RECENT_FILES = "recent_files"; //NOI18N
    public static final String SHOW_TRACE = "show_trace"; //NOI18N

    public static String getProperty(String key) {
        return mProps.getProperty(key);
    }

    public static void setProperty(String key, String value) {
        mProps.setProperty(key, value);
    }

    public static Color getColourProperty(String key, Color defaultColour) {
        Color colour = defaultColour;
        String prop = MqfProperties.getProperty(key);
        if (prop != null) {
            try {
                int c = Integer.parseInt(prop, 16);
                colour = new Color(c);
            } catch (NumberFormatException nfe) {
                setColourProperty(key, defaultColour);
            }
        } else {
            setColourProperty(key, defaultColour);
        }

        return colour;
    }

    public static void setColourProperty(String key, Color colour) {
        MqfProperties.setProperty(key, encodeColourValue(colour));
    }

    private static String encodeColourValue(Color colour) {
        StringBuilder sb = new StringBuilder(8);
        int r = colour.getRed();
        if (r < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(r));

        int g = colour.getGreen();
        if (g < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(g));

        int b = colour.getBlue();
        if (b < 16) {
            sb.append('0');
        }
        sb.append(Integer.toHexString(b));

        return sb.toString();
    }

    public static Font getFontProperty(String key, Font defaultFont) {
        Font font = defaultFont;
        String prop = MqfProperties.getProperty(key);
        if (prop != null) {
            try {
                font = Font.decode(prop);
            } catch (NumberFormatException nfe) {
                setFontProperty(key, defaultFont);
            }
        } else {
            setFontProperty(key, defaultFont);
        }
        return font;
    }

    public static void setFontProperty(String key, Font font) {
        MqfProperties.setProperty(key, encodeFontName(font));
    }

    private static String encodeFontName(Font font) {
        String name = font.getName();
        String style = ""; //NOI18N
        if (font.isBold()) {
            style = "BOLD"; //NOI18N
        }
        if (font.isItalic()) {
            style += "ITALIC"; //NOI18N
        }
        if (font.isPlain()) {
            style = "PLAIN"; //NOI18N
        }
        String size = Integer.toString(font.getSize());
        return name + "-" + style + "-" + size;
    }

    public static float getFloatProperty(String key, float defaultVal) {
        float val = defaultVal;
        String prop = MqfProperties.getProperty(key);
        if (prop != null) {
            try {
                val = Float.parseFloat(prop);
            } catch (NumberFormatException nfe) {
                setFloatProperty(key, defaultVal);
            }
        } else {
            setFloatProperty(key, defaultVal);
        }
        return val;
    }

    public static void setFloatProperty(String key, float value) {
        MqfProperties.setProperty(key, encodeFloatValue(value));
    }

    private static String encodeFloatValue(float value) {
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setGroupingUsed(false);
        nf.setMaximumFractionDigits(3);
        nf.setMinimumFractionDigits(1);
        nf.setMinimumIntegerDigits(1);
        return nf.format(value);
    }

    public static int getIntegerProperty(String key, int defaultVal) {
        int val = defaultVal;
        String prop = MqfProperties.getProperty(key);
        if (prop != null) {
            try {
                val = Integer.parseInt(prop);
            } catch (NumberFormatException nfe) {
                setIntegerProperty(key, defaultVal);
            }
        } else {
            setIntegerProperty(key, defaultVal);
        }
        return val;
    }

    public static void setIntegerProperty(String key, int value) {
        MqfProperties.setProperty(key, encodeIntegerValue(value));
    }

    private static String encodeIntegerValue(int value) {
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(false);
        nf.setMinimumIntegerDigits(1);
        return nf.format(value);
    }

    public static boolean getBooleanProperty(String key, boolean defaultVal) {
        boolean val = defaultVal;
        String prop = MqfProperties.getProperty(key);
        if (prop != null) {
            try {
                val = Boolean.parseBoolean(prop);
            } catch (NumberFormatException nfe) {
                setBooleanProperty(key, defaultVal);
            }
        } else {
            setBooleanProperty(key, defaultVal);
        }
        return val;
    }

    public static void setBooleanProperty(String key, boolean value) {
        MqfProperties.setProperty(key, encodeBooleanValue(value));
    }

    private static String encodeBooleanValue(boolean value) {
        return Boolean.toString(value);
    }

    public static String getStringProperty(String key, String defaultVal) {
        String val = defaultVal;
        String prop = MqfProperties.getProperty(key);
        if (prop != null) {
            val = prop;
        } else {
            setStringProperty(key, defaultVal);
        }
        return val;
    }

    public static void setStringProperty(String key, String value) {
        MqfProperties.setProperty(key, value);
    }

    public static String getPropertiesFileName() {
        return mPropertiesFileName;
    }

    public static void readProperties() {
        try {
            mProps.load(new FileReader(mPropertiesFileName));
        } catch (FileNotFoundException ex) {
            // Shame really
        } catch (IOException ex) {
            // oops
        }
    }

    public static void writeProperties() {
        try {
            mProps.store(new FileWriter(mPropertiesFileName),
                "MidiQuickFix properties"); //NOI18N
        } catch (FileNotFoundException ex) {
            // Shame really
        } catch (IOException ex) {
            // oops
        }
    }
}
