/*
 * Source https://github.com/evanx by @evanxsummers
 */
package postgra.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import vellum.jx.JMap;

/**
 *
 * @author evan.summers
 */
public class CreateDatabase implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(CreateDatabase.class); 

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap response = new JMap();
        response.put("pathArgs", httpx.getPathArgs());
        return response;
    }
}
