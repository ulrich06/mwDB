package org.mwg.ws;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.attribute.ExchangeAttributes;
import io.undertow.client.UndertowClient;
import io.undertow.connector.ByteBufferPool;
import io.undertow.protocols.ssl.UndertowXnioSsl;
import io.undertow.server.DefaultByteBufferPool;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.LearningPushHandler;
import io.undertow.server.handlers.resource.PathResourceManager;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.AbstractReceiveListener;
import io.undertow.websockets.core.BufferedTextMessage;
import io.undertow.websockets.core.WebSocketChannel;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.xnio.*;
import org.xnio.ssl.SslConnection;
import org.xnio.ssl.XnioSsl;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

import static io.undertow.Handlers.predicate;
import static io.undertow.Handlers.resource;
import static io.undertow.predicate.Predicates.secure;

public class WSStorageWrapperSecure implements WebSocketConnectionCallback{
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

        Undertow server = Undertow.builder()
                .addHttpsListener(8443, "0.0.0.0", sslContext)
//                .setHandler(Handlers.websocket(this))
                .setHandler(new SessionAttachmentHandler(new LearningPushHandler(100, -1, Handlers.header(predicate(secure(), resource(new PathResourceManager(Paths.get(System.getProperty("example.directory", System.getProperty("user.home"))), 100))
                        .setDirectoryListingEnabled(true), new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().add(Headers.LOCATION, "https://" + exchange.getHostName() + ":" + (exchange.getHostPort() + 363) + exchange.getRelativePath());
                        exchange.setStatusCode(StatusCodes.TEMPORARY_REDIRECT);
                    }
                }), "x-undertow-transport", ExchangeAttributes.transportProtocol())), new InMemorySessionManager("test"), new SessionCookieConfig()))
                .build();

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
        WSStorageWrapperSecure server = new WSStorageWrapperSecure();

        SSLContext clientSslContext = createSSLContext(loadKeyStore("client.keystore","WS-Client-Keystore-16".toCharArray()),
                "WS-Client-Keystore-16".toCharArray(), loadKeyStore("client.truststore","WS-Client-Truststore-16".toCharArray()),null);


        try {
            /*XnioWorker _worker;
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
                    .connectionBuilder(_worker, _buffer, new URI("wss://" + "0.0.0.0" + ":" + 8443)).setSsl(xnioSsl);

            builder.connect().get();*/


            XnioSsl ssl = new UndertowXnioSsl(Xnio.getInstance(), OptionMap.EMPTY,clientSslContext);
            XnioWorker worker;
            worker = Xnio.getInstance().createWorker(OptionMap.builder()
                    .set(Options.WORKER_IO_THREADS, 2)
                    .set(Options.CONNECTION_HIGH_WATER, 1_000_000)
                    .set(Options.CONNECTION_LOW_WATER, 1_000_000)
                    .set(Options.WORKER_TASK_CORE_THREADS, 30)
                    .set(Options.WORKER_TASK_MAX_THREADS, 30)
                    .set(Options.TCP_NODELAY, true)
                    .set(Options.CORK, true)
                    .getMap());
            ByteBufferPool buffer = new DefaultByteBufferPool(true, 1024 * 1024);





            ssl.openSslConnection(worker, new InetSocketAddress(8443), new ChannelListener<SslConnection>() {
                @Override
                public void handleEvent(SslConnection channel) {
                    System.out.println(channel.getIoThread().isAlive());
                    try {
                        channel.startHandshake();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
            },OptionMap.EMPTY).addNotifier(new IoFuture.HandlingNotifier<SslConnection, String>() {
                @Override
                public void handleCancelled(String attachment) {
                    System.err.println("1-Cancelled");
                }

                @Override
                public void handleFailed(IOException exception, String attachment) {
                    System.err.println("2-Failed");
                    exception.printStackTrace();
                }

                @Override
                public void handleDone(SslConnection data, String attachment) {
                    System.out.println("2-Done");
                    try {
                        data.startHandshake();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            },"Toto");

            UndertowClient.getInstance().connect();

            /*builder.connect().addNotifier(new IoFuture.HandlingNotifier<WebSocketChannel, String>() {
                @Override
                public void handleCancelled(String attachement) {
                    System.err.println("Cancelled");
                }

                @Override
                public void handleFailed(IOException exception, String attachment) {
                    System.err.println("Failed");
                    exception.printStackTrace();
                }

                @Override
                public void handleDone(WebSocketChannel data, String attachment) {
                    System.out.println("Done");
                }
            },"Titi");*/




        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }
}
