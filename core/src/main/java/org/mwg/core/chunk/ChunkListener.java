package org.mwg.core.chunk;

import org.mwg.Graph;

public interface ChunkListener {

    void declareDirty(Chunk chunk);

    Graph graph();

}
