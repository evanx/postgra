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
package postgra.web;

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
