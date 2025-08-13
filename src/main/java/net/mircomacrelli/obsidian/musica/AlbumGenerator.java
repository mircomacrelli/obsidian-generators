package net.mircomacrelli.obsidian.musica;


import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.mp4.Mp4FieldKey;
import org.jaudiotagger.tag.mp4.Mp4Tag;
import org.jaudiotagger.tag.mp4.field.Mp4DiscNoField;
import org.jaudiotagger.tag.mp4.field.Mp4TrackField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Set;

import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static net.mircomacrelli.obsidian.musica.Template.generateBody;
import static net.mircomacrelli.obsidian.utils.Checks.*;
import static net.mircomacrelli.obsidian.utils.Obsidian.sanitizePath;


public final class AlbumGenerator {
    private AlbumGenerator() {
    }

    private static boolean isM4aFile(Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".m4a");
    }

    private static boolean isMp3File(Path path) {
        return path.getFileName().toString().toLowerCase().endsWith(".mp3");
    }

    private static boolean isM4aOrMp3File(Path path) {
        return isMp3File(path) || isM4aFile(path);
    }

    private static Set<Path> searchFiles(Path directory) {
        try (var stream = Files.list(directory)) {
            return stream.filter(Files::isRegularFile)
                         .filter(AlbumGenerator::isM4aOrMp3File)
                         .collect(toUnmodifiableSet());
        } catch (IOException e) {
            error("could not list files in directory ''{0}'': {1}", directory, e.getMessage());
        }
        return emptySet();
    }

    private static Tag readTag(Path path) {
        isReadable(path, "track");
        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(path.toFile());
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            error("could not read the tag from the file ''{0}'': {1}", path, e.getMessage());
        }
        //noinspection DataFlowIssue
        return audioFile.getTag();
    }

    private static String getFirstValue(Path path, Tag tag, FieldKey fieldKey, Mp4FieldKey mp4FieldKey) {
        if (isMp3File(path)) {
            return tag.getFirst(fieldKey);
        }
        return ((Mp4Tag)tag).getFirst(mp4FieldKey);
    }

    private static String getAlbum(Path path, Tag tag) {
        return getFirstValue(path, tag, FieldKey.ALBUM, Mp4FieldKey.ALBUM);
    }

    private static String getAlbumArtist(Path path, Tag tag) {
        return getFirstValue(path, tag, FieldKey.ALBUM_ARTIST, Mp4FieldKey.ALBUM_ARTIST);
    }

    private static String getGenre(Path path, Tag tag) {
        return getFirstValue(path, tag, FieldKey.GENRE, Mp4FieldKey.GENRE);
    }

    private static String getDate(Path path, Tag tag) {
        return getFirstValue(path, tag, FieldKey.YEAR, Mp4FieldKey.DAY);
    }

    private static String getArtist(Path path, Tag tag) {
        return getFirstValue(path, tag, FieldKey.ARTIST, Mp4FieldKey.ARTIST);
    }

    private static String getTitle(Path path, Tag tag) {
        return getFirstValue(path, tag, FieldKey.TITLE, Mp4FieldKey.TITLE);
    }

    private static String getLyrics(Path path, Tag tag) {
        return getFirstValue(path, tag, FieldKey.LYRICS, Mp4FieldKey.LYRICS);
    }

    private static Short getTrackNo(Path path, Tag tag) {
        if (isMp3File(path)) {
            return Short.valueOf(tag.getFirst(FieldKey.TRACK));
        }
        return ((Mp4TrackField)((Mp4Tag)tag).getFirstField(Mp4FieldKey.TRACK)).getTrackNo();
    }

    private static Album initializeAlbum(Path path) {
        Tag tag = readTag(path);

        var album = new Album();
        album.setTitle(getAlbum(path, tag));
        album.setArtist(getAlbumArtist(path, tag));
        album.setGenre(getGenre(path, tag));
        try {
            album.setDate(LocalDate.parse(getDate(path, tag)));
        } catch (DateTimeParseException e) {
            error("could not parse the date ''{0}'': {1}", getDate(path, tag), e.getMessage());
        }
        return album;
    }

    private static Track createTrack(Path path, Tag tag) {
        var track = new Track();
        track.setArtist(getArtist(path, tag));
        track.setTitle(getTitle(path, tag));
        track.setNumber(getTrackNo(path, tag));
        track.setLyrics(getLyrics(path, tag));

        return track;
    }

    private static Disk getDisk(Path path, Album album, Tag tag) {
        int diskNumber;
        if (isMp3File(path)) {
            diskNumber = Integer.parseInt(tag.getFirst(FieldKey.DISC_NO));
        } else {
            diskNumber = ((Mp4DiscNoField)((Mp4Tag)tag).getFirstField(Mp4FieldKey.DISCNUMBER)).getDiscNo();
        }
        return album.getDisk(diskNumber);
    }

    public static void main(String[] args) {
        hasArguments(args, 2, "AlbumGenerator <input directory> <output directory>");

        var inputDirectory = Paths.get(args[0]);
        isDirectory(inputDirectory, "<input directory>");
        isReadable(inputDirectory, "<input directory>");

        //noinspection UseOfSystemOutOrSystemErr
        System.out.println("Input directory: " + inputDirectory);

        var outputDirectory = Paths.get(args[1]);
        isDirectory(outputDirectory, "<output directory>");
        isWritable(outputDirectory, "<output directory>");

        var files = searchFiles(inputDirectory);
        notEmpty(files, "not m4a files found");

        var album = initializeAlbum(files.iterator().next());
        for (var file : files) {
            isReadable(file, "current track");

            var tag = readTag(file);
            var disk = getDisk(file, album, tag);
            disk.addTrack(createTrack(file, tag));
        }

        var fileName = sanitizePath(album.getArtist() + " - " + album.getTitle()) + ".md";
        var mdFile = outputDirectory.resolve(fileName);
        try (var os = newBufferedWriter(mdFile, CREATE_NEW)) {
            os.append(generateBody(album));
        } catch (IOException e) {
            error("could not write the file ''{0}'': {1}", mdFile, e.getMessage());
        }
    }
}
