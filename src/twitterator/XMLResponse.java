package twitterator;

/**
 * Just makes it easier to format responses for AJAX requests. Allows for
 * incremental building of an XML document, which will hopefully make it easier
 * to make an extensible system later.
 * 
 * @author William Gaul
 */
public class XMLResponse {
    private String str;
    
    public XMLResponse() {
        str = "<doc>";
    }
    
    /**
     * Adds a location/message pair to the XML message.
     */
    public void addLMPair(String location, String message) {
        str += "<location>" + location + "</location><message>" + message + "</message>";
    }
    
    public String getString() {
        return str + "</doc>";
    }
}
