package twitterator;

import java.util.Properties;
import org.eclipse.jetty.security.ConstraintMapping;
import org.eclipse.jetty.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.HashLoginService;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.log.StdErrLog;
import org.eclipse.jetty.util.security.Constraint;
import org.eclipse.jetty.util.security.Password;
import org.h2.server.web.WebServlet;

/**
 * Our custom server, using Jetty.
 *
 * @author William Gaul
 */
public class TWServer {

    private static final int WEBSITE_PORT = 8080;
    private static final String RESOURCE_BASE = "/webapp";
    private Server website;

    /**
     * Gets a basic resource handler, which serves up static files (HTML,
     * JavaScript, CSS, images, etc.).
     */
    private ResourceHandler getResourceHandler() {
        ResourceHandler rh = new ResourceHandler();
        rh.setResourceBase(TWServer.class.getResource(RESOURCE_BASE).toExternalForm());
        rh.setWelcomeFiles(new String[]{"index.html"});
        return rh;
    }

    /**
     * Constructs and returns our security handler. At the moment everything is
     * hard-coded which really sucks, but this should be fixed soon.
     */
    private ConstraintSecurityHandler getConstraintSecurityHandler() {
        // Create a new FORM security constraint
        Constraint constraint = new Constraint();
        constraint.setName(Constraint.__FORM_AUTH);
        constraint.setRoles(new String[]{"user", "admin", "moderator"});
        constraint.setAuthenticate(true);

        // Map the constraint to our entire site
        ConstraintMapping constraintMapping = new ConstraintMapping();
        constraintMapping.setConstraint(constraint);
        constraintMapping.setPathSpec("/*");

        // Add a security handler with a hash login service
        ConstraintSecurityHandler csh = new ConstraintSecurityHandler();
        csh.addConstraintMapping(constraintMapping);
        HashLoginService loginService = new HashLoginService();
        loginService.putUser(
                TWProperties.getProperty("SUPERUser"),
                new Password(TWProperties.getProperty("SUPERPass")),
                new String[]{"user"});
        csh.setLoginService(loginService);

        // Point the authenticator to our FORM at login.html
        FormAuthenticator authenticator = new FormAuthenticator("/login.html", "/login.html", false);
        csh.setAuthenticator(authenticator);

        return csh;
    }
    
    /**
     * Gets a servlet handler that maps the H2 Console to the /console URL path.
     * This allows us to remotely query databases through our console!
     * 
     * Don't forget to set -webAllowOthers = true for remote connections!
     */
    private ServletContextHandler getH2ConsoleHandler() {
        ServletContextHandler console = new ServletContextHandler(ServletContextHandler.SESSIONS);
        console.setContextPath("/");
        ServletHolder sh = new ServletHolder(new WebServlet());
        sh.setInitParameter("-webAllowOthers", "true");
        console.addServlet(sh, "/console/*");
        return console;
    }

    private void configure() {
        // Turn off jetty logging (it likes to warn too much)
        Properties p = new Properties();
        p.setProperty("org.eclipse.jetty.LEVEL", "OFF");
        StdErrLog.setProperties(p);

        website = new Server(WEBSITE_PORT);

        // List of handlers, called in order (console, then resource, then AJAX)
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{
            this.getH2ConsoleHandler(),
            this.getResourceHandler(),
            new TWHandler()
        });

        // Wrap a security handler around our site next
        ConstraintSecurityHandler csh = this.getConstraintSecurityHandler();
        csh.setHandler(handlers);

        // Set up a secure session for the site
        SessionHandler sh = new SessionHandler();
        sh.setHandler(csh);

        // Finally set the session handler as our global site handler
        website.setHandler(sh);
    }

    public void awaken() throws Exception {
        configure();
        website.start();
        Twitterator.log("The webserver is now running at http://localhost:" + WEBSITE_PORT);
    }

    public void banish() {
        Twitterator.log("Tearing down the webserver...");
        try {
            website.stop();
        } catch (Exception ex) {
            // Too bad, we're shutting down!
        }
    }
}
