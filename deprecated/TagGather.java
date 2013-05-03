package tagGather;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

/**
 *
 * @author Baohuy Ung
 * @version 1.0
 *
 * This class is an example module to display statistics. It creates a list of
 * hashtag co-occurrences and ranks them by popularity. There may be a problem
 * with occurrences being off for large datasets, requires some testing.
 */
public class TagGather {

    Statement stmt;
    String[] tempTags;
    boolean isNotEnd = true;
    int tweetsRead = 0;
    int wordCount = 0;
    int wordTotal = 0;
    int listCount = 0;
    //hashtable that sorts the unique words and tracks the frequency of occurances
    ArrayList<HashNode>[] hashList = (ArrayList<HashNode>[]) new ArrayList[100];

    /**
     * Queries the DB for all tweets with hashtags with a starting root word
     *
     * @param conn Connection to the DB
     * @param root word to start with, suggested that it be a keyword used
     * during collection
     * @throws SQLException thrown if there is an error in data retrieval
     */
    public TagGather(Connection conn, String root) throws SQLException {

        System.out.println("Creating statement...");
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_READ_ONLY);
        String sql;
        sql = "SELECT * FROM TWEETS WHERE LOWER(HASHTAGS) LIKE LOWER('%" + root + "%')";
        ResultSet rs = stmt.executeQuery(sql);

        rs.first();
        String tempString;
        tempString = rs.getString("HASHTAGS");
        tweetsRead++;

        while (isNotEnd) {
            rs.next();
            tweetsRead++;
            tempString += " " + rs.getString("HASHTAGS");
            //System.out.println(tweetsRead + ": "+ rs.getString("HASHTAGS"));
            //very last run
            if (rs.isLast()) {
                isNotEnd = false;
            }
        }

        //tags are stored as a single string, so split them up into an array of all tags
        tempTags = tempString.split("\\s+");

        listAdd(tempTags);

    }

    /**
     * Helper method to initialize the hash table.
     */
    public void listInit() {
        for (int i = 0; i < 100; i++) {
            hashList[i] = new ArrayList<HashNode>();
        }
    }

    /**
     * Hashes the list of tags into the hashtable and counts their frequency
     *
     * @param list the list of all tags returned from the DB query
     */
    public void listAdd(String[] list) {
        listInit();
        for (String a : list) {
            int hashValue = hashInt(a.toLowerCase().hashCode());

            HashNode tempNode = new HashNode(a);
            //this is a new occurrence of a tag
            if (hashList[hashValue].isEmpty()) {
                hashList[hashValue].add(tempNode);
                wordCount++;
                wordTotal++;
            } //this happens if the tag already exists
            else if (!hashFind(hashList[hashValue], tempNode.getItem())) {
                hashList[hashValue].add(tempNode);
                wordCount++;
                wordTotal++;
            } else {
                wordTotal++;
            };

        }
        //these are all test printouts
        System.out.println("Tweets Read:" + tweetsRead);
        System.out.println("Words Found: " + wordCount);
        hashPrint();
        System.out.println("Word Total: " + wordTotal);
        System.out.println("List Total: " + listCount);
    }

    /**
     * Truncates the hash value to a range of 0-99
     *
     * @param a hash value
     * @return new hash value range from 0-99
     */
    public int hashInt(Integer a) {

        if (Integer.signum(a) == -1) {
            a = a * -1;
        }

        String temp = Integer.toString(a);
        temp = temp.substring(0, 2);

        return Integer.valueOf(temp);
    }

    /**
     * Helper method to print all the values in the hash table.
     */
    public void hashPrint() {

        for (ArrayList<HashNode> a : hashList) {
            for (HashNode b : a) {
                System.out.println("Tag: " + b.getItem() + " Count: " + b.getCount());
                listCount += b.getCount();
            }
        }

    }

    /**
     * Determines of a tag already exists in the table
     *
     * @param hl the chain hash list
     * @param key the keyword to be searched for
     * @return true or false depending on if the key is found
     */
    public boolean hashFind(ArrayList<HashNode> hl, String key) {
        boolean findFlag = true;
        boolean is_Found = false;
        int index = 0;
        while (findFlag) {
            HashNode tempNode = hl.get(index);
            //key is not case sensitive
            if (tempNode.getItem().toLowerCase().equals(key.toLowerCase())) {
                tempNode.increment();
                findFlag = false;
                is_Found = true;
            }
            if (index == hl.size() - 1) {
                findFlag = false;
            }
            index++;
        }
        return is_Found;
    }

    /**
     * Private class for the hash table node that stores key and frequency
     *
     * @author Baohuy Ung @verion 1.0
     */
    private class HashNode {

        private String item;
        private int count;

        private HashNode(String name) {
            item = name;
            count = 1;
        }

        private String getItem() {
            return item;
        }

        private int getCount() {
            return count;
        }

        private void setItem(String name) {
            item = name;
        }

        private void setCount(int a) {
            count = a;
        }

        private void increment() {
            count++;
        }
    }
}
