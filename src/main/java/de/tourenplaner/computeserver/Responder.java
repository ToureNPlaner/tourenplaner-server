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

package de.tourenplaner.computeserver;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.smile.SmileFactory;
import de.tourenplaner.computecore.ComputeRequest;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpHeaders.Names;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * This class deals with writing ComputeResults as well as error messages to the wire
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class Responder {
    public enum ResultFormat {
        JSON("application/json; charset=UTF-8", new ObjectMapper(new JsonFactory()).setPropertyNamingStrategy(new JSONLowerCaseStrategy())),
        SMILE("application/x-jackson-smile", new ObjectMapper(new SmileFactory()).setPropertyNamingStrategy(new JSONLowerCaseStrategy()));

        private final String contentType;
        private final ObjectMapper mapper;

        ResultFormat(String contentType, ObjectMapper mapper){
            this.contentType = contentType;
            this.mapper = mapper;
        }

        public final ObjectMapper getMapper() {
            return mapper;
        }

        public final String getContentType() {
            return contentType;
        }

        public static ResultFormat fromHeaders(HttpHeaders headers){
            String accept = headers.get("Accept");
            if(accept != null && accept.contains(SMILE.contentType)){
                return SMILE;
            }

            return JSON;
        }

    }

    private static Logger log = Logger.getLogger("de.tourenplaner.server");

    private final Channel replyChannel;
    private ResultFormat format;
    private boolean keepAlive;

    /**
     * Constructs a new Responder from the given Channel
     *
     * @param replyChan reply channel
     */
    public Responder(Channel replyChan) {
        this.replyChannel = replyChan;
        this.format = null;
        this.keepAlive = false;
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
     * Set the format to be used for responses
     * @param format
     */
    public void setFormat(ResultFormat format) {
        this.format = format;
    }

    /**
     * Gets the Channel associated with this Responder
     *
     * @return Returns the reply channel
     */
    public Channel getChannel() {
        return replyChannel;
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
    public void writeObject(Object toWrite, HttpResponseStatus status) throws IOException {

        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);

        response.headers().set("Access-Control-Allow-Origin", "*");
        response.headers().set(Names.CONTENT_TYPE, format.getContentType());
        OutputStream resultStream = new ByteBufOutputStream(response.content());

        // let's hope that the mapper can actually transform our object to
        // something that makes sense
        try {
            format.getMapper().writeValue(resultStream, toWrite);
        } catch (JsonGenerationException e) {
            log.severe("Couldn't generate format from object: " + e.getMessage() + "; " + toWrite.toString());
            throw e;
        } catch (JsonMappingException e) {
            log.severe("Couldn't map object to format: " + e.getMessage() + "; " + toWrite.toString());
            throw e;
        }

        resultStream.flush();

        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(Names.CONTENT_LENGTH, response.content().readableBytes());
        }

        // Write the response.
        ChannelFuture future = replyChannel.writeAndFlush(response);

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
	    // Build the response object.
	    FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, errorMessage.status);

        OutputStream resultStream = new ByteBufOutputStream(response.content());
        JsonGenerator gen = format.getMapper().getFactory().createGenerator(resultStream);
        gen.writeStartObject();
        gen.writeStringField("errorId", errorMessage.errorId);
        gen.writeStringField("message", errorMessage.message);
        gen.writeStringField("details", details);
        gen.writeEndObject();
        gen.close();
        resultStream.flush();



        response.headers().set("Access-Control-Allow-Origin", "*");

        response.headers().set(Names.CONTENT_TYPE, format.getContentType());

        // Write the response.
        ChannelFuture future = replyChannel.writeAndFlush(response);

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
        return  "{\"errorId\":\"" + errorMessage.errorId +
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
        // Build the response object.
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status);

        response.headers().set("Access-Control-Allow-Origin", "*");
        response.headers().set(Names.CONTENT_TYPE, format.getContentType());

        OutputStream resultStream = new ByteBufOutputStream(response.content());

        work.getResultObject().writeToStream(format, resultStream);
        resultStream.flush();
        
        if (keepAlive) {
            // Add 'Content-Length' header only for a keep-alive connection.
            response.headers().set(Names.CONTENT_LENGTH, response.content().readableBytes());
        }

        // Write the response.
        ChannelFuture future = replyChannel.writeAndFlush(response);

        // Close the non-keep-alive connection after the write operation is
        // done.
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }


        return;

    }

}
