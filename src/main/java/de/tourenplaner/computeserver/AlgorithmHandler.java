/*
 * Copyright 2012 ToureNPlaner
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package de.tourenplaner.computeserver;

import de.tourenplaner.algorithms.AlgorithmFactory;
import de.tourenplaner.computecore.AlgorithmRegistry;
import de.tourenplaner.computecore.ComputeCore;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.config.ConfigManager;
import de.tourenplaner.server.ErrorMessage;
import de.tourenplaner.server.RequestHandler;
import de.tourenplaner.server.Responder;
import de.tourenplaner.utils.SHA1;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferInputStream;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class  AlgorithmHandler extends RequestHandler {

    private static Logger log = Logger.getLogger("de.tourenplaner.server");

    private static final class MapType extends TypeReference<Map<String, Object>> {
    }

    private static final MapType JSONOBJECT = new MapType();
    private static final ObjectMapper mapper = new ObjectMapper();
    private final ComputeCore computer;
    private final AlgorithmRegistry algReg;


    protected AlgorithmHandler(ComputeCore computer) {
        super(null);
        this.computer = computer;
        this.algReg = computer.getAlgorithmRegistry();
    }


    /**
     * Reads a JSON encoded compute request from the content field of the given
     * request
     *
     * @param algName algorithm name
     * @param responder Responder
     * @param request HttpRequest
     * @return A ComputeRequest object representing the received request.
     * @throws JsonParseException Thrown if parsing json content fails
     * @throws JsonProcessingException Thrown if json generation processing fails
     * @throws IOException Thrown if error message sending or reading json content fails
     */
    private ComputeRequest readComputeRequest(final String algName, final Responder responder, final HttpRequest request) throws IOException {
        // Check whether Client accepts "application/x-jackson-smile"
        boolean acceptsSmile = (request.getHeader("Accept") != null) && request.getHeader("Accept").contains("application/x-jackson-smile");

        Map<String, Object> constraints = null;
        final RequestPoints points = new RequestPoints();
        final ChannelBuffer content = request.getContent();
        if (content.readableBytes() > 0) {

            final JsonParser jp = mapper.getJsonFactory().createJsonParser(new ChannelBufferInputStream(content));
            jp.setCodec(mapper);

            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new JsonParseException("Request contains no json object", jp.getCurrentLocation());
            }

            String fieldname;
            JsonToken token;
            Map<String, Object> pconsts;
            int lat = 0, lon = 0;
            boolean finished = false;
            while (!finished) {
                //move to next field or END_OBJECT/EOF
                token = jp.nextToken();
                if(token == JsonToken.FIELD_NAME){
                    fieldname = jp.getCurrentName();
                    token = jp.nextToken(); // move to value, or
                    // START_OBJECT/START_ARRAY
                    if ("points".equals(fieldname)) {
                        // Should be on START_ARRAY
                        if (token != JsonToken.START_ARRAY) {
                            throw new JsonParseException("points is no array", jp.getCurrentLocation());
                        }
                        // Read array elements
                        while (jp.nextToken() != JsonToken.END_ARRAY) {
                            pconsts = new HashMap<String, Object>();
                            while (jp.nextToken() != JsonToken.END_OBJECT) {
                                fieldname = jp.getCurrentName();
                                token = jp.nextToken();

                                if ("lt".equals(fieldname)) {
                                    lat = jp.getIntValue();
                                } else if ("ln".equals(fieldname)) {
                                    lon = jp.getIntValue();
                                } else {
                                    pconsts.put(fieldname, jp.readValueAs(Object.class));
                                }
                            }
                            points.addPoint(lat, lon, pconsts);
                        }

                    } else if ("constraints".equals(fieldname)) {
                        // Should be on START_OBJECT
                        if (token != JsonToken.START_OBJECT) {
                            throw new JsonParseException("constraints is not an object", jp.getCurrentLocation());
                        }
                        constraints = jp.readValueAs(JSONOBJECT);
                    } else {
                        // ignore for now TODO: user version string etc.
                        if ((token == JsonToken.START_ARRAY) || (token == JsonToken.START_OBJECT)) {
                            jp.skipChildren();
                        }
                    }
                } else if (token == JsonToken.END_OBJECT){
                    // Normal end of request
                    finished = true;
                } else if (token == null){
                    //EOF
                    throw new JsonParseException("Unexpected EOF in Request", jp.getCurrentLocation());
                } else {
                    throw new JsonParseException("Unexpected token "+token, jp.getCurrentLocation());
                }

            }

        } else {
            responder.writeErrorMessage(ErrorMessage.EBADJSON_NOCONTENT);
            return null;
        }

        return new ComputeRequest(responder, algName, points, constraints, acceptsSmile);
    }

    /**
     * Handles an algorithm request.
     *
     * @param request HttpRequest
     * @param algName algorithm name as String
     * @throws JsonParseException Thrown if parsing json content fails
     * @throws JsonProcessingException Thrown if json generation processing fails
     * @throws IOException Thrown if error message sending or reading json fails
     */
    public void handleAlg(HttpRequest request, String algName) throws IOException {

        try {
            // Get the AlgorithmFactory for this Alg to check if it's registered and not isHidden
            AlgorithmFactory algFac = algReg.getAlgByURLSuffix(algName);
            if (algFac == null) {
                log.warning("Unsupported algorithm " + algName + " requested");
                responder.writeErrorMessage(ErrorMessage.EUNKNOWNALG);
                return;
            }
            // Only now read the request
            final ComputeRequest req = readComputeRequest(algName, responder, request);


            if (req != null) {
                // Log what is requested
                request.getContent().resetReaderIndex();
                String ip = request.getHeader("X-Forwarded-For");
                if (ip == null) {
                    ip = ((InetSocketAddress) req.getResponder().getChannel().getRemoteAddress()).getAddress().getHostAddress();
                }

                if (Level.parse(ConfigManager.getInstance().getEntryString("loglevel", "info").toUpperCase())
                         .intValue() <= Level.FINE.intValue()) {
                    // time in milliseconds / 1000 = unix time / 86400 = one day
                    long day = (System.currentTimeMillis() / 86400000L);
                    //TODO: (persistent?) random salt to make ip not bruteforceable
                    String anonident = SHA1.SHA1(ip + day + "somesalt");
                    log.fine("\"" + algName + "\" for Client " + anonident + "  " +
                             request.getContent().toString(CharsetUtil.UTF_8));
                }

                final boolean success = computer.submit(req);

                if (!success) {
                    String errorMessage = responder.writeAndReturnErrorMessage(ErrorMessage.EBUSY);
                    log.warning("Server had to deny algorithm request because of OVERLOAD");

                }
            }
        } catch (JsonParseException e) {
            responder.writeErrorMessage(ErrorMessage.EBADJSON, e.getMessage());
        }

    }
}
