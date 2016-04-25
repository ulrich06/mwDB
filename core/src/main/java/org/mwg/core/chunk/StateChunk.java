package org.mwg.core.chunk;

import org.mwg.plugin.NodeState;
import org.mwg.plugin.Resolver;

public interface StateChunk extends Chunk, NodeState {

    void each(StateChunkCallBack callBack, Resolver resolver);

}
