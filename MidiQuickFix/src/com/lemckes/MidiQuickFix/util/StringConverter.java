package com.lemckes.MidiQuickFix.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 *
 */
public class StringConverter
{

    private static String charsetName;
    private static Charset charset;

    static {
        charsetName = "UTF-8";
        charset = Charset.forName(charsetName);
    }

    public static Charset getCharset() {
        return charset;
    }

    public static void setCharset(Charset charset) {
        StringConverter.charset = charset;
    }

    public static String getCharsetName() {
        return charset.name();
    }

    public static void setCharsetName(String charsetName) {
        if ("LATIN".equalsIgnoreCase(charsetName)) {
            charsetName = "ISO-8859-1";
        } else if ("JP".equalsIgnoreCase(charsetName)) {
            charsetName = "UTF-8";
        }
        if (Charset.isSupported(charsetName)) {
            StringConverter.charsetName = charsetName;
            StringConverter.charset = Charset.forName(charsetName);
        }
    }

    static public String convertBytesToString(byte[] bytes) throws
        UnsupportedEncodingException {
        return new String(bytes, charset);
    }

    static public byte[] convertStringToBytes(String string) throws
        UnsupportedEncodingException {
        return string.getBytes(charset);
    }
}
