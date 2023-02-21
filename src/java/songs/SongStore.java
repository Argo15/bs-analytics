package songs;

import java.util.*;

public class SongStore {

    public final List<Song> rawSongs = new ArrayList<>();
    public final Map<Integer, Song> songIdLookup = new HashMap<>();
    public final Map<String, Song> songHashLookup = new HashMap<>();

    public SongStore() {}

    public SongStore(Songs songs) {
        for (Song song : songs.songs) {
            addSong(song);
        }
    }

    public void addSong(Song song) {
        rawSongs.add(song);
        songIdLookup.put(song.uid, song);
        songHashLookup.put(song.hash(), song);
    }
}
