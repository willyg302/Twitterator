package twitterFeed;

import authentication.OAuth;
import db.DB_Connect;
import java.util.ArrayList;
import twitter4j.*;

/**
 *
 * @author Baohuy Ung
 * @version 1.0
 *
 * Creates a stream connection using the Streaming API for twitter. Limited to
 * 400 keywords, 5000 users, 25 geo-locations Streams do not user the rate
 * limit, but it is limited to 1 connect per authenticated ID
 *
 */
public class PublicStream {

    int tweetTotal = 0;
    int followCount = 0;
    int keywordCount = 0;
    long userID;
    //list of users and keywords, geolocations count as keywords
    long[] followers = new long[5000];
    ArrayList<String> keywords = new ArrayList<String>();
    TwitterStream ts;
    //filter queries are how you start a few stream
    FilterQuery fq = new FilterQuery();

    /**
     * Constructor sets the twitter stream
     *
     * @param tweetStream twitter stream should be created from the UI when the
     * program begins
     */
    public PublicStream(TwitterStream tweetStream) {
        ts = tweetStream;
    }

    /**
     * Sets the listener for the stream by enabling the connection and listening
     * for updates Depending on what comes through such as a status update, then
     * call the appropriate DB option
     *
     * @throws TwitterException there is a stall in the stream or bandwith
     * issues
     */
    public void startStream() throws TwitterException {


        //creates a new listener
        StatusListener listener = new StatusListener() {
            //Only looking for status updates

            @Override
            public void onStatus(Status status) {
                System.out.println("@" + status.getUser().getScreenName() + " - " + status.getText());
                try {
                    DB_Connect.trackerAdd(status);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
                System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
            }

            @Override
            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
                System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
            }

            @Override
            public void onScrubGeo(long userId, long upToStatusId) {
                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
            }

            @Override
            public void onException(Exception ex) {
                System.out.println("Connection timed out...Will attempt to reconnect.");
                //ex.printStackTrace();
            }

            @Override
            public void onStallWarning(StallWarning sw) {
                System.out.println("Connection stalled...");
                // Not supported yet
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
        followers[followCount] = id;
        fq.follow(followers);
        followCount++;
        DB_Connect.trackerUserAdd(id);
    }

    /**
     * This helper method structures print out all tracked users to the UI
     *
     * @return a string with a list of all users currently tracked
     */
    public String userToString() {
        String temp = "";
        for (int i = 0; i < followCount; i++) {
            if (i == 0) {
                temp = "UserID: " + followers[i];
            } else {
                temp = temp + "\nUserID: " + followers[i];
            }
        }
        return temp;
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
        keywordCount++;

    }

    /**
     * Helper method to print out all currently tracked search terms to the UI.
     *
     * @return a string of all search terms being tracked.
     */
    public String searchToString() {
        String temp = "";
        for (int i = 0; i < keywords.size(); i++) {
            if (i == 0) {
                temp = "Keyword: " + keywords.get(i);
            } else {
                temp = temp + "\n" + "Keyword: " + keywords.get(i);
            }

        }
        return temp;

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
}
