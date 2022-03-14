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

    private static String LEADERBOARD_SCORES_URL = "https://scoresaber.com/api/leaderboard/by-id/%d/scores?page=%d\n";
    private static final Pattern PERCENT_PATTERN = Pattern.compile("<span>([0-9\\.]+)\\%<\\/span>");
    private static final Pattern PP_PATTERN = Pattern.compile("\"pp\">([0-9\\.]+)<");
    private static final Pattern SCORES_PATTERN = Pattern.compile(" plays\": ([,0-9\\.]+) ");

    public final Songs rawSongs;
    public final Map<LeaderboardKey, String> leaderboardScorePages = new HashMap<>();
    public final Map<PageKey, Double> songRankToPP = new HashMap<>();
    public final Map<PageKey, Double> songRankToPercent = new HashMap<>();
    public final Map<Integer, Song> songIdLookup = new HashMap<>();

    public SongStore(Songs songs) {
        rawSongs = songs;
        for (Song song : songs.songs) {
            songIdLookup.put(song.uid, song);
        }
    }

    public Optional<Double> getPPForRank(int songId, int rank) {
        return getForRank(PP_PATTERN, songRankToPP, songId, rank);
    }

    public Optional<Double> getPercentForRank(int songId, int rank) {
        return getForRank(PERCENT_PATTERN, songRankToPercent, songId, rank);
    }

    private Optional<Double> getForRank(Pattern pattern, Map<PageKey, Double> cache, int songId, int rank) {
        Song song  = songIdLookup.get(songId);
        if (song != null && song.scores() < rank) {
            return Optional.empty();
        }
        PageKey pageKey = new PageKey(songId, rank);
        if (cache.containsKey(pageKey)) {
            if (cache.get(pageKey) == 0.0) {
                return Optional.empty();
            }
            return Optional.of(cache.get(pageKey));
        }
        int page = ((rank-1) / 12) + 1;
        int index = (rank-1) % 12 + 1;
        LeaderboardKey key = new LeaderboardKey(songId, page);
        if (!leaderboardScorePages.containsKey(key)) {
            try {
                _loadLeaderboardScorePage(key);
            } catch (IOException e) {
                e.printStackTrace();
                cache.put(pageKey, 0.0);
                return Optional.empty();
            }
        }
        String pageHtml = leaderboardScorePages.get(key);
        Matcher matcher = pattern.matcher(pageHtml);
        int curIdx = 0;
        String sVal = null;
        while (matcher.find() && curIdx++ < index) {
            sVal = matcher.group(1);
        }
        if (sVal != null)
        {
            Double val = Double.valueOf(sVal);
            cache.put(pageKey, val);
            return Optional.of(val);
        }
        System.out.println("Cannot find page data for " + songId + " rank " + rank);
        cache.put(pageKey, 0.0);
        return Optional.empty();
    }

    private void _loadLeaderboardScorePage(LeaderboardKey key) throws IOException {
        String path = Utils.LEADERBOARD_FILEPATH + key.songId + "_" + key.page + ".html";
        File file = new File(path);
        if (!file.exists()) {
            leaderboardScorePages.put(key, _fetchLeaderboardPage(key));
            return;
        }

        // Check if page is out of date
        String page = Files.lines(Paths.get(path)).findFirst().orElse("");
        Matcher matcher = SCORES_PATTERN.matcher(page);
        matcher.find();
        int scores = Integer.parseInt(matcher.group(1).replaceAll(",",""));
        Song song = songIdLookup.get(key.songId);
        if (1.1 * scores < song.scores())
        {
            leaderboardScorePages.put(key, _fetchLeaderboardPage(key));
            return;
        }

        leaderboardScorePages.put(key, page);
    }

    private String _fetchLeaderboardPage(LeaderboardKey key) throws IOException {
        File directory = new File(Utils.LEADERBOARD_FILEPATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String requestUrl = String.format(LEADERBOARD_SCORES_URL, key.songId, key.page);
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

    private static class PageKey {
        private final int songId;
        private final int rank;

        public PageKey(int songId, int rank) {
            this.songId = songId;
            this.rank = rank;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PageKey that = (PageKey) o;
            return songId == that.songId &&
                    rank == that.rank;
        }

        @Override
        public int hashCode() {
            return Objects.hash(songId, rank);
        }
    }
}
