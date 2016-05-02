package org.mwg.ws;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedBinaryMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.core.WebSockets;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.plugin.Storage;
import org.mwg.struct.Buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Add a WS wrapper on another storage to allow a remote access on this storage
 *
 * Each received messages finish with 5 specific bytes (added by the client, that are: 4 bytes for the message ID
 * (specific and only useful for the client) and one byte for the type of request (see {@link WSMessageType}).
 */
public class WSStorageWrapper implements Storage, WebSocketConnectionCallback{
    private final Storage _wrapped;
    private final Undertow _server;
    private Graph _graph;

    public WSStorageWrapper(Storage _wrapped, int port) {
        this._wrapped = _wrapped;
        HttpHandler handler = Handlers.websocket(this);
        _server = Undertow.builder().addHttpListener(port,"0.0.0.0").setHandler(handler).build();
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        channel.getReceiveSetter().set(new WSListener());
        channel.resumeReceives();
    }

    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        _wrapped.get(keys,callback);
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {
        _wrapped.put(stream,callback);
    }

    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {
        _wrapped.remove(keys,callback);
    }

    @Override
    public void connect(Graph graph, Callback<Short> callback) {
        _server.start();
        _graph = graph;
        _wrapped.connect(graph,callback);
    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        _server.stop();
        _graph = null;
        _wrapped.disconnect(prefix,callback);
    }

    private class WSListener extends AbstractReceiveListener {
        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            ByteBuffer[] data = message.getData().getResource();

            Buffer buffer;
            for(ByteBuffer byteBuffer : data) {
                buffer = _graph.newBuffer();
                byte[] bytes = new byte[byteBuffer.limit()];
                byteBuffer.get(bytes);
                for(int i =0; i<bytes.length - 5;i++) { //we does not add the bytes specific to the WS plugin
                    buffer.write(bytes[i]);
                }
                switch (bytes[bytes.length - 1]) {
                    case WSMessageType.RQST_GET:
                        get(buffer, new Callback<Buffer>() {
                            @Override
                            public void on(Buffer result) {
                                result.write(bytes[bytes.length - 5]); //write message ID
                                result.write(WSMessageType.RESP_GET);
                                ByteBuffer toSend = ByteBuffer.wrap(result.data());
                                WebSockets.sendBinary(toSend,channel,null);
                            }
                        });
                        break;
                    case WSMessageType.RQST_PUT:
                        put(buffer, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                Buffer toSend = _graph.newBuffer();
                                byte byteResult = (byte) ((result)?1 : 0);
                                toSend.write(byteResult);
                                toSend.write(bytes[bytes.length - 5]); //write message ID
                                toSend.write(WSMessageType.RESP_PUT);
                                ByteBuffer byteBuffer = ByteBuffer.wrap(toSend.data());
                                WebSockets.sendBinary(byteBuffer,channel,null);
                            }
                        });
                        break;
                    case WSMessageType.RQST_REMOVE:
                        remove(buffer, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                Buffer toSend = _graph.newBuffer();
                                byte byteResult = (byte) ((result)?1 : 0);
                                toSend.write(byteResult);
                                toSend.write(bytes[bytes.length - 5]); //write message ID
                                toSend.write(WSMessageType.RESP_REMOVE);
                                ByteBuffer byteBuffer = ByteBuffer.wrap(toSend.data());
                                WebSockets.sendBinary(byteBuffer,channel,null);
                            }
                        });
                        break;
                    default:
                        System.err.println("Unknown message with code " + bytes[bytes.length - 1]);
                }
                buffer = null;
            }
        }

    }


}
