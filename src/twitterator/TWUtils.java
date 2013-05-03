package twitterator;

import authentication.OAuth;
import db.DB_Connect;
import db.DB_Info;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;
import twitter4j.RateLimitStatus;
import twitterFeed.PublicStream;

/**
 * Utility functions for our TWHandler. This is the highest-level wrapper for
 * our database/Twitter functionality in other packages. It is called directly
 * by the handler when receiving a request.
 * 
 * @author William Gaul
 */
public class TWUtils {
    
    private PublicStream stream;
    private long rateLimit;
    
    public TWUtils() throws Exception {
        stream = new PublicStream(OAuth.streamAuthenticate());
        stream.startStream();
        
        /* @Will: This is actually weird. There are many different rate limits now,
         * each one for a different thing (based on the endpoint). Which one
         * to go by...?
         */
        Map<String, RateLimitStatus> rates = OAuth.authenticate().getRateLimitStatus();
        for (String endpoint : rates.keySet()) {
            int remaining = rates.get(endpoint).getRemaining();
            if (rateLimit < remaining)
                rateLimit = remaining;
        }
        //System.out.println(rateLimit);
        //rateLimit = OAuth.authenticate().getRateLimitStatus()
        //rateLimit = OAuth.authenticate().getRateLimitStatus().getRemainingHits();
    }
    
    public long rateLimit() {
        return rateLimit;
    }
    
    /**
     * View table is nonfunctional at this time, leaving stub.
     */
    public void viewTable() {
        //needs to be modified
    }

    
    private String export(String type) {
        try {
            if (type.equals("CSV")) {
                DB_Connect.csvExport();
            }
            return "Export successful!";
        } catch (Exception e) {
            return "ERROR:<br><br>" + e.getMessage() + "<br><br>when trying to export";
        }
    }
    
    /**
     * Server-side SHA. Not used yet.
     */
    public static String sha256(String base) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    
    
    private boolean validate(String pass) {
        // For now hardcoded
        /*
         * @TODO: what this SHOULD do is, the user can add a NEW password, which
         * will be SHA'd on the client side using Crypto-JS and transferred over
         * network securely. We can then store the HASH in a database, and query
         * whether a subsequent user input is in the database.
         */
        return pass.equals("[DEPRECATED]");
    }
    
    public String getHTMLFormattedDBList() {
        ArrayList<String> DBs = DB_Info.getExistingDBList();
        String msg = "";
        String delim = "";
        for (int i = 0; i < DBs.size(); i++) {
            msg += (delim + DBs.get(i));
            delim = ",";
        }
        return msg;
    }
    
    
    
    private String addUser(String prompt, String db) {
        Twitterator.log("Adding user: " + prompt);
        try {
            DB_Info.setDB(db);
            DB_Connect.addUser(prompt);
            return "Successfully added " + prompt + "!";
        } catch (Exception e) {
            return "ERROR:<br><br>" + e.getMessage() + "<br><br>when trying to add user " + prompt;
        }
    }
    
    public String createDB(String prompt, String db) {
        Twitterator.log("Creating new DB: " + prompt);
        try {
            DB_Info.setDB(prompt);
            DB_Connect.createNewDB();
            return "Database " + prompt + " successfully created!";
        } catch (Exception e) {
            return "ERROR:<br><br>" + e.getMessage() + "<br><br>when trying to create database " + prompt;
        }
    }
    
    private String export(String prompt, String db) {
        DB_Info.setDB(db);
        String fileUrl = "/download.html?query=download&name=" + db + "&type=" + prompt.toLowerCase();
        return "<![CDATA[" + export(prompt) + " Click <a href=\"" + fileUrl + "\">here</a> to download your file!]]>";
    }
    
    public String search(String prompt, String db) {
        Twitterator.log("Searching: " + prompt);
        try {
            DB_Info.setDB(db);
            DB_Connect.query(prompt);
            // Should somehow return searches to webpage...
            return "";
        } catch (Exception e) {
            return "ERROR:<br><br>" + e.getMessage() + "<br><br>when trying to search on " + prompt;
        }
    }
    
    public String trackKeyword(String prompt, String db) {
        Twitterator.log("Tracking new keyword: " + prompt);
        try {
            DB_Info.setDB(db);
            String temp = prompt.replace("\n", "").replace("\r", "");
            DB_Connect.addHash(prompt);
            stream.addSearch(temp);
            stream.refresh();
            return "Tracker updated for keyword " + prompt + "!";
        } catch (Exception e) {
            return "ERROR:<br><br>" + e.getMessage() + "<br><br>when trying to track keyword " + prompt;
        }
    }
    
    public String trackUser(String prompt, String db) {
        Twitterator.log("Tracking user: " + prompt);
        Long id = 0L;
        try {
            DB_Info.setDB(db);
            try {
                id = new Long(prompt);
            } catch (NumberFormatException e) {
                id = OAuth.authenticate().showUser(prompt).getId();
            }
            stream.addUser(id);
            
            // What to return here?
            return "";
        } catch (Exception e) {
            return "ERROR:<br><br>" + e.getMessage() + "<br><br>when trying to track user " + prompt;
        }
    }
    
    /**
     * Called by our TWHandler. Uses reflection to call the specified action
     * and pass along the prompt and db. Note that these may not be required
     * (for example, in createDB), but all values not specified by the client
     * are set to "undefined" via JavaScript.
     */
    public String handle(String action, String prompt, String db) {
        String ret;
        try {
            Method method = this.getClass().getDeclaredMethod(action, new Class[] {
                String.class,
                String.class
            });
            ret = (String)method.invoke(this, new Object[] {prompt, db});
        } catch (Exception ex) {
            ret = "Fatal error! Unrecognized action...";
        }
        return ret;
    }
}
