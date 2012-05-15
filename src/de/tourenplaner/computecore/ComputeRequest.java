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

package de.tourenplaner.computecore;

import de.tourenplaner.server.Responder;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * This class is used to represent a request for computation
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ComputeRequest {

	private final de.tourenplaner.computecore.RequestPoints points;
	private final List<de.tourenplaner.computecore.Way> resultWays;
	private final Map<String, Object> constraints;
	private Map<String, Object> misc;
	private final String algName;
	private final Responder responder;
    private final boolean acceptsSmile;

	/**
	 * Constructs a new ComputeRequest using the given Responder, Points and
	 * Constraints.
	 *
     * @param responder The to this compute request corresponding Responder
     * @param algName The algorithm name
     * @param points RequestPoints
     * @param constraints map with constraints
     * @param acceptsSmile Flag if client accepts Smile
     */
	public ComputeRequest(Responder responder, String algName,
                          de.tourenplaner.computecore.RequestPoints points, Map<String, Object> constraints, boolean acceptsSmile) {
		this.algName = algName;
		this.points = points;
		this.resultWays = new ArrayList<de.tourenplaner.computecore.Way> (1);
		this.constraints = constraints;
		this.responder = responder;
		this.misc = null;
        this.acceptsSmile = acceptsSmile;
	}

	/**
	 * Gets the responder object which is used to send the result to the correct
	 * client connection
	 * 
	 * @return Returns the Responder object
	 */
	public Responder getResponder() {
		return responder;
	}

	/**
	 * Gets the URLSuffix for the requested algorithm e.g. "sp" for a shortest
	 * path algorithm
	 * 
	 * @return Returns the URLSuffix
	 */
	public String getAlgorithmURLSuffix() {
		return algName;
	}

	/**
	 * Returns the Points associated with this request
	 * 
	 * @return RequestPoints
	 */
	public RequestPoints getPoints() {
		return points;
	}

	/**
	 * Returns the list of Ways making up the result of the computation <br />
     * It's an Algorithms job to ensure that after the computation the list contains all
     * the ways connecting the Points
	 * 
	 * @return A List with the result ways
	 */
	public List<Way> getResultWays() {
		return resultWays;
	}

	/**
	 * Returns the constraints associated with this request
	 * 
	 * @return A Map representing the constraints
	 */
	public Map<String, Object> getConstraints() {
		return constraints;
	}

	/**
	 * Returns the misc field used to store results, initially this is null
	 * 
	 * @return A Map representing the misc field
	 */
	public Map<String, Object> getMisc() {
		return misc;
	}

    /**
     * Sets the misc field used to store results, initially this is null
     *
     * @param misc A Map representing the misc field
     */
	public void setMisc(Map<String, Object> misc) {
		this.misc = misc;
	}

    /**
     * Returns if request comes from a client accepting "application/x-jackson-smile"
     * @return Returns if client is accepting smile
     */
    public boolean isAcceptsSmile() {
        return acceptsSmile;
    }

	/**
	 * Writes a json representation of the result of this request to the given
	 * stream
	 * 
	 * @param mapper Jackson ObjectMapper
	 * @param stream OutputStream
	 * @throws JsonGenerationException Thrown if generating json fails
     * @throws JsonProcessingException Thrown if json generation processing fails
     * @throws IOException Thrown if writing json onto the stream fails
	 */
	public void writeToStream(ObjectMapper mapper, OutputStream stream) throws IOException {

		JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(stream);
		Map<String, Object> pconsts;

		gen.setCodec(mapper);
		gen.writeStartObject();
        gen.writeObjectField("constraints", this.constraints);

		gen.writeArrayFieldStart("points");
		RequestPoints points = this.points;
		for (int i = 0; i < points.size(); i++) {
			pconsts = points.getConstraints(i);
			gen.writeStartObject();
			gen.writeNumberField("lt", points.getPointLat(i));
			gen.writeNumberField("ln", points.getPointLon(i));
			if (pconsts != null) {
				for (Entry<String, Object> entry : pconsts.entrySet()) {
					gen.writeObjectField(entry.getKey(), entry.getValue());
				}
			}
			gen.writeEndObject();
		}
		gen.writeEndArray();

		gen.writeArrayFieldStart("way");
        for(Way way : this.resultWays){
            gen.writeStartArray();
            for (int i = 0; i < way.size(); i++) {
                gen.writeStartObject();
                gen.writeNumberField("lt", way.getPointLat(i));
                gen.writeNumberField("ln", way.getPointLon(i));
                gen.writeEndObject();
            }
            gen.writeEndArray();
        }

		gen.writeEndArray();
		gen.writeObjectField("misc", this.misc);
		gen.writeEndObject();
		gen.close();
	}
}
