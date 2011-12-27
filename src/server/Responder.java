/**
 * $$\\ToureNPlaner\\$$
 */
package server;

import computecore.ComputeRequest;
import org.codehaus.jackson.JsonGenerationException;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * This class encapsulates the translation from Map<String, Object> to JSON and
 * then to the wire. As well as providing utility methods for sending answers to
 * clients
 *
 * @author Niklas Schnelle, Peter Vollmer
 */
public class Responder {

    private static Logger log = Logger.getLogger("server");

    private final Channel replyChannel;
    private boolean keepAlive;
    private final ObjectMapper mapper;
    private final StringBuilder sb;
    private ChannelBuffer outputBuffer;

    /**
     * Constructs a new Responder from the given Channel
     *
     * @param replyChan
     */
    public Responder(ObjectMapper mapper, Channel replyChan) {
        this.mapper = mapper;
        this.replyChannel = replyChan;
        this.keepAlive = false;
        this.sb = new StringBuilder();
        this.outputBuffer = null;
    }


    /**
     * Gets the KeepAlive flag
     *
     * @return
     */
    public boolean getKeepAlive() {
        return keepAlive;
    }

    /**
     * Sets the KeepAlive flag
     *
     * @param keepAlive1
     */
    public void setKeepAlive(boolean keepAlive1) {
        this.keepAlive = keepAlive;
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
     */
    public void writeUnauthorizedClose() {
        log.info("Writing unauthorized close");
        // Respond with Unauthorized Access
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, UNAUTHORIZED);
        // Send the client the realm so it knows we want Basic Access Auth.
        response.setHeader("WWW-Authenticate", "Basic realm=\"ToureNPlaner\"");
        // Write the response.
        ChannelFuture future = replyChannel.write(response);
        future.addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Translates a given json compatible object (see simple_json) to JSON and
     * writes it onto the wire
     *
     * @param toWrite
     * @param status
     * @throws IOException
     * @throws JsonMappingException
     * @throws JsonGenerationException
     */
    public void writeJSON(Object toWrite, HttpResponseStatus status) throws JsonGenerationException, JsonMappingException, IOException {

        // Allocate buffer if not already done
        // do this here because we are in a worker thread
        if (outputBuffer == null) {
            outputBuffer = ChannelBuffers.dynamicBuffer(4096);
        }

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");
        outputBuffer.clear();
        OutputStream resultStream = new ChannelBufferOutputStream(outputBuffer);

        // let's hope that the mapper can actually transform our object to
        // something that makes sense
        try {
            mapper.writeValue(resultStream, toWrite);
        } catch (JsonGenerationException e) {
            log.severe("Couldn't generate json from object: " + e.getMessage() + "; " + toWrite.toString());
            throw e;
        } catch (JsonMappingException e) {
            log.severe("Couldn't map object to json: " + e.getMessage() + "; " + toWrite.toString());
            throw e;
        }

        resultStream.flush();
        response.setContent(outputBuffer);

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
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
    public void writeErrorMessage(String errorId, String message, String details, HttpResponseStatus status) {
        log.info("Writing Error Message: " + message + " --- " + details);
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

        response.setContent(ChannelBuffers.copiedBuffer(sb.toString(), CharsetUtil.UTF_8));

        // Write the response.
        ChannelFuture future = replyChannel.write(response);

        // Close the connection after the write operation is
        // done.
        future.addListener(ChannelFutureListener.CLOSE);

    }

    /**
     * Creates the response for the ComputeResult. Returns a
     * ByteArrayOutputStream which contains the json object of this response.
     *
     * @param work
     * @param status
     * @return
     * @throws IOException
     */
    public ByteArrayOutputStream writeComputeResult(ComputeRequest work, HttpResponseStatus status) throws IOException {
        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");

        ByteArrayOutputStream resultStream = new ByteArrayOutputStream();
        work.writeToStream(mapper, resultStream);
        resultStream.flush();

        response.setContent(ChannelBuffers.wrappedBuffer(resultStream.toByteArray()));
        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, response.getContent().readableBytes());
        }

        // Write the response.
        ChannelFuture future = replyChannel.write(response);

        // Close the non-keep-alive connection after the write operation is
        // done.
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

        return resultStream;

    }

}
