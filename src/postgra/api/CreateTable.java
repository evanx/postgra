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
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.jdbc.DataSources;
import vellum.jx.JMap;
import vellum.jx.JMapException;

/**
 *
 * @author evan.summers
 */
public class CreateTable implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(CreateTable.class); 

    Connection connection;
    PreparedStatement statement;
    String sql;

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap responseMap = new JMap();
        responseMap.put("pathArgs", httpx.getPathArgs());
        JMap requestMap = httpx.parseJsonMap();
        String database = requestMap.getString("database");
        String password = requestMap.getString("password");
        String table = requestMap.getString("table");
        sql = requestMap.getString("sql");
        connection = app.getDataSourceManager().getDatabaseConnection(database, password);
        try {
            sql = String.format("id serial, %s", sql);
            sql = String.format("create table %s (%s)", table, sql);
            responseMap.put("sql", sql);
            logger.info("sql {}", sql);
            statement = connection.prepareStatement(sql);
            statement.execute();
            return responseMap;            
        } catch (SQLException e) {
            throw new JMapException(responseMap, e.getMessage());
        } finally {
            DataSources.close(connection);
        }
    }
}
