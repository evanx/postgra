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
package postgra.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.jdbc.RowSets;
import vellum.jx.JMap;

/**
 *
 * @author evan.summers
 */
public class DropDatabase implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(DropDatabase.class); 

    Connection connection;
    PreparedStatement statement;

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap requestMap = httpx.parseJsonMap();
        String database = requestMap.getString("database");
        String user = requestMap.getString("user");
        String password = requestMap.getString("password");
        app.getConnectionManager().close(database, user, password);
        connection = app.getConnectionManager().getConnection("template1", "postgra", "postgra");
        try {
            String sql = "drop database " + database;
            statement = connection.prepareStatement(sql);
            statement.execute();
            RowSets.close(statement);
            user = database;
            sql = String.format("drop user %s", user);
            statement = connection.prepareStatement(sql);
            statement.execute();
            JMap response = new JMap();
            response.put("pathArgs", httpx.getPathArgs());
            response.put("sql", sql);
            return response;
        } finally {
            app.getConnectionManager().close(statement, connection);
        }
    }
}
