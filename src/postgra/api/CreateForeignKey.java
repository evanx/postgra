/*
 * Source https://github.com/evanx by @evanxsummers
 */
package postgra.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import postgra.app.PostgraApp;
import postgra.app.PostgraEntityService;
import postgra.app.PostgraHttpx;
import postgra.app.PostgraHttpxHandler;
import postgra.jdbc.RowSets;
import vellum.jx.JMap;

/**
 *
 * @author evan.summers
 */
public class CreateForeignKey implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(CreateForeignKey.class); 

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap requestMap = httpx.parseJsonMap();
        String database = requestMap.getString("database");
        String user = requestMap.getString("user");
        String password = requestMap.getString("password");
        String table = requestMap.getString("table");
        String sql = requestMap.getString("sql");
        Connection connection = app.getConnection(database, user, password);
        try {
            sql = String.format("alter table %s (%s)", table, sql);
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
