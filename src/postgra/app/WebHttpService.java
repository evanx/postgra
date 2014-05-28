/*
 * Source https://github.com/evanx by @evanxsummers

 */
package postgra.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import postgra.handler.ErrorHttpHandler;
import postgra.handler.PersonaLogin;
import postgra.handler.PersonaLogout;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.service.CreateDatabase;
import vellum.exception.Exceptions;
import vellum.httphandler.WebHttpHandler;
import vellum.jx.JMap;

/**
 *
 * @author evan.summers
 */
public class WebHttpService implements HttpHandler {

    private final static Logger logger = LoggerFactory.getLogger(WebHttpService.class);
    private final PostgraApp app;
    private final WebHttpHandler webHandler;
    private int requestCount = 0;
    private int requestCompletedCount = 0;

    public WebHttpService(PostgraApp app) {
        this.app = app;
        webHandler = new WebHttpHandler("/frontend/app", app.getProperties());
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        requestCount++;
        String path = httpExchange.getRequestURI().getPath();
        logger.info("handle {}", path);
        Thread.currentThread().setName(path);
        try {
            app.ensureInitialized();
            if (path.equals("/api/personaLogin")) {
                handle(new PersonaLogin(), new PostgraHttpx(app, httpExchange));
            } else if (path.equals("/api/personaLogout")) {
                handle(new PersonaLogout(), new PostgraHttpx(app, httpExchange));
            } else if (path.startsWith("/api/")) {
                if (path.startsWith("/api/createDatabase")) {
                    handle(new CreateDatabase(), new PostgraHttpx(app, httpExchange));
                } else {
                    new ErrorHttpHandler(app).handle(httpExchange, "Service not found: " + path);
                }
            } else {
                webHandler.handle(httpExchange);
            }
        } catch (RuntimeException e) {
            handle(httpExchange, e);
        } catch (InterruptedException | IOException e) {
            handle(httpExchange, e);
        } finally {
            requestCompletedCount++;
        }
    }

    private void handle(HttpExchange httpExchange, Throwable e) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String errorMessage = Exceptions.getMessage(e);
        logger.warn("error {} {}", path, errorMessage);
        e.printStackTrace(System.err);
        new ErrorHttpHandler(app).handle(httpExchange, errorMessage);
    }

    private void handle(PostgraHttpxHandler handler, PostgraHttpx httpx) {
        PostgraEntityService es = new PostgraEntityService(app);
        try {
            es.begin();
            JMap responseMap = handler.handle(app, httpx, es);
            logger.trace("response {}", responseMap);
            httpx.sendResponse(responseMap);
            es.commit();
        } catch (RuntimeException e) {
            handleError(httpx, es, e);
        } catch (Exception e) {
            handleError(httpx, es, e);
        } finally {
            es.close();
            httpx.close();
        }
    }

    private void handleError(PostgraHttpx httpx, PostgraEntityService es, Throwable e) {
        httpx.sendError(e);
        es.rollback();
        e.printStackTrace(System.out);
    }

    private void handle(PlainHttpxHandler handler, PostgraHttpx httpx) {
        PostgraEntityService es = new PostgraEntityService(app);
        try {
            es.begin();
            String response = handler.handle(app, httpx, es);
            logger.trace("response {}", response);
            httpx.sendPlainResponse(response);
            es.commit();
        } catch (RuntimeException e) {
            httpx.sendPlainError(String.format("ERROR: %s\n", e.getMessage()));
            es.rollback();
            e.printStackTrace(System.out);
        } catch (Exception e) {
            httpx.sendPlainError(String.format("ERROR: %s\n", e.getMessage()));
            es.rollback();
            e.printStackTrace(System.out);
        } finally {
            es.close();
        }
    }

    public JMap getMetrics() {
        JMap map = new JMap();
        map.put("requestCount", requestCount);
        map.put("requestCompletedCount", requestCompletedCount);
        return map;
    }
}
