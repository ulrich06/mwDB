package org.mwg.ws;

import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.protocols.ssl.UndertowXnioSsl;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import org.xnio.OptionMap;
import org.xnio.Xnio;

import javax.net.ssl.*;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore;

/**
 * Created by ludovicmouline on 10/05/16.
 */
public class TestKeys {

    public static void main(String[] args) throws Exception{
        SSLContext sslContext = createSSLContext(loadKeyStore("server.keystore","WS-Server-Keystore-16".toCharArray()),
                "WS-Server-Keystore-16".toCharArray(),loadKeyStore("server.truststore","WS-Server-Truststore-16".toCharArray()),
                null);
        Undertow server = Undertow.builder()
//                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
//                .setServerOption(UndertowOptions.ENABLE_SPDY, true)
                .addHttpsListener(8443, "0.0.0.0", sslContext)
                /*.setHandler(new SessionAttachmentHandler(new LearningPushHandler(100, -1, Handlers.header(predicate(secure(), resource(new PathResourceManager(Paths.get(System.getProperty("example.directory", System.getProperty("user.home"))), 100))
                        .setDirectoryListingEnabled(true), new HttpHandler() {
                    @Override
                    public void handleRequest(HttpServerExchange exchange) throws Exception {
                        exchange.getResponseHeaders().add(Headers.LOCATION, "https://" + exchange.getHostName() + ":" + (exchange.getHostPort() + 363) + exchange.getRelativePath());
                        exchange.setStatusCode(StatusCodes.TEMPORARY_REDIRECT);
                    }
                }), "x-undertow-transport", ExchangeAttributes.transportProtocol())), new InMemorySessionManager("test"), new SessionCookieConfig()))*/.build();

        server.start();

        SSLContext clientSslContext = createSSLContext(loadKeyStore("client.keystore","WS-Client-Keystore-16".toCharArray()),
                "WS-Client-Keystore-16".toCharArray(), loadKeyStore("client.truststore","WS-Client-Truststore-16".toCharArray()),null);


        LoadBalancingProxyClient proxy = new LoadBalancingProxyClient()
                .addHost(new URI("https://localhost:8447"), null, new UndertowXnioSsl(Xnio.getInstance(), OptionMap.EMPTY, clientSslContext), OptionMap.create(UndertowOptions.ENABLE_HTTP2, true))
                .setConnectionsPerThread(20);

        Undertow reverseProxy = Undertow.builder()
//                .setServerOption(UndertowOptions.ENABLE_HTTP2, true)
//                .setServerOption(UndertowOptions.ENABLE_SPDY, true)
                .addHttpsListener(8444, "0.0.0.0", sslContext)
                .setHandler(new ProxyHandler(proxy, 30000, ResponseCodeHandler.HANDLE_404))
                .build();
        reverseProxy.start();

    }


    private static KeyStore loadKeyStore(String name, char[] password) throws Exception {
        String storeLoc = System.getProperty(name);
        final InputStream stream;
        if(storeLoc == null) {
            stream = TestKeys.class.getResourceAsStream(name);
        } else {
            stream = Files.newInputStream(Paths.get(storeLoc));
        }

        try(InputStream is = stream) {
            KeyStore loadedKeystore = KeyStore.getInstance("JKS");
            loadedKeystore.load(is, password);
            return loadedKeystore;
        }
    }

    private static SSLContext createSSLContext(final KeyStore keyStore, char[] passwordKeyStore,
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



}
