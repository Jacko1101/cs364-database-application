import java.sql.Date;

public class Game {
    private int GameID;
    private String Title;
    private String Developer;
    private String GameDescription;
    private Date ReleaseDate;
    private String AgeRating;
    private int Sales;

    public Game() {};

    public Game(int id, String title, String dev, String desc, Date releaseDate, String ageRating, int sales) {
        GameID = id;
        Title = title;
        Developer = dev;
        GameDescription = desc;
        ReleaseDate = releaseDate;
        AgeRating = ageRating;
        Sales = sales;
    }

    public Game(String title, String dev, String desc, Date releaseDate, String ageRating, int sales) {
        Title = title;
        Developer = dev;
        GameDescription = desc;
        ReleaseDate = releaseDate;
        AgeRating = ageRating;
        Sales = sales;
    }

    public int getGameID() {
        return GameID;
    }

    public String getTitle() {
        return Title;
    }

    public String getDeveloper() {
        return Developer;
    }

    public String getGameDescription() {
        return GameDescription;
    }

    public Date getReleaseDate() {
        return ReleaseDate;
    }

    public String getAgeRating() {
        return AgeRating;
    }

    public int getSales() {
        return Sales;
    }
}
