package org.mwdb.plugin;

import org.mwdb.KCallback;

public interface KStorage {

    /**
     * Call the storage to retrieve keys, the string[] should be always keys.length / Constants.KEY_SIZE (3)
     */
    void get(long[] keys, KCallback<String[]> callback);

    void atomicGetIncrement(long[] key, KCallback<Short> callback);

    void put(long[] keys, String[] values, KCallback<Throwable> callback, int excludeListener);

    void remove(long[] keys, KCallback<Throwable> callback);

    /**
     * Connect the storage
     *
     * @param callback closure when ready
     */
    void connect(KCallback<Throwable> callback);

    /**
     * Disconnect the storage
     *
     * @param callback closure when ready
     */
    void disconnect(KCallback<Throwable> callback);

    /*
    int addUpdateListener(KContentUpdateListener interceptor);
    void removeUpdateListener(int id);
    */

}