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
public class CreatePrimaryKey implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(CreatePrimaryKey.class); 

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        Connection connection = RowSets.getLocalPostgresConnection("template1", "postgra", "postgra");
        try {
            String sql = "create user " + httpx.getPathString(2);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            JMap response = new JMap();
            response.put("pathArgs", httpx.getPathArgs());
            response.put("sql", sql);
            return response;
        } finally {
            RowSets.close(connection);
        }
    }
}
