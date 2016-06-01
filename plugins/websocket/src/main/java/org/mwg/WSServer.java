package org.mwg;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.mwg.plugin.Chunk;
import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    private class PeerInternalListener extends AbstractReceiveListener {

        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            ByteBuffer byteBuffer = WebSockets.mergeBuffers(message.getData().getResource());
            process_rpc(byteBuffer.array(), channel);
            super.onFullBinaryMessage(channel, message);
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
            process_rpc(message.getData().getBytes(), channel);
            super.onFullTextMessage(channel, message);
        }

        @Override
        protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
            peers.remove(webSocketChannel);
            super.onClose(webSocketChannel, channel);
        }

    }

    private void process_rpc(final byte[] input, final WebSocketChannel channel) {
        if (input.length == 0) {
            return;
        }
        final Buffer payload = graph.newBuffer();
        payload.writeAll(input);
        final BufferIterator it = payload.iterator();
        final Buffer codeView = it.next();
        final Buffer callbackCodeView = it.next();
        if (codeView != null && callbackCodeView != null && codeView.size() != 0) {
            byte firstCodeView = codeView.read(0);
            //compute resp prefix
            switch (firstCodeView) {
                case WSConstants.REQ_GET:
                    //build keys list
                    final List<ChunkKey> keys = new ArrayList<ChunkKey>();
                    while (it.hasNext()) {
                        keys.add(ChunkKey.build(it.next()));
                    }
                    process_get(keys.toArray(new ChunkKey[keys.size()]), streamResult -> {
                        Buffer concat = graph.newBuffer();
                        concat.write(WSConstants.RESP_GET);
                        concat.write(Constants.BUFFER_SEP);
                        concat.writeAll(callbackCodeView.data());
                        concat.write(Constants.BUFFER_SEP);
                        concat.writeAll(streamResult.data());
                        streamResult.free();
                        payload.free();
                        send_rpc_resp(concat, channel);
                    });
                    break;
                case WSConstants.REQ_LOCK:
                    process_lock(result -> {
                        Buffer concat = graph.newBuffer();
                        concat.write(WSConstants.RESP_LOCK);
                        concat.write(Constants.BUFFER_SEP);
                        concat.writeAll(callbackCodeView.data());
                        concat.write(Constants.BUFFER_SEP);
                        concat.writeAll(result.data());
                        result.free();
                        payload.free();
                        send_rpc_resp(concat, channel);
                    });
                    break;
                case WSConstants.REQ_UNLOCK:
                    process_unlock(it.next(), result -> {
                        Buffer concat = graph.newBuffer();
                        concat.write(WSConstants.RESP_UNLOCK);
                        concat.write(Constants.BUFFER_SEP);
                        concat.writeAll(callbackCodeView.data());
                        payload.free();
                        send_rpc_resp(concat, channel);
                    });
                    break;
                case WSConstants.REQ_PUT:
                    final List<ChunkKey> flatKeys = new ArrayList<ChunkKey>();
                    final List<Buffer> flatValues = new ArrayList<Buffer>();
                    while (it.hasNext()) {
                        final Buffer keyView = it.next();
                        final Buffer valueView = it.next();
                        if (valueView != null) {
                            flatKeys.add(ChunkKey.build(keyView));
                            flatValues.add(valueView);
                        }
                    }
                    process_put(flatKeys.toArray(new ChunkKey[flatKeys.size()]), flatValues.toArray(new Buffer[flatValues.size()]), result -> {
                        Buffer concat = graph.newBuffer();
                        concat.write(WSConstants.RESP_UNLOCK);
                        concat.write(Constants.BUFFER_SEP);
                        concat.writeAll(callbackCodeView.data());
                        payload.free();
                        send_rpc_resp(concat, channel);
                    });
                    break;
            }
        }
    }

    private void process_lock(Callback<Buffer> callback) {
        graph.storage().lock(callback);
    }

    private void process_unlock(Buffer toUnlock, Callback<Boolean> callback) {
        graph.storage().unlock(toUnlock, callback);
    }

    private void process_put(final ChunkKey[] keys, final Buffer[] values, Callback<Boolean> callback) {
        DeferCounter defer = graph.counter(keys.length);
        defer.then(callback);
        for (int i = 0; i < keys.length; i++) {
            final int finalI = i;
            ChunkKey tuple = keys[i];
            graph.space().getOrLoadAndMark(tuple.type, tuple.world, tuple.time, tuple.id, new Callback<Chunk>() {
                @Override
                public void on(Chunk memoryChunk) {
                    if (memoryChunk != null) {
                        memoryChunk.merge(values[finalI]);
                        graph.space().unmarkChunk(memoryChunk);
                    }
                    defer.count();
                }
            });
        }
    }

    private void process_get(ChunkKey[] keys, Callback<Buffer> callback) {
        DeferCounter defer = graph.counter(keys.length);
        final Buffer[] buffers = new Buffer[keys.length];
        defer.then(result -> {
            Buffer stream = graph.newBuffer();
            for (int i = 0; i < buffers.length; i++) {
                if (i != 0) {
                    stream.write(Constants.BUFFER_SEP);
                }
                if (buffers[i] != null) {
                    stream.writeAll(buffers[i].data());
                    buffers[i].free();
                }
            }
            callback.on(stream);
        });
        for (int i = 0; i < keys.length; i++) {
            final int fixedI = i;
            ChunkKey tuple = keys[i];
            graph.space().getOrLoadAndMark(tuple.type, tuple.world, tuple.time, tuple.id, new Callback<Chunk>() {
                @Override
                public void on(Chunk memoryChunk) {
                    if (memoryChunk != null) {
                        final Buffer toSaveBuffer = graph.newBuffer();
                        memoryChunk.save(toSaveBuffer);
                        graph.space().unmarkChunk(memoryChunk);
                        buffers[fixedI] = toSaveBuffer;
                    } else {
                        buffers[fixedI] = null;
                    }
                    defer.count();
                }
            });
        }
    }

    private void send_rpc_resp(Buffer stream, final WebSocketChannel channel) {
        ByteBuffer finalBuf = ByteBuffer.wrap(stream.data());
        stream.free();
        WebSockets.sendBinary(finalBuf, channel, new WebSocketCallback<Void>() {
            @Override
            public void complete(WebSocketChannel webSocketChannel, Void aVoid) {
                //TODO process
            }

            @Override
            public void onError(WebSocketChannel webSocketChannel, Void aVoid, Throwable throwable) {
                throwable.printStackTrace();
            }
        });
    }

}
