package songs;

import common.Utils;
import data.Scores;
import user.User;

import java.io.File;
import java.io.IOException;

public class LoadSongs {

    public LoadSongs() { }

    public static SongStore loadSongs() throws IOException {
        File file = new File(Utils.SONGS_FILEPATH);

        Songs parsedSongs = Utils.OBJECT_MAPPER.readValue(file, Songs.class);
        System.out.println("Loaded " + parsedSongs.songs.length + " songs");

        return new SongStore(parsedSongs);
    }

    public void run() throws IOException {
        loadSongs();
    }

    public static void main(String[] args) throws IOException {
        LoadSongs loader = new LoadSongs();
        loader.run();
    }
}
