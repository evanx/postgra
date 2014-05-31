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
package postgra.api.guest;

import postgra.app.PostgraUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.jdbc.DataSources;
import vellum.jx.JMap;
import vellum.jx.JMapException;
import vellum.util.Lists;

/**
 *
 * @author evan.summers
 */
public class GuestInsert implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(GuestInsert.class); 

    Connection connection;
    PreparedStatement statement;
    String sql;
    
    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap requestMap = httpx.parseJsonMap();
        JMap responseMap = new JMap();
        String database = requestMap.getString("database");
        try {
            String user = app.authenticateGuest(requestMap);
            String table = requestMap.getString("table");
            connection = app.getDataSourceManager().getGuestConnection(database);
            JMap dataMap = requestMap.getMap("data");
            List<String> columnNameList = Lists.coerceString(Lists.listKeys(dataMap.entrySet()));
            List<Object> valueList = Lists.listValues(dataMap.entrySet());
            sql = String.format("insert into %s (username, %s) values ('%s', %s) returning id", table, 
                    PostgraUtil.formatNamesCsv(columnNameList), user, 
                    PostgraUtil.formatSqlValuesCsv(valueList));
            responseMap.put("sql", sql);
            logger.info("sql {}", sql);
            statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {                
                responseMap.put("id", resultSet.getInt("id"));
            }
            return responseMap;            
        } catch (Exception e) {
            throw new JMapException(responseMap, e.getMessage());
        } finally {
            DataSources.close(connection);
        }
    }

}
