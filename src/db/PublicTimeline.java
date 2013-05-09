package db;

import authentication.OAuth;
import java.util.List;
import twitter4j.*;

/**
 * Crawls through a public timeline and gathers up to 3200 of the most recent tweets.
 * A single call can only return up to 200 statuses so it may take up to 16 requests.
 *
 * @author William Gaul, Baohuy Ung
 * @version 1.0
 */
public class PublicTimeline {
    
    private String userID;
    private List<Status> timeline;
    private int tweetTotal;

    public PublicTimeline(String userID) throws TwitterException {
        this.userID = userID;
        this.tweetTotal = 0;
        cacheTimeline();
    }

    public List<Status> getTimeline() {
        return timeline;
    }
    
    /**
     * Begins collection of up to 3200 most recent statuses. This MUST be
     * called before you call getTimeline(). May consider moving this up to
     * the constructor?
     * 
     * @throws TwitterException if UserID mismatch or rate limit exceeded
     */
    private void cacheTimeline() throws TwitterException {
        Twitter twitter = OAuth.authenticate();
        ResponseList<Status> temp;
        
        int pageIndex = 1;
        Paging page = new Paging(1, 200);
        boolean flag = true;
        
        // Page through each page and add all statuses to the list
        while (flag) {
            temp = twitter.getUserTimeline(userID, page);
            if (temp.isEmpty()) {
                System.out.println("Maximum number of tweets gathered");
                flag = false;
            }
            if (flag) {
                if (pageIndex == 1) {
                    timeline = temp;
                } else {
                    timeline.addAll(temp);
                }
                page.setPage(pageIndex++);
            }
            tweetTotal = timeline.size();
            System.out.println("Number of Tweets gathered: " + tweetTotal);
        }
    }
}