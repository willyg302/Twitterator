package db;

import authentication.OAuth;
import export.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.h2.tools.Csv;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitterator.Twitterator;

/**
 * This is a intermediary class that handles connections with the database and
 * executes commands from the outer classes.
 *
 * There are two different packages that this class unifies. The first step is
 * to query the Twitter servers for the data. This utilizes twitterFeed classes,
 * then the returned data is then sent to the DB classes which prepare the raw
 * data for insertion into the DB and inserts it.
 * 
 * @author William Gaul, Michael Terada, Baohuy Ung
 * @version 1.0
 */
public class DB_Connect {

    /**
     * The database portion of add hashtags. Currently only adds the hashtag
     * name, does not work with gather additional tags.
     */
    public static void addHash(String hashtags) throws ClassNotFoundException, SQLException {
        Connection conn = DB_Info.getConnection();
        DB_Interface.insertHashtag(conn, hashtags);
        conn.close();
    }

    /**
     * Adds a user to the DB given a user handle.
     *  - Scrapes a user's timeline
     *  - Most recent 3200 tweets
     *  - Inserts their profile into the DB
     */
    public static void addUser(String u) throws TwitterException, ClassNotFoundException, SQLException {
        Connection conn = DB_Info.getConnection();
        PublicTimeline pt = new PublicTimeline(u);
        DB_Interface.insertUser(conn, pt.getTimeline().get(0).getUser());
        DB_Interface.insertTimeline(conn, pt);
        conn.close();
    }

    /**
     * Queries the Twitter API given a query string.
     *  - USES a 1-TO-1 RATE LIMIT
     *  - Limit is 1500 tweets or 7 days
     */
    public static void search(String q) throws TwitterException, SQLException, ClassNotFoundException {
        Connection conn = DB_Info.getConnection();
        Search search = new Search(q);
        Twitter a = OAuth.authenticate();
        for (Status status : search.getResults()) {
            long statusID = status.getId();
            if (!DB_Interface.statusExists(statusID, conn)) {
                
                /* Note: We use a.showStatus(statusID) instead of just passing
                 * the status because only queried tweets are formatted correctly.
                 * This takes up a rate limit and is a difficult problem to
                 * address. @TODO: Maybe a workaround?
                 */
                
                DB_Interface.insertStatus(conn, a.showStatus(statusID));
            } else {
                System.out.println("Search result Status already exists, skipping");
            }
        }
        conn.close();
    }
    

    /**
     * Connection between the streaming API to add a status into the DB.
     */
    public static void trackerAdd(Status s) throws ClassNotFoundException, SQLException {
        Connection conn = DB_Info.getConnection();
        DB_Interface.insertStatus(conn, s);
        conn.close();
    }

    /**
     * Connection between the streaming API to add a user into the DB.
     *
     * A user is added into the DB when they are added to the tracking list
     *
     * @param id the userID of the user to be added into the DB
     * @throws SQLException invalid inserts of data
     * @throws ClassNotFoundException H2 not started
     * @throws TwitterException there is insufficient requests left
     */
    public static void trackerUserAdd(long id) throws TwitterException, ClassNotFoundException, SQLException {
        Connection conn = DB_Info.getConnection();
        if (!DB_Interface.userExists(id, conn)) {
            DB_Interface.insertUser(conn, OAuth.authenticate().showUser(id));
        } else {
            System.out.println("User already exists in database");
        }
        conn.close();
    }

    /**
     * Queries the database for all tweets. This is to view it from the UI.
     *
     * @throws ClassNotFoundException when connecting to the DB if H2 is not
     * running
     * @throws SQLException for invalid SQL queries
     */
    public static String view() throws SQLException, ClassNotFoundException {
        Connection conn = DB_Info.getConnection();
        // Class Display has been deprecated
        //String displayText = Display.run(conn);
        conn.close();
        //return displayText;
        return "";
    }
    
    
    public static void createNewDB() throws SQLException, ClassNotFoundException {
        Connection conn = DB_Info.getConnection();
        
        // I'm assuming we do nothing else here...should be created now
        conn.close();
    }
    
    
    ////////////////////////////////////////////////////////////////////////////
    //
    //   EXPORT FUNCTIONS
    //
    ////////////////////////////////////////////////////////////////////////////

    public enum Export {
        CSV, GEPHI, GEO, CSVH2
    }

    /**
     * Generic function for exporting data.
     */
    public static void exportData(Export type) throws ClassNotFoundException, SQLException, IOException {
        String fileName = DB_Info.getDB();
        Twitterator.logTime("Exporting " + fileName + " in format " + type.toString() + "...");
        
        Connection conn = DB_Info.getConnection();

        switch (type) {
            case CSV: new CSV(fileName).make(conn); break;
            case GEPHI: new Gephi(conn).make(); break;
            case GEO: new GeoExport(conn).make(); break;
            case CSVH2: new Csv().write(fileName + ".csv",
                    conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT * FROM TWEETS"), null);
            default: break;
        }

        conn.close();
    }
    
    // CSV export using H2's built-in CSV function (more reliable and preferred)
    public static void csvH2Export() throws ClassNotFoundException, SQLException, IOException {
        exportData(Export.CSVH2);
    }

    // Raw export of all data in the tweets table as CSV
    public static void csvExport() throws ClassNotFoundException, SQLException, IOException {
        exportData(Export.CSV);
    }

    // Exports each entity in the table as a node for Gephi (graph utility)
    public static void gephiExport() throws ClassNotFoundException, SQLException, IOException {
        exportData(Export.GEPHI);
    }

    // Exports based on geo tags; tags are converted from string into latitude/longitude
    public static void geoExport() throws ClassNotFoundException, SQLException, IOException {
        exportData(Export.GEO);
    }
    

    /**
     * Gathers co-related tags based on a given tag.
     *
     * Mike: Pretty sure this doesn't work.
     * 
     * @throws ClassNotFoundException when connecting to the DB if H2 is not
     * running
     * @throws SQLException for invalid SQL queries
     */
    public static void hashGather() throws SQLException, ClassNotFoundException {
        Connection conn = DB_Info.getConnection();
        //TagGather tg = new TagGather(conn, "usa");
        conn.close();

    }

    ////////////////////////////////////////////////////////////////////////////
    //
    //  Currently 2/24/2013 hardcoded.  Need to edit.
    //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Gephi hashtag co-occurrence export.
     * Mike: Needs heavy editing, see the file pathnames
     * 
     * @throws ClassNotFoundException when connecting to the DB if H2 is not
     * running
     * @throws SQLException for invalid SQL queries
     * @throws IOExcetion when specified write location cannot be found
     */
    public static void gHash() throws SQLException, ClassNotFoundException, IOException {
        Connection conn = DB_Info.getConnection();
        GephiHash tg = new GephiHash(conn);
        System.out.println("Making initial list");
        tg.init();
        System.out.println("initiliazation finished... Makeing vertex list");
        tg.writeVertecies("/Users/Brian/Desktop/DB12_11_08_Vertex.csv");
        System.out.println("vertextList finished... creating edge list");
        tg.edgeMake("/Users/Brian/Desktop/DB12_11_08_Edge.csv");
        System.out.println("edge list finished");
        tg.close();
        conn.close();

    }

    /**
     * timeLapse raw export for data with timestamp consideration. Needs to be
     * expanded as the granularity is too small.
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public static void timeExport() throws ClassNotFoundException, SQLException, IOException {
        Connection conn = DB_Info.getConnection();
        TimeGraph tg = new TimeGraph(conn);
        tg.pointList();
        tg.export("/Users/Brian/Desktop/PresDeb1_time.csv");
    }

    /**
     * Two types of expoets for conversations. Retweets and Replies.
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     * @throws IOException
     */
    public static void convoExport() throws ClassNotFoundException, SQLException, IOException {
        Connection conn = DB_Info.getConnection();
        ConvoGraph rt = new ConvoGraph(conn, "HISenOG", "/Users/Brian/Desktop/");
        rt.createNodesRT();
        rt.createRT();
        rt.nodeWrite("RT");
        ConvoGraph rp = new ConvoGraph(conn, "HISenOG", "/Users/Brian/Desktop/");
        rp.createNodesRP();
        rp.createReply();
        rp.nodeWrite("RP");
    }
}
