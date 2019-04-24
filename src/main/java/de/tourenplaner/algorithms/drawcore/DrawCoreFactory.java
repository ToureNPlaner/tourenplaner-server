package de.tourenplaner.algorithms.drawcore;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.tourenplaner.algorithms.Algorithm;
import de.tourenplaner.algorithms.GraphAlgorithmFactory;
import de.tourenplaner.algorithms.bbbundle.BBBundleRequestData;
import de.tourenplaner.computecore.RequestData;
import de.tourenplaner.computeserver.ErrorMessage;
import de.tourenplaner.computeserver.Responder;
import de.tourenplaner.graphrep.GraphRep;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.FullHttpRequest;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by niklas on 30.03.15.
 */
public class DrawCoreFactory extends GraphAlgorithmFactory {
    private final Map<String, Object> details;

    public DrawCoreFactory(GraphRep graphRep){
        super(graphRep);
        details = new HashMap<String, Object>(1);
        details.put("hidden", this.isHidden());
    }

    /**
     * Reads ClassicRequestData unless overridden
     */
    public RequestData readRequestData(ObjectMapper mapper, Responder responder, FullHttpRequest request) throws IOException {
        int nodeCount = 400;
        final ByteBuf content = request.content();
        BBBundleRequestData.LevelMode mode = BBBundleRequestData.LevelMode.EXACT;
        double minLen = 20.0;
        double maxLen = 400.0;
        double maxRatio = 0.01;
        boolean latLonMode = false;

        if (content.readableBytes() > 0) {

            final JsonParser jp = mapper.getFactory().createParser((InputStream) new ByteBufInputStream(content));
            jp.setCodec(mapper);

            if (jp.nextToken() != JsonToken.START_OBJECT) {
                throw new JsonParseException("Request contains no json object", jp.getCurrentLocation());
            }

            String fieldname;
            JsonToken token;
            boolean finished = false;


            while (!finished) {
                //move to next field or END_OBJECT/EOF
                token = jp.nextToken();
                if (token == JsonToken.FIELD_NAME) {
                    fieldname = jp.getCurrentName();
                    token = jp.nextToken(); // move to value, or
                    // in number of nodes
                    if ("nodeCount".equals(fieldname)) {

                        if (token != JsonToken.VALUE_NUMBER_INT) {
                            throw new JsonParseException("nodeCount is not an int", jp.getCurrentLocation());
                        }
                        nodeCount = jp.getIntValue();
                    } else if ("mode".equals(fieldname)) {
                        String m = jp.getText();
                        if (m.equalsIgnoreCase("hinted")){
                            mode = BBBundleRequestData.LevelMode.HINTED;
                        } else if (m.equalsIgnoreCase("exact")) {
                            mode = BBBundleRequestData.LevelMode.EXACT;
                        }
                    } else if ("minLen".equals(fieldname)) {
                        minLen = jp.getDoubleValue();
                    }else if ("maxLen".equals(fieldname)) {
                        maxLen = jp.getDoubleValue();
                    }else if ("maxRatio".equals(fieldname)) {
                        maxRatio = jp.getDoubleValue();
                    }else if ("coords".equals(fieldname)) {
                        latLonMode = jp.getText().equals("latlon");
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
        return new DrawCoreRequestData(this.getURLSuffix(), latLonMode, nodeCount, mode, minLen, maxLen, maxRatio);
    }

    @Override
    public List<Map<String, Object>> getPointConstraints() {
        return null;
    }

    @Override
    public Algorithm createAlgorithm() {
        return new DrawCore(graph);
    }

    @Override
    public String getURLSuffix() {
        return "drawcore";
    }

    @Override
    public String getAlgName() {
        return "Drawable Core Graph";
    }

    @Override
    public int getVersion() {
        return 0;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public List<Map<String, Object>> getConstraints() {
        return null;
    }

    @Override
    public Map<String, Object> getDetails() {
        return details;
    }

    @Override
    public String getDescription() {
        return "Compute the current graphs core, that is the nodes and edges above level minLevel. Output with drawable edges";
    }
}
