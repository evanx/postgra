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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.jdbc.DataSources;

/**
 *
 * @author evan.summers
 */
public class DataSourceManager {
    private static Logger logger = LoggerFactory.getLogger(DataSourceManager.class); 
    
    Map<String, DataSource> map = new HashMap();
    Connection templateConnection; 
    
    public DataSourceManager() {
    }

    public void init() throws Exception {
    }
    
    public Connection getTemplateConnection() throws SQLException {
        if (templateConnection == null) {
            templateConnection = newTemplateConnection();
        } else if (!DataSources.isValid(templateConnection)) {
            DataSources.close(templateConnection);
            templateConnection = newTemplateConnection();
        }
        return templateConnection;
    }

    public Connection newTemplateConnection() throws SQLException {
        return DataSources.getLocalPostgresConnection("template1", "postgra", "postgra");
    }
    
    void close() {
        for (DataSource dataSource : map.values()) {
            dataSource.close(true);
        }
    }
    
    public void close(String database) {
        DataSource dataSource = map.get(database);
        if (dataSource != null) {
            dataSource.close(true);
        } else {
            logger.warn("close {}", dataSource);
            
        }
    }

    public Connection getDatabaseConnection(String database, String password) throws SQLException {
        return getDataSource(database, database, password).getConnection();
    }
    
    public Connection getGuestConnection(String database) throws SQLException {
        return getDataSource(database, "postgra", "postgra").getConnection();
    }
    
    public DataSource getDataSource(String database, String user, String password) {
        DataSource dataSource = map.get(database);
        if (dataSource == null) {
            dataSource = newDataSource(database, user, password);
            map.put(database, dataSource);            
        } 
        return dataSource;
    }    
    
    DataSource newDataSource(String database, String user, String password) {
        DataSource dataSource = DataSources.newDataSource(database, user, password);        
        if (map.containsKey(database)) {
            logger.error("map contains key");
        }
        map.put(database, dataSource);            
        return dataSource;
    }    
}
