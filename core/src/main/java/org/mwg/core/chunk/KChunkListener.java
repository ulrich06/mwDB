package org.mwg.core.chunk;

import org.mwg.Graph;

public interface KChunkListener {

    void declareDirty(KChunk chunk);

    Graph graph();

}
