package data;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

class DownloadUserTopScores {
    private static String ARGO_USER_ID = "76561198024990541";
    private static String TOP_SCORE_API =
            "https://new.scoresaber.com/api/player/" +
            ARGO_USER_ID +
            "/scores/top/";
    private static String TOP_SCORE_FILEPATH =
            "api-data/player/" + ARGO_USER_ID + "/scores/top/";

    public DownloadUserTopScores(){}

    public void run() throws IOException, InterruptedException {
        File directory = new File(TOP_SCORE_FILEPATH);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        int curPage = 1;
        while (true) {
            Optional<String> pageJson = getPageJson(curPage);
            if (!pageJson.isPresent())
            {
                break;
            }
            String filename = TOP_SCORE_FILEPATH + String.valueOf(curPage) + ".json";
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(pageJson.get());
            writer.close();
            curPage++;
            Thread.sleep(1000);
        }
    }

    private static Optional<String> getPageJson(int page) throws IOException, InterruptedException {
        System.out.println("Page " + page);
        URL apiUrl = new URL(TOP_SCORE_API + String.valueOf(page));
        HttpsURLConnection con = (HttpsURLConnection) apiUrl.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer response = new StringBuffer();
            String readLine = null;
            while ((readLine = in.readLine()) != null) {
                response.append(readLine);
            }
            in.close();
            return Optional.of(response.toString());
        } else {
            System.out.println("Page " + page + " failed");
            return Optional.empty();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DownloadUserTopScores downloader = new DownloadUserTopScores();
        downloader.run();
    }
}