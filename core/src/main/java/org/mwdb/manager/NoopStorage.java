package org.mwdb.manager;

import org.mwdb.KCallback;
import org.mwdb.KGraph;
import org.mwdb.chunk.KBuffer;
import org.mwdb.plugin.KStorage;
import org.mwdb.utility.PrimitiveHelper;

public class NoopStorage implements KStorage {

    @Override
    public void get(KBuffer[] keys, KCallback<KBuffer[]> callback) {
        KBuffer[] result = new KBuffer[keys.length];
        callback.on(result);
    }

    @Override
    public void put(KBuffer[] keys, KBuffer[] values, KCallback<Boolean> callback) {
        if (PrimitiveHelper.isDefined(callback)) {
            callback.on(true);
        }
    }

    @Override
    public void remove(KBuffer[] keys, KCallback<Boolean> callback) {
        callback.on(true);
    }

    @Override
    public void connect(KGraph graph, KCallback<Short> callback) {
        callback.on(new Short("0"));
    }

    @Override
    public void disconnect(Short prefix, KCallback<Boolean> callback) {
        callback.on(true);
    }

}
