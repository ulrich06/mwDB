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
    public void connect(Graph graph, Callback<Short> callback) {
        wrapped.connect(graph, callback);
    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        wrapped.disconnect(prefix, callback);
    }
}
