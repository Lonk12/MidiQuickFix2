package com.lemckes.MidiQuickFix;

class NoteNames {
    
    static double[] limits =
    { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };
    static String[] sharpNames = {
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    };
    static String[] flatNames = {
        "C", "Db", "D", "Eb", "E", "F", "Gb", "G", "Ab", "A", "Bb", "B"
    };
    
    static java.text.ChoiceFormat cf =
    new java.text.ChoiceFormat(limits, sharpNames);
    
    static String getNoteName(int note, boolean flats) {
        
        if (flats) {
            cf.setChoices(limits, flatNames);
        } else {
            cf.setChoices(limits, sharpNames);
        }
        return cf.format(note % 12) + (int)(note / 12);
    }
    
    public static int getNoteNumber(String name) {
        int octave = Integer.parseInt(name.substring(name.length() - 1, name.length()));
        int noteNum = octave * 12;
        String note = name.substring(0, name.length() - 1);
        //StringBuffer sb = new StringBuffer(note);
        //sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        System.out.println("getNoteNumber name=" + name + " note=" + note + " oct=" + octave);
        boolean found = false;
        for (int i = 0; i < flatNames.length; ++i) {
            if (note.equalsIgnoreCase(flatNames[i])) {
                noteNum += i;
                found = true;
                break;
            }
        }
        if (!found) {
            for (int i = 0; i < sharpNames.length; ++i) {
                if (note.equalsIgnoreCase(sharpNames[i])) {
                    noteNum += i;
                    break;
                }
            }
        }
        
        return noteNum;
    }
}
