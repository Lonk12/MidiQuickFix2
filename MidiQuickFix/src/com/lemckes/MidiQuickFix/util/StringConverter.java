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
        charsetName = "ISO-8859-1";
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

    public static boolean setCharsetName(String charsetName) {
        boolean ok = false;
        if ("LATIN".equalsIgnoreCase(charsetName)) {
            charsetName = "ISO-8859-1";
        } else if ("JP".equalsIgnoreCase(charsetName)) {
            charsetName = "Shift-JIS";
        }
        if (Charset.isSupported(charsetName)) {
            StringConverter.charsetName = charsetName;
            StringConverter.charset = Charset.forName(charsetName);
            ok = true;
        }
        return ok;
    }

    public static void resetDefaultCharset() {
        StringConverter.setCharsetName("ISO-8859-1");
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
