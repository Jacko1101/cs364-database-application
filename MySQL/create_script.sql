USE schutt1037_GameLibrary;

CREATE TABLE User (
	Username varchar(32) NOT NULL UNIQUE,
    UserEmail varchar(64) NOT NULL UNIQUE,
    HashedPassword varchar(64) NOT NULL,
    UserID int unsigned auto_increment PRIMARY KEY,
    BirthDate date,
    JoinDate date,
    Bio varchar(256)
);

CREATE TABLE Game (
	GameID int unsigned auto_increment PRIMARY KEY,
    Title varchar(64) NOT NULL,
    Developer varchar(64) NOT NULL,
    GameDescription varchar(1028),
    ReleaseDate date NOT NULL,
    AgeRating varchar(16),
    Sales int
);

CREATE TABLE Review (
	ReviewID int unsigned auto_increment PRIMARY KEY,
    ReviewText varchar(1028),
    Rating tinyint NOT NULL,
    ReviewDate date NOT NULL
);

CREATE TABLE Played_By (
	GameID int unsigned,
    UserID int unsigned,
    Hours mediumint unsigned,
    FOREIGN KEY (GameID) REFERENCES Game(GameID),
    FOREIGN KEY (UserID) REFERENCES User(UserID),
    CONSTRAINT PlayID PRIMARY KEY (GameID, UserID)
);

CREATE TABLE Created_By (
	GameID int unsigned,
    UserID int unsigned,
    FOREIGN KEY (GameID) REFERENCES Game(GameID),
    FOREIGN KEY (UserID) REFERENCES User(UserID),
    CONSTRAINT CreateID PRIMARY KEY (GameID, UserID)
);

CREATE TABLE User_Review (
	UserID int unsigned,
    ReviewID int unsigned,
    FOREIGN KEY (UserID) REFERENCES User(UserID),
    FOREIGN KEY (ReviewID) REFERENCES Review(ReviewID),
    CONSTRAINT URID PRIMARY KEY (UserID, ReviewID)
);

CREATE TABLE Game_Review (
	GameID int unsigned,
    ReviewID int unsigned,
    FOREIGN KEY (GameID) REFERENCES Game(GameID),
    FOREIGN KEY (ReviewID) REFERENCES Review(ReviewID),
    CONSTRAINT GRID PRIMARY KEY (GameID, ReviewID)
);