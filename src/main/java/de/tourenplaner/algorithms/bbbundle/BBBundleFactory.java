package de.tourenplaner.algorithms.bbbundle;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tourenplaner.algorithms.Algorithm;
import de.tourenplaner.algorithms.GraphAlgorithmFactory;
import de.tourenplaner.computecore.RequestData;
import de.tourenplaner.computeserver.ErrorMessage;
import de.tourenplaner.computeserver.Responder;
import de.tourenplaner.graphrep.GraphRep;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by niklas on 19.03.15.
 */
public class BBBundleFactory  extends GraphAlgorithmFactory {
    private static final class MapType extends TypeReference<Map<String, Object>> {
    }


    public BBBundleFactory(GraphRep graph) {
        super(graph);
    }

    /**
     * Creates a new instance of the Algorithm class(s) associated with this
     * factory
     *
     * @return a new Algorithm instance
     */
    @Override
    public Algorithm createAlgorithm() {
        return new BBBundle(graph);
    }

    /**
     * Used to get the URLSuffix for the constructed Algorithms e.. "sp" will
     * make the Algorithm available under /algsp
     *
     * @return
     */
    @Override
    public String getURLSuffix() {
        return "bbbundle";
    }

    /**
     * Returns the human readable name of the constructed Algorithms e.g.
     * "Shortest Path"
     *
     * @return
     */
    @Override
    public String getAlgName() {
        return "BBBundle";
    }

    /**
     * Returns the version of the constructed Algorithms
     *
     * @return
     */
    @Override
    public int getVersion() {
        return 1;
    }

    /**
     * Returns if it is an isHidden algorithm
     */
    @Override
    public boolean isHidden() {
        return true;
    }

    /**
     * Gets the Constraints not bound to any Point
     *
     * @return A List of Maps with the constraints or null
     */
    @Override
    public List<Map<String, Object>> getConstraints() {
        return null;
    }

    /**
     * Gets Details for the the algorithm
     * this includes whether the algorithm has sourceIsTarget
     * and the minimal number of points
     *
     * @return A map with the details or null
     */
    @Override
    public Map<String, Object> getDetails() {
        return null;
    }


    /**
     * Gets a human readable (english) description of the implemented
     * algorithm. Clients should provide translations for common algorithms
     *
     * @return A description of the algorithm
     */
    @Override
    public String getDescription() {
        return "";
    }

    /**
     * Gets the List of PointConstraints in a jsonserializable Map format
     *
     * @return A list of maps of pointconstraints or null
     */
    @Override
    public List<Map<String, Object>> getPointConstraints() {
        return null;
    }

    /**
     * Reads ClassicRequestData unless overwritten
     */
    public RequestData readRequestData(ObjectMapper mapper, Responder responder, FullHttpRequest request) throws IOException {
        final ByteBuf content = request.content();
        if (content.readableBytes() > 0) {

            final JsonParser jp = mapper.getFactory().createParser((InputStream) new ByteBufInputStream(content));
            jp.setCodec(mapper);

            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new JsonParseException(jp, "Request contains no json object", jp.getCurrentLocation());
            }

            String fieldname;
            JsonToken token;
            BoundingBox bbox = new BoundingBox();
            double minLen = 0.0;
            double maxLen = 0.0;
            double maxRatio = 0.0;
            boolean latlon = false;
            int hintLevel = 0;
            int nodeCountHint = 0;
            int coreSize = 0;
            BBBundleRequestData.LevelMode mode = BBBundleRequestData.LevelMode.AUTO;
            int lat = 0, lon = 0;
            boolean finished = false;
            while (!finished) {
                //move to next field or END_OBJECT/EOF
                token = jp.nextToken();
                if (token == JsonToken.FIELD_NAME) {
                    fieldname = jp.getCurrentName();
                    token = jp.nextToken(); // move to value, or
                    // START_OBJECT/START_ARRAY
                    if ("bbox".equals(fieldname)) {
                        // Should be on START_ARRAY
                        if (token != JsonToken.START_OBJECT) {
                            throw new JsonParseException(jp, "bbox is no object", jp.getCurrentLocation());
                        }

                        while (jp.nextToken() != JsonToken.END_OBJECT) {
                            fieldname = jp.getCurrentName();
                            token = jp.nextToken();

                            if ("x".equals(fieldname)) {
                                bbox.x = jp.getIntValue();
                            } else if ("y".equals(fieldname)) {
                                bbox.y = jp.getIntValue();
                            } else if ("width".equals(fieldname)) {
                                bbox.width = jp.getIntValue();
                            } else if ("height".equals(fieldname)) {
                                bbox.height = jp.getIntValue();
                            }
                        }
                    } else if ("nodeCountHint".equals(fieldname)) {
                        nodeCountHint = jp.getIntValue();
                    } else if ("minPrio".equals(fieldname)) {
                        hintLevel = jp.getIntValue();
                    } else if ("coreSize".equals(fieldname)) {
                        // in number of nodes
                        coreSize = jp.getIntValue();
                    } else if ("mode".equals(fieldname)) {
                        String m = jp.getText();
                        if (m.equalsIgnoreCase("hinted")){
                            mode = BBBundleRequestData.LevelMode.HINTED;
                        } else if (m.equalsIgnoreCase("exact")) {
                            mode = BBBundleRequestData.LevelMode.EXACT;
                        }
                    } else if ("minLen".equals(fieldname)) {
                        minLen = jp.getDoubleValue();
                    } else if ("maxLen".equals(fieldname)) {
                        maxLen = jp.getDoubleValue();
                    } else if ("maxRatio".equals(fieldname)) {
                        maxRatio = jp.getDoubleValue();
                    } else if ("coords".equals(fieldname)){
                        latlon = jp.getText().equals("latlon");
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
                    throw new JsonParseException(jp, "Unexpected EOF in Request", jp.getCurrentLocation());
                } else {
                    throw new JsonParseException(jp, "Unexpected token " + token, jp.getCurrentLocation());
                }
            }

            return new BBBundleRequestData(this.getURLSuffix(), latlon, bbox, mode, minLen, maxLen, maxRatio, nodeCountHint, hintLevel, coreSize);
        } else {
            responder.writeErrorMessage(ErrorMessage.EBADJSON_NOCONTENT);
            return null;
        }
    }
}
