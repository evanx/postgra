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

import java.security.GeneralSecurityException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.entity.Person;
import vellum.crypto.asymetric.AsymmetricCipher;
import vellum.httpserver.VellumHttpsServer;
import vellum.jx.JMap;
import vellum.jx.JMaps;
import vellum.mail.Mailer;
import vellum.ssl.OpenTrustManager;
import vellum.util.Base64;


/**
 *
 * @author evan.summers
 */
public class PostgraApp {

    Logger logger = LoggerFactory.getLogger(PostgraApp.class);
    static final String persistenceUnit = "postgraPU";
    PostgraProperties properties;
    Mailer mailer;
    VellumHttpsServer webServer = new VellumHttpsServer();
    EntityManagerFactory emf;
    boolean initalized = false;
    boolean running = true;
    Thread initThread = new InitThread();
    Thread messageThread = new MessageThread(this);
    Deque messageQueue = new ArrayDeque();
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    DataSourceManager dataSourceManager = new DataSourceManager();
    AsymmetricCipher cipher = new AsymmetricCipher();
    
    public PostgraApp() {
        super();
    }

    public void init(PostgraProperties properties) throws Exception {
        this.properties = properties;
        cipher.generateKeyPair();
        mailer = new Mailer(properties.getMailerProperties());
        logger.info("properties {}", properties);
        webServer.start(properties.getWebServer(),
                new OpenTrustManager(),
                new PostgraHttpService(this));
        initThread.start();
    }

    public void ensureInitialized() throws InterruptedException {
        if (!initalized) {
            logger.info("ensureInitialized");
            if (initThread.isAlive()) {
                initThread.join();
            }
            logger.info("ensureInitialized complete");
        }
    }

    public void initDeferred() throws Exception {
        emf = Persistence.createEntityManagerFactory(persistenceUnit);
        initalized = true;
        logger.info("initialized");
        if (false) {
            messageThread.start();
        }
        logger.info("started");
    }

    public String encrypt(JMap responseMap) throws GeneralSecurityException {
        return Base64.encode(cipher.encrypt(responseMap.toJson().getBytes()));
    }    

    public JMap decrypt(String encryptedString) throws GeneralSecurityException {
        return JMaps.parseMap(new String(cipher.decrypt(Base64.decode(encryptedString))));
    }
    
    class InitThread extends Thread {

        @Override
        public void run() {
            try {
                initDeferred();
            } catch (Exception e) {
                logger.warn("init", e);
            }
        }
    }

    public void shutdown() throws Exception {
        logger.info("shutdown");
        running = false;
        executorService.shutdown();
        if (webServer != null) {
            webServer.shutdown();
        }
        if (messageThread != null && messageThread.isAlive()) {
            messageThread.interrupt();
            messageThread.join(2000);
        }
        logger.info("shutdown complete");
    }

    class MessageThread extends Thread {

        PostgraApp app;

        public MessageThread(PostgraApp app) {
            this.app = app;
        }

        @Override
        public void run() {
            while (running && !messageQueue.isEmpty()) {
                try {
                    Object message = messageQueue.poll();
                } catch (Throwable t) {
                    logger.warn("run", t);
                }
            }
        }
    }

    public EntityManager newEntityManager() {
        return emf.createEntityManager();
    }

    public PostgraEntityService newEntityService() {
        return new PostgraEntityService(emf.createEntityManager());
    }
    
    public PostgraEntityService beginEntityService() {
        PostgraEntityService es = newEntityService();
        es.begin();
        return es;
    }
    
    public PostgraProperties getProperties() {
        return properties;
    }

    public Mailer getMailer() {
        return mailer;
    }

    public DataSourceManager getDataSourceManager() {
        return dataSourceManager;
    }        

    public AsymmetricCipher getCipher() {
        return cipher;
    }
    
    public String authenticateGuest(JMap map) throws Exception {
        String user = map.getString("user");
        String password = map.getString("password");
        EntityManager em = emf.createEntityManager();
        try {
            Person person = em.find(Person.class, user);
            if (person != null) {
                if (person.matchesPassword(password.toCharArray())) {
                    return user;
                } else {
                    throw new PersistenceException("Invalid password: " + user);
                }                        
            } else {
                throw new PersistenceException("User not found: " + user);
            }
        } finally {
            em.close();
        }
    }
    
    public boolean authenticatePersonPassword(String email, char[] password) throws Exception {
        EntityManager em = emf.createEntityManager();
        try {
            Person person = em.find(Person.class, email);
            if (person != null) {
                return person.matchesPassword(password);
            } else {
                return false;
            }
        } finally {
            em.close();
        }
    }

    
}
