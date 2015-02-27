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

package de.tourenplaner.algorithms;

import de.tourenplaner.computecore.RequestData;
import de.tourenplaner.computecore.RequestPoints;
import de.tourenplaner.graphrep.GraphRep;
import de.tourenplaner.computeserver.ErrorMessage;
import de.tourenplaner.computeserver.Responder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.FullHttpRequest;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class is used to construct Graphalgorithms
 * and provides additional information only used by Graphalgorithms
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public abstract class GraphAlgorithmFactory extends AlgorithmFactory {
    protected static final class MapType extends TypeReference<Map<String, Object>> {
    }
    protected static final MapType JSONOBJECT = new MapType();
	
	protected GraphRep graph;
	
	
	public GraphAlgorithmFactory(GraphRep graph){
		this.graph = graph;
	}
	
	/**
	 * Gets the List of PointConstraints in a jsonserializable Map format
	 * 
	 * @return A list of maps of pointconstraints or null
	 */
	public abstract List<Map<String, Object>> getPointConstraints();

    /**
     * Reads ClassicRequestData unless overridden
     */
    public RequestData readRequestData(ObjectMapper mapper, Responder responder, FullHttpRequest request) throws IOException {
        Map<String, Object> constraints = null;
        final RequestPoints points = new RequestPoints();
        final ByteBuf content = request.content();
        if (content.readableBytes() > 0) {

            final JsonParser jp = mapper.getJsonFactory().createJsonParser(new ByteBufInputStream(content));
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
                if (token == JsonToken.FIELD_NAME) {
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
                } else if (token == JsonToken.END_OBJECT) {
                    // Normal end of request
                    finished = true;
                } else if (token == null) {
                    //EOF
                    throw new JsonParseException("Unexpected EOF in Request", jp.getCurrentLocation());
                } else {
                    throw new JsonParseException("Unexpected token " + token, jp.getCurrentLocation());
                }

            }

        } else {
            responder.writeErrorMessage(ErrorMessage.EBADJSON_NOCONTENT);
            return null;
        }
        return new ClassicRequestData(this.getURLSuffix(), points, constraints);
    }
}
