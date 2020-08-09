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
}