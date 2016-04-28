package org.mwg;

import org.mwg.plugin.Storage;
import org.mwg.struct.Buffer;

public class WebSocketStorage implements Storage {

    //TODO remotely call a remote storage
    //TODO no cache mecanism

    @Override
    public void get(Buffer[] keys, Callback<Buffer[]> callback) {

    }

    @Override
    public void put(Buffer[] keys, Buffer[] values, Callback<Boolean> callback) {

    }

    @Override
    public void remove(Buffer[] keys, Callback<Boolean> callback) {

    }

    @Override
    public void connect(Graph graph, Callback<Short> callback) {

    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {

    }

}
