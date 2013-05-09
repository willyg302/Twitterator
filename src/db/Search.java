package db;

import authentication.OAuth;
import java.util.List;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Uses the search API to query twitter with search terms. Similar to using the
 * search bar in twitter. Twitter imposes a limit of 1500 most recent tweets or
 * up to 1 week back. A single search returns up to 100-200 results per page but
 * requires each to be converted into a status.
 * 
 * @author William Gaul, Baohuy Ung
 * @version 1.0
 */
public class Search {

    private List<Status> results;
    private int tweetTotal;

    public Search(String query) throws TwitterException {
        this.tweetTotal = 0;
        cacheResults(query);
    }

    public List<Status> getResults() {
        return results;
    }
    
    /**
     * Gets all results of a search in a list of tweets, this is raw tweets and
     * must be converted into a status
     *
     * @param q the query string
     * @return a list of all tweets returned
     * @throws TwitterException
     */
    private void cacheResults(String q) throws TwitterException {
        Twitter twitter = OAuth.authenticate();
        List<Status> temp;
        
        Query newQuery = new Query(q);
        newQuery.setCount(200);
        int pageIndex = 1;
        boolean flag = true;
        
        try {
            while (flag) {
                temp = twitter.search(newQuery).getTweets();
                if (temp.isEmpty()) {
                    System.out.println("Maximum number of tweets gathered");
                    flag = false;
                }
                if (flag) {
                    if (pageIndex == 1) {
                        results = temp;
                    } else {
                        results.addAll(temp);
                    }
                    pageIndex++;
                }
                tweetTotal = results.size();
                System.out.println("Tweets Gathered: " + tweetTotal);
            }
        } catch (TwitterException e) {
            //rate limit will be exhuast very fast using this, you may want to create a timer to track again 
            System.out.println("Rate Limit Exceeded");
        }
    }
}