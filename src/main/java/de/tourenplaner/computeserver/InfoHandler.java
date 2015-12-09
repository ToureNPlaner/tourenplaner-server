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

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.io.IOException;
import java.util.Map;

/**
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class InfoHandler {
    private final Map<String, Object> serverInfo;
    private Responder responder;

    public InfoHandler(Map<String, Object> serverInfo) {
        this.serverInfo = serverInfo;
    }

    /**
     * Sets the Responder to use, this must be called before
     * messages can be handled
     *
     * @param responder
     */
    public void setResponder(Responder responder) {
        this.responder = responder;
    }

    public void handleInfo(final FullHttpRequest request) throws IOException {
        responder.setFormat(Responder.ResultFormat.fromHeaders(request.headers()));
        responder.writeObject(serverInfo, HttpResponseStatus.OK);
	    request.release();
    }
}
