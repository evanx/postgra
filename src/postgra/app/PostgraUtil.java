/*
 * Source https://github.com/evanx by @evanxsummers
 */
package postgra.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
