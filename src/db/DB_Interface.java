package db;

import authentication.OAuth;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import org.h2.jdbc.JdbcSQLException;
import twitter4j.*;

/**
 * This class contains methods that handle the conversion of Twitter data to
 * data entities for the database. Data retrieved from Twitter must be separated
 * into columns then built as a SQL query.
 * 
 * @author William Gaul, Baohuy Ung
 * @version 1.0
 */
public class DB_Interface {
    
    // Enums below are ordered by column appearing in the DB
    
    public enum eStatus {
        S_CREATED, S_ID, S_TEXT, S_SOURCE, S_IS_TRUNCATED, S_REPLY_TO, S_REPLY_USER,
        S_FAVORITED, S_REPLY_USER_SN, S_GEOLOCATION, S_PLACE, S_RETWEET_COUNT, S_RETWEETED_BY_ME, S_CONTRIBUTORS,
        S_ANNOTATIONS, S_RETWEETED_STATUS, S_USER_MENTIONS, S_URLS, S_HASHTAGS, S_USERS, S_USER_SN
    }

    public enum eUser {
        U_ID, U_SN, U_FULL_NAME, U_LOCATION, U_DESCRIPTION, U_URL, U_FOLLOWER_COUNT,
        U_FRIEND_COUNT, U_STATUS_COUNT, U_DATE_CREATED, U_CONTRIBUTOR_ENABLED,
        U_PROFILE_IMG_URL, U_PROTECTED, U_LANGUAGE, U_GEO, U_VERIFIED, U_LISTED_COUNT
    }
    
    public enum eHashtag {
        HASH_SEARCH_TAG, HASH_DATE
    }
    
    private static int ord(Enum e) {
        return e.ordinal() + 1;
    }
    
    /**
     * Adds a column of data to the PreparedStatement.
     * @param prep PreparedStatement to add data to
     * @param e Column in the table to add data to
     * @param o Object (data) to add
     * @param t SQL data type
     * @throws SQLException If something bad happens
     */
    private static void addColumn(PreparedStatement prep, Enum e, Object o, int t) throws SQLException {
        prep.setObject(ord(e), o, t);
    }

    /**
     * Inserts a new user into the DB.
     */
    public static void insertUser(Connection conn, User u) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("INSERT INTO USER VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        try {
            userBuilder(u, prep);
            prep.execute();
        } catch (Exception e) {
            System.out.println("insertUser: JDBC error occurred");
        }
    }

    /**
     * Inserts an entire timeline, which is a collection of statuses.
     */
    public static void insertTimeline(Connection conn, PublicTimeline pt) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("INSERT INTO TWEETS VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        for (Status s : pt.getTimeline()) {
            try {
                statusBuilder(s, prep);
                prep.execute();
            } catch (JdbcSQLException e) {
                System.out.println("insertTimeline: JDBC error occurred");
            }
        }
    }

    /**
     * Inserts a single status, used with the Search and Streaming APIs.
     */
    public static void insertStatus(Connection conn, Status s) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("INSERT INTO TWEETS VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
        try {
            statusBuilder(s, prep);
            prep.execute();
        } catch (JdbcSQLException e) {
            System.out.println("insertStatus: JDBC error occurred");
        }
    }
    
    /**
     * Inserts hashtags into the Hashtags table. Does not track tweets or track
     * hashtags. You can add more than one hashtag at a time since the hashtags
     * parameter may be space-separated. For reference, date is also inserted.
     * 
     * NOTE: Unsure if this should be the Hashtag object instead...
     */
    public static void insertHashtag(Connection conn, String hashtags) throws SQLException {
        PreparedStatement prep = conn.prepareStatement("INSERT INTO HASHTAGS VALUES(?,?)");
        String[] tags = hashtags.split(" ");
        for (int i = 0; i < tags.length; i++) {
            try {
                addColumn(prep, eHashtag.HASH_SEARCH_TAG, tags[i], Types.VARCHAR);
                addColumn(prep, eHashtag.HASH_DATE, new Date().toString(), Types.VARCHAR);
                prep.executeUpdate();
            } catch (JdbcSQLException e) {
                System.out.println("insertHash: JDBC error occurred");
            }
        }
    }
    
    public static void statusBuilder(Status s, PreparedStatement prep) throws SQLException {
        addColumn(prep, eStatus.S_CREATED, getTimestamp(s), Types.TIMESTAMP);
        addColumn(prep, eStatus.S_ID, s.getId(), Types.BIGINT);
        addColumn(prep, eStatus.S_TEXT, s.getText(), Types.VARCHAR);
        addColumn(prep, eStatus.S_SOURCE, s.getSource(), Types.VARCHAR);
        addColumn(prep, eStatus.S_IS_TRUNCATED, s.isTruncated(), Types.BOOLEAN);
        addColumn(prep, eStatus.S_REPLY_TO, s.getInReplyToStatusId(), Types.BIGINT);
        addColumn(prep, eStatus.S_REPLY_USER, s.getInReplyToUserId(), Types.BIGINT);
        addColumn(prep, eStatus.S_FAVORITED, s.isFavorited(), Types.BOOLEAN);
        addColumn(prep, eStatus.S_REPLY_USER_SN, s.getInReplyToScreenName(), Types.VARCHAR);
        addColumn(prep, eStatus.S_GEOLOCATION, (s.getGeoLocation() != null) ? String.valueOf(s.getGeoLocation()) : null, Types.VARCHAR);
        addColumn(prep, eStatus.S_PLACE, (s.getPlace() != null) ? s.getPlace().getName() : null, Types.VARCHAR);
        addColumn(prep, eStatus.S_RETWEET_COUNT, s.getRetweetCount(), Types.BIGINT);
        addColumn(prep, eStatus.S_RETWEETED_BY_ME, s.isRetweetedByMe(), Types.BOOLEAN);
        addColumn(prep, eStatus.S_CONTRIBUTORS, contributorsConverter(s.getContributors()), Types.VARCHAR);
	addColumn(prep, eStatus.S_ANNOTATIONS, "NULL", Types.VARCHAR);
        addColumn(prep, eStatus.S_RETWEETED_STATUS, (s.getRetweetedStatus() != null) ? s.getRetweetedStatus().getId() : -1, Types.BIGINT);
        addColumn(prep, eStatus.S_USER_MENTIONS, usermentionsConverter(s.getUserMentionEntities()), Types.VARCHAR);
        addColumn(prep, eStatus.S_URLS, urlConverter(s.getURLEntities()), Types.VARCHAR);
        addColumn(prep, eStatus.S_HASHTAGS, hashtagsConverter(s.getHashtagEntities()), Types.VARCHAR);
	addColumn(prep, eStatus.S_USERS, s.getUser().getId(), Types.BIGINT);
        addColumn(prep, eStatus.S_USER_SN, s.getUser().getScreenName(), Types.VARCHAR);
    }
    
    public static void userBuilder(User u, PreparedStatement prep) throws SQLException {
        addColumn(prep, eUser.U_ID, u.getId(), Types.BIGINT);
        addColumn(prep, eUser.U_SN, u.getScreenName(), Types.VARCHAR);
        addColumn(prep, eUser.U_FULL_NAME, u.getName(), Types.VARCHAR);
        addColumn(prep, eUser.U_LOCATION, u.getLocation(), Types.VARCHAR);
        addColumn(prep, eUser.U_DESCRIPTION, u.getDescription(), Types.VARCHAR);
        addColumn(prep, eUser.U_URL, (u.getURL() != null) ? u.getURL().toString() : null, Types.VARCHAR);
        addColumn(prep, eUser.U_FOLLOWER_COUNT, u.getFollowersCount(), Types.INTEGER);
        addColumn(prep, eUser.U_FRIEND_COUNT, u.getFriendsCount(), Types.INTEGER);
        addColumn(prep, eUser.U_STATUS_COUNT, u.getStatusesCount(), Types.INTEGER);
        addColumn(prep, eUser.U_DATE_CREATED, getTimestamp(u), Types.TIMESTAMP);
        addColumn(prep, eUser.U_CONTRIBUTOR_ENABLED, u.isContributorsEnabled(), Types.BOOLEAN);
        addColumn(prep, eUser.U_PROFILE_IMG_URL, u.getProfileImageURL().toString(), Types.VARCHAR);
        addColumn(prep, eUser.U_PROTECTED, u.isProtected(), Types.BOOLEAN);
        addColumn(prep, eUser.U_LANGUAGE, u.getLang(), Types.VARCHAR);
        addColumn(prep, eUser.U_GEO, u.isGeoEnabled(), Types.BOOLEAN);
	addColumn(prep, eUser.U_VERIFIED, u.isVerified(), Types.BOOLEAN);
        addColumn(prep, eUser.U_LISTED_COUNT, u.getListedCount(), Types.INTEGER);
    }
    
    public static Timestamp getTimestamp(Status s) {
        return new Timestamp(s.getCreatedAt().getTime());
    }
    
    public static Timestamp getTimestamp(User u) {
        return new Timestamp(u.getCreatedAt().getTime());
    }

    public static String hashtagsConverter(HashtagEntity[] ht) {
        String ret = "";
        String d = "";
        if (ht!= null) {
            for (int i = 0; i < ht.length; i++) {
                ret += d + ht[i].getText();
                d = "#";
            }
        }
        return ret;
    }
    
    public static String usermentionsConverter(UserMentionEntity[] um) {
        String ret = "";
        String d = "";
        if (um != null) {
            for (int i = 0; i < um.length; i++) {
                ret += d + um[i].getScreenName();
                d = "@";
            }
        }
        return ret;
    }
    
    public static String urlConverter(URLEntity[] url) {
        String ret = "";
        String d = "";
        if (url != null) {
            for (int i = 0; i < url.length; i++) {
                ret += d + url[i].getDisplayURL();
                d = " ";
            }
        }
        return ret;
    }
    
    private static String contributorsConverter(long[] c) {
        String ret = "";
        String d = "";
        if (c != null) {
            for (int i = 0; i < c.length; i++) {
                ret += d + c[i];
                d = ", ";
            }
        }
        return ret;
    }
    
    public static boolean userExists(long id, Connection conn) throws SQLException {
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM USER WHERE ID = '" + id + "'");
        rs.next();
        return (rs.getInt(1) != 0);
    }
    
    public static boolean statusExists(long id, Connection conn) throws SQLException {
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM TWEETS WHERE ID = '" + id + "'");
        rs.next();
        return (rs.getInt(1) != 0);
    }

    /**
     * Method to insert multiple users from a list of User IDs.
     * @TODO: Is this even used?
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
