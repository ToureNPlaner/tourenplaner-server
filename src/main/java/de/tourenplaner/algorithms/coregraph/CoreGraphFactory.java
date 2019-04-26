package de.tourenplaner.algorithms.coregraph;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphAlgorithm factory used to create the Core export Algorithm instances
 */
public class CoreGraphFactory extends GraphAlgorithmFactory {
	private final Map<String, Object> details;

	public CoreGraphFactory(GraphRep graphRep){
		super(graphRep);
		details = new HashMap<String, Object>(3);
		details.put("hidden", this.isHidden());
		details.put("sourceistarget", false);
	}

	/**
	 * Reads unless overridden
	 */
	public RequestData readRequestData(ObjectMapper mapper, Responder responder, FullHttpRequest request) throws IOException {
		Map<String, Object> constraints = null;
		final ByteBuf content = request.content();
		if (content.readableBytes() > 0) {

			final JsonParser jp = mapper.getFactory().createParser((InputStream) new ByteBufInputStream(content));
			jp.setCodec(mapper);

			if (jp.nextToken() != JsonToken.START_OBJECT) {
				throw new JsonParseException(jp, "Request contains no json object", jp.getCurrentLocation());
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
					if ("constraints".equals(fieldname)) {
						// Should be on START_OBJECT
						if (token != JsonToken.START_OBJECT) {
							throw new JsonParseException(jp, "constraints is not an object", jp.getCurrentLocation());
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
					throw new JsonParseException(jp, "Unexpected EOF in Request", jp.getCurrentLocation());
				} else {
					throw new JsonParseException(jp, "Unexpected token " + token, jp.getCurrentLocation());
				}

			}

		} else {
			responder.writeErrorMessage(ErrorMessage.EBADJSON_NOCONTENT);
			return null;
		}
		return new CoreGraphRequest(this.getURLSuffix(),constraints);
	}

	@Override
	public List<Map<String, Object>> getPointConstraints() {
		return null;
	}

	@Override
	public Algorithm createAlgorithm() {
		return new CoreGraph(graph);
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
