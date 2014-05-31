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

import postgra.app.PostgraUtil;
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
public class GuestDelete implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(GuestUpdate.class); 

    Connection connection;
    PreparedStatement statement;
    
    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap responseMap = new JMap();
        JMap requestMap = httpx.parseJsonMap();
        try {
            String user = app.authenticateGuest(requestMap);
            String database = requestMap.getString("database");
            connection = app.getDataSourceManager().getGuestConnection(database);
            String table = requestMap.getString("table");
            JMap whereMap = requestMap.getMap("where");
            String sql = String.format("delete from %s where %s and username = '%s'", table, 
                    PostgraUtil.formatWhere(whereMap), user);
            responseMap.put("sql", sql);
            logger.info("sql {}", sql);
            statement = connection.prepareStatement(sql);
            int count = statement.executeUpdate();
            responseMap.put("count", count);
            return responseMap;            
        } catch (Exception e) {
            throw new JMapException(responseMap, e.getMessage());
        } finally {
            DataSources.close(connection);
        }
    }
}
