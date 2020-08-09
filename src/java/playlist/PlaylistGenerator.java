package playlist;

import data.DownloadSongs;
import data.DownloadUserRecentScores;
import org.codehaus.jackson.map.ObjectWriter;
import songs.LoadSongs;
import songs.Song;
import songs.SongStore;
import user.LoadUser;
import user.User;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static common.Utils.OBJECT_MAPPER;

public class PlaylistGenerator {
    private static final String PLAYLIST_PATH = "C://Program Files (x86)/Steam/steamapps/common/Beat Saber/Playlists/";
    private static final String THUMBNAIL_PATH = "thumbnails/";

    private SongStore songs;
    private User user;

    public PlaylistGenerator() throws IOException {
        songs = LoadSongs.loadSongs();
        user = LoadUser.loadUser();
    }

    public void run() throws IOException {
        buildPlaylist(stars(0, 1).and(rankOver(80)), worstRank(), 50, "Worst 0 Stars >80", "worst 0-1 star songs", "0.thumb");
        buildPlaylist(stars(1, 2).and(rankOver(80)), worstRank(), 50, "Worst 1 Stars >80", "worst 1-2 star songs", "1.thumb");
        buildPlaylist(stars(2, 3).and(rankOver(500)), worstRank(), 50, "Worst 2 Stars >500", "worst 2-3 star songs", "2.thumb");
        buildPlaylist(stars(3, 4).and(rankOver(500)), worstRank(), 50, "Worst 3 Stars >500", "worst 3-4 star songs", "3.thumb");
        buildPlaylist(stars(4, 5).and(rankOver(1000)), worstRank(), 50, "Worst 4 Stars >1000", "worst 4-5 star songs", "4.thumb");
        buildPlaylist(stars(5, 6).and(rankOver(1000)), worstRank(), 50, "Worst 5 Stars >1000", "worst 5-6 star songs", "5.thumb");
        buildPlaylist(stars(6, 7).and(played().negate()), lowestStars(), 500, "xUnbeaten 6 stars", "unbeaten maps with 6 star rating", "easy.thumb");
        buildPlaylist(stars(7, 8).and(played().negate()), lowestStars(), 500, "xUnbeaten 7 stars", "unbeaten maps with 7 star rating", "easy.thumb");
        buildPlaylist(stars(8, 9).and(played().negate()), lowestStars(), 500, "xUnbeaten 8 stars", "unbeaten maps with 8 star rating", "easy.thumb");
        buildPlaylist(stars(9, 10).and(played().negate()), lowestStars(), 500, "xUnbeaten 9 stars", "unbeaten maps with 9 star rating", "easy.thumb");
        buildPlaylist(stars(10, 11).and(played().negate()), lowestStars(), 500, "xUnbeaten x10 stars", "unbeaten maps with 10 star rating", "easy.thumb");
        buildPlaylist(stars(11, 100).and(played().negate()), lowestStars(), 500, "xUnbeaten x11 stars", "unbeaten maps with 11+ star rating", "easy.thumb");


        final int minPPIncrease = 10;
        final int expectedRank = 300;

        // initialize leaderboards
        for (Song song : songs.rawSongs.songs) {
            if (song.stars < 7)
                continue;
            songs.getPPForRank(song.uid, 300);
        }

        buildPlaylist(
                stars(7, 100).and(played().negate()).and(ppIncreaseAtRankOver(expectedRank, minPPIncrease)),
                mostPPAtRank(expectedRank), 100,
                "Most PP at 300 (unbeaten)",
                "Most PP at rank 300 (unbeaten)",
                "shinobu.thumb");
        buildPlaylist(
                stars(7, 100).and(played()).and(ppIncreaseAtRankOver(expectedRank, minPPIncrease)),
                ppIncreaseAtRank(expectedRank), 100,
                "PP increase at 300",
                "Songs that give most pp if improved to rank 300",
                "avatar.thumb");

        printStats();
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
                .sorted(Comparator.comparing(song -> song.name.toLowerCase()))
                .forEach(playlist::addSong);
        filteredSongs.stream()
                .limit(limit)
                .forEach(song -> {
                    StringBuilder out = new StringBuilder();
                    user.getRank(song).ifPresent(rank -> out.append(rank));
                    out.append("(" + -1.0 * sortBy.apply(song) + ") - ");
                    out.append(song.name + " (" + song.stars + ")");
                    System.out.println(out);
                });


        ObjectWriter writer = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
        String filename = playlist.playlistTitle.replaceAll("[^A-Za-z0-9 ]", "") + ".json";
        writer.writeValue(new File(PLAYLIST_PATH + filename), playlist);
        System.out.println();
    }

    private Predicate<Song> stars(int minStars, int maxStars) {
        return song -> song.stars >= minStars && song.stars < maxStars;
    }

    private Predicate<Song> played() {
        return song -> user.getRank(song).isPresent();
    }

    private Predicate<Song> rankOver(int threshold) {
        return song -> user.getRank(song).orElse(song.scores()) >= threshold;
    }

    private Predicate<Song> ppAtRankHasPPOver(int rank, int pp) {
        return song -> songs.getPPForRank(song.uid, rank).orElse(0.0) >= pp;
    }

    private Predicate<Song> ppIncreaseAtRankOver(int rank, int pp) {
        return song -> -1.0 * ppIncreaseAtRank(rank).apply(song) >= pp;
    }

    private Function<Song, Double> worstRank() {
        return song -> -1.0 * user.getRank(song).orElse(1000000);
    }

    private Function<Song, Double> mostPPAtRank(int rank) {
        return song -> -1.0 * songs.getPPForRank(song.uid, rank).orElse(-1000000.0);
    }

    private Function<Song, Double> lowestStars() {
        return song -> song.stars;
    }

    private Function<Song, Double> ppIncreaseAtRank(int rank) {
        return song -> {
            double ppAtRank = songs.getPPForRank(song.uid, rank).orElse(0.0);
            double currentPP = user.getTotalPP();
            double newPP = user.getTotalPP(song.uid, ppAtRank);
            return -1.0 * (newPP - currentPP);
        };
    }

    private void printStats() {
        List<Integer> ranks = new ArrayList<>();
        int numUnbeaten = 0;
        for (Song song : songs.rawSongs.songs) {
            OptionalInt rank = user.getRank(song);
            if (rank.isPresent()) {
                ranks.add(rank.getAsInt());
            } else {
                numUnbeaten++;
            }
        }

        System.out.println("Unbeaten: " + numUnbeaten);
        Collections.sort(ranks);
        System.out.println("Ranks");
        System.out.println("avg: " + ranks.stream().mapToInt(Integer::intValue).average().getAsDouble());
        System.out.println("10 percentile: " + ranks.get(ranks.size() / 10));
        System.out.println("25 percentile: " + ranks.get(ranks.size() / 4));
        System.out.println("50 percentile: " + ranks.get(ranks.size() / 2));
        System.out.println("75 percentile: " + ranks.get(3 * (ranks.size() / 4)));
        System.out.println("90 percentile: " + ranks.get(9 * (ranks.size() / 10)));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new DownloadSongs().run();
        new DownloadUserRecentScores().run();
        new PlaylistGenerator().run();
    }
}
