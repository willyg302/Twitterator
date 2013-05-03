package authentication;

import twitter4j.*;
import twitter4j.auth.AccessToken;
import twitterator.TWProperties;
import twitterator.Twitterator;

/**
 * The authentication package allows the app to verify a user. This is similar
 * to logging in. Being authenticated provides access to a lot of options.
 *
 * @TODO: Allow a user to login with their own ID
 *
 * @author Baohuy Ung, William Gaul
 */
public class OAuth {

    private static Twitter twitter = new TwitterFactory().getInstance();
    private static TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
    
    // Access Tokens and App Keys are stored in a properties file
    
    private static AccessToken getAccessToken() {
        String test = Twitterator.DEBUG ? "Test" : "";
        return new AccessToken(TWProperties.getProperty("ATP" + test), TWProperties.getProperty("ATS" + test));
    }
    
    private static String getAppKeyPublic() {
        return TWProperties.getProperty("AKP" + (Twitterator.DEBUG ? "Test" : ""));
    }
    
    private static String getAppKeySecret() {
        return TWProperties.getProperty("AKS" + (Twitterator.DEBUG ? "Test" : ""));
    }

    /**
     * Gives users an authenticated instance of the Twitter class to access the
     * API
     */
    public static Twitter authenticate() throws TwitterException {
        try {
            twitter.setOAuthConsumer(getAppKeyPublic(), getAppKeySecret());
            twitter.setOAuthAccessToken(getAccessToken());
        } catch (IllegalStateException e) {
        }
        return twitter;
    }

    /**
     * Gives users an authenticated instance of the streaming class to access
     * the API
     */
    public static TwitterStream streamAuthenticate() throws TwitterException {
        try {
            twitterStream.setOAuthConsumer(getAppKeyPublic(), getAppKeySecret());
            twitterStream.setOAuthAccessToken(getAccessToken());
        } catch (IllegalStateException e) {
        }
        return twitterStream;
    }
}