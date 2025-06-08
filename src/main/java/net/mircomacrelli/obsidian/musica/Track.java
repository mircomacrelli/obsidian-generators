package net.mircomacrelli.obsidian.musica;


public class Track {
    private String title;
    private String artist;
    private int number;
    private String lyrics;

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

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Track track)) {
            return false;
        }

        return number == track.number;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(number);
    }
}
