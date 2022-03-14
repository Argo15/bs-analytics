package data;

import songs.LoadSongs;
import songs.MapInfo;
import songs.MapInfoStore;
import songs.SongStore;

import java.io.*;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class DownloadBeatmaps {
    private static String MAP_DIR = "C:/Program Files (x86)/Steam/steamapps/common/Beat Saber/Beat Saber_Data/CustomLevels/";
    private static final int THREAD_COUNT = 10;

    private SongStore songs;
    private int numTotal = 0;
    private volatile int numProcessed = 0;
    MapInfoStore mapInfos;

    public DownloadBeatmaps(){
        mapInfos = new MapInfoStore();
    }

    public void run() throws IOException, InterruptedException {
        new DownloadSongs().run();
        songs = LoadSongs.loadSongs();

        Set<String> songHashes = Arrays.stream(songs.rawSongs.songs)
                .map(s -> s.id.toLowerCase())
                .filter(hash -> !Files.exists(Paths.get(MAP_DIR + hash)))
                .collect(Collectors.toSet());
        numTotal = songHashes.size();

        List<Set<String>> splitHashes = new ArrayList<>(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            splitHashes.add(new HashSet<>());
        }

        int index = 0;
        for (String hash : songHashes) {
            splitHashes.get(index++ % THREAD_COUNT).add(hash);
        }

        splitHashes.forEach(hashes -> {
//            Runnable runnable = () -> processHashes(hashes);
//            Thread thread = new Thread(runnable);
//            thread.start();
            processHashes(hashes);
        });
    }

    private void processHashes(Set<String> hashes) {
        hashes.forEach(this::downloadMap);
    }

    private synchronized void downloadMap(String hash) {
        MapInfo mapInfo = mapInfos.getForHash(hash);
        String zipURL =mapInfo.versions[0].downloadURL;
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(zipURL))
                    .timeout(Duration.ofMinutes(1))
                    .header("Authorization", "none")
                    .header("accept-encoding", "application/json")
                    .header("accept-language", "en-US,en;q=0.9")
                    .header("cache-control", "no-cache")
                    .header("cookie", "__cfduid=ddeb704612830e2755f69b4e2bf2e87f01611446118")
                    .header("pragma", "no-cache")
                    .header("sec-fetch-dest", "document")
                    .header("sec-fetch-mode", "navigate")
                    .header("sec-fetch-site", "none")
                    .header("sec-fetch-user", "?1")
                    .header("upgrade-insecure-requests", "1")
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.104 Safari/537.36")
                    .GET()
                    .build();

            HttpResponse<InputStream> response = mapInfos.client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            ZipInputStream zipIn = new ZipInputStream(response.body());

            String destDirectory = MAP_DIR + hash;
            File destDir = new File(destDirectory);
            if (!destDir.exists()) {
                destDir.mkdir();
            }
            ZipEntry entry = zipIn.getNextEntry();
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // if the entry is a file, extracts it
                    extractFile(zipIn, filePath);
                } else {
                    // if the entry is a directory, make the directory
                    File dir = new File(filePath);
                    dir.mkdirs();
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();
            }

            numProcessed++;
            printProgress();

            Thread.sleep(1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Extracts a zip entry (file entry)
     * @param zipIn
     * @param filePath
     * @throws IOException
     */
    private void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read = 0;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }

    private synchronized void printProgress() {
        System.out.print(numProcessed + " / " + numTotal + "\r");
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        DownloadBeatmaps downloader = new DownloadBeatmaps();
        downloader.run();
    }
}
