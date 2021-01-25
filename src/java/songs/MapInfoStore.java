package songs;

import common.Utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class MapInfoStore {
    public static String MAP_INFO_PATH = "api-data/map-info/";
    private static String MAP_API = "https://beatsaver.com/api/maps/by-hash/";

    public final Map<String, MapInfo> hashToMapInfo = new HashMap<>();
    public HttpClient client;

    public MapInfoStore() {
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .build();
    }

    public MapInfo getForHash(String hash) {
        if (!hashToMapInfo.containsKey(hash)) {
            try {
                _loadMapInfo(hash);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        return hashToMapInfo.get(hash);
    }

    private void _loadMapInfo(String hash) throws IOException {
        String path = MAP_INFO_PATH + hash + ".json";
        String pageJson = "";
        File file = new File(path);
        if (!file.exists()) {
            pageJson = fetchPage(MAP_API + hash);
            try (PrintWriter out = new PrintWriter(path)) {
                out.println(pageJson);
            }
        }

        hashToMapInfo.put(hash, Utils.OBJECT_MAPPER.readValue(file, MapInfo.class));
    }

    public synchronized String fetchPage(String URL) throws IOException {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL))
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
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }
}
