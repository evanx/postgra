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

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 *
 * @author evan.summers
 */
public class PostgraMain {

    public PostgraMain() {
    }

    public void init() throws Exception {
        PostgraApp app = new PostgraApp();
        app.init(new PostgraProperties());
    }
    
    public static void main(String[] args) throws Exception {
        try {
            Logger.getRootLogger().getLoggerRepository().resetConfiguration();
            Logger.getRootLogger().addAppender(new ConsoleAppender(
                    new PatternLayout("%d{ISO8601} %p [%c{1}] %m%n")));
            new PostgraMain().init();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }

}
