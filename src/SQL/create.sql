CREATE TABLE IF NOT EXISTS USER(
	ID BIGINT PRIMARY KEY, 
	SN VARCHAR(255),
	FULL_NAME VARCHAR(255),
	LOCATION VARCHAR(255),
	DESCRIPTION VARCHAR(255),
	URL VARCHAR(255),
	FOLLOWER_COUNT INT,
	FRIEND_COUNT INT,
	STATUS_COUNT INT,
	DATE_CREATED TIMESTAMP,
	CONTRIBUTOR_ENABLED BOOLEAN,
	PROFILE_IMG_URL VARCHAR(255),
	PROTECTED BOOLEAN,
	LANGUAGE VARCHAR(255),
	GEO BOOLEAN,
	VERIFIED BOOLEAN,
	LISTED_COUNT INT
);

CREATE TABLE IF NOT EXISTS TWEETS(
	CREATED TIMESTAMP,
	ID BIGINT PRIMARY KEY, 
	TEXT VARCHAR(255),
	SOURCE VARCHAR(255),
	IS_TRUNCATED BOOLEAN,
	REPLY_TO BIGINT,
	REPLY_USER BIGINT,
	FAVORITED BOOLEAN,
	REPLY_USER_SN VARCHAR(255),
	GEOLOCATION VARCHAR(255),
	PLACE VARCHAR(255),
	RETWEET_COUNT BIGINT,
	RETWEETED_BY_ME BOOLEAN,
	CONTRIBUTORS VARCHAR(255),
	ANNOTATIONS VARCHAR(255),
	RETWEETED_STATUS BIGINT,
	USER_MENTIONS VARCHAR(255),
	URLS VARCHAR(255),
	HASHTAGS VARCHAR(255),
	USERS BIGINT,
	USER_SN VARCHAR(255),
);

/* Hashtag Table */
CREATE TABLE IF NOT EXISTS HASHTAG(
	SEARCH_TAG VARCHAR(255),
	SIMILAR_TAGS VARCHAR(255),
	ID BIGINT PRIMARY KEY REFERENCES TWEETS(ID), 
	
);

/* Hashtags Table */
CREATE TABLE IF NOT EXISTS HASHTAGS(
	SEARCH_TAG VARCHAR(255) PRIMARY KEY,
	DATE VARCHAR(255)
);

/* Schedule Table */
CREATE TABLE IF NOT EXISTS SCHEDULE(
	FOLLOW_ID BIGINT,
	SEARCH_STRING VARCHAR(255),
	IS_USER BOOLEAN,
	IS_KEYWORD BOOLEAN
)