package net.mircomacrelli.obsidian.musica;


import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;


final class Disk {
    private int number;
    private Set<Track> tracks = new TreeSet<>(Comparator.comparingInt(Track::getNumber));

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public Set<Track> getTracks() {
        return tracks;
    }

    public void addTrack(Track track) {
        tracks.add(track);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Disk disk)) {
            return false;
        }

        return number == disk.number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }
}
