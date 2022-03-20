package leaderboard;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Leaderboards {
    public LeaderboardInfo[] leaderboards;
    public Metadata metadata;

    public static class Metadata {
        public int total;
        public int page;
        public int itemsPerPage;
    }
}
