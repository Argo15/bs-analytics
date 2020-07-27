package data;

import common.Utils;
import user.LoadUser;
import user.User;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class DownloadUserRecentScores {
    private static String RECENT_SCORE_API =
            "https://new.scoresaber.com/api/player/" +
            Utils.ARGO_USER_ID +
            "/scores/recent/";

    public DownloadUserRecentScores(){}

    public void run() throws IOException, InterruptedException {
        File directory = new File(Utils.RECENT_SCORE_FILEPATH);
        for (File file : directory.listFiles()) {
            file.delete();
        }

        User user = LoadUser.loadUser();

        int curPage = 1;
        while (true) {
            Optional<String> pageJson = Utils.getPage(RECENT_SCORE_API + curPage);
            if (!pageJson.isPresent())
            {
                break;
            }
            String filename = Utils.RECENT_SCORE_FILEPATH + curPage + ".json";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(pageJson.get());
            writer.close();
            Scores parsedScores = Utils.OBJECT_MAPPER.readValue(pageJson.get(), Scores.class);
            if (!user.addScores(parsedScores))
            {
                break;
            }
            curPage++;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DownloadUserRecentScores downloader = new DownloadUserRecentScores();
        downloader.run();
    }
}