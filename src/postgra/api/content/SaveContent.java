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
package postgra.api.content;

import java.util.Date;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.entity.Content;
import postgra.entity.Person;
import vellum.jx.JMap;
import vellum.jx.JMapException;
import vellum.util.Streams;

/**
 *
 * @author evan.summers
 */
public class SaveContent implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(SaveContent.class); 

    Date now = new Date();
    JMap responseMap = new JMap();

    final String pattern = "/api/content/";
    
    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx) throws Exception {
        logger.info("handle {}", httpx.getPath());
        PostgraEntityService es = app.beginEntityService();
        try {
            String email = httpx.getRequestHeader("email");
            String contentType = httpx.getRequestHeader("content-type");
            String cacheSecondsString = httpx.getRequestHeader("cache-seconds");
            if (email == null) {
                logger.warn("no email");
            } else if (false) {
                String accessToken = httpx.getRequestHeader("accessToken");
                // TODO auth
                Person person = es.findPerson(email);
                if (person == null) {
                    throw new PersistenceException("Email not found: " + email);
                }
                logger.info("person {}", person);
            }
            String path = httpx.getPath().substring(pattern.length());
            logger.info("content", path);
            Content content = es.findContent(path);
            if (content == null) {
                content = new Content(path);
                content.setCreated(now);
            } else {
                content.setModified(now);
            }
            if (cacheSecondsString != null) {
                content.setCacheSeconds(Integer.parseInt(cacheSecondsString));
            }
            content.setContentType(contentType);
            byte[] bytes = Streams.readBytes(httpx.getInputStream());
            logger.info("content length", bytes.length);
            content.setContent(bytes);
            es.merge(content);
            es.commit();
            responseMap.put("email", email);
            responseMap.put("path", path);
            responseMap.put("contentType", contentType);
            responseMap.put("id", content.getId());
            return responseMap;
        } catch (PersistenceException e) {
            throw new JMapException(responseMap, e.getMessage());
        } finally {
            es.close();
        }
    }
}
