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
            if (path.equals("/app/personaLogin")) {
                handle(new PersonaLogin(), new PostgraHttpx(app, httpExchange));
            } else if (path.equals("/app/personaLogout")) {
                handle(new PersonaLogout(), new PostgraHttpx(app, httpExchange));
            } else if (path.startsWith("/app/")) {
                String handlerName = getHandlerName(path);
                if (handlerName != null) {
                    handle(getHandler(handlerName), new PostgraHttpx(app, httpExchange));
                } else {
                    new ErrorHttpHandler(app).handle(httpExchange, "Service not found: " + path);
                }
            } else {
                webHandler.handle(httpExchange);
            }
        } catch (Throwable e) {
            String errorMessage = Exceptions.getMessage(e);
            logger.warn("error {} {}", path, errorMessage);
            e.printStackTrace(System.err);
            new ErrorHttpHandler(app).handle(httpExchange, errorMessage);
        } finally {
            requestCompletedCount++;
        }
    }

    private String getHandlerName(String path) {
        int index = path.lastIndexOf("/forwarded");
        if (index > 0) {
            path = path.substring(0, index);
        }
        final String handlerPathPrefix = "/encryptoapp/";
        if (path.startsWith(handlerPathPrefix)) {
            return path.substring(handlerPathPrefix.length());
        }
        return null;
    }

    private PostgraHttpxHandler getHandler(String handlerName) throws ClassNotFoundException,
            InstantiationException, IllegalAccessException {
        String className = "encrypto.handler."
                + Character.toUpperCase(handlerName.charAt(0)) + handlerName.substring(1);
        logger.trace("handler {}", className);
        return (PostgraHttpxHandler) Class.forName(className).newInstance();
    }

    private void handle(PostgraHttpxHandler handler, PostgraHttpx httpx) {
        PostgraEntityService es = new PostgraEntityService(app);
        try {
            es.begin();
            JMap responseMap = handler.handle(app, httpx, es);
            logger.trace("response {}", responseMap);
            httpx.sendResponse(responseMap);
            es.commit();
        } catch (Throwable e) {
            httpx.sendError(e);
            es.rollback();
            e.printStackTrace(System.out);
        } finally {
            es.close();
            httpx.close();
        }
    }

    private void handle(PlainHttpxHandler handler, PostgraHttpx httpx) {
        PostgraEntityService es = new PostgraEntityService(app);
        try {
            es.begin();
            String response = handler.handle(app, httpx, es);
            logger.trace("response {}", response);
            httpx.sendPlainResponse(response);
            es.commit();
        } catch (Throwable e) {
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
