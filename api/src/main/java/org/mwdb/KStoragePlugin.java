package org.mwdb;

public interface KStoragePlugin {

    void get(long[] keys, KCallback<String[]> callback);

    void atomicGetIncrement(long[] key, KCallback<Short> cb);

    void put(long[] keys, String[] values, KCallback<Throwable> error, int excludeListener);

    void remove(long[] keys, KCallback<Throwable> error);

    void connect(KCallback<Throwable> callback);

    void disconnect(KCallback<Throwable> callback);

    /*
    int addUpdateListener(KContentUpdateListener interceptor);

    void removeUpdateListener(int id);
    */

}