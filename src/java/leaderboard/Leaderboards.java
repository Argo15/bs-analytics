package leaderboard;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Leaderboards {
    public LeaderboardInfo[] leaderboards;
    public Metadata metadata;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Metadata {
        public int total;
        public int page;
        public int itemsPerPage;
    }
}
