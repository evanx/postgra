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
public class CreateIndex implements PostgraHttpxHandler {
    
    private static Logger logger = LoggerFactory.getLogger(CreateIndex.class); 

    @Override
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception {
        logger.info("handle", httpx.getPathArgs());
        JMap requestMap = httpx.parseJsonMap();
        String database = requestMap.getString("database");
        String user = requestMap.getString("user");
        String password = requestMap.getString("password");
        String index = requestMap.getString("index");
        String sql = requestMap.getString("sql");
        Connection connection = RowSets.getLocalPostgresConnection(database, user, password);
        try {
            sql = String.format("create index %s (%s)", index, sql);
            logger.info("sql {}", sql);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.execute();
            JMap responseMap = new JMap();
            responseMap.put("pathArgs", httpx.getPathArgs());
            responseMap.put("sql", sql);
            return responseMap;            
        } finally {
            RowSets.close(connection);
        }
    }
}

