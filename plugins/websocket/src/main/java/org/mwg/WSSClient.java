package org.mwg;

import io.undertow.connector.ByteBufferPool;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.*;
import org.mwg.plugin.Storage;
import org.mwg.struct.Buffer;
import org.xnio.*;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class WSSClient implements Storage {

    private final String url;

    private WebSocketChannel channel;

    private Graph graph;

    private Map<Integer, Callback<Buffer>> callbacks;

    public WSSClient(String p_url) {
        this.url = p_url;
        this.callbacks = new HashMap<Integer, Callback<Buffer>>();
    }

    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        if (channel == null) {
            throw new RuntimeException(WSConstants.DISCONNECTED_ERROR);
        }
        byte[] payload = keys.data();
        int hash = callback.hashCode();
        callbacks.put(hash, callback);


        byte[] ex_payload = new byte[payload.length + 5];
        ex_payload[0] = WSConstants.REQ_GET;
        ex_payload[1] = (byte) ((hash & 0xFF000000) >> 24);
        ex_payload[2] = (byte) ((hash & 0x00FF0000) >> 16);
        ex_payload[3] = (byte) ((hash & 0x0000FF00) >> 8);
        ex_payload[4] = (byte) ((hash & 0x000000FF) >> 0);
        System.arraycopy(payload,0,ex_payload,5,payload.length);
        send(ByteBuffer.wrap(ex_payload));
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {
        if (channel == null) {
            throw new RuntimeException(WSConstants.DISCONNECTED_ERROR);
        }
        byte[] payload = stream.data();
        ByteBuffer buffer = ByteBuffer.allocate(payload.length + 1);
        buffer.put(WSConstants.REQ_PUT);
        buffer.put(payload);
        send(buffer);
    }

    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {
        if (channel == null) {
            throw new RuntimeException(WSConstants.DISCONNECTED_ERROR);
        }
        byte[] payload = keys.data();
        ByteBuffer buffer = ByteBuffer.allocate(payload.length + 1);
        buffer.put(WSConstants.REQ_REMOVE);
        buffer.put(payload);
        send(buffer);
    }

    @Override
    public void connect(final Graph p_graph, final Callback<Short> callback) {
        if (channel != null) {
            if (callback != null) {
                callback.on(null);
            }
        }
        this.graph = p_graph;
        try {
            XnioWorker _worker;
            Xnio xnio = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
            _worker = xnio.createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, 2)
                    .set(Options.CONNECTION_HIGH_WATER, 1_000_000)
                    .set(Options.CONNECTION_LOW_WATER, 1_000_000)
                    .set(Options.WORKER_TASK_CORE_THREADS, 30)
                    .set(Options.WORKER_TASK_MAX_THREADS, 30)
                    .set(Options.TCP_NODELAY, true)
                    .set(Options.CORK, true)
                    .getMap());
            ByteBufferPool _buffer = new DefaultByteBufferPool(true, 1024 * 1024);
            //String scheme =  (_sslContext == null)? "ws" : "wss";
            WebSocketClient.ConnectionBuilder builder = io.undertow.websockets.client.WebSocketClient
                    .connectionBuilder(_worker, _buffer, new URI(url));

            /*
            if(_sslContext != null) {
                UndertowXnioSsl ssl = new UndertowXnioSsl(Xnio.getInstance(), OptionMap.EMPTY, _sslContext);
                builder.setSsl(ssl);
            }*/

            IoFuture<WebSocketChannel> futureChannel = builder.connect();
            futureChannel.await(30, TimeUnit.SECONDS); //Todo change this magic number!!!
            if (futureChannel.getStatus() != IoFuture.Status.DONE) {
                System.err.println("Error during connexion with webSocket");
                if (callback != null) {
                    callback.on(null);
                }
            }
            channel = futureChannel.get();
            channel.getReceiveSetter().set(new MessageReceiver());
            channel.resumeReceives();
        } catch (Exception e) {
            throw new RuntimeException("Error during connection to " + url + ":" + e.getMessage());
        }

        /*
        if (callback != null) {
            Buffer buffer = _graph.newBuffer();
            buffer.write(WSMessageType.RQST_PREFIX);
            int msgID = nextMessageID();
            Base64.encodeIntToBuffer(msgID, buffer);
            _callBacks.put(msgID, callback);
            send(buffer);
        }*/

        callback.on(new Short("10"));//todo change this

    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        try {
            channel.sendClose();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            callback.on(true);
        }
    }

    private void send(ByteBuffer buffer) {
        WebSockets.sendBinary(buffer, channel, new WebSocketCallback<Void>() {
            @Override
            public void complete(WebSocketChannel webSocketChannel, Void aVoid) {
                //TODO process
                System.out.println("Yes");
            }

            @Override
            public void onError(WebSocketChannel webSocketChannel, Void aVoid, Throwable throwable) {
                //TODO process
                System.out.println("Error");
            }
        });
    }

    //pass to a thread pool
    private void processMessage(byte[] buffer) {
        switch (buffer[0]) {
            case WSConstants.RESP_GET:
                //read 4 bytes
                if (buffer.length >= 5) {
                    int hash = buffer[1] << 24 | (buffer[2] & 0xFF) << 16 | (buffer[3] & 0xFF) << 8 | (buffer[4] & 0xFF);;
                    Callback<Buffer> callback = callbacks.get(hash);
                    if (callback != null) {
                        callbacks.remove(hash);

                        Buffer bufferResult = graph.newBuffer();
                        byte[] shrinked = new byte[buffer.length - 5];
                        System.arraycopy(buffer, 5, shrinked, 0, buffer.length - 5);
                        bufferResult.writeAll(shrinked);
                        callback.on(bufferResult);

                    }
                }


                break;
            case WSConstants.REQ_UPDATE:
                //TODO
                break;
        }
    }

    private class MessageReceiver extends AbstractReceiveListener {
        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            ByteBuffer byteBuffer = WebSockets.mergeBuffers(message.getData().getResource());
            processMessage(byteBuffer.array());
            super.onFullBinaryMessage(channel, message);
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
            processMessage(message.getData().getBytes());
            super.onFullTextMessage(channel, message);
        }
    }

}
