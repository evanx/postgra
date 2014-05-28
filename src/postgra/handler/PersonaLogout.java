/*
 * Source https://github.com/evanx by @evanxsummers
 * 
 */
package postgra.handler;

import postgra.app.PostgraApp;
import postgra.app.PostgraCookie;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.entity.Person;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evan.summers
 */
public class PersonaLogout implements PostgraHttpxHandler {

    static Logger logger = LoggerFactory.getLogger(PersonaLogout.class);
    PostgraCookie cookie;
    
    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) 
            throws Exception {
        logger.info("handle", getClass().getSimpleName(), httpx.getPath());
        try {
            String email = httpx.parseJsonMap().getString("email");
            if (PostgraCookie.matches(httpx.getCookieMap())) {
                cookie = new PostgraCookie(httpx.getCookieMap());
                logger.debug("cookie {}", cookie.getEmail());
                if (!cookie.getEmail().equals(email)) {
                    logger.warn("email {}", email);
                }
                if (app.getProperties().isTesting()) {
                    logger.info("testing mode: ignoring logout");
                } else {
                    logger.info("cookie", cookie.getEmail());
                    Person user = es.findPerson(cookie.getEmail());
                    user.setLogoutTime(new Date());
                }
            }
            return new JMap();
        } finally {
            httpx.setCookie(PostgraCookie.emptyMap(), PostgraCookie.MAX_AGE_MILLIS);
        }
    }
}
