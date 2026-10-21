import java.sql.*;
import java.security.MessageDigest;
import java.time.LocalDate;


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
            //shut 'er down
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

    public String authenticate(String username, String pass) {
        String hash;
        String user = null;

        // Hash password given
        try {
            hash = hashPassword(pass);
        } catch (Exception e) {
            System.err.printf("Cannot hash password.\nActual error: %s\n", e);
            return null;
        }

        String sql = "SELECT Username FROM User WHERE UserEmail = ? AND HashedPassword = ?";
        PreparedStatement stmt;
        try {
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hash);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                user = rs.getString("Username");
            }
        } catch (Exception e) {
            System.err.printf("Cannot authenticate user.\nActual error: %s\n", e);
            return null;
        }

        return user;
    }


}
