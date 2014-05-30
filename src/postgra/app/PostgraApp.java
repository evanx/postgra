/*
 * Source https://github.com/evanx by @evanxsummers

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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.httpserver.VellumHttpsServer;
import vellum.mail.Mailer;
import vellum.ssl.OpenTrustManager;


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
    ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public PostgraApp() {
        super();
    }

    public void init(PostgraProperties properties) throws Exception {
        this.properties = properties;
        mailer = new Mailer(properties.getMailerProperties());
        logger.info("properties {}", properties);
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
        webServer.start(properties.getWebServer(),
                new OpenTrustManager(),
                new PostgraHttpService(this));
        messageThread.start();
        logger.info("started");
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
        if (messageThread != null) {
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
            while (running) {
                try {
                } catch (Throwable t) {
                    logger.warn("run", t);
                }
            }
        }
    }

    public EntityManager createEntityManager() {
        return emf.createEntityManager();
    }

    public void persistEntity(Object entity) {
        PostgraEntityService es = new PostgraEntityService(this);
        try {
            es.begin();
            es.persist(entity);
            es.commit();
        } catch (PersistenceException e) {
            logger.warn("persist {} {}", entity, e);
        } finally {
            es.close();
        }
    }

    public PostgraProperties getProperties() {
        return properties;
    }

    public Mailer getMailer() {
        return mailer;
    }

}
