/*
 * Source https://github.com/evanx by @evanxsummers

 */
package postgra.app;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import postgra.web.ErrorHttpHandler;
import postgra.web.PersonaLogin;
import postgra.web.PersonaLogout;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.api.CreateDatabase;
import postgra.api.CreateForeignKey;
import postgra.api.CreateIndex;
import postgra.api.CreatePrimaryKey;
import postgra.api.CreateTable;
import postgra.api.CreateUser;
import postgra.api.Delete;
import postgra.api.DropDatabase;
import postgra.api.DropTable;
import postgra.api.DropUser;
import postgra.api.Insert;
import postgra.api.Update;
import vellum.exception.Exceptions;
import vellum.httphandler.WebHttpHandler;
import vellum.jx.JMap;

/**
 *
 * @author evan.summers
 */
public class PostgraHttpService implements HttpHandler {

    private final static Logger logger = LoggerFactory.getLogger(PostgraHttpService.class);
    private final PostgraApp app;
    private final WebHttpHandler webHandler;
    private int requestCount = 0;
    private int requestCompletedCount = 0;

    public PostgraHttpService(PostgraApp app) {
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
                handle(new PersonaLogin(), httpExchange);
            } else if (path.equals("/api/personaLogout")) {
                handle(new PersonaLogout(), httpExchange);
            } else if (path.startsWith("/api/")) {
                handle(newHandler(path), httpExchange);
            } else {
                webHandler.handle(httpExchange);
            }
        } catch (RuntimeException e) {
            handle(httpExchange, e);
            e.printStackTrace(System.err);
        } catch (Exception e) {
            handle(httpExchange, e);
        } finally {
            requestCompletedCount++;
        }
    }

    private void handle(HttpExchange httpExchange, Throwable e) throws IOException {
        String path = httpExchange.getRequestURI().getPath();
        String errorMessage = Exceptions.getMessage(e);
        logger.warn("error {} {}", path, errorMessage);
        new ErrorHttpHandler(app).handle(httpExchange, errorMessage);
    }

    private PostgraHttpxHandler newHandler(String path) throws Exception {
        if (path.startsWith("/api/createDatabase")) {
            return new CreateDatabase();
        } else if (path.startsWith("/api/dropDatabase")) {
            return new DropDatabase();
        } else if (path.startsWith("/api/createUser")) {
            return new CreateUser();
        } else if (path.startsWith("/api/dropUser")) {
            return new DropUser();
        } else if (path.startsWith("/api/createTable")) {
            return new CreateTable();
        } else if (path.startsWith("/api/dropTable")) {
            return new DropTable();
        } else if (path.startsWith("/api/createIndex")) {
            return new CreateIndex();
        } else if (path.startsWith("/api/dropIndex")) {
        } else if (path.startsWith("/api/createCreatePrimaryKey")) {
            return new CreatePrimaryKey();
        } else if (path.startsWith("/api/dropCreatePrimaryKey")) {
        } else if (path.startsWith("/api/createForeignKey")) {
            return new CreateForeignKey();
        } else if (path.startsWith("/api/dropForeignKey")) {
        } else if (path.startsWith("/api/insert")) {
            return new Insert();
        } else if (path.startsWith("/api/update")) {
            return new Update();
        } else if (path.startsWith("/api/delete")) {
            return new Delete();
        }
        throw new Exception("Service not found: " + path);
    }

    private void handle(PostgraHttpxHandler handler, HttpExchange httpExchange) {
        handle(handler, new PostgraHttpx(app, httpExchange));
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

    public JMap getMetrics() {
        JMap map = new JMap();
        map.put("requestCount", requestCount);
        map.put("requestCompletedCount", requestCompletedCount);
        return map;
    }
}
