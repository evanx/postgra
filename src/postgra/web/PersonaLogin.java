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
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.entity.Person;
import postgra.persona.PersonaInfo;
import postgra.persona.PersonaVerifier;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraEntityService;
import vellum.jx.JMap;

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
    public JMap handle(PostgraApp app, PostgraHttpx httpx) throws Exception {
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
        try (PostgraEntityService es = app.newEntityService()) {
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
}
