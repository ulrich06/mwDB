package org.mwg.core;

import org.mwg.*;
import org.mwg.struct.Buffer;
import org.mwg.plugin.Storage;
import org.mwg.core.utility.PrimitiveHelper;

public class NoopStorage implements Storage {

    @Override
    public void get(Buffer[] keys, Callback<Buffer[]> callback) {
        Buffer[] result = new Buffer[keys.length];
        callback.on(result);
    }

    @Override
    public void put(Buffer[] keys, Buffer[] values, Callback<Boolean> callback) {
        if (PrimitiveHelper.isDefined(callback)) {
            callback.on(true);
        }
    }

    @Override
    public void remove(Buffer[] keys, Callback<Boolean> callback) {
        callback.on(true);
    }

    @Override
    public void connect(org.mwg.Graph graph, Callback<Short> callback) {
        callback.on(new Short("0"));
    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        callback.on(true);
    }

}
