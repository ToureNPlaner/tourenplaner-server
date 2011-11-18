/**
 * $$\\ToureNPlaner\\$$
 */
package server;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.util.CharsetUtil;

import algorithms.Points;

import computecore.ComputeRequest;

/**
 * This class encapsulates the translation from Map<String, Object> to JSON and
 * then to the wire. As well as providing utility methods for sending answers to
 * clients
 * 
 * @author Niklas Schnelle, Peter Vollmer
 * 
 */
public class Responder {

	private final Channel replyChannel;
	private final boolean keepAlive;
	private final ObjectMapper mapper;
	private final StringBuilder sb;
	private final ChannelBuffer outputBuffer;

	/**
	 * Constructs a new Responder from the given Channel that either uses
	 * keepAlive HTTP connections or not based on the corresponding flag
	 * 
	 * @param replyChan
	 * @param keepAlive
	 */
	public Responder(ObjectMapper mapper, Channel replyChan, boolean keepAlive) {
		this.mapper = mapper;
		this.replyChannel = replyChan;
		this.keepAlive = keepAlive;
		this.sb = new StringBuilder();
		this.outputBuffer = ChannelBuffers.dynamicBuffer(4096);
	}

	/**
	 * Gets the Channel associated with this Responder
	 * 
	 * @return
	 */
	public Channel getChannel() {
		return replyChannel;
	}

	/**
	 * Writes a HTTP Unauthorized answer to the wire and closes the connection
	 * 
	 */
	public void writeUnauthorizedClose() {
		// Respond with Unauthorized Access
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, UNAUTHORIZED);
		// Send the client the realm so it knows we want Basic Access Auth.
		response.setHeader("WWW-Authenticate", "Basic realm=\"ToureNPlaner\"");
		// Write the response.
		ChannelFuture future = replyChannel.write(response);
		future.addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * Translates a given Map<String, Object> (must be json compatible see
	 * simple_json) to JSON and writes it onto the wire
	 * 
	 * @param toWrite
	 * @param status
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonGenerationException
	 */
	public void writeJSON(Map<String, Object> toWrite, HttpResponseStatus status)
			throws JsonGenerationException, JsonMappingException, IOException {

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
		outputBuffer.clear();
		OutputStream resultStream = new ChannelBufferOutputStream(outputBuffer);

		mapper.writeValue(resultStream, toWrite);
		resultStream.flush();
		response.setContent(outputBuffer);

		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.setHeader(CONTENT_LENGTH, response.getContent()
					.readableBytes());
		}

		// Write the response.
		ChannelFuture future = replyChannel.write(response);

		// Close the non-keep-alive connection after the write operation is
		// done.
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	/**
	 * Sends an error to the client, the connection will be closed afterwards
	 * 
	 * @param errorId
	 * @param message
	 * @param details
	 * @param status
	 */
	public void writeErrorMessage(String errorId, String message,
			String details, HttpResponseStatus status) {
		sb.delete(0, sb.length());
		sb.append("{\"errorid\":");
		sb.append("\"");
		sb.append(errorId);
		sb.append("\",");

		sb.append("\"message\":");
		sb.append("\"");
		sb.append(message);
		sb.append("\"");

		if (details != null) {
			sb.append(",\"details\":");
			sb.append("\"");
			sb.append(details);
			sb.append("\"}");
		} else {
			sb.append("}");
		}

		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");

		response.setContent(ChannelBuffers.copiedBuffer(sb.toString(),
				CharsetUtil.UTF_8));

		// Write the response.
		ChannelFuture future = replyChannel.write(response);

		// Close the connection after the write operation is
		// done.
		future.addListener(ChannelFutureListener.CLOSE);

	}

	public void writeComputeResult(ComputeRequest work,
			HttpResponseStatus status) throws IOException {
		// Build the response object.
		HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");

		outputBuffer.clear();
		OutputStream resultStream = new ChannelBufferOutputStream(outputBuffer);

		JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(
				resultStream);
		gen.setCodec(mapper);
		gen.writeStartObject();
		gen.writeArrayFieldStart("points");
		Points points = work.getResultPoints();
		for (int i = 0; i < points.size(); i++) {
			gen.writeStartObject();
			gen.writeNumberField("lt", points.getPointLat(i));
			gen.writeNumberField("ln", points.getPointLon(i));
			gen.writeEndObject();
		}
		gen.writeEndArray();
		gen.writeObjectField("misc", work.getMisc());
		gen.writeEndObject();
		gen.close();
		response.setContent(outputBuffer);
		if (keepAlive) {
			// Add 'Content-Length' header only for a keep-alive connection.
			response.setHeader(CONTENT_LENGTH, response.getContent()
					.readableBytes());
		}

		// Write the response.
		ChannelFuture future = replyChannel.write(response);

		// Close the non-keep-alive connection after the write operation is
		// done.
		if (!keepAlive) {
			future.addListener(ChannelFutureListener.CLOSE);
		}

	}
}
