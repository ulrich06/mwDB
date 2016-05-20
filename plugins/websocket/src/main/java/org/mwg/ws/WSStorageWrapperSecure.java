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
import org.xnio.OptionMap;
import org.xnio.Options;
import org.xnio.Xnio;
import org.xnio.XnioWorker;
import org.xnio.ssl.XnioSsl;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

public class WSStorageWrapperSecure implements WebSocketConnectionCallback{
    private Undertow server;
    @Override
    public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
        System.out.println("connection");
        channel.getReceiveSetter().set(new AbstractReceiveListener() {
            @Override
            protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) throws IOException {
                System.out.println(message.getData());
            }
        });
        channel.resumeReceives();
    }

    public WSStorageWrapperSecure() throws Exception {
        SSLContext sslContext = createSSLContext(loadKeyStore("server.keystore","WS-Server-Keystore-16".toCharArray()),
                "WS-Server-Keystore-16".toCharArray(),loadKeyStore("server.truststore","WS-Server-Truststore-16".toCharArray()),
                null);

        server = Undertow.builder()
                .addHttpsListener(8443, "0.0.0.0", NaiveSSLContext.getInstance("TLS"))
                .setHandler(Handlers.websocket(this))
//                .setHandlerStow-transport", ExchangeAttributes.transportProtocol())), new InMemorySessionManager("test"), new SessionCookieConfig()))
                .build();

        //server.start();
    }

    public void start() {
        server.start();
    }



    public static KeyStore loadKeyStore(String name, char[] password) throws Exception {
        String storeLoc = System.getProperty(name);
        final InputStream stream;
        if(storeLoc == null) {
            stream = WSStorageWrapperSecure.class.getResourceAsStream(name);
        } else {
            stream = Files.newInputStream(Paths.get(storeLoc));
        }

        try(InputStream is = stream) {
            KeyStore loadedKeystore = KeyStore.getInstance("JKS");
            loadedKeystore.load(is, password);
            return loadedKeystore;
        }
    }


    public static SSLContext createSSLContext(final KeyStore keyStore, char[] passwordKeyStore,
                                               final KeyStore trustStore, char[] passwordTrustStore) throws Exception {
        KeyManager[] keyManagers;
        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, passwordKeyStore);
        keyManagers = keyManagerFactory.getKeyManagers();

        TrustManager[] trustManagers;
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(trustStore);
        trustManagers = trustManagerFactory.getTrustManagers();

        SSLContext sslContext;
        sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagers, trustManagers, null);

        return sslContext;
    }

    public static void main(String[] args) throws Exception {
//        WSStorageWrapperSecure server = new WSStorageWrapperSecure();
//        server.start();

//        SSLContext clientSslContext = createSSLContext(loadKeyStore("client.keystore","WS-Client-Keystore-16".toCharArray()),
//                "WS-Client-Keystore-16".toCharArray(), loadKeyStore("client.truststore","WS-Client-Truststore-16".toCharArray()),null);


        SSLContext clientSslContext = Http2Server.createSSLContext(Http2Server.loadKeyStore("client.keystore"), Http2Server.loadKeyStore("client.truststore"));
        try {
            XnioWorker _worker;
            Xnio xnio2 = Xnio.getInstance(io.undertow.websockets.client.WebSocketClient.class.getClassLoader());
            XnioSsl xnioSsl = new UndertowXnioSsl(xnio2, OptionMap.EMPTY, clientSslContext);
            _worker = xnio2.createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, 2)
                    .set(Options.CONNECTION_HIGH_WATER, 1_000_000)
                    .set(Options.CONNECTION_LOW_WATER, 1_000_000)
                    .set(Options.WORKER_TASK_CORE_THREADS, 30)
                    .set(Options.WORKER_TASK_MAX_THREADS, 30)
                    .set(Options.TCP_NODELAY, true)
                    .set(Options.CORK, true)
                    .getMap());
            ByteBufferPool _buffer = new DefaultByteBufferPool(true, 1024 * 1024);
            WebSocketClient.ConnectionBuilder builder = io.undertow.websockets.client.WebSocketClient
                    .connectionBuilder(_worker, _buffer, new URI("wss://localhost:8443")).setSsl(xnioSsl);

            builder.connect().get();

            WebSocketChannel channel = builder.connect().get();
            channel.getReceiveSetter().set(new AbstractReceiveListener() {
                @Override
                protected void onText(WebSocketChannel webSocketChannel, StreamSourceFrameChannel messageChannel) throws IOException {
                    System.out.println("toto");
                }
            });
            channel.resumeReceives();
            WebSockets.sendText("Toto", channel, new WebSocketCallback<Void>() {
                @Override
                public void complete(WebSocketChannel channel, Void context) {
                    System.err.println("Complete complete");
                }

                @Override
                public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
                    System.err.println("Error error");
                }
            });

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
