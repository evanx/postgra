/*
 * Source https://github.com/evanx by @evanxsummers
 */
package postgra.app;

import com.sun.net.httpserver.HttpExchange;
import postgra.persona.PersonaException;
import postgra.persona.PersonaInfo;
import postgra.persona.PersonaVerifier;
import java.io.IOException;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.httpserver.Httpx;
import vellum.jx.JMapException;

/**
 *
 * @author evan.summers
 */
public class PostgraHttpx extends Httpx {

    static Logger logger = LoggerFactory.getLogger(PostgraHttpx.class);

    PostgraApp app;
    PostgraCookie cookie;

    public PostgraHttpx(PostgraApp app, HttpExchange delegate) {
        super(delegate);
        this.app = app;
    }

    public PostgraCookie getCookie() throws JMapException {
        if (cookie == null) {
            if (PostgraCookie.matches(getCookieMap())) {
                cookie = new PostgraCookie(getCookieMap());
            } else {
                logger.warn("cookie not matching");
                return null;
            }
        }
        return cookie;
    }

    public String getEmail() throws JMapException, IOException, PersonaException {
        if (getCookie() != null) {
            if (cookie.getEmail() != null) {
                if (app.properties.isTesting()) {
                    return cookie.getEmail();
                }
                PersonaInfo personInfo = new PersonaVerifier(app, cookie).
                        getPersonaInfo(getHostUrl(), cookie.getAssertion());
                if (!cookie.getEmail().equals(personInfo.getEmail())) {
                    logger.warn("email differs: persona {}, cookie {}", personInfo.getEmail(), cookie.getEmail());
                } else {
                    return personInfo.getEmail();
                }
            }
        }
        logger.warn("getEmail cookie {}", getCookieMap());
        setCookie(PostgraCookie.emptyMap(), PostgraCookie.MAX_AGE_MILLIS);
        throw new PersonaException("no verified email");
    }

    public TimeZone getTimeZone() throws JMapException {
        if (getCookie() != null) {
            return getTimeZone(cookie.getTimeZoneOffset());
        }
        return TimeZone.getDefault();
    }

    public static TimeZone getTimeZone(int timeZoneOffset) {
        String[] timeZoneIds = TimeZone.getAvailableIDs(timeZoneOffset);
        logger.info("timeZoneIds {}", timeZoneIds);
        if (timeZoneIds.length > 0) {
            String timeZoneId = timeZoneIds[0];
            return TimeZone.getTimeZone(timeZoneId);
        }
        return TimeZone.getTimeZone(String.format("GMT%+03d", timeZoneOffset));
    }
}
