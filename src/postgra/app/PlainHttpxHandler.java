/*
 * Source https://github.com/evanx by @evanxsummers
 */
package postgra.app;

/**
 *
 * @author evan.summers
 */
public interface PlainHttpxHandler {
    
    public String handle(PostgraApp app, PostgraHttpx httpx, PostgraEntityService es) throws Exception;
    
}
