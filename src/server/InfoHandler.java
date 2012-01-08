package server;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.util.Map;

/**
 * User: Niklas Schnelle
 * Date: 12/26/11
 * Time: 11:33 PM
 */
public class InfoHandler {
    private final Map<String, Object> serverInfo;
    private Responder responder;

    public InfoHandler(Map<String, Object> serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * Sets the Responder to use, this must be called before
     * messages can be handled
     *
     * @param responder
     */
    public void setResponder(Responder responder) {
        this.responder = responder;
    }

    public void handleInfo(final HttpRequest request) throws JsonGenerationException, JsonMappingException, IOException {
        responder.writeJSON(serverInfo, HttpResponseStatus.OK);
    }
}
