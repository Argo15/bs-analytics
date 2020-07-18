package data;

import common.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class DownloadSongs {
    private static String SONGS_API = "https://scoresaber.com/api.php?function=get-leaderboards&cat=3&page=1&limit=10000";

    public DownloadSongs(){}

    public void run() throws IOException, InterruptedException {
        Optional<String> pageJson = Utils.getPageJson(SONGS_API);
        if (!pageJson.isPresent())
        {
            return;
        }

        File filename = new File(Utils.SONGS_FILEPATH);
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        writer.write(pageJson.get());
        writer.close();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DownloadSongs downloader = new DownloadSongs();
        downloader.run();
    }
}