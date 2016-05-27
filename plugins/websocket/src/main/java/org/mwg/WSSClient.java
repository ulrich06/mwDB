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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class WSSClient implements Storage {

    private final String url;

    private WebSocketChannel channel;

    private Graph graph;

    private BlockingQueue<Byte> keyPool;

    private Callback[] callbacks;

    public WSSClient(String p_url) {
        this.url = p_url;
        keyPool = new ArrayBlockingQueue<Byte>(Byte.MAX_VALUE - Byte.MIN_VALUE);
        for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
            keyPool.add(i);
        }
        keyPool.add(Byte.MAX_VALUE);
        callbacks = new Callback[Byte.MAX_VALUE - Byte.MIN_VALUE];
    }

    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        if (channel == null) {
            throw new RuntimeException("Please connect your websocket client first.");
        }

        Short.MAX_VALUE


        byte[] keys_payload = keys.data();
        ByteBuffer buffer = ByteBuffer.allocate(keys_payload.length + 2);
        buffer.put(WSConstants.REQ_GET);
        try {
            buffer.put(keyPool.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {

    }

    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {

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

    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {

    }

    private void send(Buffer buffer) {
        ByteBuffer wrapped = ByteBuffer.wrap(buffer.data());
        buffer.free();
        WebSockets.sendBinary(wrapped, channel, new WebSocketCallback<Void>() {
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

    private class MessageReceiver extends AbstractReceiveListener {
        @Override
        protected void onFullBinaryMessage(WebSocketChannel channel, BufferedBinaryMessage message) throws IOException {
            //TODO
            super.onFullBinaryMessage(channel, message);
        }

        @Override
        protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
            //TODO
            super.onFullTextMessage(channel, message);
        }
    }


}
