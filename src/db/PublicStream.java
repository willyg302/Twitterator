package db;

import authentication.OAuth;
import java.util.ArrayList;
import twitter4j.*;

/**
 * Creates a stream connection using the Streaming API for twitter. Limited to
 * 400 keywords, 5000 users, 25 geo-locations. Streams do not user the rate
 * limit, but it is limited to 1 connect per authenticated ID.
 * 
 * @author William Gaul, Baohuy Ung
 * @version 1.0
 */
public class PublicStream {

    int tweetTotal = 0;
    long userID;
    
    ArrayList<Long> followers = new ArrayList<>();
    ArrayList<String> keywords = new ArrayList<>();
    TwitterStream ts;
    
    //filter queries are how you start a new stream
    FilterQuery fq = new FilterQuery();
    
    public PublicStream(TwitterStream twitterStream) {
        ts = twitterStream;
    }

    /**
     * Sets the stream's listener, which listens for updates. Depending on what
     * comes through (such as a status update) we call the appropriate DB thing.
     *
     * @throws TwitterException Stream stall or appropriate bandwidth issues
     */
    public void startStream() throws TwitterException {
        StatusListener listener = new StatusListener() {

            @Override
            public void onStatus(Status status) {
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                try {
                    DB_Connect.trackerAdd(status);
                } catch (Exception e) {
                    //
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice sdn) {
                System.out.println("Got a status deletion notice id:" + sdn.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int i) {
                System.out.println("Got track limitation notice:" + i);
            }

            @Override
            public void onScrubGeo(long l, long l1) {
                System.out.println("Got scrub_geo event userId:" + l + " upToStatusId:" + l1);
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                System.out.println("Connection stalled...");
            }

            @Override
            public void onException(Exception excptn) {
                System.out.println("Connection timed out...Will attempt to reconnect.");
            }
        };
        ts.addListener(listener);
    }

    /**
     * when a user is added to the track list, this adds the user's profile into
     * the database. This uses 1 query limit.
     *
     * @param id user to be added into the DB
     * @throws TwitterException user not found
     * @throws Exception DB issues
     */
    public void addUser(long id) throws TwitterException, Exception {
        followers.add(id);
        fq.follow(buildLongArray(followers));
        DB_Connect.trackerUserAdd(id);
    }

    /**
     * This helper method structures print out all tracked users to the UI
     *
     * @return a string with a list of all users currently tracked
     */
    public String userToString() {
        String ret = "";
        String d = "";
        for (Long l : followers) {
            ret += d + "UserID: " + l;
            d = "\n";
        }
        return ret;
    }

    /**
     * Adds a search term to be tracked. Similar to the search API, but single
     * keywords are recommended due to character limit.
     *
     * @param keyword to be tracked
     */
    public void addSearch(String keyword) {
        keywords.add(keyword);
        String[] temp = keywords.toArray(new String[keywords.size()]);
        fq.track(temp);

    }

    /**
     * Helper method to print out all currently tracked search terms to the UI.
     *
     * @return a string of all search terms being tracked.
     */
    public String searchToString() {
        String ret = "";
        String d = "";
        for (String s : keywords) {
            ret += d + "Keyword: " + s;
            d = "\n";
        }
        return ret;
    }

    /**
     * The track list is not dynamic and must be restarted to update the tracked
     * users/words. This method restarts the stream with the new list.
     *
     * @throws TwitterException if an error occurs during reconnection
     */
    public void refresh() throws TwitterException {
        ts = OAuth.streamAuthenticate();
        ts.filter(fq);
    }
    
    private long[] buildLongArray(ArrayList<Long> longs) {
        long[] ret = new long[longs.size()];
        int i = 0;
        for (Long l : longs) {
            ret[i++] = l;
        }
        return ret;
    }
}
