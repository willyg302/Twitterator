package twitterator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.DefaultHandler;

/**
 * A special AJAX handler. This is defined as the "last resort" for our server
 * if everything else fails. Thus, the way to roll queries that will reach
 * here from the client side is to send them through a bogus GET request.
 * 
 * Methods are called via reflection. Each defined method must exactly match
 * the "query" parameter passed through AJAX, and must take the following
 * parameters:
 *    - String user (the name of the user who initiated the request)
 *    - HttpServletRequest request
 *    - HttpServletResponse response
 * 
 * If reflection throws an error, we assume that the query was undefined and
 * pass along the request (this usually results in an HTTP error).
 * 
 * The response is encoded as an XML object.
 * 
 * @author William Gaul
 */
public class TWHandler extends DefaultHandler {
    
    private TWUtils tu;
    
    public TWHandler() {
        try {
            tu = new TWUtils();
        } catch (Exception ex) {
            throw new IllegalArgumentException("ERROR: Couldn't initialize Twitterator bridge!");
        }
    }
    
    
    
    private void startup(String user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Twitterator.logTime("User " + user + " has logged in");
        
        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        response.setStatus(HttpServletResponse.SC_OK);
        
        XMLResponse xmlr = new XMLResponse();
        xmlr.addLMPair("username", user);
        xmlr.addLMPair("dblist", tu.getHTMLFormattedDBList());
        
        response.getWriter().write(xmlr.getString());
        response.flushBuffer();
    }
    
    private void logout(String user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        Twitterator.logTime("User " + user + " has logged out");
        request.getSession(false).invalidate();
        response.sendRedirect("/login.html");
    }
    
    private void download(String user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String fileName = request.getParameter("name") + "." + request.getParameter("type").toLowerCase();
        
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        ServletOutputStream out =  response.getOutputStream();
        FileInputStream fis = new FileInputStream(new File(fileName));
        int n = 0;
        while ((n = fis.read()) != -1) {
            out.write(n);
        }
        out.flush();
        out.close();
    }
    
    /**
     * A query direct from the user through the main omnibox. We perform all
     * parsing and calling of respective methods through here.
     */
    private void omnibox(String user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String action = request.getParameter("action");
        String prompt = request.getParameter("prompt");
        String db = request.getParameter("db");
        Twitterator.logTime("Servicing: " + action + " " + prompt + " " + db);
        
        String resp = tu.handle(action, prompt, db);
        //System.out.println(resp);
        
        response.setContentType("text/xml");
        response.setHeader("Cache-Control", "no-cache");
        response.setStatus(HttpServletResponse.SC_OK);
        
        XMLResponse xmlr = new XMLResponse();
        xmlr.addLMPair("prompt", resp);
        // Auto-update DB list if it was changed
        if (action.equals("createDB")) {
            xmlr.addLMPair("dblist", tu.getHTMLFormattedDBList());
        }
        
        response.getWriter().write(xmlr.getString());
        response.flushBuffer();
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        String query = request.getParameter("query");
        boolean handled = true;
        
        // This is how you get the NAME of the user who logged in!
        String user = baseRequest.getRemoteUser();
        
        try {
            Method method = this.getClass().getDeclaredMethod(query, new Class[] {
                String.class,
                HttpServletRequest.class,
                HttpServletResponse.class
            });
            method.invoke(this, new Object[] {user, request, response});
        } catch (Exception ex) {
            handled = false;
        }
        
        baseRequest.setHandled(handled);
    }
}
