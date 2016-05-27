package org.mwg;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class WSServer implements WebSocketConnectionCallback {

    private final Graph graph;
    private final int port;
    private final Undertow server;

    private Set<WebSocketChannel> peers;

    public WSServer(Graph p_graph, int p_port) {
        this.graph = p_graph;
        this.port = p_port;
        this.server = Undertow.builder().addHttpListener(port, "0.0.0.0", Handlers.websocket(this)).build();
        peers = new HashSet<WebSocketChannel>();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    @Override
    public void onConnect(WebSocketHttpExchange webSocketHttpExchange, WebSocketChannel webSocketChannel) {
        webSocketChannel.getReceiveSetter().set(new PeerInternalListener());
        webSocketChannel.resumeReceives();
        peers.add(webSocketChannel);
    }

    private void processMessage(org.mwg.struct.Buffer in) {
        //TODO


        //TODO  in.free();!
    }

    private class PeerInternalListener extends AbstractReceiveListener {

        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            ByteBuffer byteBuffer = WebSockets.mergeBuffers(message.getData().getResource());
            org.mwg.struct.Buffer graphBuffer = graph.newBuffer();
            graphBuffer.writeAll(byteBuffer.array());
            processMessage(graphBuffer);
            super.onFullBinaryMessage(channel, message);
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
            org.mwg.struct.Buffer graphBuffer = graph.newBuffer();
            graphBuffer.writeAll(message.getData().getBytes());
            processMessage(graphBuffer);
            super.onFullTextMessage(channel, message);
        }

        @Override
        protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
            peers.remove(webSocketChannel);
            super.onClose(webSocketChannel, channel);
        }
    }


}
