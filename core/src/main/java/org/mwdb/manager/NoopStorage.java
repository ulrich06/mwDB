package org.mwdb.manager;

import org.mwdb.Constants;
import org.mwdb.KCallback;
import org.mwdb.plugin.KStorage;

public class NoopStorage implements KStorage {

    @Override
    public void get(long[] keys, KCallback<String[]> callback) {
        String[] result = new String[keys.length / Constants.KEYS_SIZE];
        callback.on(result);
    }

    @Override
    public void atomicGetIncrement(long[] key, KCallback<Short> callback) {
        callback.on(new Short("0"));
    }

    @Override
    public void put(long[] keys, String[] values, KCallback<Throwable> callback, int excludeListener) {
        callback.on(null);
    }

    @Override
    public void remove(long[] keys, KCallback<Throwable> callback) {
        callback.on(null);
    }

    @Override
    public void connect(KCallback<Throwable> callback) {
        callback.on(null);
    }

    @Override
    public void disconnect(KCallback<Throwable> callback) {
        callback.on(null);
    }
}
