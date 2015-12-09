package de.tourenplaner.computecore;

import com.fasterxml.jackson.core.JsonGenerator;
import de.tourenplaner.computeserver.Responder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is used to hold classic ToureNPlaner results
 * in a more abstract way.
 */
public class WayResult implements FormattedStreamWriter {


    private Map<String, Object> misc;
    private final List<Way> resultWays;
    private final RequestPoints points;
    private final Map<String, Object> constraints;

    public WayResult(RequestPoints points, Map<String, Object> constraints){
        this.points = points;
        this.constraints = constraints;
        this.resultWays = new ArrayList<Way>(1);
        this.misc = null;
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
     * Writes a json representation of the result to the given
     * stream
     *
     * @param format ResultFormat to use for the response
     * @param stream OutputStream
     * @throws com.fasterxml.jackson.core.JsonGenerationException
     *                             Thrown if generating json fails
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     *                             Thrown if json generation processing fails
     * @throws java.io.IOException Thrown if writing json onto the stream fails
     */
    public void writeToStream(Responder.ResultFormat format, OutputStream stream) throws IOException {

        JsonGenerator gen = format.getMapper().getFactory().createGenerator(stream);
        Map<String, Object> pconsts;
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
                for (Map.Entry<String, Object> entry : pconsts.entrySet()) {
                    gen.writeObjectField(entry.getKey(), entry.getValue());
                }
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();

        gen.writeArrayFieldStart("way");
        for (Way way : this.resultWays) {
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
