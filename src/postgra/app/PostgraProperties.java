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
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JConsoleMap;
import vellum.jx.JMap;
import vellum.jx.JMaps;
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
    List<String> adminEmails;
    List<String> adminAddresses;
    MailerProperties mailerProperties = new MailerProperties();
    MockableConsole console; 
    
    public PostgraProperties() throws Exception {
        super(new SystemConsole(), JMaps.parseMap(Streams.readString(new File("config.json"))));
        publicKeyFile = getString("publicKeyFile");
        privateKeyFile = getString("privateKeyFile");
        appHost = getString("appHost");
        testing = getBoolean("testing", testing);
        adminEmails = getList("adminEmails");
        adminAddresses = getList("adminAddresses");
        webServer = getMap("webServer");
        mailerProperties.init(getMap("mailer"));
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

    public List<String> getAdminAddresses() {
        return adminAddresses;
    }
    
    public boolean isAdminAddress(String address) {
        return adminAddresses.contains(address);
    }
}
