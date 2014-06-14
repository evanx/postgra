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

import java.io.File;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.json.JsonObjectDelegate;
import vellum.jx.JConsoleMap;
import vellum.jx.JMap;
import vellum.mail.MailerProperties;
import vellum.system.SystemConsole;
import vellum.util.MockableConsole;
import vellum.util.Streams;

/**
 *
 * @author evan.summers
 */
public class PostgraProperties extends JConsoleMap {

    static Logger logger = LoggerFactory.getLogger(PostgraProperties.class);

    JMap properties;
    JMap webServer;
    String appHost;
    String privateKeyFile;
    String publicKeyFile;
    boolean testing = false;
    Set<String> adminEmails = new HashSet();
    MailerProperties mailerProperties = new MailerProperties();
    MockableConsole console; 
    
    public PostgraProperties() throws Exception {
        this(new SystemConsole(), new Properties());
    } 
    
    public PostgraProperties(MockableConsole console, Properties properties) throws Exception {
        super(console, properties);
        String jsonConfigFileName = getString("config.json", "config.json");
        JsonObjectDelegate object = new JsonObjectDelegate(new File(jsonConfigFileName));
        putAll(object.getMap());
        publicKeyFile = object.getString("publicKeyFile");
        privateKeyFile = object.getString("privateKeyFile");
        appHost = object.getString("appHost");
        testing = object.getBoolean("testing", testing);
        adminEmails = object.getStringSet("adminEmails");
        webServer = object.getMap("webServer");
        mailerProperties.init(object.getMap("mailer"));
        mailerProperties.setLogoBytes(Streams.readBytes(getClass().getResourceAsStream("/resources/app48.png")));
        logger.info("mailer {}", mailerProperties);
    }

    public String getAppHost() {
        return appHost;
    }

    public boolean isTesting() {
        return testing;
    }

    public MailerProperties getMailerProperties() {
        return mailerProperties;
    }

    public JConsoleMap getWebServer() {
        return new JConsoleMap(console, webServer);
    }       
    
}
