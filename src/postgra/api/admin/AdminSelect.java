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
package postgra.api.admin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.app.PostgraUtil;
import postgra.jdbc.DataSources;
import postgra.jdbc.RowSets;
import vellum.jx.JMap;
import vellum.jx.JMapException;

/**
 *
 * @author evan.summers
 */
public class AdminSelect implements PostgraHttpxHandler {

    private static Logger logger = LoggerFactory.getLogger(AdminSelect.class);

    Connection connection;
    PreparedStatement statement;
    ResultSetMetaData metaData;
    List<String> columnNameList;
    String sql;

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap responseMap = new JMap();
        JMap requestMap = httpx.parseJsonMap();
        String database = requestMap.getString("database");
        String password = requestMap.getString("password");
        connection = app.getDataSourceManager().getDatabaseConnection(database, password);
        try {
            String table = requestMap.getString("table");
            JMap dataMap = requestMap.getMap("data");
            sql = String.format("select * from %s where %s", table, PostgraUtil.formatWhere(dataMap));
            responseMap.put("sql", sql);
            logger.info("sql {}", sql);
            statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery();
            List data = list(resultSet);
            responseMap.put("data", data);
            return responseMap;
        } catch (SQLException e) {
            throw new JMapException(responseMap, e.getMessage());
        } finally {
            DataSources.close(statement, connection);
        }
    }

    private List list(ResultSet resultSet) throws SQLException {
        metaData = resultSet.getMetaData();
        columnNameList = RowSets.getColumnNameList(metaData);
        List<JMap> list = new ArrayList();
        while (resultSet.next()) {
            list.add(map(resultSet));
        }
        return list;
    }

    private JMap map(ResultSet resultSet) throws SQLException {
        JMap map = new JMap();
        for (String columnName : columnNameList) {
            map.put(columnName, resultSet.getObject(columnName));
        }
        return map;
    }

}
