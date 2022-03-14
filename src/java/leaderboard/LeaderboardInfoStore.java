package leaderboard;

import common.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class LeaderboardInfoStore {
    private static String LEADERBOARD_INFO_FILEPATH = Utils.LEADERBOARD_FILEPATH + "info/";
    private static String LEADERBOARD_INFO_URL = "https://scoresaber.com/api/leaderboard/by-id/%d/info\n";

    public final Map<Integer, String> leaderboardInfoPages = new HashMap<>();
    public final Map<Integer, LeaderboardInfo> cache = new HashMap<>();

    public LeaderboardInfoStore(){}

    public Optional<LeaderboardInfo> getInfoById(int leaderboardId) {
        if (cache.containsKey(leaderboardId)) {
            return Optional.of(cache.get(leaderboardId));
        }

        try {
            if (!leaderboardInfoPages.containsKey(leaderboardId)) {
                _loadLeaderboardScorePage(leaderboardId);
            }
            String pageHtml = leaderboardInfoPages.get(leaderboardId);
            cache.put(leaderboardId, Utils.OBJECT_MAPPER.readValue(pageHtml, LeaderboardInfo.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.ofNullable(cache.get(leaderboardId));
    }

    public int getMaxScore(int leaderboardId, int defaultVal) {
        return getInfoById(leaderboardId).map(info -> info.maxScore).orElse(defaultVal);
    }

    public void _loadLeaderboardScorePage(int leaderboardId) throws IOException {
        String path = Utils.LEADERBOARD_FILEPATH + "info/" + leaderboardId + ".html";
        File file = new File(path);
        if (!file.exists()) {
            leaderboardInfoPages.put(leaderboardId, _fetchLeaderboardInfoPage(leaderboardId));
            return;
        }

        String page = Files.lines(Paths.get(path)).findFirst().orElse("");
        leaderboardInfoPages.put(leaderboardId, page);
    }

    public String _fetchLeaderboardInfoPage(int leaderboardId) throws IOException {
        File directory = new File(LEADERBOARD_INFO_FILEPATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String requestUrl = String.format(LEADERBOARD_INFO_URL, leaderboardId);
        Optional<String> pageHTML = Utils.getPage(requestUrl);
        if (!pageHTML.isPresent())
        {
            System.out.println("Failed to fetch leaderboard for " + leaderboardId);
            return "";
        }

        File filename = new File(LEADERBOARD_INFO_FILEPATH + leaderboardId + ".html");
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(pageHTML.get());
        writer.close();
        return pageHTML.get();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        LeaderboardInfoStore lbInfo = new LeaderboardInfoStore();
        Optional<LeaderboardInfo> info = lbInfo.getInfoById(357426);
        System.out.println(lbInfo.getMaxScore(357425, 10));
    }
}
