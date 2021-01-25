package songs;

public class MapInfo {
    public MetaData metadata;
    public Stats stats;
    public String description;
    public String deletedAt;
    public String _id;
    public String key;
    public String name;
    public Uploader uploader;
    public String hash;
    public String uploaded;
    public String directDownload;
    public String downloadURL;
    public String coverURL;

    private static class MetaData {
        public Difficulties difficulties;
        public int duration;
        public String automapper;
        public Characteristics[] characteristics;
        public String levelAuthorName;
        public String songAuthorName;
        public String songName;
        public String songSubName;
        public int bpm;

        private static class Difficulties {
            public boolean easy;
            public boolean normal;
            public boolean hard;
            public boolean expert;
            public boolean expertPlus;
        }

        private static class Characteristics {
            public String name;
            public Difficulties difficulties;

            private static class Difficulties {
                public Characteristic easy;
                public Characteristic normal;
                public Characteristic hard;
                public Characteristic expert;
                public Characteristic expertPlus;

                private static class Characteristic {
                    public double duration;
                    public int length;
                    public double njs;
                    public double njsOffset;
                    public int bombs;
                    public int notes;
                    public int obstacles;
                }
            }
        }
    }

    private static class Stats {
        public int downloads;
        public int plays;
        public int downVotes;
        public int upVotes;
        public double heat;
        public double rating;
    }

    private static class Uploader {
        public String _id;
        public String username;
    }
}
