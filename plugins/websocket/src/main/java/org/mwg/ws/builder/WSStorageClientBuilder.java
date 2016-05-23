package org.mwg.ws.builder;

import org.mwg.ws.WSStorageClient;
import org.mwg.ws.utils.SSLContextFactory;

import java.io.IOException;

import static org.mwg.ws.builder.WSStorageWrapperBuilder.DEFAULT_KEYSTORE_TYPE;

public class WSStorageClientBuilder extends WebSocketBuilder{
    public static WSStorageClientBuilder builder() {
        return new WSStorageClientBuilder();
    }

    private String _url;
    private int _port;
    private boolean _ssl;

    public WSStorageClientBuilder withPort(int port) {
        _port = port;
        return this;
    }

    public WSStorageClientBuilder withUrl(String url) {
        _url = url;
        return this;
    }

    public WSStorageClientBuilder withSSL() {
        _ssl = true;
        return this;
    }

    public WSStorageClientBuilder withKeyStore(String path, String password, String type) {
        internalWithKeyStore(path,password,type);
        return this;
    }

    public WSStorageClientBuilder withKeyStore(String path, String password) {
        internalWithKeyStore(path,password,DEFAULT_KEYSTORE_TYPE);
        return this;
    }

    public WSStorageClientBuilder withKeyStore(String path) {
        internalWithKeyStore(path,null,DEFAULT_KEYSTORE_TYPE);
        return this;
    }

    public WSStorageClientBuilder withTrustStore(String path, String password, String type) {
        internalWithTrustStore(path,password,type);
        return this;
    }

    public WSStorageClientBuilder withTrustStore(String path, String password) {
        internalWithTrustStore(path,password,DEFAULT_KEYSTORE_TYPE);
        return this;
    }

    public WSStorageClientBuilder withTrustStore(String path) {
        internalWithTrustStore(path,null,DEFAULT_KEYSTORE_TYPE);
        return this;
    }

    private void checkParamWithoutSSL() {
        if(_url == null) {
            throw new RuntimeException("The url should be initialized before build");
        }
        if(_port == 0) {
            throw new RuntimeException("The port should be initialized before build");
        }
    }

    public WSStorageClient build() throws IOException {
        checkParamWithoutSSL();
        if(_ssl) {
            checkSSLParam();
            return new WSStorageClient(_url,_port, SSLContextFactory.createSSLContext(_pathKeyStore,_passKeyStore,
                    _keyStoreType,_pathTrustStore,_passTrustStore,_trustStoreType));
        } else {
            return new WSStorageClient(_url,_port);
        }
    }


}
