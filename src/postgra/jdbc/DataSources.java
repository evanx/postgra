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
package postgra.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.apache.tomcat.jdbc.pool.PoolProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.exception.Exceptions;

/**
 *
 * @author evan.summers
 */
public class DataSources {

    public static Logger logger = LoggerFactory.getLogger(DataSources.class);

    public static Connection getLocalPostgresConnection(String database, String user, String password) {
        String databaseUrl = String.format("jdbc:postgresql://localhost:5432/%s", database);
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(databaseUrl, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            throw Exceptions.newRuntimeException(e, databaseUrl, user);
        }
    }
    
    public static DataSource newDataSource(String database, String username, String password) {
        DataSource dataSource = new DataSource();
        dataSource.setPoolProperties(newPoolProperties(database, username, password));
        return dataSource;
    }

    public static PoolProperties newPoolProperties(String database, String username, String password) {
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
    
    public static void close(ResultSet resultSet, PreparedStatement statement, Connection connection) {
        close(resultSet);
        close(statement);
        close(connection);
    }

    public static void close(PreparedStatement statement, Connection connection) {
        close(statement);
        close(connection);
    }
    
    public static void close(Connection connection) {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            } else {
                logger.warn("reclose connection");
            }
        } catch (SQLException e) {
            logger.warn("close", e);
        }
    }
    
    
    public static void close(Statement statement) {
        try {
            if (statement != null && !statement.isClosed()) {
                statement.close();
            } else {
                logger.warn("reclose statement");
            }
        } catch (SQLException e) {
            logger.warn("close", e);
        }
    }

    public static void close(ResultSet resultSet) {
        try {
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            } else {
                logger.warn("reclose result set");
            }
        } catch (SQLException e) {
            logger.warn("close", e);
        }
    }    

    public static boolean isValid(Connection connection) {
        try {
            connection.createStatement().execute("select 1");
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
