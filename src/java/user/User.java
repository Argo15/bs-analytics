package user;

import data.Score;
import data.Scores;
import songs.Song;

import java.util.*;

public class User {
    private final String userId;
    private Map<Integer, Score> scores = new HashMap<>();
    private Map<Integer, Integer> songToRank = new HashMap<>();
    private Map<Integer, Double> songToPP = new HashMap<>();
    private Map<Integer, Double> songToPercent = new HashMap<>();

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
            songToRank.put(score.leaderboardId, score.rank);
            songToPP.put(score.leaderboardId, score.pp);
            songToPercent.put(score.leaderboardId, (double) score.score / (double) score.maxScore);
        }
        return noRepeats;
    }

    public OptionalInt getRank(Song song) {
        if (!songToRank.containsKey(song.uid)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(songToRank.get(song.uid));
    }

    public OptionalDouble getPercent(Song song) {
        if (!songToPercent.containsKey(song.uid)) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(songToPercent.get(song.uid));
    }

    public OptionalDouble getPP(Song song) {
        if (!songToPP.containsKey(song.uid)) {
            return OptionalDouble.empty();
        }
        return OptionalDouble.of(songToPP.get(song.uid));
    }

    public double getTotalPP() {
        return getTotalPP(0, 0);
    }

    public double getTotalPP(int overrideSong, double ppOverride) {
        List<Double> ppList = new ArrayList<>(songToPP.size());
        for (int uid : songToPP.keySet()) {
            if (uid == overrideSong) {
                ppList.add(ppOverride);
            } else {
                ppList.add(songToPP.get(uid));
            }
        }
        if (!songToPP.containsKey(overrideSong)) {
            ppList.add(ppOverride);
        }
        Collections.sort(ppList);
        Collections.reverse(ppList);
        double weight = 1.0;
        double ppTotal = 0;
        for (Double pp : ppList) {
            double weightedPP = pp * weight;
            ppTotal += weightedPP;
            weight *= 0.965;
        }
        return ppTotal;
    }
}
