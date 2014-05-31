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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.util.HashMap;
import java.util.Map;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.jdbc.RowSets;

/**
 *
 * @author evan.summers
 */
public class PostgraConnectionManager {
    private static Logger logger = LoggerFactory.getLogger(PostgraConnectionManager.class); 
    int getConnectionCount;
    int newConnectionCount;
    int closeConnectionCount;
    int closeCount;
    int removeClosedCount; 
    
    Map<String, Connection> connectionMap = new HashMap();

    public PostgraConnectionManager() {
    }

    public void init() throws Exception {
    }
    
    void close() {
        for (Connection connection : connectionMap.values()) {
            RowSets.close(connection);            
        }
    }
    
    void close(String database, Connection connection) {
        closeCount++;
        connectionMap.remove(database);
        RowSets.close(connection);
    }
    
    public void close(String database, String user, String password) {
        Connection connection = connectionMap.get(database);
        if (connection != null) {
            close(database, connection);
        }
    }
    
    public Connection getConnection(String database, String user, String password) {
        getConnectionCount++;
        Connection connection = connectionMap.get(database); // TODO proper connection pool and auth
        if (connection == null) {
            connection = newConnection(database, user, password);
            connectionMap.put(database, connection);            
        } else {
            try {
                if (connection.isClosed()) {
                    removeClosedCount++;
                    connectionMap.remove(database);
                    connection = newConnection(database, user, password);
                }
            } catch (SQLException e) {
                logger.error("check connection", e);
                close(database, connection);
                connection = newConnection(database, user, password);
            }
        }
        return connection;
    }    
    
    Connection newConnection(String database, String user, String password) {
        newConnectionCount++;
        Connection connection = RowSets.getLocalPostgresConnection(database, user, password);        
        if (connectionMap.containsKey(database)) {
            logger.error("map contains key");
        }
        connectionMap.put(database, connection);            
        return connection;
    }
    
    public void close(Connection connection) {
        close(connection, true);
    }
    
    public void close(Connection connection, boolean ok) {
        closeConnectionCount++;
        if (!ok) {
            RowSets.close(connection);    
        }
    }   

    public void close(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        try {
            SQLWarning warning = resultSet.getWarnings();
            RowSets.close(resultSet);
            if (warning == null) {
                close(statement, connection);
            } else {
                throw warning;
            }
        } catch (SQLException e) {
            logger.warn("close", e);
            RowSets.close(statement);    
            close(connection, false);    
        }
    }
    
    public void close(PreparedStatement statement, Connection connection) {
        try {
            SQLWarning warning = statement.getWarnings();
            RowSets.close(statement);
            if (warning == null) {
                close(connection, true);
            } else {
                throw warning;
            }
        } catch (SQLException e) {
            logger.warn("close", e);
            close(connection, false);
        }
    }
    
    public PoolProperties getPoolProperties(String database, String username, String password) {
        PoolProperties poolProperties = new PoolProperties();
        poolProperties.setUrl(String.format("jdbc:postgresql://localhost:5432/%s", database));
        poolProperties.setDriverClassName("org.postgresql.Driver");
        poolProperties.setUsername(username);
        poolProperties.setPassword(password);
        poolProperties.setJmxEnabled(true);
        poolProperties.setTestWhileIdle(false);
        poolProperties.setTestOnBorrow(true);
        poolProperties.setValidationQuery("SELECT 1");
        poolProperties.setTestOnReturn(false);
        poolProperties.setValidationInterval(30000);
        poolProperties.setTimeBetweenEvictionRunsMillis(30000);
        poolProperties.setMaxActive(100);
        poolProperties.setInitialSize(10);
        poolProperties.setMaxWait(10000);
        poolProperties.setRemoveAbandonedTimeout(60);
        poolProperties.setMinEvictableIdleTimeMillis(30000);
        poolProperties.setMinIdle(10);
        poolProperties.setLogAbandoned(true);
        poolProperties.setRemoveAbandoned(true);
        poolProperties.setJdbcInterceptors("org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;"
                + "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer");
        return poolProperties;
    }
    
    
}
