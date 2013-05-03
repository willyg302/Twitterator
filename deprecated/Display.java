package db;

import java.sql.*;

/**
 *
 * @author Baohuy Ung
 * @version 1.0
 *
 * This class is an example display class that queries the database for
 * information to create information that can be displayed to the UI for users.
 */
public class Display {

    public static String run(Connection conn) throws SQLException {

        String dbText = "USER 	|	 TIME 	|	 TEXT\n";
        Statement stmt = null;
        try {
            // Execute a query to create statment with
            // required arguments for RS example.
            System.out.println("Creating statement...");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            String sql;
            sql = "SELECT * FROM TWEETS";
            ResultSet rs = stmt.executeQuery(sql);

            rs.first();
            while (!rs.isAfterLast()) {
                String tempString;
                // Retrieve by column name
                Timestamp ts = rs.getTimestamp("Created");
                String user = rs.getString("User_SN");
                String text = rs.getString("Text");
                String tags = rs.getString("Hashtags");

                // Display values
                tempString = user + ":  [" + ts + "] " + text + "\n";

                dbText += tempString;
                rs.next();
            }

            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            // Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            // Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            // finally block used to close resources
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException se2) {
            }// nothing we can do
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }// end finally try
        }// end try
        return dbText;
    }// end main
}
