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
package postgra.persona;

import postgra.app.PostgraApp;
import postgra.app.PostgraCookie;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.json.JsonObjectDelegate;
import vellum.jx.JMapException;

/**
 *
 * @author evan.summers
 */
public class PersonaVerifier {

    static Logger logger = LoggerFactory.getLogger(PersonaVerifier.class);

    PostgraApp app; 
    PostgraCookie cookie;
    
    public PersonaVerifier(PostgraApp app) {
        this.app = app;
    }

    public PersonaVerifier(PostgraApp app, PostgraCookie cookie) {
        this.app = app;
        this.cookie = cookie;
    }
        
    public PersonaInfo getPersonaInfo(String serverUrl, String assertion) 
            throws IOException, JMapException, PersonaException {
        logger.trace("getUserInfo {}", serverUrl);
        URL url = new URL("https://verifier.login.persona.org/verify");
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-type", "application/x-www-form-urlencoded");
        connection.setDoOutput(true);
        StringBuilder builder = new StringBuilder();
        builder.append("assertion=").append(URLEncoder.encode(assertion, "UTF-8"));
        builder.append("&audience=").append(URLEncoder.encode(serverUrl, "UTF-8"));
        logger.trace("persona {} {}", url, builder.toString());
        connection.getOutputStream().write(builder.toString().getBytes());
        JsonObjectDelegate object = new JsonObjectDelegate(connection.getInputStream());
        if (object.hasProperty("status")) {
            String status = object.getString("status");
            if (status.equals("okay")) {
                logger.trace("persona", object.getMap().toString());
                return new PersonaInfo(object.getMap());
            } else {
                String reason = object.getString("reason");
                logger.debug("{}: {}", status, reason);
                if (reason.equals("assertion has expired")) {
                    if (app.getProperties().isTesting() && cookie != null) {
                        return new PersonaInfo(cookie.getEmail());
                    }
                }
                throw new PersonaException(status, reason);
            }
        } else {
            logger.warn("status {}", object.keySet());
            throw new PersonaException("status not found");
        }
    }
}
