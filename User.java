import java.sql.Date;

public class User {
    private String Username;
    private String UserEmail;
    private int UserID;
    private Date BirthDate;
    private Date JoinDate;
    private String Bio;

    public User() {};

    public User(String user, String email, int id, Date birthDate, Date joinDate, String bio) {
        Username = user;
        UserEmail = email;
        UserID = id;
        BirthDate = birthDate;
        JoinDate = joinDate;
        Bio = bio;
    }

    public String getUsername() {
        return Username;
    }

    public String getUserEmail() {
        return UserEmail;
    }

    public int getUserID() {
        return UserID;
    }

    public Date getBirthDate() {
        return BirthDate;
    }

    public Date getJoinDate() {
        return JoinDate;
    }

    public String getBio() {
        return Bio;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public void setUserEmail(String userEmail) {
        UserEmail = userEmail;
    }

    public void setBio(String bio) {
        Bio = bio;
    }
}
