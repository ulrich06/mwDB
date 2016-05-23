package org.mwg.ws;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.connector.ByteBufferPool;
import io.undertow.protocols.ssl.UndertowXnioSsl;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.client.WebSocketClient;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.xnio.*;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.mwg.ws.utils.SSLContextFactory.createSSLContext;


/**
 * Created by ludovicmouline on 23/05/16.
 */
public class TestWSS {

    public static void main(String[] args) throws Exception{

        System.out.println(Arrays.toString("".toCharArray()));
        String serverKeyStore = TestWSS.class.getClassLoader().getResource("server.keystore").getPath();
        String serverTrustStore = TestWSS.class.getClassLoader().getResource("server.truststore").getPath();

        String clientKeyStore = TestWSS.class.getClassLoader().getResource("client.keystore").getPath();
        String clientTrustStore = TestWSS.class.getClassLoader().getResource("client.truststore").getPath();

        Undertow server = Undertow.builder().addHttpsListener(7778, "localhost", createSSLContext(serverKeyStore,"password","JKS",serverTrustStore,"password","JKS"))
                .setHandler(Handlers.websocket(new WebSocketConnectionCallback() {
                    @Override
                    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                        channel.getReceiveSetter().set(new AbstractReceiveListener() {
                            @Override
                            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                                System.out.println("Server: " + message.getData());
                            }
                        });
                        channel.resumeReceives();
                    }
                }))
                .build();
        server.start();

        //client

        Xnio xnio = Xnio.getInstance();
        XnioWorker worker = xnio.createWorker(OptionMap.builder()
                .set(Options.WORKER_IO_THREADS, 2)
                .set(Options.CONNECTION_HIGH_WATER, 1000000)
                .set(Options.CONNECTION_LOW_WATER, 1000000)
                .set(Options.WORKER_TASK_CORE_THREADS, 30)
                .set(Options.WORKER_TASK_MAX_THREADS, 30)
                .set(Options.TCP_NODELAY, true)
                .set(Options.CORK, true)
                .getMap());
        ByteBufferPool buffer = new DefaultByteBufferPool(true, 1024 * 1024);

        UndertowXnioSsl ssl = new UndertowXnioSsl(Xnio.getInstance(), OptionMap.EMPTY, createSSLContext(clientKeyStore,"password","JKS",clientTrustStore,"password","JKS"));
        final WebSocketClient.ConnectionBuilder connectionBuilder = WebSocketClient.connectionBuilder(worker, buffer, new URI("wss://localhost:7778"))
                .setSsl(ssl);
        IoFuture<WebSocketChannel> future = connectionBuilder.connect();
        future.await(/*4, TimeUnit.SECONDS*/);
        final WebSocketChannel webSocketChannel = future.get();

        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<String> result = new AtomicReference<>();
        webSocketChannel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                String data = message.getData();
                result.set(data);
                latch.countDown();
            }

            @Override
            protected void onError(WebSocketChannel channel, Throwable error) {
                super.onError(channel, error);
                error.printStackTrace();
                latch.countDown();
            }
        });
        webSocketChannel.resumeReceives();

        ByteBuffer bb = ByteBuffer.wrap("Hello World".getBytes());
        WebSockets.sendText(bb, webSocketChannel, new WebSocketCallback<Void>() {
            @Override
            public void complete(WebSocketChannel channel, Void context) {
                System.out.println("COMPLETE");
            }

            @Override
            public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
                System.err.println("ERROR");
            }
        });
    }

}
