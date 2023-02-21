package data;

import common.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

public class DownloadSongs {
    public static String SONGS_DIRECTORY = "api-data/songs/";
    private static String SONGS_API = "https://scoresaber.com/api.php?function=get-leaderboards&cat=3&limit=20&page=%d";

    public DownloadSongs(){}

    public void run() throws IOException, InterruptedException {
        File directory = new File(SONGS_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        int page = 1;
        while (page < 1000) {
            Optional<String> pageJson = Utils.getPage(String.format(SONGS_API, page));
            if (!pageJson.isPresent() || pageJson.get().contains("songs: [ ]"))
            {
                break;
            }

            File filename = new File(SONGS_DIRECTORY + page + ".json");
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(pageJson.get());
            writer.close();
            page++;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DownloadSongs downloader = new DownloadSongs();
        downloader.run();
    }
}