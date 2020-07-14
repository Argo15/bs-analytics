package user;

import common.Utils;
import data.Scores;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class LoadUser {

    public LoadUser() { }

    public static User loadUser() throws IOException {
        User user = new User(Utils.ARGO_USER_ID);

        // Read full dump of scores
        for (File file : new File(Utils.TOP_SCORE_FILEPATH).listFiles()) {
            Scores parsedScores = Utils.OBJECT_MAPPER.readValue(file, Scores.class);
            user.addScores(parsedScores);
            System.out.println(file.getName() + " " + parsedScores.scores[0].pp);
        }

        // Read recent scores since last dump
        for (File file : new File(Utils.RECENT_SCORE_FILEPATH).listFiles()) {
            Scores parsedScores = Utils.OBJECT_MAPPER.readValue(file, Scores.class);
            user.addScores(parsedScores);
            System.out.println(file.getName() + " " + parsedScores.scores[0].pp);
        }

        return user;
    }

    public void run() throws IOException {
        loadUser();
    }

    public static void main(String[] args) throws IOException {
        LoadUser loader = new LoadUser();
        loader.run();
    }
}
