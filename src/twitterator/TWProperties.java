package twitterator;

import java.io.IOException;
import java.util.Properties;

/**
 * Read-only.
 * 
 * @author William
 */
public class TWProperties {
    private static Properties prop = new Properties();
    
    private static final String PROP_FILE = "/config.properties";
    
    public static void load() {
        try {
            prop.load(TWProperties.class.getResourceAsStream(PROP_FILE));
        } catch (IOException ex) {
            System.err.println("Unable to load Twitterator properties file!");
        }
    }
    
    public static String getProperty(String key) {
        return prop.getProperty(key);
    }
}
