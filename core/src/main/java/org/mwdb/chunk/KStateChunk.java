package org.mwdb.chunk;

import org.mwdb.plugin.KNodeState;
import org.mwdb.plugin.KResolver;

public interface KStateChunk extends KChunk, KNodeState {

    void cloneFrom(KStateChunk origin);

    void each(KStateChunkCallBack callBack, KResolver resolver);

}
