package leaderboard;

import common.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

import static leaderboard.LeaderboardInfoStore.LEADERBOARD_INFO_FILEPATH;

public class FetchLeaderboards {
    private static String LEADERBOARD_URL = "https://scoresaber.com/api/leaderboards?ranked=true&page=%d";

    public FetchLeaderboards() throws IOException {
        int currentPage = 1;
        Leaderboards page = null;

        File directory = new File(LEADERBOARD_INFO_FILEPATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        do {
            String requestUrl = String.format(LEADERBOARD_URL, currentPage++);
            Optional<String> pageHTML = Utils.getPage(requestUrl);
            if (!pageHTML.isPresent()) {
                System.out.println("Page Failed: " + requestUrl);
                continue;
            }
            String pageString = pageHTML.get();
            page = Utils.OBJECT_MAPPER.readValue(pageString, Leaderboards.class);
            for (LeaderboardInfo lb : page.leaderboards) {
                String lbInfo = Utils.OBJECT_MAPPER.writeValueAsString(lb);
                File filename = new File(LEADERBOARD_INFO_FILEPATH + lb.id + ".html");
                BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
                writer.write(lbInfo);
                writer.close();
                filename = new File(LEADERBOARD_INFO_FILEPATH + lb.songHash + "_" + lb.difficulty.difficulty + ".html");
                writer = new BufferedWriter(new FileWriter(filename));
                writer.write(lbInfo);
                writer.close();
            }
        } while (page.metadata.total > (page.metadata.itemsPerPage * page.metadata.page));
    }

    public void run() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        FetchLeaderboards fetchLeaderboards = new FetchLeaderboards();
        fetchLeaderboards.run();
    }
}
