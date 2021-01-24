package playlist;

import data.DownloadSongs;
import data.DownloadUserRecentScores;
import songs.LoadSongs;
import songs.Song;
import songs.SongStore;
import user.LoadUser;
import user.User;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class PlaylistGenerator {
    static final String PLAYLIST_PATH = "C://Program Files (x86)/Steam/steamapps/common/Beat Saber/Playlists/";
    static final String THUMBNAIL_PATH = "thumbnails/";
    static final Set<String> DOWNLOADED = new HashSet<>();

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

    public PlaylistGenerator() throws IOException {
        songs = LoadSongs.loadSongs();
        user = LoadUser.loadUser();
        File downloadDir = new File("C:\\Program Files (x86)\\Steam\\steamapps\\common\\Beat Saber\\Beat Saber_Data\\CustomLevels");
        for (File file : downloadDir.listFiles()) {
            DOWNLOADED.add(file.getName().replaceFirst("[^ ]* " , "").toLowerCase());
        }
    }

    public void run() throws IOException {
        PlaylistBuilder builder = new PlaylistBuilder(songs, user);

//        int cuts = 1;
//        int size = 10;
//        builder.offset(0).limit(size).stars(3,7).filter(played().negate()).sort(rand());
//        for (int i=0; i<cuts; i++) {
//            String count = String.valueOf(i);
//            builder.offset(i * size)
//                    .title("3-7 star p" + count)
//                    .image(count + ".thumb");
//            if (i > 9) {
//                builder.save("19" + count +"_unplayed.json");
//            } else {
//                builder.save("1" + count +"_unplayed.json");
//            }
//        }

        int cuts = 16;
        int size = 10;
        builder = new PlaylistBuilder(songs, user);
        builder.offset(0).limit(size).stars(8,100).sort(lowestStars())
                .filter(ppIncreaseAtRankOver(100, 10.0)
                        .or(ppOver(310)))
                .title(songs -> {
                    if (songs.isEmpty()) {
                        return "empty";
                    }
                    double min = songs.stream().mapToDouble(s -> s.stars).min().orElse(0);
                    double max = songs.stream().mapToDouble(s -> s.stars).max().orElse(0);
                    return min + " - " + max + " stars";
                });
        for (int i=0; i<cuts; i++) {
            String count = String.valueOf(i);
            builder.offset(i * size).image(count + ".thumb");
            if (i > 9) {
                builder.save("3" + count +"_improve.json");
            } else {
                builder.save("2" + count +"_improve.json");
            }
        }

//        new PlaylistBuilder(songs, user)
//                .stars(0,1000)
//                .limit(1000)
//                .filter(downloaded().negate())
//                .title("to download")
//                .image("avatar.thumb")
//                .save("toDownload.json");

        printStats();
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

    private Predicate<Song> isTroll() {
        return song -> TROLL_SONGS.contains(song.id);
    }

    private Predicate<Song> ppOver(int pp) {
        return song -> user.getPP(song).orElse(0) >= pp;
    }

    private Predicate<Song> ppAtRankHasPPOver(int rank, int pp) {
        return song -> songs.getPPForRank(song.uid, rank).orElse(0.0) >= pp;
    }

    private Predicate<Song> ppIncreaseAtRankOver(int rank, double pp) {
        return song -> -1.0 * ppIncreaseAtRank(rank).apply(song) >= pp;
    }

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

    private Function<Song, Double> mostStars() {
        return song -> -1.0 * song.stars;
    }

    private Function<Song, Double> ppIncreaseAtRank(int rank) {
        return song -> {
            double ppAtRank = songs.getPPForRank(song.uid, rank).orElse(0.0);
            double currentPP = user.getTotalPP();
            double newPP = user.getTotalPP(song.uid, ppAtRank);
            return -1.0 * (newPP - currentPP);
        };
    }

    private Function<Song, Double> recent() {
        return song -> -1.0 * song.scores_day / song.scores();
    }

    private Function<Song, Double> nearTop10() {
        return song -> {
            double ppAtRank10 = songs.getPPForRank(song.uid, 10).orElse(0.0);
            double currentPP = user.getPP(song).orElse(0.001);
            if (user.getRank(song).orElse(1000) <= 10) {
                return 100000.0; // already top 10
            }
            if (ppAtRank10 / currentPP < 1.0) {
                // bad data, to fix, likely user used no fail or something
                ppAtRank10 = songs.getPPForRank(song.uid, 9).orElse(0.0);
            }
            return ppAtRank10 / currentPP; // larger number is further away from top 10
        };
    }

    private Function<Song, Double> rand() {
        return song -> {
            Random rand = new Random(song.hashCode());
            return rand.nextDouble();
        };
    }

    private Predicate<Song> expectedPercent(int rank, double minPercent, double maxPercent) {
        return song -> {
            double percent = songs.getPercentForRank(song.uid, rank).orElse(0.0);
            return percent >= minPercent && percent < maxPercent;
        };
    }

    private void printStats() {
        List<Integer> ranks = new ArrayList<>();
        int numUnbeaten = 0;
        int sixStarUnbeaten = 2; // for Mare and Ultimate
        int numTopTen = 0;
        int numTop20 = 0;
        for (Song song : songs.rawSongs.songs) {
            OptionalInt rank = user.getRank(song);
            if (rank.isPresent()) {
                ranks.add(rank.getAsInt());
                if (rank.getAsInt() <= 10) {
                    numTopTen++;
                }
                if (rank.getAsInt() <= 20) {
                    numTop20++;
                }
            } else {
                numUnbeaten++;
                if (song.stars >= 7) {
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
        System.out.println("Top 10: " + numTopTen + " / " + songs.rawSongs.songs.length);
        System.out.println("Top 20: " + numTop20 + " / " + songs.rawSongs.songs.length);
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        new DownloadSongs().run();
        new DownloadUserRecentScores().run();
        new PlaylistGenerator().run();
    }
}
