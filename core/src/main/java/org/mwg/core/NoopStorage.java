package org.mwg.core;

import org.mwg.*;
import org.mwg.struct.Buffer;
import org.mwg.plugin.Storage;
import org.mwg.core.utility.PrimitiveHelper;
import org.mwg.struct.BufferIterator;

public class NoopStorage implements Storage {

    private Graph _graph;

    @Override
    public void get(Buffer keys, Callback<Buffer> callback) {
        Buffer result = _graph.newBuffer();
        BufferIterator it = keys.iterator();
        while (it.hasNext()) {
            Buffer tempView = it.next();
            result.write(CoreConstants.BUFFER_SEP);
        }
        callback.on(result);
    }

    @Override
    public void put(Buffer stream, Callback<Boolean> callback) {
        if (PrimitiveHelper.isDefined(callback)) {
            callback.on(true);
        }
    }

    @Override
    public void remove(Buffer keys, Callback<Boolean> callback) {
        callback.on(true);
    }

    @Override
    public void connect(org.mwg.Graph graph, Callback<Short> callback) {
        _graph = graph;
        callback.on((short)0);
    }

    @Override
    public void disconnect(Short prefix, Callback<Boolean> callback) {
        _graph = null;
        callback.on(true);
    }

}
