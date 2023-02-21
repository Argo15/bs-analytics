package leaderboard;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeaderboardScoresPage {
    public LeaderboardScore[] scores;
    public Metadata metadata;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        public int total;
        public int page;
        public int itemsPerPage;
    }
}
