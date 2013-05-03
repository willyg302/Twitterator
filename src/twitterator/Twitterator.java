package twitterator;

import java.sql.SQLException;
import java.util.Date;
import java.util.Scanner;
import org.h2.tools.Server;

/**
 * Our MAIN class. Kicks everything off and manages the log, H2, and server.
 * We keep everything static so that other classes can reference them.
 * 
 * @author William Gaul
 */
public class Twitterator {
    
    public static final String APP_NAME = "Twitterator";
    public static final String VERSION = "0.5";
    
    public static final boolean DEBUG = true;
    
    public static Log log;
    private static Server H2;
    private static TWServer server;
    
    public static void log(String s) {
        log.log(s);
    }
    
    public static void logTime(String s) {
        log.logTime(s);
    }
    
    public static void exit() {
        log.logBreak(40);
        log(APP_NAME + " exiting...");
        
        // Stop our server
        if (server != null) {
            server.banish();
        }
        
        // Shut down H2
        if (H2 != null) {
            H2.stop();
        }
        
        log("Exited at " + new Date(System.currentTimeMillis()).toString());
        log.close();
        
        // Just in case, we force quit
        System.exit(0);
    }
    
    public static void enter() {
        TWProperties.load();
        log = new Log();
        
        log.logBreak(40);
        log(APP_NAME + " starting up on " + new Date(System.currentTimeMillis()).toString());
        log("Version: " + VERSION);
        
        // Auto-start the H2 server
        try {
            H2 = Server.createTcpServer().start();
            log("Successfully started H2");
        } catch (SQLException ex) {
            log("ERROR: Unable to start H2!");
            exit();
        }
        
        // Start our own server
        server = new TWServer();
        try {
            server.awaken();
        } catch (Exception ex) {
            log("ERROR: Unable to start webserver!");
            exit();
        }
        
        log(APP_NAME + " is up and running");
        log.logBreak(40);
        log.logBreak(0);
        
        // Finally add a hook to our exit handler (CMD enter "exit")
        new ExitHandler().start();
    }
    
    public static void main(String[] args) {
        enter();
    }
}

class ExitHandler extends Thread {

    @Override
    public void run() {
        Scanner input = new Scanner(System.in);
        while (!input.nextLine().equals("exit")) {
            // Do nothing
        }
        Twitterator.exit();
    }
}