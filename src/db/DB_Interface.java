package db;

import authentication.OAuth;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import org.h2.jdbc.JdbcSQLException;
import twitter4j.*;
import twitterFeed.PublicTimeline;

/**
 * This class contains methods that handle the conversion of Twitter data to
 * data entities for the database. Data retrieved from Twitter must be separated
 * into columns then built as a SQL query.
 * 
 * @author William Gaul, Baohuy Ung
 * @version 1.0
 */
public class DB_Interface {

    //constants below are ordered by columns appearing in the DB
    // Constants for a status
    final static int CREATED = 1;
    final static int ID = 2;
    final static int TEXT = 3;
    final static int SOURCE = 4;
    final static int IS_TRUNCATED = 5;
    final static int REPLY_TO = 6;
    final static int REPLY_USER = 7;
    final static int FAVORITED = 8;
    final static int REPLY_USER_SN = 9;
    final static int GEOLOCATION = 10;
    final static int PLACE = 11;
    final static int RETWEET_COUNT = 12;
    final static int RETWEETED_BY_ME = 13;
    final static int CONTRIBUTORS = 14;
    final static int ANNOTATIONS = 15;
    final static int RETWEETED_STATUS = 16;
    final static int USER_MENTIONS = 17;
    final static int URLS = 18;
    final static int HASHTAGS = 19;
    final static int USER = 20;
    final static int USERSN = 21;
    // Constants for a user
    final static int USER_ID = 1;
    final static int USER_SN = 2;
    final static int USER_FN = 3;
    final static int USER_LOCATION = 4;
    final static int USER_DESCRIPTION = 5;
    final static int USER_URL = 6;
    final static int USER_FOLLOWER_COUNT = 7;
    final static int USER_FRIEND_COUNT = 8;
    final static int USER_STATUS_COUNT = 9;
    final static int USER_DATE_CREATED = 10;
    final static int USER_CONTRIBUTOR_ENABLED = 11;
    final static int USER_PROFILE_IMG_URL = 12;
    final static int USER_PROTECTED = 13;
    final static int USER_LANGUAGE = 14;
    final static int USER_GEO = 15;
    final static int USER_VERIFIED = 16;
    final static int USER_LISTED_COUNT = 17;
    // Constants for a Hashtag
    final static int HASH_SEARCH_TAG = 1;
    final static int HASH_DATE = 2;

    /**
     * Prepares an sql statement to insert a new user entity.
     *
     * @param conn connection to the DB passed from DB_Connect
     * @param u user class retrieved from the twitter factory
     * @throws SQLException thrown when there is an error in the sql query
     */
    public static void insertUser(Connection conn, User u) throws SQLException {
        //prep the sql statement, each ? is a columnID
        PreparedStatement prep = conn.prepareStatement("INSERT INTO USER VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        //this extracts the data from a user instance and structures it into an DB entity
        userBuilder(u, prep);
        try {
            prep.execute();
        } catch (Exception e) {
            System.out.println("User already in database");
        }
    }

    /**
     * Inserts a new status into the DB. This is for public timelines that are
     * scraped from twitter.
     *
     * @param conn connection to the DB
     * @param pt instance of a public timeline
     * @throws SQLException thrown when an error occurs in the sql statement
     */
    public static void insertTimeline(Connection conn, PublicTimeline pt)
            throws SQLException {
        //prep sql statement
        PreparedStatement prep = conn.prepareStatement("INSERT INTO TWEETS VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        //timeline is a list of statuses
        for (int i = 0; i < pt.getTimeline().size(); i++) {
            try {
                //build each status in the list and insert them
                statusBuilder(pt.getTimeline().get(i), prep);
                prep.execute();
            } catch (JdbcSQLException e) {
                //the ID of a status is a unique primary key so this avoids duplicate inserts.
                System.out.println("Duplicate status found skipping this status");
                //e.printStackTrace();
            }
        }
    }

    /**
     * Inserts a single status from a class, used with the Search API and
     * Streaming API
     *
     * @param conn the connection to the DB
     * @param s the status to be inserted
     * @throws SQLException thrown when an error occurs in the sql statement
     */
    public static void insertStatus(Connection conn, Status s) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("INSERT INTO TWEETS VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

        try {
            statusBuilder(s, prep);
            prep.execute();
        } catch (JdbcSQLException e) {
            System.out.println("Duplicate status found skipping this status");
            //e.printStackTrace();
        }
    }
 
    /**
     * Inserts hash tag into Hashtags table. Does not track tweets or track
     * hashtags.       
     * Takes a string and structures the parts for insertions into
     * the DB. It does so by setting each "?" in the prepared statement to the
     * specified constant number.
     * 
     * For reference (date inserted) and for crash recovery.
     *
     * NOTE: Unsure if this should be the hashtag object.  String was easier to work with.
     * 
     * @param conn the connection to the DB
     * @param pt the public timeline of tweets
     * @throws Exception
     */
    public static void insertHash(Connection conn, String hashtags)
            throws Exception {
        String[] array = hashtags.split(" ");  //Splits hashtags/keywords into single words
        String date = new Date().toString();
        PreparedStatement prep = conn.prepareStatement("INSERT INTO HASHTAGS VALUES(?,?)");
        for (int i = 0; i < array.length; i++) {
            try {
                prep.setString(HASH_SEARCH_TAG, array[i]);
                prep.setString(HASH_DATE, date);
                prep.executeUpdate();   //inserts into the db
            } catch (JdbcSQLException e) {
                System.out.println("Duplicate status found skipping this status");
                //e.printStackTrace();
            }
        }
    }

    /**
     * Takes a instance of a Status and structures the parts for insertions into
     * the DB. It does so by setting each "?" in the prepared statement to the
     * specified constant number.
     *
     * @param s the status to be inserted into the DB.
     * @param prep the Prepared statement.
     * @throws SQLException
     */
    public static void statusBuilder(Status s, PreparedStatement prep)
            throws SQLException {

        prep.setTimestamp(CREATED, dateConvertor(s));
        prep.setLong(ID, s.getId());
        prep.setString(TEXT, s.getText());
        prep.setString(SOURCE, s.getSource());
        prep.setBoolean(IS_TRUNCATED, s.isTruncated());
        prep.setLong(REPLY_TO, s.getInReplyToStatusId());
        prep.setLong(REPLY_USER, s.getInReplyToUserId());
        prep.setBoolean(FAVORITED, s.isFavorited());
        prep.setString(REPLY_USER_SN, s.getInReplyToScreenName());
        if (s.getGeoLocation() != null) {
            prep.setString(GEOLOCATION, String.valueOf(s.getGeoLocation()));
        } else {
            prep.setString(GEOLOCATION, null);
        }
        if (s.getPlace() != null) {
            prep.setString(PLACE, s.getPlace().getName());
        } else {
            prep.setString(PLACE, null);
        }
        prep.setLong(RETWEET_COUNT, s.getRetweetCount());
        prep.setBoolean(RETWEETED_BY_ME, s.isRetweetedByMe());

        // prep.setArray(CONTRIBUTORS, s.getContributors());
        prep.setString(CONTRIBUTORS, contributorConverter(s.getContributors()));

        // retrieval methods are deprecated
        prep.setString(ANNOTATIONS, "NULL");

        if (s.getRetweetedStatus() != null) {
            prep.setLong(RETWEETED_STATUS, s.getRetweetedStatus().getId());
        } else {
            prep.setLong(RETWEETED_STATUS, -1);
        }

        prep.setObject(USER_MENTIONS,
                userMentionsConvertor(s.getUserMentionEntities()));

        prep.setObject(URLS, urlConvertor(s.getURLEntities()));


        prep.setString(HASHTAGS, hashTagConvertor(s.getHashtagEntities()));

        prep.setLong(USER, s.getUser().getId());
        prep.setString(USERSN, s.getUser().getScreenName());

    }

    

    /**
     * Takes an instance of a User and prepares it for insert into the DB. Does
     * so by using the prepared statement in conjunction with the constants for
     * users.
     *
     * @param u User to be inserted into the DB
     * @param prep prepared SQL statement
     * @throws SQLException
     */
    public static void userBuilder(User u, PreparedStatement prep)
            throws SQLException {
        prep.setLong(USER_ID, u.getId());
        prep.setString(USER_SN, u.getScreenName());
        prep.setString(USER_FN, u.getName());
        prep.setString(USER_LOCATION, u.getLocation());
        prep.setString(USER_DESCRIPTION, u.getDescription());

        if (u.getURL() != null) {
            prep.setString(USER_URL, u.getURL().toString());
        } else {
            prep.setString(USER_URL, null);
        }

        prep.setInt(USER_FOLLOWER_COUNT, u.getFollowersCount());
        prep.setInt(USER_FRIEND_COUNT, u.getFriendsCount());
        prep.setInt(USER_STATUS_COUNT, u.getStatusesCount());
        prep.setTimestamp(USER_DATE_CREATED, dateConvertor(u));
        prep.setBoolean(USER_CONTRIBUTOR_ENABLED, u.isContributorsEnabled());
        prep.setString(USER_PROFILE_IMG_URL, u.getProfileImageURL().toString());
        prep.setBoolean(USER_PROTECTED, u.isProtected());
        prep.setString(USER_LANGUAGE, u.getLang());
        prep.setBoolean(USER_GEO, u.isGeoEnabled());
        prep.setBoolean(USER_VERIFIED, u.isVerified());
        prep.setInt(USER_LISTED_COUNT, u.getListedCount());
    }

    /**
     * Converts the timestamp values from a status into a java timestamp
     * compatible with the database.
     *
     * @param s the Status with the timestamp to be extracted
     * @return a timestamp object
     */
    public static Timestamp dateConvertor(Status s) {
        Timestamp d = new Timestamp(s.getCreatedAt().getTime());
        return d;

    }

    

    /**
     * Converts the timestamp values from a user into a java timestamp
     * compatible with the DB.
     *
     * @param u the user with the timestamp to be extracted
     * @return a timestamp object
     */
    public static Timestamp dateConvertor(User u) {
        Timestamp d = new Timestamp(u.getCreatedAt().getTime());
        return d;

    }

    /**
     * Converts an array of hashtags into a string.
     *
     * @param ht hashtag array
     * @return string to insert into the DB
     */
    public static String hashTagConvertor(HashtagEntity[] ht) {
        String tags = "";
        for (int i = 0; i < ht.length; i++) {
            // System.out.println(i+": " + ht[i]);
            if (i == 0) {
                tags = "#" + ht[i].getText();
            } else {
                tags = tags + " #" + ht[i].getText();
            }
        }
        return tags;

    }

    /**
     * Converts an array of user mentions into a string.
     *
     * @param um user mentions array
     * @return string to insert into the DB
     */
    public static String userMentionsConvertor(UserMentionEntity[] um) {
        String users = "";
        for (int i = 0; i < um.length; i++) {
            // System.out.println(i+": " + um[i]);
            if (i == 0) {
                users = "@" + um[i].getScreenName();
            } else {
                users = users + " @" + um[i].getScreenName();
            }
        }
        return users;

    }

    /**
     * Converts an array of urls into a string.
     *
     * @param url array of urls
     * @return string to insert into the DB
     */
    public static String urlConvertor(URLEntity[] url) {
        String urlList = "";
        for (int i = 0; i < url.length; i++) {
            // System.out.println(i+": " + url[i]);
            if (i == 0) {
                urlList = url[i].getDisplayURL();
            } else {
                urlList = " " + url[i].getDisplayURL();
            }
        }
        return urlList;

    }

    /**
     * Converts an array of contributors into a string.
     *
     * @param url array of contributors
     * @return string to insert into the DB
     */
    private static String contributorConverter(long[] c) {
        String temp = "";

        if (c != null) {
            for (int i = 0; i < c.length; i++) {
                if (i == 0) {
                    temp = "" + c[i];
                } else {
                    temp = temp + ", " + c[i];
                }
            }
        }
        return temp;
    }

    /**
     * Helper method to check if a user exists in the DB already.
     *
     * @param id user ID
     * @param conn connection to the DB
     * @return true if the user exist false otherwise
     * @throws SQLException
     */
    public static boolean exist_User(long id, Connection conn)
            throws SQLException {
        Statement stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String sql = "SELECT COUNT(*) FROM USER WHERE ID = '" + id + "'";
        //System.out.println("Looking for user " + id + " in the DB...");
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        if (!(rs.getInt(1) == 0)) {
            //System.out.println("User Match Found on " + id + " ...");
            return true;
        } else {
            //System.out.println("No matches for " + id
            //+ " inserting data into DB...");
            return false;
        }
    }

    /**
     * Helper method to check if a status already exists in the DB.
     *
     * @param id ID of the status to check for.
     * @param conn connection to the DB
     * @return True/False depending on if the status is found or not
     * @throws SQLException
     */
    public static boolean exist_Status(long id, Connection conn)
            throws SQLException {
        Statement stmt = conn.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String sql = "SELECT COUNT(*) FROM TWEETS WHERE ID = '" + id + "'";
        //System.out.println("Looking for status " + id + " in the DB...");
        ResultSet rs = stmt.executeQuery(sql);
        rs.next();
        if (!(rs.getInt(1) == 0)) {
            //System.out.println("Status Match Found...");
            return true;
        } else {
            //System.out.println("No status matches found...");
            return false;
        }
    }

    /**
     * method to insert multiple users from a list.
     *
     * @param conn Connection to the DB
     * @param uid list of user IDs to be inserted
     * @throws Exception
     */
    public static void insertUserList(Connection conn, ArrayList<Long> uid) throws Exception {

        Twitter a = OAuth.authenticate();
        ResponseList<User> rl = null;
        long idArray[] = new long[100];
        int index = 0;
        while (!uid.isEmpty()) {
            idArray[index] = uid.get(0);
            uid.remove(0);
            index++;
            if ((index == 100) || uid.isEmpty()) {
                if (rl.isEmpty()) {
                    rl.addAll(0, a.lookupUsers(idArray));
                } else {
                    rl.addAll(a.lookupUsers(idArray));
                    index = 0;
                }
            }
        }
        for (User u : rl) {
            insertUser(conn, u);
        }
    }
}
