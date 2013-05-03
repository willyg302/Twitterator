package twitterator;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Essentially just lets the server know which class to instantiate for
 * WebSocket connections (in this case, the PingSocket class).
 * 
 * @author William Gaul
 */
public class TWServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory wssf) {
        wssf.register(PingSocket.class);
    }
}