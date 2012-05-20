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

import org.jboss.netty.channel.*;
import org.jboss.netty.handler.codec.http.*;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * This handler is used when a socket must only handle "/info" requests
 * <p>
 *          Initially based on:
 *          http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
 * </p>
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ServerInfoHandler extends SimpleChannelUpstreamHandler {
    private static Logger log = Logger.getLogger("de.tourenplaner.server");

    private final InfoHandler infoHandler;


    private Responder responder;

    public ServerInfoHandler() {
        this.infoHandler = new InfoHandler();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        responder = new Responder(e.getChannel());
        infoHandler.setResponder(responder);

    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        HttpRequest request = (HttpRequest) e.getMessage();
        Channel channel = e.getChannel();
        // System.out.print(request.toString());
        // Handle preflighted requests so wee need to work with OPTION Requests
        if (request.getMethod().equals(HttpMethod.OPTIONS)) {
            handlePreflights(request, channel);
            return;
        }

        // Get the Requeststring e.g. /info
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

        String path = queryStringDecoder.getPath();

        if (path.equals("/info")) {
            infoHandler.handleInfo(request);
        } else {
            channel.close();
        }
    }

    /**
     * Handles preflighted OPTION Headers
     *
     * @param request
     * @param channel
     */
    private void handlePreflights(HttpRequest request, Channel channel) {
        boolean keepAlive = isKeepAlive(request);
        HttpResponse response;

        // We only allow GET methods so only allow request when Method is Post
        String methodType = request.getHeader("Access-Control-Request-Method");
        if (methodType != null && methodType.trim().equals("GET")) {
            response = new DefaultHttpResponse(HTTP_1_1, OK);
            response.addHeader("Connection", "Keep-Alive");
        } else {
            response = new DefaultHttpResponse(HTTP_1_1, FORBIDDEN);
            // We don't want to keep the connection now
            keepAlive = false;
        }

        ArrayList<String> allowHeaders = new ArrayList<String>(2);
        allowHeaders.add("Content-Type");
        allowHeaders.add("Authorization");

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        response.setHeader(CONTENT_TYPE, "application/json");
        response.setHeader("Content-Length", "0");

        response.setHeader("Access-Control-Allow-Headers", allowHeaders);

        ChannelFuture future = channel.write(response);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        log.log(Level.WARNING,"Exception caught",e.getCause());
        e.getChannel().close();
    }
}
