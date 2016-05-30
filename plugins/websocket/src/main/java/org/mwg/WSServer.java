package org.mwg;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.mwg.plugin.Chunk;
import org.mwg.plugin.ChunkSpace;
import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;

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

    private void send(ByteBuffer buffer, final WebSocketChannel channel) {
        WebSockets.sendBinary(buffer, channel, new WebSocketCallback<Void>() {
            @Override
            public void complete(WebSocketChannel webSocketChannel, Void aVoid) {
                //TODO process
            }

            @Override
            public void onError(WebSocketChannel webSocketChannel, Void aVoid, Throwable throwable) {
                //TODO process
            }
        });
    }


    //multi thread process
    private void processMessage(final byte[] payload, final WebSocketChannel channel) {
        if (payload.length == 0) {
            return;
        }
        switch (payload[0]) {
            case WSConstants.REQ_GET:
                //sift 4 bytes
                final Buffer buffer = graph.newBuffer();
                final byte[] sliced = new byte[payload.length - 5];
                System.arraycopy(payload, 5, sliced, 0, payload.length - 5);
                buffer.writeAll(sliced);
                BufferIterator it = buffer.iterator();
                int nbWait = 0;
                while (it.hasNext()) {
                    nbWait++;
                    it.next();
                }
                it = buffer.iterator();
                DeferCounter defer = graph.counter(nbWait);
                byte[][] results = new byte[nbWait][];
                int index = 0;
                while (it.hasNext()) {
                    final int toSet = index;
                    index++;
                    final Buffer sub = it.next();
                    //now we have the tuple, it the space :-)
                    final WSTuple tuple = WSTuple.build(sub);
                    graph.space().getOrLoadAndMark(tuple.type, tuple.world, tuple.time, tuple.id, new Callback<Chunk>() {
                        @Override
                        public void on(Chunk memoryChunk) {
                            if (memoryChunk != null) {
                                final Buffer toSaveBuffer = graph.newBuffer();
                                memoryChunk.save(toSaveBuffer);
                                graph.space().unmarkChunk(memoryChunk);
                                byte[] toSendData = toSaveBuffer.data();
                                toSaveBuffer.free();
                                results[toSet] = toSendData;
                            } else {
                                results[toSet] = null;
                            }
                            defer.count();
                        }
                    });
                }
                int finalNbWait = nbWait;
                defer.then(new Callback() {
                    @Override
                    public void on(Object result) {
                        int finalSize = 0;
                        for (int i = 0; i < finalNbWait; i++) {
                            if (i != 0) {
                                finalSize++;
                            }
                            if (results[i] != null) {
                                finalSize = finalSize + results[i].length;
                            }
                        }
                        finalSize = finalSize + 5;
                        byte[] finalResult = new byte[finalSize];
                        finalResult[0] = WSConstants.RESP_GET;
                        finalResult[1] = payload[1];
                        finalResult[2] = payload[2];
                        finalResult[3] = payload[3];
                        finalResult[4] = payload[4];
                        int insertIndex = 5;
                        for (int i = 0; i < finalNbWait; i++) {
                            if (i != 0) {
                                finalResult[insertIndex] = Constants.BUFFER_SEP;
                                insertIndex++;
                            }
                            if (results[i] != null) {
                                byte[] subResult = results[i];
                                System.arraycopy(subResult, 0, finalResult, insertIndex, subResult.length);
                                insertIndex = insertIndex + subResult.length;
                            }
                        }
                        send(ByteBuffer.wrap(finalResult), channel);
                    }
                });
                buffer.free();
                break;
            case WSConstants.REQ_REMOVE:
                break;
            case WSConstants.REQ_PUT:
                break;
            default:
                //NOOP
        }
    }

    private class PeerInternalListener extends AbstractReceiveListener {

        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            ByteBuffer byteBuffer = WebSockets.mergeBuffers(message.getData().getResource());
            processMessage(byteBuffer.array(), channel);
            super.onFullBinaryMessage(channel, message);
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
            processMessage(message.getData().getBytes(), channel);
            super.onFullTextMessage(channel, message);
        }

        @Override
        protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
            peers.remove(webSocketChannel);
            super.onClose(webSocketChannel, channel);
        }

    }


}
