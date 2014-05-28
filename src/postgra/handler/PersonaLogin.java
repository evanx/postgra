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
import postgra.persona.PersonaInfo;
import postgra.persona.PersonaVerifier;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;
import vellum.util.Lists;

/**
 *
 * @author evan.summers
 */
public class PersonaLogin implements PostgraHttpxHandler {

    static Logger logger = LoggerFactory.getLogger(PersonaLogin.class);
    String assertion;
    int timezoneOffset;
    PostgraCookie cookie;
    
    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) 
            throws Exception {
        JMap map = httpx.parseJsonMap();
        timezoneOffset = map.getInt("timezoneOffset");
        logger.trace("timezoneOffset {}", timezoneOffset);
        assertion = map.getString("assertion");
        if (PostgraCookie.matches(httpx.getCookieMap())) {
            cookie = new PostgraCookie(httpx.getCookieMap());
        }
        PersonaInfo userInfo = new PersonaVerifier(app, cookie).getPersonaInfo(
                httpx.getHostUrl(), assertion);
        logger.trace("persona {}", userInfo);
        String email = userInfo.getEmail();
        Person person = es.findPerson(email);
        if (person == null) {
            person = new Person(email);
            person.setEnabled(true);
            person.setLoginTime(new Date());
            es.persist(person);
            logger.info("insert user {}", email);
        } else {
            person.setEnabled(true);
            person.setLoginTime(new Date());
        }
        cookie = new PostgraCookie(person.getEmail(), person.getLabel(),
                person.getLoginTime().getTime(), timezoneOffset, assertion);
        JMap cookieMap = cookie.toMap();
        logger.trace("cookie {}", cookieMap);
        cookieMap.put("timezoneOffset", timezoneOffset);
        httpx.setCookie(cookieMap, PostgraCookie.MAX_AGE_MILLIS);
        return cookieMap;
    }
}
