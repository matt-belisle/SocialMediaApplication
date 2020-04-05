USE SocialMedia;

DROP TABLE IF EXISTS `auspol2019Data`;
CREATE TABLE `auspol2019Data` (
  `created_at` text,
  `id` double DEFAULT NULL,
  `full_text` text,
  `retweet_count` decimal(10,0) DEFAULT NULL,
  `favorite_count` decimal(10,0) DEFAULT NULL,
  `user_id` double DEFAULT NULL,
  `user_name` text,
  `user_screen_name` text,
  `user_description` text,
  `user_location` text,
  `user_created_at` text,
  `autoID` INTEGER auto_increment PRIMARY KEY
);

DROP TABLE IF EXISTS HashTags;
CREATE TABLE HashTags (
  hashTag VARCHAR(100) PRIMARY KEY
);

DROP TABLE IF EXISTS User;
CREATE TABLE User (
    id INTEGER auto_increment PRIMARY KEY ,
    userID VARCHAR(32) UNIQUE,
    description VARCHAR(255) DEFAULT '',
    userCreated timestamp

);

DROP TABLE IF EXISTS Tweet;
CREATE TABLE Tweet(
    id INTEGER auto_increment PRIMARY KEY,
    userID INTEGER,
    text TEXT,
    timestamp DATETIME,
    FOREIGN KEY (userID) REFERENCES User(id)
);

DROP TABLE IF EXISTS TweetHashtags;
CREATE TABLE TweetHashtags (
    hashTag VARCHAR(100),
    tweetID INTEGER,
    PRIMARY KEY (hashTag,tweetID),
    FOREIGN KEY (tweetID) REFERENCES Tweet(id),
    FOREIGN KEY (hashTag) REFERENCES HashTags(hashTag)
);

DROP TABLE IF EXISTS TweetMentions;
CREATE TABLE TweetMentions (
    userID INTEGER,
    tweetID INTEGER,
    PRIMARY KEY (userID,tweetID),
    FOREIGN KEY (tweetID) REFERENCES Tweet(id),
    FOREIGN KEY (userID) REFERENCES User(id)
);

DROP TABLE IF EXISTS Favorite;
CREATE TABLE Favorite (
    tweetID INTEGER,
    userID INTEGER,
    PRIMARY KEY (userID, tweetID),
    FOREIGN KEY (tweetID) REFERENCES Tweet(id),
    FOREIGN KEY (userID) REFERENCES User(id)
);

DROP TABLE IF EXISTS Retweet;
CREATE TABLE Retweet (
    tweetID INTEGER,
    userID INTEGER,
    timestamp DATETIME,
    PRIMARY KEY (userID, tweetID),
    FOREIGN KEY (tweetID) REFERENCES Tweet(id),
    FOREIGN KEY (userID) REFERENCES User(id)
);
DROP TABLE IF EXISTS Follows;
CREATE TABLE Follows(
    userIDFollower INTEGER,
    userIDFollowed INTEGER,
    PRIMARY KEY (userIDFollower, userIDFollowed),
    FOREIGN KEY (userIDFollower) REFERENCES User(id),
    FOREIGN KEY (userIDFollowed) REFERENCES User(id)
);

DROP TABLE IF EXISTS Reply;
CREATE TABLE Reply (
    originalID INTEGER,
    replyID INTEGER,
    PRIMARY KEY (originalID, replyID),
    FOREIGN KEY (originalID) REFERENCES Tweet(id),
    FOREIGN KEY (replyID) REFERENCES Tweet(id)
);


DROP TABLE IF EXISTS SubHashtag;
CREATE TABLE SubHashtag (
    parent VARCHAR(100),
    subHashtag VARCHAR(100),
    PRIMARY KEY (parent, subHashtag),
    FOREIGN KEY (parent) REFERENCES HashTags(hashTag),
    FOREIGN KEY (subHashtag) REFERENCES HashTags(hashTag)
);

