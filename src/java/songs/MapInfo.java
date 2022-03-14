package songs;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MapInfo {
    public String id;
    public String name;
    public String description;
    public Uploader uploader;
    public MetaData metadata;
    public Stats stats;
    public String uploaded;
    public boolean automapper;
    public boolean ranked;
    public boolean qualified;
    public Version[] versions;
    public String createdAt;
    public String updatedAt;
    public String lastPublishedAt;

    public static class Version {
        public String hash;
        public String key;
        public String state;
        public String createdAt;
        public int sageScore;
        public Difficulties[] diffs;
        public String downloadURL;
        public String coverURL;
        public String previewURL;

        public static class Difficulties {
            public int njs;
            public double offset;
            public int notes;
            public int bombs;
            public int obstacles;
            public double nps;
            public double length;
            public String characteristic;
            public String difficulty;
            public int events;
            public boolean chroma;
            public boolean me;
            public boolean ne;
            public boolean cinema;
            public double seconds;
            public ParitySummary paritySummary;
            public double stars;

            public static class ParitySummary {
                public int errors;
                public int warns;
                public int resets;
            }
        }
    }

    public static class Uploader {
        public String id;
        public String name;
        public boolean uniqueSet;
        public String hash;
        public String avatar;
        public String type;
    }

    public static class MetaData {
        public int duration;
        public String automapper;
        public String levelAuthorName;
        public String songAuthorName;
        public String songName;
        public String songSubName;
        public int bpm;
    }

    public static class Stats {
        public int downloads;
        public int plays;
        public int downvotes;
        public int upvotes;
        public double score;
    }
}
