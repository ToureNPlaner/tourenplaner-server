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

import de.tourenplaner.computecore.ComputeCore;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import static io.netty.handler.codec.http.HttpVersion.*;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This handler handles HTTP Requests on the normal operation socket including *
 * <p>
 *          Initially based on:
 *          http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
 * </p>
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class MasterHandler  extends ChannelInboundHandlerAdapter {

    private static Logger log = Logger.getLogger("de.tourenplaner.server");

    private Responder responder;

    private InfoHandler infoHandler;

    private AlgorithmHandler algHandler;

    /**
     * Constructs a new RequestHandler using the given ComputeCore and
     * ServerInfo
     *
     * @param cCore ComputeCore
     * @param serverInfo String-Object-Map
     */
    public MasterHandler(final ComputeCore cCore, final Map<String, Object> serverInfo) {
        this.infoHandler = new InfoHandler(serverInfo);
        this.algHandler = new AlgorithmHandler(cCore);
    }


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        responder = new Responder(ctx.channel());
        algHandler.setResponder(responder);
        infoHandler.setResponder(responder);
    }


    /**
     * Called when a message is received
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        final FullHttpRequest request = (FullHttpRequest) msg;
	    if (HttpHeaders.is100ContinueExpected(request)) {
		    ctx.write(new DefaultFullHttpResponse(HTTP_1_1, HttpResponseStatus.CONTINUE));
	    }

        final Channel channel = ctx.channel();

        // Get the Requeststring e.g. /info
        final QueryStringDecoder queryStringDecoder = new QueryStringDecoder(request.getUri());

	    final String path = queryStringDecoder.path();

        log.finer("Request for: " + path);
        log.finer("Request: " + request.content().toString(CharsetUtil.UTF_8));

        responder.setKeepAlive(HttpHeaders.isKeepAlive(request));

            if ("/info".equals(path)) {

                infoHandler.handleInfo(request);

            } else if (path.startsWith("/alg")) {

                final String algName = queryStringDecoder.path().substring(4);
                algHandler.handleAlg(request, algName);

            } else {
                // Unknown request, close connection
                log.warning("An unknown URL was requested: " + path);
                responder.writeErrorMessage(ErrorMessage.EUNKNOWNURL, "unknown URL: " + path);
            }
    }


	/**
	 * Called when an uncaught exception occurs
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		// Ignore if it's just a client cutting the connection
		if (!(cause instanceof IOException)) {
			log.log(Level.WARNING, "Exception caught", cause);
		}
		ctx.close();
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}
}
