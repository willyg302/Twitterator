package db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This a deprecated class that was designed for DB exploration while the
 * collection was running.
 *
 * @author Baohuy Ung
 *
 */
public class IndividualDisplay implements Runnable {

    public void init(Connection conn) throws SQLException {
        System.out.println("creating statement...");
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        String sql = "SELECT * FROM TWEETS";
        ResultSet rs = stmt.executeQuery(sql);
        rs.beforeFirst();

    }

    @Override
    public void run() {
        //init(null);
    }
}
