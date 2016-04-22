package org.mwdb.chunk;

import org.mwdb.KGraph;

public interface KChunkListener {

    void declareDirty(KChunk chunk);

    KGraph graph();

}
