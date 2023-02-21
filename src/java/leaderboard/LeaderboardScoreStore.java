package leaderboard;

import common.Utils;
import songs.Song;
import songs.SongStore;
import songs.Songs;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class LeaderboardScoreStore {
    public static String LEADERBOARD_SCORE_FILEPATH = Utils.LEADERBOARD_FILEPATH + "scores/";
    private static String LEADERBOARD_SCORE_ID_URL = "https://scoresaber.com/api/leaderboard/by-id/%d/scores?page=%d";
    private static String LEADERBOARD_SCORE_HASH_URL = "https://scoresaber.com/api/leaderboard/by-hash/%s/scores?difficulty=%d&page=%d";

    public final Map<String, String> leaderboardScorePages = new HashMap<>();
    public final Map<String, LeaderboardScoresPage> cache = new HashMap<>();
    public final LeaderboardInfoStore info = new LeaderboardInfoStore();
    public final SongStore songs;

    public Set<String> EXCLUDE_REFRESH = new HashSet<>();
    {
        EXCLUDE_REFRESH.add("2920185B3EC35535E46E990980607F5C33547317"); // Tool-Assisted Speedcore, has 800 more plays in song api than in reality
        EXCLUDE_REFRESH.add("4867E299296A5B965EDC4DCD930C07703B06B96E"); // Daisuki, Evolution
    }

    public LeaderboardScoreStore(SongStore songs){
        this.songs = songs;
    }

    public Optional<LeaderboardScore> getScore(Song song, int rank) {
        return getScoreByHash(song.id, song.difficulty(), rank);
    }

    public Optional<LeaderboardScore> getScoreById(int leaderboardId, int rank) {
        int page = ((rank-1) / 12) + 1;
        String key = leaderboardId + "_" + page;
        String requestUrl = String.format(LEADERBOARD_SCORE_ID_URL, leaderboardId, page);
        int totalPlays = info.getInfoById(leaderboardId).map(lb -> {
            String hash = lb.songHash + lb.difficulty;
            return songs.songHashLookup.containsKey(hash) ? songs.songHashLookup.get(hash).scores() : 0;
        }).orElse(0);
        return getScore(key, requestUrl, rank, totalPlays);
    }

    public Optional<LeaderboardScore> getScoreByHash(String hash, int difficulty, int rank) {
        int page = ((rank-1) / 12) + 1;
        String songHash = hash + "_" + difficulty;
        int totalPlays = songs.songHashLookup.containsKey(songHash) ? songs.songHashLookup.get(songHash).scores() : 0;
        String key = songHash + "_" + page;
        String requestUrl = String.format(LEADERBOARD_SCORE_HASH_URL, hash, difficulty, page);
        return getScore(key, requestUrl, rank, totalPlays);
    }

    public Optional<LeaderboardScore> getScore(String key, String requestUrl, int rank, int totalPlays) {
        LeaderboardScoresPage page = getScoresPage(key, requestUrl).orElse(null);
        if (page == null) {
            return Optional.empty();
        }

        // Clear files that are too old or invalid
        String songId = key.split("_")[0];
        if (page.metadata.total * 3 < totalPlays && !EXCLUDE_REFRESH.contains(songId)) {
            new File(LEADERBOARD_SCORE_FILEPATH + key + ".html").delete();
            leaderboardScorePages.remove(key);
            cache.remove(key);
            return getScore(key, requestUrl, rank, totalPlays);
        }

        int index = (rank-1) % 12;

        // Index out of range, can happen on last page
        if (page.scores == null || page.scores.length <= index) {
            return Optional.empty();
        }

        return Optional.of(page.scores[index]);
    }

    public Optional<LeaderboardScoresPage> getScoresPage(String key, String requestUrl) {
        if (cache.containsKey(key)) {
            return Optional.of(cache.get(key));
        }

        try {
            if (!leaderboardScorePages.containsKey(key)) {
                _loadLeaderboardScorePage(key, requestUrl);
            }
            String pageHtml = leaderboardScorePages.get(key);
            if (!pageHtml.isEmpty()) {
                cache.put(key, Utils.OBJECT_MAPPER.readValue(pageHtml, LeaderboardScoresPage.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(cache.get(key));
    }

    public void _loadLeaderboardScorePage(String key, String requestUrl) throws IOException {
        String path = LEADERBOARD_SCORE_FILEPATH + key + ".html";
        File file = new File(path);
        if (!file.exists()) {
            leaderboardScorePages.put(key, _fetchLeaderboardScorePage(key, requestUrl));
            return;
        }

        String page = Files.lines(Paths.get(path)).findFirst().orElse("");
        leaderboardScorePages.put(key, page);
    }

    public String _fetchLeaderboardScorePage(String key, String requestUrl) throws IOException {
        File directory = new File(LEADERBOARD_SCORE_FILEPATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Optional<String> pageHTML = Utils.getPage(requestUrl);
        if (!pageHTML.isPresent()) {
            System.out.println("Failed to fetch leaderboard for " + key);
            return "";
        }

        File filename = new File(LEADERBOARD_SCORE_FILEPATH + key + ".html");
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(pageHTML.get());
        writer.close();
        return pageHTML.get();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        LeaderboardScoreStore lbScores = new LeaderboardScoreStore(new SongStore(new Songs()));
        Optional<LeaderboardScore> score = lbScores.getScoreById(357426, 57);
        score = lbScores.getScoreByHash("232961FEDF93A41616BF7767763E52C45A106D8F", 5, 101);
        System.out.println(score.get().maxCombo);
    }
}
