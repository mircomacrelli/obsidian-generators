package net.mircomacrelli.obsidian.utils;


import java.util.regex.Pattern;


public final class Obsidian {
    private static final Pattern BAD_CHARACTERS = Pattern.compile("[/\\\\<>:\"|?*\u0000-\u001f]+");
    private static final Pattern START_OF_LINE = Pattern.compile("^", Pattern.MULTILINE);
    private static final Pattern OPEN_BRACKET = Pattern.compile("\\[");

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

    public static String escapeSingleQuote(String s) {
        if (s.contains("'")&& !s.startsWith("\"")) {
            return '"' + s + '"';
        }
        return s;
    }

    public static String escapeColon(String s) {
        if (s.contains(":") && !s.startsWith("\"")) {
            return '"' + s + '"';
        }
        return s;
    }

    public static String quoteBlock(String lyrics) {
        return escapeOpenBracket(START_OF_LINE.matcher(lyrics).replaceAll("> "));
    }

    public static String escapeOpenBracket(String s) {
        return OPEN_BRACKET.matcher(s).replaceAll("\\\\[");
    }

    public static String escapeNumbers(String s) {
        for (var c : s.toCharArray()) {
            if (!Character.isDigit(c)) {
                return s;
            }
        }
        return '"' + s + '"';
    }
}
