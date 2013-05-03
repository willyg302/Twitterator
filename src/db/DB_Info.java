package db;

import java.io.File;
import java.io.FilenameFilter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Separates DB data from DB_Connect. To edit anything (including embedded
 * schemas) we now only have to go here.
 * 
 * @author William Gaul
 */
public class DB_Info {
    
    //default username and pass for the H2 admin
    private static final String USER = "sa";
    private static final String PASS = "";
    
    private static final String DEFAULT_DB = "test";
    private static final String SCRIPT = "SQL/create.sql";
    
    // Initially the DB is the default one (test)
    private static String DB = DEFAULT_DB;
    
    private static String getInitString() {
        return "INIT=RUNSCRIPT FROM 'classpath:" + SCRIPT + "';";
    }
    
    public static Connection getConnection() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        return DriverManager.getConnection("jdbc:h2:tcp://localhost/~/" + DB + ";" + getInitString(), USER, PASS);
    }
    
    public static void setDB(String newDB) {
        DB = newDB;
    }
    
    public static String getDB() {
        return DB;
    }
    
    public static ArrayList<String> getExistingDBList() {
        List<File> files = Arrays.asList(new File(System.getProperty("user.home")).listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".h2.db");
            }
        }));
        ArrayList<String> ret = new ArrayList<String>();
        for (File f : files) {
            String s = f.getName();
            ret.add(s.substring(0, s.indexOf(".")));
        }
        // Always have the test DB (whether or not it yet exists)
        if (!ret.contains(DEFAULT_DB)) {
            ret.add(DEFAULT_DB);
        }
        return ret;
    }
}