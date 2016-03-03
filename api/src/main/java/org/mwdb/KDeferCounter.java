package org.mwdb;

public interface KDeferCounter {

    void count();

    void then(KCallback callback);

}
