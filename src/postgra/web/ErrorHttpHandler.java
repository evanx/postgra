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

import com.sun.net.httpserver.HttpExchange;
import postgra.app.PostgraApp;
import java.io.IOException;
import java.net.HttpURLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMaps;

/**
 *
 * @author evan.summers
 */
public class ErrorHttpHandler {

    static Logger logger = LoggerFactory.getLogger(ErrorHttpHandler.class);

    PostgraApp app ;

    public ErrorHttpHandler(PostgraApp app) {
        this.app = app;
    }
        
    public void handle(HttpExchange http, String errorMessage) throws IOException {
        try {
            String path = http.getRequestURI().getPath();
            String errorResponse = JMaps.mapValue("errorMessage", errorMessage).toJson();
            sendResponse(http, "plain/json", errorResponse.getBytes());
        } finally {
            http.close();
        }
    }

    public static void sendResponse(HttpExchange http, String contentType, byte[] bytes) 
            throws IOException {
        http.getResponseHeaders().set("Content-type", contentType);
        http.getResponseHeaders().set("Content-length", Integer.toString(bytes.length));
        http.sendResponseHeaders(HttpURLConnection.HTTP_OK, bytes.length);
        http.getResponseBody().write(bytes);
    }
}
