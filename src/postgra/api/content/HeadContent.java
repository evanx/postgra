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

import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxContentHandler;
import postgra.entity.Content;
import vellum.jx.JMap;
import vellum.jx.JMapException;

/**
 *
 * @author evan.summers
 */
public class HeadContent implements PostgraHttpxContentHandler {
    
    static Logger logger = LoggerFactory.getLogger(HeadContent.class); 

    JMap responseMap = new JMap();

    final String pattern = "/api/content/";
    
    @Override
    public void handle(PostgraApp app, PostgraHttpx httpx) throws Exception {
        logger.info("handle", httpx.getPath());
        try (PostgraEntityService es = app.beginEntityService()) {
            String path = httpx.getPath().substring(pattern.length());
            Content content = es.findContent(path);
            if (content == null) {
                throw new PersistenceException();
            }
            httpx.sendResponseHeaders(content.getContentType(), content.getContentLength());
        } catch (PersistenceException e) {
            throw new JMapException(responseMap, e.getMessage());
        }
    }
}
