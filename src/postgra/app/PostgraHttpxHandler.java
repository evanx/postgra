/*
 * Source https://github.com/evanx by @evanxsummers
 */
package postgra.app;

import vellum.jx.JMap;

/**
 *
 * @author evan.summers
 */
public interface PostgraHttpxHandler {
    
    public JMap handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception;
}
