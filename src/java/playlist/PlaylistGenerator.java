package playlist;

import data.DownloadUserRecentScores;
import leaderboard.LeaderboardScoreStore;
import songs.LoadSongs;
import songs.Song;
import songs.SongStore;
import user.LoadUser;
import user.User;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class PlaylistGenerator {
    static final String PLAYLIST_PATH = "C://Program Files (x86)/Steam/steamapps/common/Beat Saber/Playlists/";
    static final String THUMBNAIL_PATH = "thumbnails/";
    static final Set<String> DOWNLOADED = new HashSet<>();
    static final int PLAYLIST_SIZE = 100;

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
        TROLL_SONGS.add("BE932481AA4F6AD42A86B38EDBE99FA9CF67239E");
        TROLL_SONGS.add("93E368A2AEB13864B03693420897B27659465CFD");
    }

    private SongStore songs;
    private User user;
    private LeaderboardScoreStore leaderboardScores;

    public PlaylistGenerator() throws IOException {
        songs = LoadSongs.loadSongs();
        user = LoadUser.loadUser();
        leaderboardScores = new LeaderboardScoreStore(songs);
        File downloadDir = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\Beat Saber_Data\\CustomLevels");
        for (File file : downloadDir.listFiles()) {
            DOWNLOADED.add(file.getName().replaceFirst("[^ ]* " , "").toLowerCase());
        }
    }

    public void run() throws IOException {
        buildAcc();
        buildRank();
        buildPractice();
        printStats();
    }

    // PP doesn't matter, so go prioritize leaderboard position
    private void buildAcc() throws IOException {
        PlaylistBuilder accBuilder = new PlaylistBuilder(songs, user);
        accBuilder.stars(0,3)
                .limit(PLAYLIST_SIZE)
                .sort(worstRank())
                .title(songs -> {
                    if (songs.isEmpty()) return "empty";
                    int min = songs.stream().limit(20).mapToInt(s -> effectiveRank(user, s)).min().orElse(0);
                    int max = songs.stream().mapToInt(s -> effectiveRank(user, s)).max().orElse(0);
                    double minPP = songs.stream().mapToDouble(this::expectedPP).min().orElse(0);
                    double maxPP = songs.stream().mapToDouble(this::expectedPP).max().orElse(0);
                    return "rank " + min + " - " + max + " | " + minPP + " - " + maxPP + " pp";
                })
                .image("easy.thumb");
        Predicate<Song> accFilter = played().negate();
        accBuilder.filter(rankBelow(100000).and(expectedPercent(20, 98.35, 100)).and(accFilter)).image("0.thumb").save("30_acc.json");
        accBuilder.filter(rankBelow(100000).and(expectedPercent(20, 97.9, 98.35)).and(accFilter)).image("1.thumb").save("31_acc.json");
        accBuilder.filter(rankBelow(100000).and(expectedPercent(20, 0, 97.9)).and(accFilter)).image("2.thumb").save("32_acc.json");
        accBuilder.stars(3, 4).filter(rankBelow(100000).and(accFilter)).image("3.thumb").save("33_acc.json");
        accBuilder.stars(4, 5).filter(rankBelow(100000).and(accFilter)).image("4.thumb").save("34_acc.json");
        accBuilder.stars(5, 6).filter(rankBelow(100000).and(accFilter)).image("5.thumb").save("35_acc.json");
    }

    // optimize by PP reward
    private void buildRank() throws IOException {
        PlaylistBuilder rankBuilder = new PlaylistBuilder(songs, user);
        rankBuilder.limit(PLAYLIST_SIZE)
                //.sort(lowestStars())
                .sort(expectedPPSort())
                .title(songs -> {
                    if (songs.isEmpty()) return "empty";
                    double min = songs.stream().mapToDouble(s -> s.stars).min().orElse(0);
                    double max = songs.stream().mapToDouble(s -> s.stars).max().orElse(0);
                    double minPP = songs.stream().mapToDouble(this::expectedPP).min().orElse(0);
                    double maxPP = songs.stream().mapToDouble(this::expectedPP).max().orElse(0);
                    return min + " - " + max + " stars | " + minPP + " - " + maxPP + " pp";
                });
        rankBuilder.stars(6, 7).image("6.thumb").save("46_rank.json");
        rankBuilder.stars(7, 8).image("7.thumb").save("47_rank.json");
        rankBuilder.stars(8, 9).image("8.thumb").save("48_rank.json");
        rankBuilder.stars(9, 10).image("9.thumb").save("490_rank.json");
        rankBuilder.stars(10, 11).image("10.thumb").save("491_rank.json");
        rankBuilder.stars(11, 100).image("11.thumb").save("492_rank.json");
    }

    private void buildPractice() throws IOException {
        PlaylistBuilder builder = new PlaylistBuilder(songs, user);
        builder.limit(PLAYLIST_SIZE)
                .sort(lowestStars())
                .filter(played().negate().and(song -> leaderboardScores.info.getDaysOld(song) <= 750))
                .title(songs -> {
                    if (songs.isEmpty()) return "empty";
                    double min = songs.stream().mapToDouble(s -> s.stars).min().orElse(0);
                    double max = songs.stream().mapToDouble(s -> s.stars).max().orElse(0);
                    return min + " - " + max + " stars (practice)";
                });
        builder.stars(6.9, 100).image("shinobu.thumb").save("80_practice.json");
    }

    private Predicate<Song> ePPBelow(double threshold) {
        return song -> expectedPP(song) < threshold;
    }

    private Predicate<Song> stars(double minStars, double maxStars) {
        return song -> song.stars >= minStars && song.stars < maxStars;
    }

    private Predicate<Song> played() {
        return song -> user.getRank(song).isPresent();
    }


    private Predicate<Song> rankOver(int threshold) {
        return song -> user.getRank(song).orElse(song.scores()) >= threshold;
    }

    private Predicate<Song> rankBelow(int threshold) {
        return rankOver(threshold).negate();
    }

    private Predicate<Song> isTroll() {
        return song -> TROLL_SONGS.contains(song.id);
    }

    private Predicate<Song> ppOver(int pp) {
        return song -> user.getPP(song).orElse(0) >= pp;
    }

//    private Predicate<Song> ppIncreaseAtRankOver(int rank, double pp) {
//        return song -> -1.0 * ppIncreaseAtRank(rank).apply(song) >= pp;
//    }

    private Predicate<Song> downloaded() {
        return song -> {
            if (user.getRank(song).isPresent()) {
                return true;
            }
            for (String name : DOWNLOADED) {
                if (name.contains(song.id.toLowerCase()) || name.contains(song.name.toLowerCase())) {
                    return true;
                }
            }
            return false;
        };
    }

    private Function<Song, Double> worstRank() {
        return song -> -1.0 * effectiveRank(user, song);
    }

    private static int effectiveRank(User user, Song song) {
        return user.getRank(song).orElse(song.scores());
    }

    private Function<Song, Double> expectedPPSort() { return song ->  -1.0 * expectedPP(song); }

    private double expectedPP(Song song) {
        int daysOld = leaderboardScores.info.getDaysOld(song);
        int yearsOld = Math.min(daysOld / 365, 5);
        // expected rank is 40 if brand new, 100 if older than 60 days. Then add 50 for each year
        int expectedRank = Math.min(40 + daysOld, 100) + 50 * yearsOld;
        return leaderboardScores.getScore(song, expectedRank).map(lb -> lb.pp).orElse(0.0);
    }

    private Function<Song, Double> mostPP() {
        return song -> -1.0 * user.getPP(song).orElse(0);
    }

    private Function<Song, Double> lowestStars() {
        return song -> song.stars;
    }

    private Function<Song, Double> mostStars() {
        return song -> -1.0 * song.stars;
    }

//    private Function<Song, Double> ppIncreaseAtRank(int rank) {
//        return song -> {
//            double ppAtRank = songs.getPPForRank(song.uid, rank).orElse(0.0);
//            double currentPP = user.getTotalPP();
//            double newPP = user.getTotalPP(song.uid, ppAtRank);
//            return -1.0 * (newPP - currentPP);
//        };
//    }

    private Function<Song, Double> recent() {
        return song -> -1.0 * song.scores_day / song.scores();
    }

    private Function<Song, Double> rand() {
        return song -> {
            Random rand = new Random(song.hashCode());
            return rand.nextDouble();
        };
    }

    private Predicate<Song> expectedPercent(int rank, double minPercent, double maxPercent) {
        return song -> {
            int maxScore = leaderboardScores.info.getMaxScore(song, -100000000);
            int scoreAtRank = leaderboardScores.getScore(song, rank).map(score -> score.baseScore).orElse(0);
            double percent = 100.0 * ((double) scoreAtRank / (double) maxScore);
            return percent >= minPercent && percent < maxPercent;
        };
    }

    private void printStats() {
        List<Integer> ranks = new ArrayList<>();
        int numUnbeaten = 0;
        int sixStarUnbeaten = 0;
        int tenStarBeaten = 0;
        int elevenStarBeaten = 0;
        int twelveStarBeaten = 0;
        int numTopTen = 0;
        int numTop20 = 0;
        for (Song song : songs.rawSongs) {
            OptionalInt rank = user.getRank(song);
            if (rank.isPresent()) {
                ranks.add(rank.getAsInt());
                if (rank.getAsInt() <= 10) {
                    numTopTen++;
                }
                if (rank.getAsInt() <= 20) {
                    numTop20++;
                }
                if (song.stars >= 12) {
                    twelveStarBeaten++;
                } else if (song.stars >= 11) {
                    elevenStarBeaten++;
                } else if (song.stars >= 10) {
                    tenStarBeaten++;
                }
            } else {
                numUnbeaten++;
                if (song.stars >= 7) {
                    sixStarUnbeaten++;
                }
            }
        }

        System.out.println("Unbeaten: " + numUnbeaten + " (" + sixStarUnbeaten + " 7+ stars)");
        System.out.println("10 star beaten: " + tenStarBeaten);
        System.out.println("11+ star beaten: " + (elevenStarBeaten + twelveStarBeaten));
        //System.out.println(">12* beaten: " + twelveStarBeaten);
        //Collections.sort(ranks);
        //System.out.println("Ranks");
        System.out.println("avg rank: " + ranks.stream().mapToInt(Integer::intValue).average().getAsDouble());
        /*System.out.println("10 percentile: " + ranks.get(ranks.size() / 10));
        System.out.println("25 percentile: " + ranks.get(ranks.size() / 4));
        System.out.println("50 percentile: " + ranks.get(ranks.size() / 2));
        System.out.println("75 percentile: " + ranks.get(3 * (ranks.size() / 4)));
        System.out.println("90 percentile: " + ranks.get(9 * (ranks.size() / 10)));
        System.out.println("Top 10: " + numTopTen + " / " + songs.rawSongs.songs.length);
        System.out.println("Top 20: " + numTop20 + " / " + songs.rawSongs.songs.length);*/
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        //new DownloadSongs().run();
        //new FetchLeaderboards().run();
        //new DownloadBeatmaps().run();
        new DownloadUserRecentScores().run();
        new PlaylistGenerator().run();
    }
}
