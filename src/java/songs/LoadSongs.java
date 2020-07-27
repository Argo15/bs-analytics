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

    public static void main(String[] args) throws IOException, InterruptedException {
        SongStore songs = LoadSongs.loadSongs();
        double pp = songs.getPPForRank(280301, 30).orElse(0.0);
        System.out.println(pp);
    }
}
