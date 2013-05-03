package twitterator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;


/**
 * Our WebSocket implementation, called a PingSocket because it routinely
 * pings clients to keep them alive (and push stuff to them if necessary,
 * see below). Annotated with POJO annotations.
 * 
 * @author William Gaul
 */
@WebSocket
public class PingSocket {

    private static class KeepAlive extends Thread {

        private CountDownLatch latch;
        private Session session;
        
        /**
         * We ping clients every 5 seconds. This is necessary in order to
         * verify that connections are still open, because if the client has
         * closed their browser window (etc.) then the ping will throw an
         * error.
         */
        private static final int PING_TIME = 5;

        public KeepAlive(Session session) {
            this.session = session;
        }

        @Override
        public void run() {
            try {
                while (!latch.await(PING_TIME, TimeUnit.SECONDS)) {
                    System.err.println("Ping");
                    ByteBuffer data = ByteBuffer.allocate(3);
                    data.put(new byte[]{(byte) 1, (byte) 2, (byte) 3});
                    data.flip();
                    session.getRemote().sendPing(data);

                    
                    /*
                    String alert = "Hello!" + data;
                    this.session.getRemote().sendStringByFuture(alert);
                    System.out.println(alert);
*/

                    
                }
            } catch (Exception e) {
                System.out.println("ERROR!");
                try {
                    session.close();
                } catch (IOException ex) {
                    //
                }
            }
        }

        public void shutdown() {
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public synchronized void start() {
            latch = new CountDownLatch(1);
            super.start();
        }
    }
    
    private static final ConcurrentLinkedQueue<PingSocket> BROADCAST = new ConcurrentLinkedQueue<PingSocket>();
    private PingSocket.KeepAlive keepAlive; // A dedicated thread is not a good way to do this
    protected Session session;

    @OnWebSocketMessage
    public void onBinary(byte buf[], int offset, int len) {
        ByteBuffer data = ByteBuffer.wrap(buf, offset, len);
        for (PingSocket sock : BROADCAST) {
            sock.session.getRemote().sendBytesByFuture(data.slice());
        }
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        System.out.println("Closing WebSocket connection");
        keepAlive.shutdown();
        BROADCAST.remove(this);
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        System.out.println("Opening WebSocket connection w/ " + BROADCAST.size() + " currently active connections");
        if (keepAlive == null) {
            keepAlive = new PingSocket.KeepAlive(session);
        }
        keepAlive.start();
        this.session = session;
        BROADCAST.add(this);
    }

    /**
     * Handles messages received from the client (for example, what they
     * enter into the Omnibox). If the message isn't a special case, it is
     * simply broadcasted to all clients.
     */
    @OnWebSocketMessage
    public void onText(String text) {
        
        System.out.println(text);
        // Client is requesting username
        if (text.startsWith("username")) {
            //HttpSession sess = (HttpSession)this.session.getUpgradeRequest().getSession();
            //System.out.println((String)sess.getAttribute("username"));
            
            String username = session.getRemoteAddress().getHostName();
            System.out.println(username);
            
            this.session.getRemote().sendStringByFuture("username:" + username);
            return;
        }
        
        // Finally, we just broadcast it all
        for (PingSocket sock : BROADCAST) {
            sock.session.getRemote().sendStringByFuture(text);
        }
    }
}