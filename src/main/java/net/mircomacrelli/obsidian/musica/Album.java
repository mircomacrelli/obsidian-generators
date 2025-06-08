package net.mircomacrelli.obsidian.musica;


import java.time.LocalDate;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;


final class Album {
    private String title;
    private String artist;
    private LocalDate date;
    private String genre;

    private Set<Disk> disks = new TreeSet<>(Comparator.comparingInt(Disk::getNumber));

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Disk getDisk(int diskNumber) {
        for (var disk : disks) {
            if (disk.getNumber() == diskNumber) {
                return disk;
            }
        }
        var disk = new Disk();
        disk.setNumber(diskNumber);
        disks.add(disk);
        return disk;
    }

    public Set<Disk> getDisks() {
        return disks;
    }
}
