import java.sql.*;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


/* many pieces reused from db_app homework template */
public class Database {
    private String url = "REDACTED";	// URL for server
    private String dbName = "REDACTED";			// dbName on the server
    private String username = "REDACTED"; 						// your username on the server
    private String password; 									// your password on the server, set in constructor for privacy
    private Connection connection;


    public Database() {

        password = "REDACTED";
    }

    public void connect() {
        try {
            url = url + dbName + "?";
            connection = DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            System.err.println("Cannot connect!");
            System.err.println(e);
            System.exit(1);
        }
    }


    public void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            System.err.println("Cannot disconnect!");
            System.err.println(e);
            System.exit(1);
        }
    }

    public static String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(password.getBytes("UTF-8"));

        StringBuilder hex = new StringBuilder();
        // get in hexadecimal instead
        for (byte b : hashBytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString(); // always 64 hex characters
    }

    public boolean register(String username, String email, String pass, String birthdate) {
        PreparedStatement stmt;
        String hash;
        String sql;
        LocalDate td = LocalDate.now();
        sql = "SELECT UserEmail FROM User WHERE UserEmail = ? OR Username = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, email);
            stmt.setString(2,username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) { // If email/username already exists in database, return false
                return false;
            }
        } catch (Exception e) {
            System.err.printf("Cannot verify new email.\nActual error: %s\n", e);
        }
        try {
            hash = hashPassword(pass);
        } catch (Exception e) {
            System.err.printf("Cannot hash password.\nActual error: %s\n", e);
            return false;
        }
        sql = "INSERT INTO User (Username, UserEmail, HashedPassword, BirthDate, JoinDate) VALUES (?, ?, ?, ?, ?)";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, email);
            stmt.setString(3, hash);
            stmt.setString(4, birthdate);
            stmt.setString(5, td.toString()); // yyyy-mm-dd
            stmt.execute();
        } catch (Exception e) {
            System.err.printf("Cannot register user.\nActual error: %s\n", e);
            return false;
        }

        return true;
    }

    public User authenticate(String username, String pass) {
        String hash;
        User user = null;

        // Hash password given
        try {
            hash = hashPassword(pass);
        } catch (Exception e) {
            System.err.printf("Cannot hash password.\nActual error: %s\n", e);
            return null;
        }

        String sql = "SELECT * FROM User WHERE UserEmail = ? AND HashedPassword = ?";
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("Username");
                String email = rs.getString("UserEmail");
                int id = rs.getInt("UserID");
                Date birthDate = rs.getDate("BirthDate");
                Date joinDate = rs.getDate("JoinDate");
                String bio = rs.getString("Bio");
                user = new User(name, email, id, birthDate, joinDate, bio);
            }
        } catch (Exception e) {
            System.err.printf("Cannot authenticate user.\nActual error: %s\n", e);
            return null;
        }

        return user;
    }

    public LinkedList<Game> getUserLibrary(int id) {
        PreparedStatement stmt;
        LinkedList<Game> library = new LinkedList<>();
        String sql = "SELECT GameID, Title, Developer, GameDescription, ReleaseDate, AgeRating, Sales FROM Game NATURAL JOIN Played_By WHERE UserID = ? ORDER BY Title DESC";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Game game;
                int gId = rs.getInt("GameID");
                String title = rs.getString("Title");
                String dev = rs.getString("Developer");
                String desc = rs.getString("GameDescription");
                Date releaseDate = rs.getDate("ReleaseDate");
                String ageRating = rs.getString("AgeRating");
                int sales = rs.getInt("Sales");
                game = new Game(gId, title, dev, desc, releaseDate, ageRating, sales);
                library.add(game);
            }
        } catch (Exception e) {
            System.err.printf("Cannot retrieve user library.\nActual error: %s\n", e);
            return null;
        }
        return library;
    }

    public LinkedList<Game> searchGames(String query, int page) {
        PreparedStatement stmt;
        LinkedList<Game> results = new LinkedList<>();
        String sql;
        if (query.isEmpty()) {
            sql = "SELECT * FROM Game ORDER BY Title ASC LIMIT 20 OFFSET ?";
        } else {
            sql = "SELECT * FROM Game WHERE Title LIKE ? ORDER BY Title ASC LIMIT 20 OFFSET ?";
        }
        int offset = 20 * (page - 1);
        try {
            stmt = connection.prepareStatement(sql);
            int i = 1;
            if (!query.isEmpty()) {
                stmt.setString(i, "%" + query + "%");
                i++;
            }
            stmt.setInt(i, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Game game;
                int gId = rs.getInt("GameID");
                String title = rs.getString("Title");
                String dev = rs.getString("Developer");
                String desc = rs.getString("GameDescription");
                Date releaseDate = rs.getDate("ReleaseDate");
                String ageRating = rs.getString("AgeRating");
                int sales = rs.getInt("Sales");
                game = new Game(gId, title, dev, desc, releaseDate, ageRating, sales);
                results.add(game);
            }
        } catch (Exception e) {
            System.err.printf("Could not retrieve search results for games.\nActual error: %s\n", e);
            return null;
        }
        return results;
    }

    public int getGameSearchPageCount(String query) {
        PreparedStatement stmt;
        int count = 0;
        String sql;
        if (query.isEmpty()) {
            sql = "SELECT COUNT(GameID) AS 'count' FROM Game";
        } else {
            sql = "SELECT COUNT(GameID) AS 'count' FROM Game WHERE Title LIKE ?";
        }
        try {
            stmt = connection.prepareStatement(sql);
            if (!query.isEmpty()) {
                stmt.setString(1, "%" + query + "%");
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (Exception e) {
            System.err.printf("Could not retrieve game page count.\nActual error: %s\n", e);
            return 0;
        }
        if (count % 20 == 0) {
            return count/20;
        } else {
            return count/20 + 1;
        }
    }

    public LinkedList<User> searchUsers(String query, int page) {
        PreparedStatement stmt;
        LinkedList<User> results = new LinkedList<>();
        String sql;
        if (query.isEmpty()) {
            sql = "SELECT * FROM User " +
                    "ORDER BY Username ASC " +
                    "LIMIT 20 OFFSET ?";
        } else {
            sql = "SELECT * FROM User " +
                    "WHERE Username LIKE ? " +
                    "ORDER BY Username ASC " +
                    "LIMIT 20 OFFSET ?";
        }
        int offset = 20 * (page - 1);
        try {
            int i = 1;
            stmt = connection.prepareStatement(sql);
            if (!query.isEmpty()) {
                stmt.setString(i, "%" + query + "%");
                i++;
            }
            stmt.setInt(i, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user;
                String username = rs.getString("Username");
                String email = rs.getString("UserEmail");
                int id = rs.getInt("UserID");
                Date birthDate = rs.getDate("BirthDate");
                Date joinDate = rs.getDate("JoinDate");
                String bio = rs.getString("Bio");
                user = new User(username, email, id, birthDate, joinDate, bio);
                results.add(user);
            }
        } catch (Exception e) {
            System.err.printf("Could not retrieve search results for games.\nActual error: %s\n", e);
            return null;
        }
        return results;
    }

    public int getUserSearchPageCount(String query) {
        PreparedStatement stmt;
        int count = 0;
        String sql;
        if (query.isEmpty()) {
            sql = "SELECT COUNT(*) AS 'count' FROM User";
        } else {
            sql = "SELECT COUNT(*) AS 'count' FROM User WHERE Username LIKE ?";
        }
        try {
            stmt = connection.prepareStatement(sql);
            if (!query.isEmpty()) {
                stmt.setString(1, "%" + query + "%");
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (Exception e) {
            System.err.printf("Could not retrieve user page count.\nActual error: %s\n", e);
            return 0;
        }
        if (count % 20 == 0) {
            return count/20;
        } else {
            return count/20 + 1;
        }
    }

    public double getAvgRating(int gameID) {
        PreparedStatement stmt;
        double avgRating = 0;
        String sql;
        sql = "SELECT AVG(Rating) AS 'avgRating' FROM Review NATURAL JOIN Game_Review WHERE GameID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                avgRating = rs.getDouble("avgRating");
            }
        } catch (Exception e) {
            System.err.printf("Could not get average rating.\nActual error: %s\n", e);
            return 0;
        }
        return avgRating / 2.0;
    }

    public int getGameReviewCount(int gameID) {
        PreparedStatement stmt;
        int count = 0;
        String sql;
        sql = "SELECT COUNT(ReviewID) AS 'reviewCount' FROM Game_Review WHERE GameID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("reviewCount");
            }
        } catch (Exception e) {
            System.err.printf("Could not get review count.\nActual error: %s\n", e);
            return 0;
        }
        return count;
    }

    public int getUserReviewCount(int userID) {
        PreparedStatement stmt;
        int count = 0;
        String sql;
        sql = "SELECT COUNT(ReviewID) AS 'reviewCount' FROM User_Review WHERE UserID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("reviewCount");
            }
        } catch (Exception e) {
            System.err.printf("Could not get review count.\nActual error: %s\n", e);
            return 0;
        }
        return count;
    }

    public boolean userOwnsGame(int userID, int gameID) {
        PreparedStatement stmt;
        String sql;
        sql = "SELECT * FROM Played_By WHERE UserID = ? AND GameID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            System.err.printf("Could not validate user owns game.\nActual error: %s\n", e);
            return false;
        }
        return false;
    }

    public void addGameToLibrary(int userID, int gameID) {
        PreparedStatement stmt;
        String sql;
        sql = "INSERT INTO Played_By (UserID, GameID) VALUES (?, ?)";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, gameID);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.printf("Could not add game to library.\nActual error: %s\n", e);
        }
    }

    public void logHoursPlayed(int userID, int gameID, int hours) {
        PreparedStatement stmt;
        String sql;
        sql = "UPDATE Played_By SET Hours = ? WHERE UserID = ? AND GameID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, hours);
            stmt.setInt(2, userID);
            stmt.setInt(3, gameID);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.printf("Could not update hours played.\nActual error: %s\n", e);
        }
    }

    public void updateGameInfo(int gameID, String newTitle, String newDescription) {
        PreparedStatement stmt;
        String sql;
        sql = "UPDATE Game SET Title = ?, GameDescription = ? WHERE GameID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, newTitle);
            stmt.setString(2, newDescription);
            stmt.setInt(3, gameID);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.printf("Could not update game info.\nActual error: %s\n", e);
        }
    }

    public void updateUserInfo(int userID, String newUsername, String newEmail, String newBio) {
        PreparedStatement stmt;
        String sql;
        sql = "UPDATE User SET Username = ?, UserEmail = ?, Bio = ? WHERE UserID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, newUsername);
            stmt.setString(2, newEmail);
            stmt.setString(3, newBio);
            stmt.setInt(4, userID);
            stmt.executeUpdate();
        } catch (Exception e) {
            System.err.printf("Could not update user info.\nActual error: %s\n", e);
        }
    }

    public boolean userIsCreator(User user, Game game) {
        int uID = user.getUserID();
        int gID = game.getGameID();
        PreparedStatement stmt;
        String sql;
        sql = "SELECT * FROM Created_By WHERE UserID = ? AND GameID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, uID);
            stmt.setInt(2, gID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (Exception e) {
            System.err.printf("Could not validate user as creator.\nActual error: %s\n", e);
            return false;
        }
        return false;
    }

    /** Inserts review into Review table, Game_Review Table, and User_Review Table
     *  Review object to be created in MainFrame.java, and its ReviewDate does NOT need to be set,
     *  will be ignored and replaced with today's date
     */
    public boolean createReview(int userID, int gameID, Review review) {
        PreparedStatement stmt;
        String sql;
        int reviewID;
        ResultSet rs;

        try {
            connection.setAutoCommit(false);
        } catch (Exception e) {
            System.err.printf("Could not start transaction.\nActual error: %s\n", e);
            return false;
        }

        sql = "INSERT INTO Review (ReviewText, Rating, ReviewDate) VALUES (?, ?, ?);";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, review.getReviewText());
            stmt.setInt(2, review.getRating());
            stmt.setString(3, LocalDate.now().toString());
            stmt.executeUpdate(); // INSERT INTO Review
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }

        sql = "SELECT LAST_INSERT_ID();";
        try {
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery(); // SELECT LAST_INSERT_ID()
            if (rs.next()) {
                reviewID = rs.getInt("LAST_INSERT_ID()");
            } else {
                throw new SQLException("Could not get Review ID.");
            }
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }

        sql = "INSERT INTO Game_Review (GameID, ReviewID) VALUES (?, ?);";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, gameID);
            stmt.setInt(2, reviewID);
            stmt.executeUpdate();
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }

        sql = "INSERT INTO User_Review (UserID, ReviewID) VALUES (?, ?);";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, reviewID);
            stmt.executeUpdate();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }


        return true;
    }

    public LinkedList<Review> getGameReviews(int gameID) {
        PreparedStatement stmt;
        String sql;
        LinkedList<Review> reviews = new LinkedList<>();
        sql = "SELECT * FROM Review " +
                "NATURAL JOIN User_Review " +
                "NATURAL JOIN Game_Review " +
                "NATURAL JOIN User " +
                "NATURAL JOIN Game WHERE GameID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, gameID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                //Create User object
                String username = rs.getString("Username");
                String email = rs.getString("UserEmail");
                int uID = rs.getInt("UserID");
                Date birthDate = rs.getDate("BirthDate");
                Date joinDate = rs.getDate("JoinDate");
                String bio = rs.getString("Bio");
                User user = new User(username, email, uID, birthDate, joinDate, bio);
                //Create Game object
                int gID = rs.getInt("GameID");
                String title = rs.getString("Title");
                String dev = rs.getString("Developer");
                String desc = rs.getString("GameDescription");
                Date releaseDate = rs.getDate("ReleaseDate");
                String ageRating = rs.getString("AgeRating");
                int sales = rs.getInt("Sales");
                Game game = new Game(gID, title, dev, desc, releaseDate, ageRating, sales);
                //Create Review object
                int revID = rs.getInt("ReviewID");
                String revText = rs.getString("ReviewText");
                byte Rating = (byte)rs.getInt("Rating");
                String revDate = rs.getString("ReviewDate");
                reviews.add(new Review(user, game, revID, revText, Rating, revDate));
            }
        } catch (Exception e) {
            System.err.printf("Could not get game reviews.\nActual error: %s\n", e);
            return null;
        }
        return reviews;
    }

    public LinkedList<Review> getUserReviews(int userID) {
        PreparedStatement stmt;
        String sql;
        LinkedList<Review> reviews = new LinkedList<>();
        sql = "SELECT * FROM Review " +
                "NATURAL JOIN User_Review " +
                "NATURAL JOIN Game_Review " +
                "NATURAL JOIN User " +
                "NATURAL JOIN Game WHERE UserID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                //Create User object
                String username = rs.getString("Username");
                String email = rs.getString("UserEmail");
                int uID = rs.getInt("UserID");
                Date birthDate = rs.getDate("BirthDate");
                Date joinDate = rs.getDate("JoinDate");
                String bio = rs.getString("Bio");
                User user = new User(username, email, uID, birthDate, joinDate, bio);
                //Create Game object
                int gID = rs.getInt("GameID");
                String title = rs.getString("Title");
                String dev = rs.getString("Developer");
                String desc = rs.getString("GameDescription");
                Date releaseDate = rs.getDate("ReleaseDate");
                String ageRating = rs.getString("AgeRating");
                int sales = rs.getInt("Sales");
                Game game = new Game(gID, title, dev, desc, releaseDate, ageRating, sales);
                //Create Review object
                int revID = rs.getInt("ReviewID");
                String revText = rs.getString("ReviewText");
                byte Rating = (byte)rs.getInt("Rating");
                String revDate = rs.getString("ReviewDate");
                reviews.add(new Review(user, game, revID, revText, Rating, revDate));
            }
        } catch (Exception e) {
            System.err.printf("Could not get game reviews.\nActual error: %s\n", e);
            return null;
        }
        return reviews;
    }

    public int getHoursPlayed(int userID, int gameID) {
        PreparedStatement stmt;
        int hours = 0;
        String sql;
        sql = "SELECT Hours FROM Played_By WHERE UserID = ? AND GameID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userID);
            stmt.setInt(2, gameID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                hours = rs.getInt("Hours");
            }
        } catch (Exception e) {
            System.err.printf("Could not get hours played.\nActual error: %s\n", e);
            return -1;
        }
        return hours;
    }

    /**
     * Filtered game search.
     * @param query       title LIKE match — empty string matches all
     * @param page        1-based page number (20 results per page)
     * @param ageRatings  exact AgeRating values to include, e.g. ["E","T","M"]
     *                    — empty list skips this filter
     */
    public LinkedList<Game> searchGames(String query, int page, List<String> ageRatings) {
        PreparedStatement stmt;
        LinkedList<Game> results = new LinkedList<>();
        String inner = String.join(",", Collections.nCopies(ageRatings.size(), "?"));
        String sql;

        boolean isSearched = query.isEmpty();

        if (isSearched) {
            sql = "SELECT * FROM Game " +
                    "WHERE Title LIKE ? " +
                    "AND AgeRating IN (" +
                    inner +
                    ") ORDER BY Title ASC LIMIT 20 OFFSET ?";
        } else {
            sql = "SELECT * FROM Game " +
                    "WHERE AgeRating IN (" +
                    inner +
                    ") ORDER BY Title ASC LIMIT 20 OFFSET ?";
        }
        int offset = 20 * (page - 1);
        try {
            int i;
            stmt = connection.prepareStatement(sql);
            if (isSearched) {
                stmt.setString(1, "%" + query + "%");
                for (i = 0; i < ageRatings.size(); i++) {
                    // i + 2 because of the ? starting at one, going up through size of ageRatings
                        // with the search query being index 1
                    stmt.setString(i + 2, ageRatings.get(i));
                }
                stmt.setInt(i + 2, offset);

            } else {
                for (i = 0; i < ageRatings.size(); i++) {
                    // i + 1 because of the ? starting at one, going up through size of ageRatings
                        // with no search query at index 1
                    stmt.setString(i + 1, ageRatings.get(i));
                }
                stmt.setInt(i + 1, offset);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Game game;
                int gId = rs.getInt("GameID");
                String title = rs.getString("Title");
                String dev = rs.getString("Developer");
                String desc = rs.getString("GameDescription");
                Date releaseDate = rs.getDate("ReleaseDate");
                String ageRating = rs.getString("AgeRating");
                int sales = rs.getInt("Sales");
                game = new Game(gId, title, dev, desc, releaseDate, ageRating, sales);
                results.add(game);
            }
        } catch (Exception e) {
            System.err.printf("Could not get search results.\nActual error: %s\n", e);
            return null;
        }

        return results;
    }
    /**
     * @param query       title LIKE match — empty string matches all
     * @param page        1-based page number (20 results per page)
     * @param developer   developer LIKE match, list for set of developers
     */
    public LinkedList<Game> searchGames(String query, int page, String developer) {
        LinkedList<Game> results = new LinkedList<>();
        PreparedStatement stmt;
        developer = developer.trim();
        if (developer.charAt(developer.length()-1) == ',') {
            developer = developer.substring(0,developer.length()-1);
        }
        String[] devs = developer.split(",");
        String in = "";
        boolean hasQuery = !query.isEmpty();
        int offset = 20 * (page - 1);
        for (int i = 0; i < devs.length; i++) {
            if (i == devs.length - 1) {
                in = in.concat("?");
            } else {
                in = in.concat("?, ");
            }
        }
        String sql;
        if (!hasQuery) {
            sql = "SELECT * FROM Game WHERE Developer IN (" + in + ") ORDER BY Title ASC LIMIT 20 OFFSET ?";
        } else {
            sql = "SELECT * FROM Game WHERE Title LIKE ? AND Developer IN (" + in + ") ORDER BY Title ASC LIMIT 20 OFFSET ?";
        }
        try {
            stmt = connection.prepareStatement(sql);
            int i;
            if (hasQuery) {
                i = 2;
                stmt.setString(1, "%" + query + "%");
            } else {
                i = 1;
            }
            for (String dev : devs) {
                stmt.setString(i, dev);
                i++;
            }
            stmt.setInt(i, offset);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Game game;
                int gId = rs.getInt("GameID");
                String title = rs.getString("Title");
                String dev = rs.getString("Developer");
                String desc = rs.getString("GameDescription");
                Date releaseDate = rs.getDate("ReleaseDate");
                String ageRating = rs.getString("AgeRating");
                int sales = rs.getInt("Sales");
                game = new Game(gId, title, dev, desc, releaseDate, ageRating, sales);
                results.add(game);
            }
        } catch (Exception e) {
            System.err.printf("Could not filter by developers.\nActual error: %s\n", e);
            return null;
        }

        return results;
    }

    /**
     * Total page count for the same filtered query (needed for pagination).
     * Uses identical WHERE conditions; page size is always 20.
     */
    public int getGameSearchPageCount(String query, List<String> ageRatings) {
        PreparedStatement stmt;
        int count = 0;
        String sql;

        String inner = String.join(",", Collections.nCopies(ageRatings.size(), "?"));

        if (query.isEmpty()) {
            sql = "SELECT COUNT(GameID) AS 'count' FROM Game " +
                    "WHERE AgeRating IN" +
                    "(" + inner + ")";
        } else {
            sql = "SELECT COUNT(GameID) AS 'count' FROM Game WHERE Title LIKE ? " +
                    "AND AgeRating IN" +
                    "(" + inner + ")";
        }
        try {
            stmt = connection.prepareStatement(sql);
            int i = 1;
            if (!query.isEmpty()) {
                stmt.setString(1, "%" + query + "%");
                i++;
            }
            for (String ageRating : ageRatings) {
                stmt.setString(i, ageRating);
                i++;
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (Exception e) {
            System.err.printf("Could not retrieve game page count.\nActual error: %s\n", e);
            return 0;
        }
        if (count % 20 == 0) {
            return count/20;
        } else {
            return count/20 + 1;
        }

    }

    public int getGameSearchPageCount(String query, String developer) {
        PreparedStatement stmt;
        int count = 0;
        String[] devs = developer.split(",");
        String in = "";
        boolean hasQuery = !query.isEmpty();
        for (int i = 0; i < devs.length; i++) {
            if (i == devs.length - 1) {
                in = in.concat("?");
            } else {
                in = in.concat("?, ");
            }
        }
        String sql;
        if (!hasQuery) {
            sql = "SELECT COUNT(GameID) AS 'count' FROM Game WHERE Developer IN (" + in + ")";
        } else {
            sql = "SELECT COUNT(GameID) AS 'count' FROM Game WHERE Title LIKE ? AND Developer IN (" + in + ")";
        }
        try {
            stmt = connection.prepareStatement(sql);
            int i;
            if (hasQuery) {
                i = 2;
                stmt.setString(1, "%" + query + "%");
            } else {
                i = 1;
            }
            for (String dev : devs) {
                stmt.setString(i, dev);
                i++;
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (Exception e) {
            System.err.printf("Could not get game page count.\nActual error: %s\n", e);
            return 0;
        }

        if (count % 20 == 0) {
            return count / 20;
        } else {
            return count / 20 + 1;
        }
    }

    /**
     * Creates a game, based on userID of who created it (to be inserted into Created_By)
     *      and inserts the data into all applicable tables.
     *  Game object must be filled out with proper values before createGame is called.
     *      GameID will be ignored because it is not inserted yet.
     *
     */
    public boolean createGame(User user, Game game) {
        PreparedStatement stmt;
        String sql;
        int gameID;
        ResultSet rs;

        try {
            connection.setAutoCommit(false);
        } catch (Exception e) {
            System.err.printf("Could not start transaction.\nActual error: %s\n", e);
            return false;
        }

        sql = "INSERT INTO Game (Title, Developer, GameDescription, ReleaseDate, AgeRating, Sales) VALUES (?, ?, ?, ?, ?, ?);";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, game.getTitle());
            stmt.setString(2, game.getDeveloper());
            stmt.setString(3, game.getGameDescription());
            stmt.setDate(4, game.getReleaseDate());
            stmt.setString(5, game.getAgeRating());
            stmt.setInt(6, game.getSales());
            stmt.executeUpdate(); // INSERT INTO Game
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }

        sql = "SELECT LAST_INSERT_ID();";
        try {
            stmt = connection.prepareStatement(sql);
            rs = stmt.executeQuery(); // SELECT LAST_INSERT_ID()
            if (rs.next()) {
                gameID = rs.getInt("LAST_INSERT_ID()");
            } else {
                throw new SQLException("Could not get Game ID.");
            }
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }

        sql = "INSERT INTO Created_By (GameID, UserID) VALUES (?, ?);";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, gameID);
            stmt.setInt(2, user.getUserID());
            stmt.executeUpdate();
            connection.setAutoCommit(true);
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }

        return true;

    }

    public LinkedList<String> searchDevelopers(String query) {
        PreparedStatement stmt;
        LinkedList<String> devs = new LinkedList<>();
        boolean hasQuery = !query.isEmpty();
        String sql;
        if (hasQuery) {
            sql = "SELECT Developer FROM Game GROUP BY Developer HAVING Developer LIKE ? ORDER BY Developer ASC";
        } else {
            sql = "SELECT Developer FROM Game GROUP BY Developer ORDER BY Developer ASC";
        }

        try {
            stmt = connection.prepareStatement(sql);
            if (hasQuery) {
                stmt.setString(1, "%" + query + "%");
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                devs.add(rs.getString("Developer"));
            }
        } catch (Exception e) {
            System.err.printf("Could not get list of developers.\nActual error: %s\n", e);
            return null;
        }
        return devs;
    }

    public boolean deleteReview(int reviewID) {
        //user_review & game_review first, then review
        PreparedStatement stmt;
        String sql;

        try {
            connection.setAutoCommit(false);
        } catch (Exception e) {
            System.err.printf("Could not start transaction.\nActual error: %s\n", e);
            return false;
        }

        sql = "DELETE FROM User_Review WHERE ReviewID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, reviewID);
            stmt.executeUpdate(); // DELETE FROM User_Review
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }

        sql = "DELETE FROM Game_Review WHERE ReviewID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, reviewID);
            stmt.executeUpdate(); // DELETE FROM Game_Review
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }

        sql = "DELETE FROM Review WHERE ReviewID = ?";
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, reviewID);
            stmt.executeUpdate(); // DELETE FROM Review
            connection.setAutoCommit(true);
        } catch (Exception e) {
            try {
                System.err.printf("Rolling back. Error: %s", e);
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (Exception e2) {
                System.err.printf("IMPORTANT: Transaction cannot rollback.\nActual error: %s\n %s\n", e, e2);
            }
            return false;
        }


        return true;
    }

}
