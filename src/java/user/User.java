package user;

import data.Score;
import data.Scores;
import songs.Song;

import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

public class User {
    private final String userId;
    private Map<Integer, Score> scores = new HashMap<>();
    private Map<String, Integer> songToRank = new HashMap<>();

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
            songToRank.put(score.songHash+score.difficultyRaw, score.rank);
        }
        return noRepeats;
    }

    public OptionalInt getRank(Song song) {
        String key = song.id + song.diff;
        if (!songToRank.containsKey(key)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(songToRank.get(key));
    }
}
