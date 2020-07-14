package common;

import org.codehaus.jackson.map.ObjectMapper;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Optional;

public class Utils {
    public static ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String ARGO_USER_ID = "76561198024990541";
    public static String TOP_SCORE_FILEPATH =
            "api-data/player/" + Utils.ARGO_USER_ID + "/scores/top/";
    public static String RECENT_SCORE_FILEPATH =
            "api-data/player/" + Utils.ARGO_USER_ID + "/scores/recent/";
    public static String SONGS_FILEPATH = "api-data/maps.json";

    public static Optional<String> getPageJson(String page) throws IOException, InterruptedException {
        System.out.println(page);
        URL apiUrl = new URL(page);
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
            System.out.println("Failed " + page);
            return Optional.empty();
        }
    }
}
