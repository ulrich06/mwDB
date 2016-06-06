package org.mwg.core.utility;

import org.mwg.Callback;
import org.mwg.Graph;
import org.mwg.plugin.Storage;
import org.mwg.struct.Buffer;

public class ReadOnlyStorage implements Storage {

    private final Storage wrapped;

    public ReadOnlyStorage(final Storage toWrap) {
        wrapped = toWrap;
    }

    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        wrapped.get(keys, callback);
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {
        System.err.println("WARNING: PUT TO A READ ONLY STORAGE");
    }

    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {
        System.err.println("WARNING: REMOVE TO A READ ONLY STORAGE");
    }

    @Override
    public void connect(Graph graph, Callback<Boolean> callback) {
        wrapped.connect(graph, callback);
    }

    @Override
    public void disconnect(Callback<Boolean> callback) {
        wrapped.disconnect(callback);
    }

    @Override
    public void lock(Callback<Buffer> callback) {
        wrapped.lock(callback);
    }

    @Override
    public void unlock(Buffer previousLock, Callback<Boolean> callback) {
        wrapped.unlock(previousLock, callback);
    }
}
