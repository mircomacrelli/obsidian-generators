package net.mircomacrelli.obsidian.utils;


import java.util.regex.Pattern;


public final class Obsidian {
    private static final Pattern BAD_CHARACTERS = Pattern.compile("[/\\\\<>:\"|?*\u0000-\u001f]+");
    private static final Pattern START_OF_LINE = Pattern.compile("^", Pattern.MULTILINE);
    private static final Pattern OPEN_CRACKET = Pattern.compile("\\[");

    private Obsidian() {
    }

    public static String sanitizePath(String path) {
        var temp = BAD_CHARACTERS.matcher(path).replaceAll("_");
        if (temp.startsWith(".")) {
            temp = "_" + temp.substring(1);
        }
        if (temp.endsWith(".")) {
            temp = temp.substring(0, temp.length() - 2);
        }
        return temp;
    }

    public static String escapeString(String s) {
        if (s.contains(":")) {
            return '"' + s + '"';
        }
        return s;
    }

    public static String quoteBlock(String lyrics) {
        return OPEN_CRACKET.matcher(START_OF_LINE.matcher(lyrics).replaceAll("> ")).replaceAll("\\\\[");
    }
}
