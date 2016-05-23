package org.mwg.ws.builder;

import org.mwg.plugin.Storage;
import org.mwg.ws.WSStorageWrapper;
import org.mwg.ws.utils.SSLContextFactory;

import java.io.IOException;

/**
 * Created by ludovicmouline on 23/05/16.
 */
public class WSStorageWrapperBuilder extends WebSocketBuilder{
    static String DEFAULT_KEYSTORE_TYPE = "JKS";


    public static WSStorageWrapperBuilder builder() {
        return new WSStorageWrapperBuilder();
    }

    private Storage _storageWrapper;
    private boolean _ssl;
    private int _port;

    public WSStorageWrapperBuilder withSSL() {
        _ssl = true;
        return this;
    }

    public WSStorageWrapperBuilder withWrappedStorage(Storage wrapped) {
        _storageWrapper = wrapped;
        return this;
    }

    public WSStorageWrapperBuilder withPort(int port) {
        _port = port;
        return this;
    }

    public WSStorageWrapperBuilder withKeyStore(String path, String password, String type) {
        internalWithKeyStore(path,password,type);
        return this;
    }

    public WSStorageWrapperBuilder withKeyStore(String path, String password) {
        internalWithKeyStore(path,password,DEFAULT_KEYSTORE_TYPE);
        return this;
    }

    public WSStorageWrapperBuilder withKeyStore(String path) {
        internalWithKeyStore(path,null,DEFAULT_KEYSTORE_TYPE);
        return this;
    }

    public WSStorageWrapperBuilder withTrustStore(String path, String password, String type) {
        internalWithTrustStore(path,password,type);
        return this;
    }

    public WSStorageWrapperBuilder withTrustStore(String path, String password) {
        internalWithTrustStore(path,password,DEFAULT_KEYSTORE_TYPE);
        return this;
    }

    public WSStorageWrapperBuilder withTrustStore(String path) {
        internalWithTrustStore(path,null,DEFAULT_KEYSTORE_TYPE);
        return this;
    }

    private void checkParamWithoutSSL() {
        if(_storageWrapper == null) {
            throw new RuntimeException("The wrapped storage should be initialized before build");
        }
        if(_port == 0) {
            throw new RuntimeException("The port should be initialized before build");
        }
    }



    public WSStorageWrapper build() throws IOException {
        checkParamWithoutSSL();
        if(_ssl) {
            checkSSLParam();
            return new WSStorageWrapper(_storageWrapper,_port,
                    SSLContextFactory.createSSLContext(_pathKeyStore,_passKeyStore,_keyStoreType,_pathTrustStore,
                            _passTrustStore,_trustStoreType));
        } else {
            return new WSStorageWrapper(_storageWrapper,_port);
        }
    }

}
