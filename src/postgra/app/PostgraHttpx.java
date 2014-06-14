/*
    Source https://github.com/evanx by @evanxsummers

        Licensed to the Apache Software Foundation (ASF) under one
        or more contributor license agreements. See the NOTICE file
        distributed with this work for additional information
        regarding copyright ownership. The ASF licenses this file to
        you under the Apache License, Version 2.0 (the "License").
        You may not use this file except in compliance with the
        License. You may obtain a copy of the License at:

           http://www.apache.org/licenses/LICENSE-2.0

        Unless required by applicable law or agreed to in writing,
        software distributed under the License is distributed on an
        "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
        KIND, either express or implied.  See the License for the
        specific language governing permissions and limitations
        under the License.  
 */
package postgra.app;

import com.sun.net.httpserver.HttpExchange;
import postgra.persona.PersonaException;
import postgra.persona.PersonaInfo;
import postgra.persona.PersonaVerifier;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.httpserver.Httpx;
import vellum.jx.JMapsException;

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

    public void ensureAllowed() throws GeneralSecurityException {
        String remoteHostAddress = getRemoteHostAddress();
        if (!app.getProperties().isAdminAddress(remoteHostAddress)) {
            throw new GeneralSecurityException("Not allowed: " + remoteHostAddress);
        }
    }
    
    public PostgraCookie getCookie() throws JMapsException {
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

    public String getEmail() throws JMapsException, IOException, PersonaException {
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

    public TimeZone getTimeZone() throws JMapsException {
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
