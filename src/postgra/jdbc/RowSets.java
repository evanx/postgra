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

import com.sun.rowset.CachedRowSetImpl;
import vellum.exception.Exceptions;
import java.beans.PropertyDescriptor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.sql.RowSet;
import javax.sql.rowset.CachedRowSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.util.Beans;
import vellum.util.Strings;

/**
 *
 * @author evan.summers
 */
public class RowSets {
    public static Logger logger = LoggerFactory.getLogger(RowSets.class);

    public static Connection getLocalPostgresConnection(String database, String user, String password) {
        String databaseUrl = String.format("jdbc:postgresql://localhost:5432/%s", database);
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(databaseUrl, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            throw Exceptions.newRuntimeException(e, databaseUrl, user);
        }
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

    public static List<String> getColumnNameList(ResultSetMetaData md) {
        try {
            List<String> list = new ArrayList();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                list.add(md.getColumnName(i));
            }
            return list;
        } catch (SQLException e) {
            throw Exceptions.newRuntimeException(e);
        }
    }

    public static List<String> getColumnTypeNameList(ResultSetMetaData md) {
        try {
            List<String> list = new ArrayList();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                list.add(md.getColumnTypeName(i));
            }
            return list;
        } catch (SQLException e) {
            throw Exceptions.newRuntimeException(e);
        }
    }

    public static ResultSetMetaData getMetaData(RowSet rowSet) {
        try {
            return rowSet.getMetaData();
        } catch (SQLException e) {
            throw Exceptions.newRuntimeException(e);
        }
    }

    public static ResultSetMetaData getMetaData(ResultSet resultSet) {
        try {
            return resultSet.getMetaData();
        } catch (SQLException e) {
            throw Exceptions.newRuntimeException(e);
        }
    }

    public static RowSet getRowSet(Connection connection, String query) throws Exception {
        CachedRowSet rowSet;
        try (Statement statement = connection.createStatement(); 
                ResultSet res = statement.executeQuery(query)) {
            rowSet = new CachedRowSetImpl();
            rowSet.populate(res);
        }
        return rowSet;
    }

    public static int executeUpdate(Connection connection, String query) throws Exception {
        int updateCount;
        try (Statement statement = connection.createStatement()) {
            updateCount = statement.executeUpdate(query);
        }
        return updateCount;
    }

    public static RowSet getRowSet(ResultSet res) throws Exception {
        CachedRowSet rowSet = new CachedRowSetImpl();
        rowSet.populate(res);
        res.close();
        return rowSet;
    }

    public static int getRowCount(RowSet rowSet) throws SQLException {
        int count = 0;
        rowSet.first();
        while (rowSet.next()) {
            count++;
        }
        return count;
    }

    public static boolean findRow(RowSet rowSet, String columnName, Object value) throws SQLException {
        rowSet.first();
        while (rowSet.next()) {
            Object v = rowSet.getObject(columnName);
            if (v != null && v.equals(value)) {
                return true;
            }
        }
        return false;
    }

    public static void populate(RowSet rs, Map map) {
        try {
            for (String columnName : getColumnNameList(rs.getMetaData())) {
                map.put(columnName, rs.getObject(columnName));
            }
        } catch (SQLException e) {
            throw Exceptions.newRuntimeException(e);
        }
    }

    public static List getList(RowSet set, String columnName) throws SQLException {
        List list = new ArrayList();
        set.beforeFirst();
        while (set.next()) {
            list.add(set.getObject(columnName));
        }
        return list;
    }

    public static <T> List<T> getList(RowSet set, Class<T> beanClass) throws Exception {
        List<T> list = new ArrayList();
        set.beforeFirst();
        Map<String, PropertyDescriptor> propertyMap = Beans.getPropertyMap(beanClass);
        List<String> columnNames = getColumnNameList(set.getMetaData());
        while (set.next()) {
            T bean = beanClass.newInstance();
            for (String columnName : columnNames) {
                String propertyName = Strings.toCamelCase(columnName);
                PropertyDescriptor property = propertyMap.get(propertyName);
                if (property != null) {
                    Beans.convert(bean, property, set.getObject(columnName));
                }
            }
            list.add(bean);
        }
        return list;
    }

    public static void putAll(RowSet rowSet, Map<String, String> map) {
        try {
            ResultSetMetaData md = rowSet.getMetaData();
            for (int i = 1; i <= md.getColumnCount(); i++) {
                Object value = rowSet.getObject(i);
                if (value != null) {
                    map.put(md.getColumnName(i), rowSet.getObject(i).toString());
                }
            }
        } catch (SQLException e) {
            throw Exceptions.newRuntimeException(e);
        }
    }
}
