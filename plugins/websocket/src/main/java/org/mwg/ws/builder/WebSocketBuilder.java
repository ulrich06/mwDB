package org.mwg.ws.builder;

class WebSocketBuilder {

    String _pathKeyStore;
    String _passKeyStore;
    String _keyStoreType;
    String _pathTrustStore;
    String _passTrustStore;
    String _trustStoreType;

    void checkSSLParam() {
        if(_pathTrustStore == null) {
            throw new RuntimeException("The trust-store should be initialized before build");
        }
        if(_pathKeyStore == null) {
            throw new RuntimeException("The key-store should be initialized before build");
        }
    }

    void internalWithKeyStore(String path, String password, String type) {
        _pathKeyStore = path;
        _passKeyStore = password;
        _keyStoreType = type;
    }

    void internalWithTrustStore(String path, String password, String type) {
        _pathTrustStore = path;
        _passTrustStore = password;
        _trustStoreType = type;
    }

}
