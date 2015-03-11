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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tourenplaner.algorithms.AlgorithmFactory;
import de.tourenplaner.computecore.AlgorithmRegistry;
import de.tourenplaner.computecore.ComputeCore;
import de.tourenplaner.computecore.ComputeRequest;
import de.tourenplaner.computecore.RequestData;
import de.tourenplaner.config.ConfigManager;
import de.tourenplaner.utils.SHA1;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
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
     * Handles an algorithm request.
     *
     * @param request HttpRequest
     * @param algName algorithm name as String
     * @throws com.fasterxml.jackson.core.JsonParseException Thrown if parsing json content fails
     * @throws com.fasterxml.jackson.core.JsonProcessingException Thrown if json generation processing fails
     * @throws IOException Thrown if error message sending or reading json fails
     */
    public void handleAlg(FullHttpRequest request, String algName) throws IOException {

        try {
            // denial early so we don't parse if Queue is full
            // we need to check later too because it can get full while parsing
            // but this leads an overloaded server focus on computing
            final boolean full = computer.isFull();
            if (full) {
                responder.writeErrorMessage(ErrorMessage.EBUSY);
                log.warning("Server had to deny algorithm request because of OVERLOAD (early)");
                return;
            }

            // Get the AlgorithmFactory for this Alg to check if it's registered and not isHidden
            AlgorithmFactory algFac = algReg.getAlgByURLSuffix(algName);
            if (algFac == null) {
                log.warning("Unsupported algorithm " + algName + " requested");
                responder.writeErrorMessage(ErrorMessage.EUNKNOWNALG);
                return;
            }
            // Only now read the request
	        HttpHeaders headers = request.headers();
            boolean acceptsSmile = (headers.get("Accept") != null) && headers.get("Accept").contains("application/x-jackson-smile");
            final RequestData requestData = algFac.readRequestData(mapper, responder, request);
            final ComputeRequest req = new ComputeRequest(responder, requestData ,acceptsSmile);


            if (req != null) {
                // Log what is requested
                request.content().resetReaderIndex();
                String ip = headers.get("X-Forwarded-For");
                if (ip == null) {
                    ip = ((InetSocketAddress) req.getResponder().getChannel().remoteAddress()).getAddress().getHostAddress();
                }

                if (Level.parse(ConfigManager.getInstance().getEntryString("loglevel", "info").toUpperCase())
                         .intValue() <= Level.FINE.intValue()) {
                    // time in milliseconds / 1000 = unix time / 86400 = one day
                    long day = (System.currentTimeMillis() / 86400000L);
                    //TODO: (persistent?) random salt to make ip not bruteforceable
                    String anonident = SHA1.SHA1(ip + day + "somesalt");
                    log.fine("\"" + algName + "\" for Client " + anonident + "  " +
                             request.content().toString(CharsetUtil.UTF_8));
                }

                final boolean success = computer.submit(req);

                if (!success) {
                    responder.writeErrorMessage(ErrorMessage.EBUSY);
                    log.warning("Server had to deny algorithm request because of OVERLOAD");

                }
            }
        } catch (JsonParseException e) {
            responder.writeErrorMessage(ErrorMessage.EBADJSON, e.getMessage());
        } finally {
	        request.release();
        }

    }
}
