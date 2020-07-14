package playlist;

import common.Utils;
import data.DownloadUserRecentScores;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;
import songs.LoadSongs;
import songs.Song;
import songs.SongStore;
import sun.misc.IOUtils;
import user.LoadUser;
import user.User;

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Predicate;

import static common.Utils.OBJECT_MAPPER;

public class PlaylistGenerator {
    private static final String PLAYLIST_PATH = "C://Program Files (x86)/Steam/steamapps/common/Beat Saber/Playlists/";
    private static final String THUMBNAIL_PATH = "thumbnails/";

    private final SongStore songs;
    private final User user;

    public PlaylistGenerator() throws IOException {
        songs = LoadSongs.loadSongs();
        user = LoadUser.loadUser();
    }

    public void run() throws IOException {
        buildPlaylist(stars(0, 1).and(played()), worstRank(), 30, "Worst 0 Stars", "worst 30 0-1 star songs", "0.thumb");
        buildPlaylist(stars(1, 2).and(played()), worstRank(), 30, "Worst 1 Stars", "worst 30 1-2 star songs", "1.thumb");
        buildPlaylist(stars(2, 3).and(played()), worstRank(), 30, "Worst 2 Stars", "worst 30 2-3 star songs", "2.thumb");
        buildPlaylist(stars(3, 4).and(played()), worstRank(), 30, "Worst 3 Stars", "worst 30 3-4 star songs", "3.thumb");
        buildPlaylist(stars(4, 5).and(played()), worstRank(), 30, "Worst 4 Stars", "worst 30 4-5 star songs", "4.thumb");
        buildPlaylist(stars(5, 6).and(played()), worstRank(), 30, "Worst 5 Stars", "worst 30 5-6 star songs", "5.thumb");
        buildPlaylist(stars(0, 6).and(played().negate()), lowestStars(), 300, "Unplayed Maps", "unplayed songs prioritized by star rating", "easy.thumb");
    }

    public void buildPlaylist(Predicate<Song> filter, Function<Song, Double> sortBy, int limit, String title, String desc, String image) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(THUMBNAIL_PATH + image)));
        Playlist playlist = new Playlist(title, desc, br.readLine());
        System.out.println("Building " + playlist.playlistTitle);

        ArrayList<Song> filteredSongs = new ArrayList<>();
        for (Song song : songs.rawSongs.songs) {
            if (filter.test(song)) {
                filteredSongs.add(song);
            }
        }

        filteredSongs.sort(Comparator.comparingDouble(sortBy::apply));
        filteredSongs.stream()
                .limit(limit)
                .forEach(song -> {
                    System.out.println(user.getRank(song).orElse(-1) + " - " + song.name + " (" + song.stars + ")");
                    playlist.addSong(song);
                });

        ObjectWriter writer = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
        writer.writeValue(new File(PLAYLIST_PATH + playlist.playlistTitle + ".json"), playlist);
        System.out.println();
    }

    private Predicate<Song> stars(int minStars, int maxStars) {
        return song -> song.stars >= minStars && song.stars < maxStars;
    }

    private Predicate<Song> played() {
        return song -> user.getRank(song).isPresent();
    }

    private Function<Song, Double> worstRank() {
        return song -> -1.0 * user.getRank(song).orElse(1000000);
    }

    private Function<Song, Double> lowestStars() {
        return song -> song.stars;
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new DownloadUserRecentScores().run();
        new PlaylistGenerator().run();
    }
}
