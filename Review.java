public class Review {
    private User user;
    private Game game;
    private int ReviewID;
    private String ReviewText;
    private byte Rating;
    private String ReviewDate;

    public Review() {};

    public Review(String ReviewText, byte Rating, String ReviewDate) {
        this.ReviewText = ReviewText;
        this.Rating = Rating;
        this.ReviewDate = ReviewDate;
    }

    public Review(User user, Game game, int ReviewID, String ReviewText, byte Rating, String ReviewDate) {
        this.user = user;
        this.game = game;
        this.ReviewID = ReviewID;
        this.ReviewText = ReviewText;
        this.Rating = Rating;
        this.ReviewDate = ReviewDate;
    }

    public int getReviewID() {
        return ReviewID;
    }

    public String getReviewText() {
        return ReviewText;
    }

    public byte getRating() {
        return Rating;
    }

    public String getReviewDate() {
        return ReviewDate;
    }

    public User getUser() { return user; }

    public Game getGame() { return game; }

}
