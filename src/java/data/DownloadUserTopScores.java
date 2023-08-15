package data;

import common.Utils;
import java.io.*;
import java.util.Optional;

class DownloadUserTopScores {
    private static String TOP_SCORE_API =
            "https://new.scoresaber.com/api/player/" +
            Utils.ARGO_USER_ID +
            "/scores/top/";

    public DownloadUserTopScores(){}

    public void run() throws IOException, InterruptedException {
        File directory = new File(Utils.TOP_SCORE_FILEPATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        int errorCnt = 0;
        int curPage = 0;
        while (true) {
            Optional<String> pageJson = Utils.getPage(TOP_SCORE_API + curPage);
            if (!pageJson.isPresent())
            {
                if (errorCnt++ > 10)
                    break;
                else
                    continue;
            }
            errorCnt = 0;
            String filename = Utils.TOP_SCORE_FILEPATH + curPage + ".json";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(pageJson.get());
            writer.close();
            curPage++;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DownloadUserTopScores downloader = new DownloadUserTopScores();
        downloader.run();
    }
}