package org.mwg.ws.utils;

import org.xnio.IoUtils;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

/**
 * Created by ludovicmouline on 23/05/16.
 */
public interface SSLContextFactory {
    static KeyStore loadKeyStore(final String filePath, final String password, final String keyStoreType) throws IOException {
        FileInputStream stream = new FileInputStream(filePath);
        try {
            KeyStore loadedKeystore = KeyStore.getInstance(keyStoreType);
            if(password != null) {
                loadedKeystore.load(stream, password.toCharArray());
            } else {
                loadedKeystore.load(stream, null);

            }

            return loadedKeystore;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new IOException(String.format("Unable to load KeyStore %s", filePath), e);
        } finally {
            IoUtils.safeClose(stream);
        }
    }

    static SSLContext createSSLContext(final String pathKeyStore, final String passKeyStore, final String keyStoreType,
                                       final String pathTrustStore, final String passTrustStore,
                                       final String trustStoreType) throws IOException {
        KeyStore keyStore = loadKeyStore(pathKeyStore,passKeyStore,keyStoreType);
        KeyStore trustStore = loadKeyStore(pathTrustStore,passTrustStore,trustStoreType);

        KeyManager[] keyManagers;
        try {
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, passKeyStore.toCharArray());
            keyManagers = keyManagerFactory.getKeyManagers();
        } catch (NoSuchAlgorithmException | UnrecoverableKeyException | KeyStoreException e) {
            throw new IOException("Unable to initialise KeyManager[]", e);
        }

        TrustManager[] trustManagers = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            trustManagers = trustManagerFactory.getTrustManagers();
        } catch (NoSuchAlgorithmException | KeyStoreException e) {
            throw new IOException("Unable to initialise TrustManager[]", e);
        }

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(keyManagers, trustManagers, null);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IOException("Unable to create and initialise the SSLContext", e);
        }

        return sslContext;
    }
}
