package user;

import common.Utils;
import data.Scores;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class LoadUser {

    public LoadUser()
    {

    }

    public static User loadUser() throws IOException {
        User user = new User(Utils.ARGO_USER_ID);
        File topScoreDirectory = new File(Utils.TOP_SCORE_FILEPATH);
        for (File file : topScoreDirectory.listFiles()) {
            ObjectMapper objectMapper = new ObjectMapper();
            Scores parsedScores = objectMapper.readValue(file, Scores.class);
            user.addScores(Arrays.asList(parsedScores.scores));
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
