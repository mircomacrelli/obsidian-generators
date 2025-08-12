package net.mircomacrelli.obsidian.musica;


import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import static java.time.format.DateTimeFormatter.ISO_DATE;
import static net.mircomacrelli.obsidian.utils.Obsidian.*;


final class Template {
    private static final Pattern AMPERSAND = Pattern.compile(" & ");
    private static final Pattern COMMA = Pattern.compile(", ");

    private Template() {
    }

    private static String createAuthorLink(Set<? super String> seenAuthors, String albumArtist, String authors) {
        String[] first = AMPERSAND.split(authors);
        String[] second = COMMA.split(first[0]);

        for (int i = 1; i < second.length; i++) {
            if (!albumArtist.equals(second[i])) {
                if (!seenAuthors.contains(second[i])) {
                    seenAuthors.add(second[i]);
                    second[i] = "[[" + second[i] + "]]";
                }
            }
        }

        first[0] = String.join(", ", second);

        if (first.length == 2 && !albumArtist.equals(first[1])) {
            if (!seenAuthors.contains(first[1])) {
                seenAuthors.add(first[1]);
                first[1] = "[[" + first[1] + "]]";
            }
        }

        return String.join(" & ", first);
    }

    private static boolean hasLyrics(Album album) {
        for (var disk : album.getDisks()) {
            for (var track : disk.getTracks()) {
                if (!track.getLyrics().isBlank()) {
                    return true;
                }
            }
        }
        return false;
    }



    public static String generateBody(Album album) {
        StringBuilder body = new StringBuilder(65535);

        body.append("---").append('\n')
            .append("aliases:").append('\n')
            .append("  - ").append(escapeNumbers(escapeOpenBracket(escapeSingleQuote(album.getTitle())))).append('\n')
            .append("artist: \"[[").append(sanitizePath(album.getArtist())).append("]]\"").append('\n')
            .append("bought: false").append('\n')
            .append("cover: \"[[").append(sanitizePath(album.getArtist() + " - " + album.getTitle())).append(".avif").append("]]\"").append('\n')
            .append("date: ").append(album.getDate().format(ISO_DATE)).append('\n')
            .append("genre: ").append(album.getGenre()).append('\n')
            .append("listened: false").append('\n')
            .append("price:").append('\n')
            .append("rating:").append('\n')
            .append("title: ").append(escapeSingleQuote(escapeColon(album.getTitle()))).append('\n')
            .append("---").append('\n');

        body.append("# ").append(escapeOpenBracket(album.getTitle())).append('\n');

        body.append('\n').append("## Tracce").append('\n');

        var seenAuthors = new TreeSet<String>();
        for (var disk : album.getDisks()) {
            if (album.getDisks().size() > 1) {
                body.append('\n').append("### Disco ").append(disk.getNumber()).append('\n');
            }

            body.append('\n');
            int lastTrack = 0;
            for (var track : disk.getTracks()) {
                if (lastTrack > 0 && (lastTrack + 1) != track.getNumber()) {
                    for (int i = lastTrack + 1; i < track.getNumber(); i++) {
                        body.append(track.getNumber()).append(". ~").append('\n');
                    }
                }
                body.append(track.getNumber()).append(". ").append(escapeOpenBracket(track.getTitle()));
                if (!track.getArtist().equals(album.getArtist())) {
                    body.append(" _by_ ").append(createAuthorLink(seenAuthors, album.getArtist(), track.getArtist()));
                }
                body.append('\n');
                lastTrack = track.getNumber();
            }
        }

        if (hasLyrics(album)) {
            body.append('\n').append("## Testi").append('\n');
            for (var disk : album.getDisks()) {
                for (var track : disk.getTracks()) {
                    if (!track.getLyrics().isBlank()) {
                        body.append('\n').append("### ").append(escapeOpenBracket(track.getTitle())).append('\n').append('\n')
                            .append(quoteBlock(track.getLyrics())).append('\n');
                    }
                }
            }
        }

        body.append('\n');

        return body.toString();
    }
}
