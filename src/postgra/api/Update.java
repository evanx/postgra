/*
 * Source https://github.com/evanx by @evanxsummers
 */
package postgra.api;

import postgra.app.PostgraUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.jdbc.RowSets;
import vellum.jx.JMap;
import vellum.util.Lists;

/**
 *
 * @author evan.summers
 */
public class Update implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(Update.class); 

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap requestMap = httpx.parseJsonMap();
        String database = requestMap.getString("database");
        String user = requestMap.getString("user");
        String password = requestMap.getString("password");
        String table = requestMap.getString("table");
        Connection connection = app.getConnection(database, user, password);
        try {
            JMap dataMap = requestMap.getMap("data");
            List<String> columnNameList = Lists.coerceString(Lists.listKeys(dataMap.entrySet()));
            List<Object> valueList = Lists.listValues(dataMap.entrySet());
            String sql = String.format("insert into table (%s) values (%s)", table, 
                    PostgraUtil.formatNamesCsv(columnNameList), PostgraUtil.formatSqlValuesCsv(valueList));
            logger.info("sql {}", sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            JMap responseMap = new JMap();
            responseMap.put("pathArgs", httpx.getPathArgs());
            responseMap.put("sql", sql);
            return responseMap;            
        } finally {
            app.close(connection);
        }
    }

}
