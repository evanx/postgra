/*
 * Source https://github.com/evanx by @evanxsummers
 */
package postgra.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author evan.summers
 */
public class PostgraUtil {
    
    private static Logger logger = LoggerFactory.getLogger(PostgraUtil.class); 

    public static List<String> coerceString(Iterable iterable) {        
        List<String> list = new ArrayList();
        for (Object item : iterable) {
            if (item == null) {
                list.add(null);
            } else {
                list.add(item.toString());
            }
        }
        return list;
    }
    
    public static List listKeys(Set<Map.Entry<String, Object>> entrySet) {        
        List keyList = new ArrayList();
        for (Map.Entry entry : entrySet) {
            keyList.add(entry.getKey());
        }
        return keyList;
    }
    
    public static List listValues(Set<Map.Entry<String, Object>> entrySet) {
        List list = new ArrayList();
        for (Map.Entry entry : entrySet) {
            list.add(entry.getValue());
        }
        return list;
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
            if (item == null) {
                builder.append("null");                
            } else if (item instanceof String) {
                builder.append(String.format("'%s'", item.toString()));
            } else {
                builder.append(item.toString());
            }
        }
        return builder.toString();        
    }    
}
