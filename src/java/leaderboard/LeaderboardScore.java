package leaderboard;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeaderboardScore {
    public int id;
    public PlayerInfo leaderboardPlayerInfo;
    public int rank;
    public int baseScore;
    public double modifiedScore;
    public double pp;
    public double weight;
    public String modifiers;
    public int multiplier;
    public int badCuts;
    public int missedNotes;
    public int maxCombo;
    public boolean fullCombo;
    public int hmd;
    public String timeSet;
    public boolean hasReplay;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlayerInfo {
        public String id;
        public String name;
        public String profilePicture;
        public String country;
        public int permissions;
        public String badges;
        public String role;
    }
}
