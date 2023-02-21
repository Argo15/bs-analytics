package songs;

public class Song {
    public int uid;
    public String id;
    public String name;
    public String songSubName;
    public String songAuthorName;
    public String levelAuthorName;
    public int bpm;
    public String diff;
    public String scores;
    public int scores_day;
    public int ranked;
    public double stars;
    public String image;

    public int scores()
    {
        return Integer.parseInt(scores.replaceAll(",", ""));
    }

    public int difficulty() {
        if (diff.startsWith("_ExpertPlus_")) return 9;
        if (diff.startsWith("_Expert_")) return 7;
        if (diff.startsWith("_Hard_")) return 5;
        if (diff.startsWith("_Normal_")) return 3;
        if (diff.startsWith("_Easy_")) return 1;
        throw new RuntimeException("Uknown difficulty: " + diff);
    }

    public String hash() {
        return id + "_" + difficulty();
    }
}