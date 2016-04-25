package org.mwg.core.chunk;

import org.mwg.plugin.NodeState;
import org.mwg.plugin.Resolver;

public interface KStateChunk extends KChunk, NodeState {

    void each(KStateChunkCallBack callBack, Resolver resolver);

}
