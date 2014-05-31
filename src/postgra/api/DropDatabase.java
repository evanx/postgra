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
public class DropDatabase implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(DropDatabase.class); 

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap requestMap = httpx.parseJsonMap();
        String database = requestMap.getString("database");
        if (requestMap.containsKey("username")) {
            logger.error("username not used");
        }
        String user = database;
        String password = requestMap.getString("password");
        Connection connection = RowSets.getLocalPostgresConnection("template1", "postgra", "postgra");
        try {
            String sql = "drop database " + database;
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            RowSets.close(statement);
            sql = String.format("drop user %s", user);
            statement = connection.prepareStatement(sql);
            statement.execute();
            JMap response = new JMap();
            response.put("pathArgs", httpx.getPathArgs());
            response.put("sql", sql);
            return response;
        } finally {
            app.close(connection);
        }
    }
}
