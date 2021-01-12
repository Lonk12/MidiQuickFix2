/*
 * The Artistic License
 *
 * Copyright (C) 2004-2021 John Lemcke
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
 */
package com.lemckes.MidiQuickFix.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Maintain a list of recently opened files
 */
public class RecentFiles
{

    public static final int MAX_ENTRIES = 8;

    public static final String NAME_SEPARATOR = "#";

    private final List<String> files;

    public RecentFiles() {
        files = new ArrayList<>(MAX_ENTRIES + 1);
    }

    /**
     * Get the list of files
     *
     * @return the list of files
     */
    public List<String> getFiles() {
        return files;
    }

    /**
     * Add a file to the list of used files.
     * If the file is already in the list then move it to the start.
     * If the list gets too long remove the oldest entry.
     *
     * @param fileName the file name to add
     */
    public void useFile(String fileName) {
        if (files.contains(fileName)) {
            files.remove(fileName);
        }

        files.add(0, fileName);

        if (files.size() > MAX_ENTRIES) {
            files.remove(MAX_ENTRIES);
        }
    }

    /**
     * Encode the list of names into a single String
     * @return a String containing the file names
     */
    public String toPropertyString() {
        return String.join(NAME_SEPARATOR, files);
    }

    /**
     * Parse the individual file names from the property string
     * @param propertyString
     */
    public void fromPropertyString(String propertyString) {
        String[] names = propertyString.split(NAME_SEPARATOR);
        files.clear();
        files.addAll(Arrays.asList(names));
        files.removeIf((String t) -> t.isEmpty());
    }
}
