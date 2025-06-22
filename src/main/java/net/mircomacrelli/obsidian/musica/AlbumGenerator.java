package net.mircomacrelli.obsidian.musica;


import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
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

    private static Set<Path> searchFiles(Path directory) {
        try (var stream = Files.list(directory)) {
            return stream.filter(Files::isRegularFile)
                         .filter(AlbumGenerator::isM4aFile)
                         .collect(toUnmodifiableSet());
        } catch (IOException e) {
            error("could not list files in directory ''{0}'': {1}", directory, e.getMessage());
        }
        return emptySet();
    }

    private static Mp4Tag readTag(Path path) {
        isReadable(path, "track");
        AudioFile audioFile = null;
        try {
            audioFile = AudioFileIO.read(path.toFile());
        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException e) {
            error("could not read the tag from the file ''{0}'': {1}", path, e.getMessage());
        }
        //noinspection DataFlowIssue
        return (Mp4Tag) audioFile.getTag();
    }

    private static Album initializeAlbum(Path path) {
        Mp4Tag tag = readTag(path);

        var album = new Album();
        album.setTitle(tag.getFirst(Mp4FieldKey.ALBUM));
        album.setArtist(tag.getFirst(Mp4FieldKey.ALBUM_ARTIST));
        album.setGenre(tag.getFirst(FieldKey.GENRE));
        try {
            album.setDate(LocalDate.parse(tag.getFirst(Mp4FieldKey.DAY)));
        } catch (DateTimeParseException e) {
            error("could not parse the date ''{0}'': {1}", tag.getFirst(Mp4FieldKey.DAY), e.getMessage());
        }
        return album;
    }

    private static Track createTrack(Mp4Tag tag) {
        var track = new Track();
        track.setArtist(tag.getFirst(Mp4FieldKey.ARTIST));
        track.setTitle(tag.getFirst(Mp4FieldKey.TITLE));
        track.setNumber(((Mp4TrackField)tag.getFirstField(Mp4FieldKey.TRACK)).getTrackNo());
        track.setLyrics(tag.getFirst(Mp4FieldKey.LYRICS));

        return track;
    }

    private static Disk getDisk(Album album, Mp4Tag tag) {
        int diskNumber = ((Mp4DiscNoField)tag.getFirstField(Mp4FieldKey.DISCNUMBER)).getDiscNo();
        return album.getDisk(diskNumber);
    }

    public static void main(String[] args) {
        hasArguments(args, 2, "AlbumGenerator <input directory> <output directory>");

        var inputDirectory = Paths.get(args[0]);
        isDirectory(inputDirectory, "<input directory>");
        isReadable(inputDirectory, "<input directory>");

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
            var disk = getDisk(album, tag);
            disk.addTrack(createTrack(tag));
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
