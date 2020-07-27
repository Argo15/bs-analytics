package songs;

import common.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongStore {
    private static String LEADERBOARD_URL = "https://scoresaber.com/leaderboard/%d?page=%d";
    private static final Pattern PP_PATTERN = Pattern.compile("ppValue\">([0-9\\.]+)<");

    public final Songs rawSongs;
    public final Map<LeaderboardKey, String> leaderboardPages = new HashMap<>();
    public final Map<PPKey, Double> songRankToPP = new HashMap<>();

    public SongStore(Songs songs) {
        rawSongs = songs;
    }

    public Optional<Double> getPPForRank(int songId, int rank) {
        PPKey ppKey = new PPKey(songId, rank);
        if (songRankToPP.containsKey(ppKey)) {
            if (songRankToPP.get(ppKey) == 0.0) {
                return Optional.empty();
            }
            return Optional.of(songRankToPP.get(ppKey));
        }
        int page = ((rank-1) / 12) + 1;
        int index = (rank-1) % 12 + 1;
        LeaderboardKey key = new LeaderboardKey(songId, page);
        if (!leaderboardPages.containsKey(key)) {
            try {
                _loadLeaderboardPage(key);
            } catch (IOException e) {
                e.printStackTrace();
                songRankToPP.put(ppKey, 0.0);
                return Optional.empty();
            }
        }
        String pageHtml = leaderboardPages.get(key);
        Matcher matcher = PP_PATTERN.matcher(pageHtml);
        int curIdx = 0;
        String sPP = null;
        while (matcher.find() && curIdx++ < index) {
            sPP = matcher.group(1);
        }
        if (sPP != null)
        {
            Double pp = Double.valueOf(sPP);
            songRankToPP.put(ppKey, pp);
            return Optional.of(pp);
        }
        System.out.println("Cannot find pp for " + songId + " rank " + rank);
        songRankToPP.put(ppKey, 0.0);
        return Optional.empty();
    }

    private void _loadLeaderboardPage(LeaderboardKey key) throws IOException {
        String path = Utils.LEADERBOARD_FILEPATH + key.songId + "_" + key.page + ".html";
        File file = new File(path);
        if (!file.exists()) {
            leaderboardPages.put(key, _fetchLeaderboardPage(key));
            return;
        }
        leaderboardPages.put(key, Files.lines(Paths.get(path)).findFirst().orElse(""));
    }

    private String _fetchLeaderboardPage(LeaderboardKey key) throws IOException {
        File directory = new File(Utils.LEADERBOARD_FILEPATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String requestUrl = String.format(LEADERBOARD_URL, key.songId, key.page);
        Optional<String> pageHTML = Utils.getPage(requestUrl);
        if (!pageHTML.isPresent())
        {
            System.out.println("Failed to fetch leaderboard for " + key.songId + " page " + key.page);
            return "";
        }

        File filename = new File(Utils.LEADERBOARD_FILEPATH + key.songId + "_" + key.page + ".html");
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(pageHTML.get());
        writer.close();
        return pageHTML.get();
    }

    private static class LeaderboardKey {
        private final int songId;
        private final int page;

        public LeaderboardKey(int songId, int page) {
            this.songId = songId;
            this.page = page;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LeaderboardKey that = (LeaderboardKey) o;
            return songId == that.songId &&
                    page == that.page;
        }

        @Override
        public int hashCode() {
            return Objects.hash(songId, page);
        }
    }

    private static class PPKey {
        private final int songId;
        private final int rank;

        public PPKey(int songId, int rank) {
            this.songId = songId;
            this.rank = rank;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PPKey that = (PPKey) o;
            return songId == that.songId &&
                    rank == that.rank;
        }

        @Override
        public int hashCode() {
            return Objects.hash(songId, rank);
        }
    }
}
