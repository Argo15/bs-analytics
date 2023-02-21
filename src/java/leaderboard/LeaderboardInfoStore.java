package leaderboard;

import common.Utils;
import songs.Song;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.time.temporal.ChronoUnit.DAYS;

public class LeaderboardInfoStore {
    public static String LEADERBOARD_INFO_FILEPATH = Utils.LEADERBOARD_FILEPATH + "info/";
    private static String LEADERBOARD_INFO_ID_URL = "https://scoresaber.com/api/leaderboard/by-id/%d/info";
    private static String LEADERBOARD_INFO_HASH_URL = "https://scoresaber.com/api/leaderboard/by-hash/%s/info?difficulty=%d";
    public static LocalDateTime CURRENT_DATE = LocalDateTime.now();

    public final Map<String, String> leaderboardInfoPages = new HashMap<>();
    public final Map<String, LeaderboardInfo> cache = new HashMap<>();

    public LeaderboardInfoStore(){}

    public Optional<LeaderboardInfo> getInfo(Song song) {
        return getInfoByHash(song.id, song.difficulty());
    }

    public Optional<LeaderboardInfo> getInfoById(int leaderboardId) {
        String key = String.valueOf(leaderboardId);
        String requestUrl = String.format(LEADERBOARD_INFO_ID_URL, leaderboardId);
        return getInfo(key, requestUrl);
    }

    public Optional<LeaderboardInfo> getInfoByHash(String hash, int difficulty) {
        String key = hash + "_" + difficulty;
        String requestUrl = String.format(LEADERBOARD_INFO_HASH_URL, hash, difficulty);
        return getInfo(key, requestUrl);
    }

    public int getMaxScore(Song song, int defaultVal) {
        return getMaxScore(song.id, song.difficulty(), defaultVal);
    }

    public int getMaxScore(int leaderboardId, int defaultVal) {
        return getInfoById(leaderboardId).map(info -> info.maxScore).orElse(defaultVal);
    }

    public int getMaxScore(String hash, int difficulty, int defaultVal) {
        return getInfoByHash(hash, difficulty).map(info -> info.maxScore).orElse(defaultVal);
    }

    public int getTotalPlays(int leaderboardId) {
        return getInfoById(leaderboardId).map(info -> info.plays).orElse(0);
    }

    public int getTotalPlays(String hash, int difficulty) {
        return getInfoByHash(hash, difficulty).map(info -> info.plays).orElse(0);
    }

    public int getDaysOld(Song song) {
        LeaderboardInfo info = getInfo(song).orElse(null);
        if (info == null || info.rankedDate == null) return 1000000;
        String date = info.rankedDate.substring(0, info.rankedDate.length() - 5);
        LocalDateTime dateTime = LocalDateTime.parse(date);
        return (int) DAYS.between(dateTime, CURRENT_DATE);
    }

    public Optional<LeaderboardInfo> getInfo(String key, String requestUrl) {
        if (cache.containsKey(key)) {
            return Optional.of(cache.get(key));
        }

        try {
            if (!leaderboardInfoPages.containsKey(key)) {
                _loadLeaderboardInfoPage(key, requestUrl);
            }
            String pageHtml = leaderboardInfoPages.get(key);
            if (!pageHtml.isEmpty()) {
                cache.put(key, Utils.OBJECT_MAPPER.readValue(pageHtml, LeaderboardInfo.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.ofNullable(cache.get(key));
    }

    public void _loadLeaderboardInfoPage(String key, String requestUrl) throws IOException {
        String path = LEADERBOARD_INFO_FILEPATH + key + ".html";
        File file = new File(path);
        if (!file.exists()) {
            leaderboardInfoPages.put(key, _fetchLeaderboardInfoPage(key, requestUrl));
            return;
        }

        String page = Files.lines(Paths.get(path)).findFirst().orElse("");
        leaderboardInfoPages.put(key, page);
    }

    public String _fetchLeaderboardInfoPage(String key, String requestUrl) throws IOException {
        File directory = new File(LEADERBOARD_INFO_FILEPATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Optional<String> pageHTML = Utils.getPage(requestUrl);
        if (!pageHTML.isPresent()) {
            System.out.println("Failed to fetch leaderboard for " + key);
            return "";
        }

        File filename = new File(LEADERBOARD_INFO_FILEPATH + key + ".html");
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(pageHTML.get());
        writer.close();
        return pageHTML.get();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LeaderboardInfoStore lbInfo = new LeaderboardInfoStore();
        Optional<LeaderboardInfo> info = lbInfo.getInfoById(357426);
        System.out.println(lbInfo.getMaxScore(33791, 10));
        System.out.println(lbInfo.getMaxScore("232961FEDF93A41616BF7767763E52C45A106D8F", 5, 10));
    }
}
