package leaderboard;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeaderboardInfo {
    public int id;
    public String songHash;
    public String songName;
    public String songSubName;
    public String songAuthorName;
    public String levelAuthorName;
    public Difficulty difficulty;
    public int maxScore;
    public String createdDate;
    public String rankedDate;
    public String qualifiedDate;
    public String lovedDate;
    public boolean ranked;
    public boolean qualified;
    public boolean loved;
    public double maxPP;
    public double stars;
    public int plays;
    public int dailyPlays;
    public boolean positiveModifiers;
    //  public Score playerScore; TODO - not showing up until my profile becomes active again
    public String coverImage;
    public Difficulty[] difficulties;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Difficulty {
        public int leaderboardId;
        public int difficulty;
        public String gameMode;
        public String difficultyRaw;
    }

}
