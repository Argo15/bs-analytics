package user;

import data.Score;
import data.Scores;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class User {
    private final String userId;
    private Map<Integer, Score> scores = new HashMap<>();

    public User(String userId) {
        this.userId = userId;
    }

    // return false if there are any repeats
    public boolean addScores(Scores scores)
    {
        boolean noRepeats = true;
        for (Score score : scores.scores)
        {
            if (this.scores.containsKey(score.scoreId) &&
                this.scores.get(score.scoreId).timeSet.equals(score.timeSet))
            {
                noRepeats = false;
            }
            this.scores.put(score.scoreId, score);
        }
        return noRepeats;
    }
}
