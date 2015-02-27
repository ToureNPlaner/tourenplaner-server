package de.tourenplaner.algorithms;

import com.carrotsearch.hppc.IntArrayList;
import de.tourenplaner.computecore.RequestData;
import de.tourenplaner.computeserver.ErrorMessage;
import de.tourenplaner.computeserver.Responder;
import de.tourenplaner.graphrep.GraphRep;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.FullHttpRequest;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Niklas Schnelle
 */
public class WayByNodeIdsFactory extends SharingAlgorithmFactory {
    private final Map<String, Object> details;
    private final List<Map<String, Object>> constraints;
    private final List<Map<String, Object>> pointConstraints;

    public WayByNodeIdsFactory(GraphRep graph){
        super(graph);
        constraints = new ArrayList<Map<String, Object>>(0);
        pointConstraints = new ArrayList<Map<String, Object>>(0);
        details = new HashMap<String, Object>(3);
        details.put("hidden", this.isHidden());
        details.put("minpoints", 2);
        details.put("sourceistarget", false);
    }

    @Override
    public Algorithm createAlgorithm(DijkstraStructs ds) {
        return new WayByNodeIds(graph, ds);
    }

    @Override
    public List<Map<String, Object>> getPointConstraints() {
        return pointConstraints;
    }

    @Override
    public Algorithm createAlgorithm() {
        return new WayByNodeIds(graph, new DijkstraStructs(graph.getNodeCount(), graph.getEdgeCount()));
    }

    @Override
    public String getURLSuffix() {
        return "waybynodeids";
    }

    @Override
    public String getAlgName() {
        return "Way by nodeIds";
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public List<Map<String, Object>> getConstraints() {
        return constraints;
    }

    @Override
    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public String getDescription() {
        return "Computes a Way with coordinates from a list of consecutive point ids";
    }

    @Override
    public RequestData readRequestData(ObjectMapper mapper, Responder responder, FullHttpRequest request) throws IOException {
        final ByteBuf content = request.content();
        IntArrayList nodeIds = null;
        if (content.readableBytes() > 0) {
            nodeIds = new IntArrayList();
            final JsonParser jp = mapper.getJsonFactory().createJsonParser(new ByteBufInputStream(content));
            jp.setCodec(mapper);

            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new JsonParseException("Request contains no json object", jp.getCurrentLocation());
            }

            String fieldname;
            JsonToken token;
            int lat = 0, lon = 0;
            boolean finished = false;
            while (!finished) {
                //move to next field or END_OBJECT/EOF
                token = jp.nextToken();
                if (token == JsonToken.FIELD_NAME) {
                    fieldname = jp.getCurrentName();
                    token = jp.nextToken(); // move to value, or
                    // START_OBJECT/START_ARRAY
                    if ("nodes".equals(fieldname)) {
                        // Should be on START_ARRAY
                        if (token != JsonToken.START_ARRAY) {
                            throw new JsonParseException("nodes is no array", jp.getCurrentLocation());
                        }
                        // Read array elements
                        while (jp.nextToken() != JsonToken.END_ARRAY) {
                            nodeIds.add(jp.getIntValue());
                        }

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
        return new NodeIdsRequestData(this.getURLSuffix(), nodeIds);
    }
}
