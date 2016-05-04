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
import org.mwg.core.CoreConstants;
import org.mwg.core.utility.Base64;
import org.mwg.core.utility.BufferView;
import org.mwg.plugin.Storage;
import org.mwg.struct.Buffer;
import org.mwg.struct.BufferIterator;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

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
    private short _prefix = -1;

    private Set<WebSocketChannel> _peers;

    public WSStorageWrapper(Storage _wrapped, int port) {
        this._wrapped = _wrapped;
        HttpHandler handler = Handlers.websocket(this);
        _server = Undertow.builder().addHttpListener(port,"0.0.0.0").setHandler(handler).build();
        _peers = new HashSet<>();
    }

    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        channel.getReceiveSetter().set(new WSListener());
        channel.resumeReceives();
        _peers.add(channel);
    }

    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        if(_graph == null) {
            throw new RuntimeException("Please connect the websocket first.");
        }
        _wrapped.get(keys,callback);
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {
        if(_graph == null) {
            throw new RuntimeException("Please connect the websocket first.");
        }
        _wrapped.put(stream, new Callback<Boolean>() {
            @Override
            public void on(Boolean result) {
                System.out.println("Data put");
                if(result) {
                    stream.write(CoreConstants.BUFFER_SEP);
                    stream.write(WSMessageType.RQST_FORCE_RELOAD);
                    ByteBuffer byteBuffer = ByteBuffer.wrap(stream.data());
                    //fixme
                    for(WebSocketChannel client: _peers) {
                        WebSockets.sendBinary(byteBuffer,client,null);
                    }
                }

                callback.on(result);
            }
        });


    }

    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {
        if(_graph == null) {
            throw new RuntimeException("Please connect the websocket first.");
        }
        _wrapped.remove(keys,callback);
    }

    @Override
    public void connect(Graph graph, Callback<Short> callback) {
        if(_graph == null) {
            _server.start();
            _graph = graph;
            _wrapped.connect(graph, new Callback<Short>() {
                @Override
                public void on(Short result) {
                    _prefix = result;
                    if(callback != null) {
                        callback.on(result);
                    }
                }
            });
        } else {
            if(callback != null) {
                callback.on(null);
            }
        }
    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        if(_graph != null) {
            _server.stop();
            _graph = null;
            _prefix = -1;
            _wrapped.disconnect(prefix, callback);
        }
    }

    private class WSListener extends AbstractReceiveListener {
        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            ByteBuffer[] data = message.getData().getResource();
            for(ByteBuffer byteBuffer : data) {
                final Buffer buffer = _graph.newBuffer();
                byte[] bytes = new byte[byteBuffer.limit()];
                byteBuffer.get(bytes);
                buffer.writeAll(bytes);
                bytes = null;

                BufferView wsInfo = null;
                BufferIterator it = buffer.iterator();
                while(it.hasNext()) {
                    wsInfo = (BufferView) it.next();
                }

                //Contains bytes representing the WS info
                final byte[] wsIndoData = wsInfo.data();

                //remove the WS info
                for(int i= 0;i<=wsInfo.size();i++) {
                    buffer.removeLast();
                }

                switch (wsIndoData[0]) {
                    case WSMessageType.RQST_GET: {
                        get(buffer, new Callback<Buffer>() {
                            @Override
                            public void on(Buffer result) {
                                result.write(CoreConstants.BUFFER_SEP);
                                result.write(WSMessageType.RESP_GET);
                                for (int i = 1; i < wsIndoData.length; i++) {
                                    result.write(wsIndoData[i]);
                                }
                                ByteBuffer toSend = ByteBuffer.wrap(result.data());
                                WebSockets.sendBinary(toSend, channel, null);
                                result.free();
                                buffer.free();
                            }
                        });
                        break;
                    }
                    case WSMessageType.RQST_PUT: {
                        put(buffer, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                byte[] toSend = new byte[wsIndoData.length + 2];
                                toSend[0] = (byte) ((result) ? 1 : 0);
                                toSend[1] = CoreConstants.BUFFER_SEP;
                                toSend[2] = WSMessageType.RESP_PUT;
                                System.arraycopy(wsIndoData, 1, toSend, 3, wsIndoData.length - 1);
                                ByteBuffer bufferToSend = ByteBuffer.wrap(toSend);
                                WebSockets.sendBinary(bufferToSend, channel, null);
                                buffer.free();
                            }
                        });
                        break;
                    }
                    case WSMessageType.RQST_REMOVE: {
                        remove(buffer, new Callback<Boolean>() {
                            @Override
                            public void on(Boolean result) {
                                byte[] toSend = new byte[wsIndoData.length + 2];
                                toSend[0] = (byte) ((result) ? 1 : 0);
                                toSend[1] = CoreConstants.BUFFER_SEP;
                                toSend[2] = WSMessageType.RESP_REMOVE;
                                System.arraycopy(wsIndoData, 1, toSend, 3, wsIndoData.length - 1);
                                ByteBuffer bufferToSend = ByteBuffer.wrap(toSend);
                                WebSockets.sendBinary(bufferToSend, channel, null);
                                buffer.free();
                            }
                        });
                        break;
                    }
                    case WSMessageType.RQST_PREFIX: {
                        Buffer toSend = _graph.newBuffer();
                        Base64.encodeIntToBuffer(_prefix,toSend);
                        toSend.write(CoreConstants.BUFFER_SEP);
                        toSend.write(WSMessageType.RESP_PREFIX);
                        for (int i = 1; i < wsIndoData.length; i++) {
                            toSend.write(wsIndoData[i]);
                        }
                        ByteBuffer bufferToSend = ByteBuffer.wrap(toSend.data());
                        WebSockets.sendBinary(bufferToSend, channel, null);
                        toSend.free();

                        break;
                    }
                    default: {
                        System.err.println("Unknown message with code " + wsIndoData[wsIndoData.length - 1]);
                        buffer.free();
                    }
                }
            }
        }

    }


}
