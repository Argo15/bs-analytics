package songs;

import common.Utils;
import data.DownloadSongs;

import java.io.File;
import java.io.IOException;

public class LoadSongs {

    public LoadSongs() { }

    public static SongStore loadSongs() throws IOException {
        File directory = new File(DownloadSongs.SONGS_DIRECTORY);
        SongStore store = new SongStore();
        for (File file : directory.listFiles()) {
            Songs parsedSongs = Utils.OBJECT_MAPPER.readValue(file, Songs.class);
            for (Song song : parsedSongs.songs)
            {
                store.addSong(song);
            }
        }

        System.out.println("Loaded " + store.rawSongs.size() + " songs");

        return store;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        SongStore songs = LoadSongs.loadSongs();
    }
}
