package net.mircomacrelli.obsidian.utils;


import java.nio.file.Files;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Set;


public final class Checks {
    private Checks() {
    }

    public static void error(String message, Object... args) {
        //noinspection UseOfSystemOutOrSystemErr
        System.err.println(MessageFormat.format(message, args));
        //noinspection CallToSystemExit
        System.exit(1);
    }

    public static void isDirectory(Path path, String name) {
        if (!Files.isDirectory(path)) {
            error("{0} is a directory: {1}", name, path);
        }
    }

    public static void isReadable(Path path, String name) {
        if (!Files.isReadable(path)) {
            error("{0} is not readable: {1}", name, path);
        }
    }

    public static void isWritable(Path path, String name) {
        if (!Files.isWritable(path)) {
            error("{0} is not writable: {1}", name, path);
        }
    }

    public static void hasArguments(String[] args, int n, String message) {
        if (args.length != n) {
            error(message);
        }
    }

    public static <T> void notEmpty(Set<T> set, String message) {
        if (set.isEmpty()) {
            error(message);
        }
    }
}
