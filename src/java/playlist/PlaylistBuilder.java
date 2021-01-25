package playlist;

import org.codehaus.jackson.map.ObjectWriter;
import songs.Song;
import songs.SongStore;
import user.User;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static common.Utils.OBJECT_MAPPER;
import static playlist.PlaylistGenerator.PLAYLIST_PATH;
import static playlist.PlaylistGenerator.THUMBNAIL_PATH;

public class PlaylistBuilder {
    private final SongStore songs;
    private final User user;

    private double minStars;
    private double maxStars;
    private Predicate<Song> filter = s -> true;
    private Function<Song, Double> sortBy = (song) -> song.stars;
    private int limit = 100;
    private int offset = 0;
    private Function<List<Song>, String> titleFunc;
    private String description = "";
    private String image = "avatar.thumb";

    public PlaylistBuilder(SongStore songs, User user) {
        this.songs = songs;
        this.user = user;
    }

    public PlaylistBuilder stars(double min, double max) {
        minStars = min;
        maxStars = max;
        return this;
    }

    public PlaylistBuilder filter(Predicate<Song> filter) {
        this.filter = filter;
        return this;
    }

    public PlaylistBuilder sort(Function<Song, Double> sort) {
        sortBy = sort;
        return this;
    }

    public PlaylistBuilder limit(int limit) {
        this.limit = limit;
        return this;
    }

    public PlaylistBuilder offset(int offset) {
        this.offset = offset;
        return this;
    }

    public PlaylistBuilder title(String title) {
        this.titleFunc = s -> title;
        return this;
    }

    public PlaylistBuilder title(Function<List<Song>, String> titleFunc) {
        this.titleFunc = titleFunc;
        return this;
    }

    public PlaylistBuilder description(String description) {
        this.description = description;
        return this;
    }

    public PlaylistBuilder image(String image) {
        this.image = image;
        return this;
    }

    public void save(String filename) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(THUMBNAIL_PATH + image)));
        Playlist playlist = new Playlist(description, br.readLine());

        ArrayList<Song> filteredSongs = new ArrayList<>();
        for (Song song : songs.rawSongs.songs) {
            if (starFilter(minStars, maxStars).and(filter).test(song)) {
                filteredSongs.add(song);
            }
        }

        List<Integer> ranks = new ArrayList<>();
        ArrayList<Song> songs = new ArrayList<>();

        filteredSongs.sort(Comparator.comparingDouble(sortBy::apply));
        filteredSongs.stream()
                .skip(offset)
                .limit(limit)
                .sorted(Comparator.comparing(song -> song.name.toLowerCase()))
                .forEach(song -> {
                    playlist.addSong(song);
                    songs.add(song);
                    int rank = user.getRank(song).orElse(song.scores());
                    ranks.add(rank);
                });
        filteredSongs.stream()
                .skip(offset)
                .limit(limit)
                .forEach(song -> {
                    StringBuilder out = new StringBuilder();
                    user.getRank(song).ifPresent(out::append);
                    out.append("(")
                            .append(-1.0 * sortBy.apply(song))
                            .append(") - ")
                            .append(song.name)
                            .append(" (")
                            .append(song.stars)
                            .append(")");
                    System.out.println(out);
                });

        playlist.playlistTitle = titleFunc.apply(songs);

        ObjectWriter writer = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
        writer.writeValue(new File(PLAYLIST_PATH + filename), playlist);

        System.out.println("Built " + playlist.playlistTitle);
        System.out.println();
    }

    private static Predicate<Song> starFilter(double minStars, double maxStars) {
        return song -> song.stars >= minStars && song.stars < maxStars;
    }
}
