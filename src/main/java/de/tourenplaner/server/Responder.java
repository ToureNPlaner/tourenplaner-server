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

package de.tourenplaner.server;

import de.tourenplaner.computecore.ComputeRequest;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.smile.SmileFactory;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBufferOutputStream;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

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
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class Responder {

    private static Logger log = Logger.getLogger("de.tourenplaner.server");

    private final Channel replyChannel;
    private boolean keepAlive;
    private static final ObjectMapper mapper;
    private static final ObjectMapper smileMapper;
    private ChannelBuffer outputBuffer;

    static {
        mapper = new ObjectMapper();
        // make all property names in sent json lowercase
        // http://wiki.fasterxml.com/JacksonFeaturePropertyNamingStrategy
        mapper.setPropertyNamingStrategy(new JSONLowerCaseStrategy());
        // Makes jackson use: ISO-8601
        mapper.configure(SerializationConfig.Feature.WRITE_DATES_AS_TIMESTAMPS, false);
        
        // The mapper used for smile
        smileMapper = new ObjectMapper(new SmileFactory());
    }
    /**
     * Constructs a new Responder from the given Channel
     *
     * @param replyChan reply channel
     */
    public Responder(Channel replyChan) {
        this.replyChannel = replyChan;
        this.keepAlive = false;
        this.outputBuffer = null;
    }


    // TODO should we keep this unused method here?
    /**
     * Gets the KeepAlive flag
     *
     * @return Returns the KeepAlive flag
     */
    public boolean getKeepAlive() {
        return keepAlive;
    }

    /**
     * Sets the KeepAlive flag
     *
     * @param keepAlive how to set the KeepAlive flag
     */
    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * Gets the Channel associated with this Responder
     *
     * @return Returns the reply channel
     */
    public Channel getChannel() {
        return replyChannel;
    }

    // TODO should we keep this unused method here?
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
     * Writes a HTTP status answer to the wire and closes the connection if it is not a keep-alive connection
     * @param status HttpResponseStatus
     */
    public void writeStatusResponse(HttpResponseStatus status) {
        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader(CONTENT_TYPE, "text-html; charset=UTF-8");

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.setHeader(CONTENT_LENGTH, 0);
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
     * Translates a given json compatible object (see simple_json) to JSON and
     * writes it onto the wire
     *
     * @param toWrite json compatible object
     * @param status HttpResponseStatus
     * @throws JsonMappingException Thrown if mapping object to json fails
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws IOException Thrown if writing json onto the output fails
     */
    public void writeJSON(Object toWrite, HttpResponseStatus status) throws IOException {

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
     * Writes a given byte array onto the wire. The given byte array should be a json object,
     * because this method will write &quot;application/json&quot; as content type into the
     * response header. If the byte array is null this method will sent an empty response content.
     * @param byteArray A json object as byte array
     * @param status HttpResponseStatus
     * @throws IOException Thrown if writing onto the output fails
     */
    public void writeByteArray(byte[] byteArray, HttpResponseStatus status) throws IOException {
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

        if (byteArray != null) {
            OutputStream resultStream = new ChannelBufferOutputStream(outputBuffer);
            resultStream.write(byteArray);
            resultStream.flush();
        }

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
     * Sends an error to the client, the connection will be closed afterwards<br /><br />
     *
     * Use this method if your details text is dynamically created
     *
     * @param errorMessage error id (see protocol specification), for example ENOTADMIN
     * @param details more detailed error information
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws IOException Thrown if writing json onto the output fails
     */
    public void writeErrorMessage(ErrorMessage errorMessage, String details)
            throws IOException
    {
        log.info("Writing Error Message: " + errorMessage.message + " --- " + details);

        // Allocate buffer if not already done
        // do this here because we are in a worker thread
        if (outputBuffer == null) {
            outputBuffer = ChannelBuffers.dynamicBuffer(4096);
        }

        outputBuffer.clear();
        OutputStream resultStream = new ChannelBufferOutputStream(outputBuffer);
        JsonGenerator gen = mapper.getJsonFactory().createJsonGenerator(resultStream);
        gen.writeStartObject();
        gen.writeStringField("errorid", errorMessage.errorId);
        gen.writeStringField("message", errorMessage.message);
        gen.writeStringField("details", details);
        gen.writeEndObject();
        gen.close();
        resultStream.flush();

        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, errorMessage.status);

        response.setHeader("Access-Control-Allow-Origin", "*");
        // Add header so that clients know how they can authenticate
        if(errorMessage.status.equals(HttpResponseStatus.UNAUTHORIZED)){
            response.setHeader("WWW-Authenticate","Basic realm=\"touenplaner\"");
        }

        response.setHeader(CONTENT_TYPE, "application/json; charset=UTF-8");

        response.setContent(outputBuffer);
        // Write the response.
        ChannelFuture future = replyChannel.write(response);

        // Close the connection after the write operation is
        // done.
        future.addListener(ChannelFutureListener.CLOSE);

    }


    /**
     * Sends an error to the client, the connection will be closed afterwards<br /><br />
     *
     * Use this method if your details text is stored within errorMessage
     *
     * @param errorMessage an enum value of ErrorMessage
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws IOException Thrown if writing json onto the output fails
     */
    public void writeErrorMessage(ErrorMessage errorMessage) throws IOException {
        writeErrorMessage(errorMessage, errorMessage.details);
    }


    /**
     * Sends an error to the client, the connection will be closed afterwards <br />
     * A String representing the error response will be returned<br /><br />
     *
     * Use this method if your details text is dynamically created
     *
     * @param errorMessage an enum value of ErrorMessage
     * @param details more detailed error information
     * @return A String representing the error response
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws IOException Thrown if writing json onto the output fails
     */
    public String writeAndReturnErrorMessage(ErrorMessage errorMessage, String details) throws IOException {
        this.writeErrorMessage(errorMessage, details);
        return  "{\"errorid\":\"" + errorMessage.errorId +
                "\",\"message\":\"" + errorMessage.message +
                "\",\"details\":\"" + details + "\"}";
    }

    /**
     * Sends an error to the client, the connection will be closed afterwards <br />
     * A String representing the error response will be returned<br /><br />
     *
     * Use this method if your details text is stored within errorMessage
     *
     * @param errorMessage an enum value of ErrorMessage
     * @return A String representing the error response
     * @throws JsonGenerationException Thrown if generating json fails
     * @throws IOException Thrown if writing json onto the output fails
     */
    public String writeAndReturnErrorMessage(ErrorMessage errorMessage) throws IOException {
        return writeAndReturnErrorMessage(errorMessage, errorMessage.details);
    }


    /**
     * Creates the response for the ComputeResult.
     *
     * @param work ComputeRequest
     * @param status HttpResponseStatus
     * @throws IOException Thrown if writing json onto the output or onto the returned ByteArrayOutputStream fails
     */
    public void writeComputeResult(ComputeRequest work, HttpResponseStatus status) throws IOException {
        // Allocate buffer if not already done
        // do this here because we are in a worker thread
        if (outputBuffer == null) {
            outputBuffer = ChannelBuffers.dynamicBuffer(4096);
        }

        ObjectMapper useMapper = (work.isAcceptsSmile()) ? smileMapper: mapper;
        
        // Build the response object.
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader(CONTENT_TYPE, (work.isAcceptsSmile()) ? "application/x-jackson-smile": "application/json; charset=UTF-8");

        outputBuffer.clear();
        OutputStream resultStream = new ChannelBufferOutputStream(outputBuffer);

        work.getResultObject().writeToStream(useMapper, resultStream);
        resultStream.flush();

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


        return;

    }

}
