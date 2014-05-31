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

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import vellum.jx.JMap;

/**
 *
 * @author evan.summers
 */
public class PostgraUtil {
    
    private static Logger logger = LoggerFactory.getLogger(PostgraUtil.class); 

    
    public static String formatSqlValue(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return String.format("'%s'", value.toString());
        } else {
            return value.toString();
        }
    }
    
    public static String formatWhere(JMap data) {    
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> item : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append(" AND ");
            }
            builder.append(String.format("%s = %s", item.getKey(), formatSqlValue(item.getValue())));
        }
        return builder.toString();        
    }
    
    public static String formatNamesCsv(Iterable<String> iterable) {
        StringBuilder builder = new StringBuilder();
        for (String item : iterable) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(item);
        }
        return builder.toString();        
    }
    
    public static String formatSqlValuesCsv(Iterable iterable) {
        StringBuilder builder = new StringBuilder();
        for (Object item : iterable) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(formatSqlValue(item));
        }
        return builder.toString();        
    }    
}
