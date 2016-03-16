package org.mwdb.chunk.heap;

import org.mwdb.chunk.KChunk;

public interface KHeapChunk extends KChunk {

    int mark();

    int unmark();

    boolean setFlags(long bitsToEnable, long bitsToDisable);

}
