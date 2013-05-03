package twitterator;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

/**
 * Our custom log.
 *
 * @author William Gaul
 */
public class Log {

    private PrintWriter log;

    public Log() {
        setup();
    }

    public final void setup() {
        try {
            log = new PrintWriter(new BufferedWriter(new FileWriter(Twitterator.APP_NAME + "-log.txt", true)), true);
        } catch (IOException ex) {
            System.err.println("ERROR: Could not initialize log file.");
        }
    }

    public void close() {
        log.close();
    }

    public void log(String s) {
        System.out.println(s);
        log.println(s);
    }

    public void logTime(String s) {
        s = ("[" + DateFormat.getTimeInstance().format(new Date(System.currentTimeMillis())) + "] ") + s;
        log(s);
    }

    /**
     * Inserts a line of dashes (-----) of the given length. length = 0 is
     * equivalent to a blank line (printing "\n").
     */
    public void logBreak(int length) {
        String s = "";
        for (int i = 0; i < length; i++) {
            s += "-";
        }
        log(s);
    }
}