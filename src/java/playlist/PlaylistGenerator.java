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

    private static Set<String> TROLL_SONGS = new HashSet<>();
    static {
        TROLL_SONGS.add("567859C06D0D010987875E2579E08899331F73CC");
        TROLL_SONGS.add("38B615ABA9F6BC3F9DEF38097C5620B9391160A1");
        TROLL_SONGS.add("6D24C4477B498830E78E2F9B4DBD9AB97642B3E7");
        TROLL_SONGS.add("4963752D07B806F9B0028D0015DC7E65BB74A1C8");
        TROLL_SONGS.add("8B53010B2F6656C5BE03C110724703763CC3CB03");
        TROLL_SONGS.add("FCB234ACF54027FBF4678DDCD4764E3AC975A6F2");
        TROLL_SONGS.add("A098FD424C66FEDF0ADE7E7B1926F1C834F36A52");
        TROLL_SONGS.add("3420D4808B47EB46344710F866557A55A48AE821");
        TROLL_SONGS.add("5F73283FCC93BA018A93C3714811E26E4F751CBB");
        TROLL_SONGS.add("839245D64719D018E40E2F11654B7C49230CA126");
        TROLL_SONGS.add("4BE275B24D15D93994DB0B86248D4015CD8B15ED");
        TROLL_SONGS.add("F70DEF97451CF9A12889197AA27C17D40B292297");
        TROLL_SONGS.add("1B0E538ED914F54F00374AF0A876DEA67D366114");
        TROLL_SONGS.add("5B546BB6D82A2667B4D46E2AB8838E68B9A63793");
        TROLL_SONGS.add("3F1DACFB11BAE00B17C9F576F07EB77E3148A029");
        TROLL_SONGS.add("06DDBE99BC8AF1EC819CE1A6D5F4760C9EDE6697");
        TROLL_SONGS.add("ED2650077F3DB44A65F890709EE5B4982CDD1494");
    }

    private SongStore songs;
    private User user;

    public PlaylistGenerator() throws IOException {
        songs = LoadSongs.loadSongs();
        user = LoadUser.loadUser();
    }

    public void run() throws IOException {
        buildPlaylist(stars(0, 1).and(isTroll().negate()), worstRank(), 20, "Worst 0 Stars", "worst 0-1 star songs", "0.thumb", "worst_0_stars.json", true);
        buildPlaylist(stars(1, 2).and(isTroll().negate()), worstRank(), 20, "Worst 1 Stars", "worst 1-2 star songs", "1.thumb", "worst_1_stars.json", true);
        buildPlaylist(stars(2, 3).and(isTroll().negate()), worstRank(), 20, "Worst 2 Stars", "worst 2-3 star songs", "2.thumb", "worst_2_stars.json", true);
        buildPlaylist(stars(3, 4).and(isTroll().negate()), worstRank(), 20, "Worst 3 Stars", "worst 3-4 star songs", "3.thumb", "worst_3_stars.json", true);
        buildPlaylist(stars(4, 5).and(isTroll().negate()), worstRank(), 20, "Worst 4 Stars", "worst 4-5 star songs", "4.thumb", "worst_4_stars.json", true);
        buildPlaylist(stars(5, 6).and(isTroll().negate()), worstRank(), 20, "Worst 5 Stars", "worst 5-6 star songs", "5.thumb", "worst_5_stars.json", true);
        buildPlaylist(stars(0, 6).and(isTroll()), worstRank(), 10, "Worst Troll Stars", "worst Troll songs", "troll.thumb", "worst_troll_stars.json", true);
        buildPlaylist(stars(6, 7).and(played().negate()), lowestStars(), 500, "xUnbeaten 6 stars", "unbeaten maps with 6 star rating", "easy.thumb", "unbeaten_6_stars.json");
        buildPlaylist(stars(7, 8).and(played().negate()), lowestStars(), 500, "xUnbeaten 7 stars", "unbeaten maps with 7 star rating", "easy.thumb", "unbeaten_7_stars.json");
        buildPlaylist(stars(8, 9).and(played().negate()), lowestStars(), 500, "xUnbeaten 8 stars", "unbeaten maps with 8 star rating", "easy.thumb", "unbeaten_8_stars.json");
        buildPlaylist(stars(9, 10).and(played().negate()), lowestStars(), 500, "xUnbeaten 9 stars", "unbeaten maps with 9 star rating", "easy.thumb", "unbeaten_9_stars.json");
        buildPlaylist(stars(10, 11).and(played().negate()), lowestStars(), 500, "xUnbeaten x10 stars", "unbeaten maps with 10 star rating", "easy.thumb", "unbeaten_10_stars.json");
        buildPlaylist(stars(11, 100).and(played().negate()), lowestStars(), 500, "xUnbeaten x11 stars", "unbeaten maps with 11+ star rating", "easy.thumb", "unbeaten_11_stars.json");
        buildPlaylist(stars(6, 100).and(played().negate()), lowestStars(), 3, "xUnbeaten 0_next", "Suggested maps to grind next", "easy.thumb", "unbeaten_next.json");

        final int minPPIncrease = 10;
        final int expectedRank = 300;

        // initialize leaderboards
        for (Song song : songs.rawSongs.songs) {
            if (song.stars < 7)
                continue;
            songs.getPPForRank(song.uid, 300);
        }
        
        buildPlaylist(stars(0, 100), mostPP(), 50, "Most PP beaten", "Beaten songs that awarded most pp", "avatar.thumb", "most_pp_beaten.json");

        buildPlaylist(
                stars(7, 100).and(played().negate()).and(ppIncreaseAtRankOver(expectedRank, minPPIncrease)),
                mostPPAtRank(expectedRank), 100,
                "Most PP at 300 (unbeaten)",
                "Most PP at rank 300 (unbeaten)",
                "shinobu.thumb",
                "most_pp_increase_unbeaten.json");
        buildPlaylist(
                stars(7, 100).and(played()).and(ppIncreaseAtRankOver(expectedRank, minPPIncrease)),
                ppIncreaseAtRank(expectedRank), 100,
                "PP increase at 300",
                "Songs that give most pp if improved to rank 300",
                "avatar.thumb",
                "most_pp_increase_beaten.json");

        printStats();
    }

    public void buildPlaylist(Predicate<Song> filter, Function<Song, Double> sortBy, int limit, String title, String desc, String image, String filename) throws IOException {
        buildPlaylist(filter, sortBy, limit, title, desc, image, filename, false);
    }

    public void buildPlaylist(Predicate<Song> filter, Function<Song, Double> sortBy, int limit, String title, String desc, String image, String filename, boolean addMinRank) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(new File(THUMBNAIL_PATH + image)));
        Playlist playlist = new Playlist(title, desc, br.readLine());
        System.out.println("Building " + playlist.playlistTitle);

        ArrayList<Song> filteredSongs = new ArrayList<>();
        for (Song song : songs.rawSongs.songs) {
            if (filter.test(song)) {
                filteredSongs.add(song);
            }
        }

        List<Integer> ranks = new ArrayList<>();

        filteredSongs.sort(Comparator.comparingDouble(sortBy::apply));
        filteredSongs.stream()
                .limit(limit)
                .sorted(Comparator.comparing(song -> song.name.toLowerCase()))
                .forEach(song -> {
                    playlist.addSong(song);
                    int rank = user.getRank(song).orElse(song.scores());
                    ranks.add(rank);
                });
        filteredSongs.stream()
                .limit(limit)
                .forEach(song -> {
                    StringBuilder out = new StringBuilder();
                    user.getRank(song).ifPresent(rank -> out.append(rank));
                    out.append("(" + -1.0 * sortBy.apply(song) + ") - ");
                    out.append(song.name + " (" + song.stars + ")");
                    System.out.println(out);
                });

        if (addMinRank) {
            int minRank = ranks.stream().mapToInt(Integer::intValue).min().orElse(0);
            playlist.playlistTitle = playlist.playlistTitle + " > " + minRank;
        }

        ObjectWriter writer = OBJECT_MAPPER.writer().withDefaultPrettyPrinter();
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

    private Predicate<Song> isTroll() {
        return song -> TROLL_SONGS.contains(song.id);
    }

    private Predicate<Song> ppAtRankHasPPOver(int rank, int pp) {
        return song -> songs.getPPForRank(song.uid, rank).orElse(0.0) >= pp;
    }

    private Predicate<Song> ppIncreaseAtRankOver(int rank, int pp) {
        return song -> -1.0 * ppIncreaseAtRank(rank).apply(song) >= pp;
    }

    private Function<Song, Double> worstRank() {
        return song -> -1.0 * user.getRank(song).orElse(song.scores());
    }

    private Function<Song, Double> mostPPAtRank(int rank) {
        return song -> -1.0 * songs.getPPForRank(song.uid, rank).orElse(-1000000.0);
    }

    private Function<Song, Double> mostPP() {
        return song -> -1.0 * user.getPP(song).orElse(0);
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
        int sixStarUnbeaten = 0;
        for (Song song : songs.rawSongs.songs) {
            OptionalInt rank = user.getRank(song);
            if (rank.isPresent()) {
                ranks.add(rank.getAsInt());
            } else {
                numUnbeaten++;
                if (song.stars >= 6) {
                    sixStarUnbeaten++;
                }
            }
        }

        System.out.println("Unbeaten: " + numUnbeaten + " (" + sixStarUnbeaten + ")");
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
        //new DownloadSongs().run();
        //new DownloadUserRecentScores().run();
        new PlaylistGenerator().run();
    }
}
