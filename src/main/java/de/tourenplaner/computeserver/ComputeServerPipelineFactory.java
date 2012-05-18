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
import de.tourenplaner.config.ConfigManager;
import de.tourenplaner.server.TPSSslContextFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.ssl.SslHandler;

import javax.net.ssl.SSLEngine;
import java.util.Map;

import static org.jboss.netty.channel.Channels.pipeline;

/**
 * This class is used to create the ServerPipeline
 * <p>
 *          Initially based on:
 *          http://docs.jboss.org/netty/3.2/xref/org/jboss/netty/example/http/snoop/package-summary.html
 * </p>
 *
 * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
 */
public class ComputeServerPipelineFactory implements ChannelPipelineFactory {
    private final ComputeCore cCore;

    private final Map<String, Object> serverInfo;

    public ComputeServerPipelineFactory(ComputeCore comCore, Map<String, Object> serverInfo) {
        this.cCore = comCore;
        this.serverInfo = serverInfo;
    }

    @Override
    public ChannelPipeline getPipeline() throws Exception {
        // Create a default pipeline implementation.
        ChannelPipeline pipeline = pipeline();

        // TODO Implement SSL SSLEngine
        if (ConfigManager.getInstance().getEntryBool("private", false)) {
            SSLEngine engine = TPSSslContextFactory.getServerContext().createSSLEngine();
            engine.setUseClientMode(false);
            pipeline.addLast("ssl", new SslHandler(engine));

        }

        pipeline.addLast("decoder", new HttpRequestDecoder());
        // TODO Might change to handling HttpChunks on our own here we have 10
        // MB request size limit
        pipeline.addLast("aggregator", new HttpChunkAggregator(10485760));
        pipeline.addLast("encoder", new HttpResponseEncoder());
        // We could add compression support by uncommenting the following line
        pipeline.addLast("deflater", new HttpContentCompressor());

        pipeline.addLast("handler", new MasterHandler(cCore, serverInfo));
        return pipeline;
    }
}
