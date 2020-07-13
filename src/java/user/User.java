package user;

import data.Score;

import java.util.ArrayList;
import java.util.Collection;

public class User {
    private final String userId;
    private ArrayList<Score> scores = new ArrayList<>();

    public User(String userId) {
        this.userId = userId;
    }

    public void addScores(Collection<Score> scores)
    {
        this.scores.addAll(scores);
    }
}
